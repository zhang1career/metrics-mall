package lab.zhang.data_science.metrics_mall.service.impl;

import cn.hutool.core.util.StrUtil;
import lab.zhang.data_science.metrics_mall.cache.MetricSnapshotCacheService;
import lab.zhang.data_science.metrics_mall.components.RequestContext;
import lab.zhang.data_science.metrics_mall.enums.SnapshotSourceTypeEnum;
import lab.zhang.data_science.metrics_mall.model.Entity;
import lab.zhang.data_science.metrics_mall.model.MetricSnapshot;
import lab.zhang.data_science.metrics_mall.model.OpLog;
import lab.zhang.data_science.metrics_mall.model.metric.AlphaMetric;
import lab.zhang.data_science.metrics_mall.model.metric.BetaMetric;
import lab.zhang.data_science.metrics_mall.model.metric.EchoMetric;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.EchoMetricDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionRelsDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.OpLogDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.metric.EchoMetricDTO;
import lab.zhang.data_science.metrics_mall.service.EntityService;
import lab.zhang.data_science.metrics_mall.service.MetricService;
import lab.zhang.data_science.metrics_mall.service.MetricSnapshotService;
import lab.zhang.data_science.metrics_mall.service.OpLogService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Metric snapshot service implementation.
 *
 * @author Rongjin Zhang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricSnapshotServiceImpl implements MetricSnapshotService {

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private EntityService entityService;

    @Autowired
    private MetricSnapshotCacheService cacheService;

    @Autowired
    private MetricService metricService;

    @Autowired
    private OpLogService opLogService;

    @Autowired
    private MetricStructMapper metricStructMapper;

    @Override
    public MetricSnapshot querySnapshot(MetricSnapshotDTO dto) {
        // validate metric list
        List<EchoMetricDTO> metricList = dto.getMetricList();
        if (CollectionUtils.isEmpty(metricList)) {
            throw new IllegalArgumentException("[snap] metric list is empty");
        }

        // validate entity meta
        Entity entity = entityService.getEntityByCode(dto.getEntityCode(), dto.getEntityId());
        if (entity == null) {
            log.warn("[snap] entity not found: entityCode={}, entityId={}",
                    dto.getEntityCode(), dto.getEntityId());
            return null;
        }

        // query metrics' information
        Map<String, MetricMetaDAO> metricDAOMap = dto.getMetricList().stream()
                .filter(Objects::nonNull)
                .map(metricDTO ->
                        metricService.getMetricMetaDaoByCode(metricDTO.getCode()))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MetricMetaDAO::getCode, metricDAO -> metricDAO));

        // query metrics' value from cache
        List<EchoMetric> echoMetricList = dto.getMetricList().stream()
                .filter(Objects::nonNull)
                .filter(echoMetricDTO -> metricDAOMap.containsKey(echoMetricDTO.getCode()))
                .map(echoMetricDTO -> {
                    // For each requested metric, query from cache
                    EchoMetricDAO echoMetricDAO = cacheService.get(
                            dto.getEntityCode(),
                            dto.getEntityId(),
                            echoMetricDTO.getCode(),
                            echoMetricDTO.getVersion(),
                            echoMetricDTO.getDimensionMap()
                    );
                    MetricMetaDAO metricDAO = metricDAOMap.get(echoMetricDTO.getCode());
                    return metricStructMapper.echoMetricDaoToModel(echoMetricDAO, metricDAO);
                })
                .toList();
        if (log.isDebugEnabled()) {
            log.debug("[snap] query result={}", echoMetricList);
        }

        // return current value without looking-up history, if no snapshot specified
        if (dto.getSnapshotTs() == null || dto.getSnapshotTs() == 0) {
            List<BetaMetric> instantMetricList = echoMetricList.stream()
                    .filter(Objects::nonNull)
                    .map(echoMetric -> {
                        return BetaMetric.builder()
                                .code(echoMetric.getCode())
                                .value(echoMetric.getValue())
                                .snapshotTs(echoMetric.getSnapshotTs())
                                .precision(echoMetric.getPrecision())
                                .unit(echoMetric.getUnit())
                                .build();
                    })
                    .collect(Collectors.toList());
            long latestTimestamp = System.currentTimeMillis();
            return MetricSnapshot.builder()
                    .entity(entity)
                    .metricList(instantMetricList)
                    .snapshotTs(latestTimestamp)
                    .build();
        }

        // filter by snapshot timestamp
        List<BetaMetric> filteredMetricList = filterSnapshotByTimestamp(echoMetricList, dto.getSnapshotTs());

        return MetricSnapshot.builder()
                .entity(entity)
                .metricList(filteredMetricList)
                .snapshotTs(dto.getSnapshotTs())
                .build();
    }

    private List<BetaMetric> filterSnapshotByTimestamp(List<EchoMetric> echoMetricList, Long snapshotTs) {
        return echoMetricList.stream()
                .filter(Objects::nonNull)
                .map(echoMetric -> {
                    AlphaMetric alphaMetric = echoMetric.getBackNearest(snapshotTs);
                    if (alphaMetric == null) {
                        return null;
                    }
                    return BetaMetric.builder()
                            .code(alphaMetric.getCode())
                            .value(alphaMetric.getValue())
                            .snapshotTs(alphaMetric.getSnapshotTs())
                            .precision(echoMetric.getPrecision())
                            .unit(echoMetric.getUnit())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public BigInteger writeSnapshot(MetricSnapshotDTO dto) {
        // validate metric list
        List<EchoMetricDTO> metricList = dto.getMetricList();
        if (CollectionUtils.isEmpty(metricList)) {
            throw new IllegalArgumentException("metric list is empty");
        }

        // validate entity meta
        Entity entity = entityService.getEntityByCode(dto.getEntityCode(), dto.getEntityId());
        if (entity == null) {
            throw new IllegalArgumentException("entity not found, entityCode=" + dto.getEntityCode());
        }

        for (EchoMetricDTO echoMetricDTO : metricList) {
            if (echoMetricDTO == null) {
                throw new IllegalArgumentException("metric write dto is null");
            }
            if (StrUtil.isBlank(echoMetricDTO.getCode())) {
                throw new IllegalArgumentException("metric code is empty");
            }
            if (StrUtil.isBlank(echoMetricDTO.getValue())) {
                throw new IllegalArgumentException("metric value is empty: metricCode=" + echoMetricDTO.getCode());
            }
        }
        List<String> metricCodeList = metricList.stream()
                .filter(Objects::nonNull)
                .map(EchoMetricDTO::getCode)
                .filter(Objects::nonNull)
                .toList();
        Map<String, Integer> requiredVersionMap = new HashMap<>();
        for (EchoMetricDTO echoMetricDTO : metricList) {
            if (echoMetricDTO != null) {
                requiredVersionMap.put(echoMetricDTO.getCode(), echoMetricDTO.getVersion());
            }
        }
        Map<String, Integer> nearestVersionMap = metricService.chooseVersionBatch(metricCodeList, requiredVersionMap);
        Map<String, Integer> acutalVersionMap = new HashMap<>();
        for (String code : metricCodeList) {
            if (!nearestVersionMap.containsKey(code) || nearestVersionMap.get(code) == null) {
                throw new IllegalArgumentException("[snap] write, no exact version found for required one");
            }
            Integer nearestVersion = nearestVersionMap.get(code);
            // choose main version by default
            if (requiredVersionMap.get(code) == null) {
                acutalVersionMap.put(code, nearestVersion);
                continue;
            }
            // nearest version must match required one strictly
            Integer requiredVersion = requiredVersionMap.get(code);
            if (!nearestVersion.equals(requiredVersion)) {
                throw new IllegalArgumentException("[snap] write, version not match required one strictly, would you want to try version=" + nearestVersion);
            }
            acutalVersionMap.put(code, nearestVersion);
        }

        // validate metric dimensions
        List<MetricDimensionRelsDTO> relsDTOList = metricList.stream()
                .filter(Objects::nonNull)
                .map(_dto -> new MetricDimensionRelsDTO(
                        _dto.getCode(),
                        _dto.getDimensionCodeList()))
                .toList();
        if (CollectionUtils.isEmpty(relsDTOList)) {
            throw new IllegalArgumentException("[snap] write, metric dimension relations are empty");
        }
        for (MetricDimensionRelsDTO relsDTO : relsDTOList) {
            List<String> dimensionCodeList = relsDTO.getDimensionCodeList();
            if (CollectionUtils.isEmpty(dimensionCodeList)) {
                // skip if no dimensions specified
                continue;
            }
            Map<String, Boolean> dimensionHotMap = metricService.checkHotBatch(relsDTO.getMetricCode(), relsDTO.getDimensionCodeList());
            for (String dimensionCode : dimensionCodeList) {
                if (!dimensionHotMap.containsKey(dimensionCode) || !dimensionHotMap.get(dimensionCode)) {
                    throw new IllegalArgumentException(String.format(
                            "[snap] write, dimension is not hot, cannot write: metricCode=%s, dimensionCode=%s",
                            relsDTO.getMetricCode(), dimensionCode));
                }
            }
        }

        BigInteger traceId = requestContext.getTraceId();

        // op_log inserted
        OpLogDTO oplogDTO = OpLogDTO.builder()
                .id(traceId)
                .event(requestContext.getEvent())
                .operatorId(requestContext.getUserId())
                .operateTime(new Date(requestContext.getStartTs()))
                .build();
        OpLog insertedOpLog = opLogService.insert(oplogDTO);

        // use current timestamp if not provided
        Long snapshotTs = dto.getSnapshotTs();
        if (snapshotTs == null || snapshotTs <= 0) {
            snapshotTs = System.currentTimeMillis();
        }

        // write each metric
        for (EchoMetricDTO echoMetricDTO : metricList) {
            String metricCode = echoMetricDTO.getCode();
            Integer actualVersion = acutalVersionMap.get(metricCode);

            try {
                // validate metric exists
                Pair<Boolean, String> validResult = metricService.validateCode(metricCode);
                if (!validResult.getLeft()) {
                    log.warn("[snap] invalid metric code, " + validResult.getRight());
                    continue;
                }

                // write to cache
                cacheService.put(
                        dto.getEntityCode(),
                        dto.getEntityId(),
                        metricCode,
                        actualVersion,
                        echoMetricDTO.getDimensionMap(),
                        snapshotTs,
                        SnapshotSourceTypeEnum.EXTERNAL.getId(),
                        echoMetricDTO.getValue()
                );

                if (log.isDebugEnabled()) {
                    log.debug("[snap] metric written: entityCode={}, entityId={}, metricCode={}, version={}, value={}, snapshotTs={}",
                            dto.getEntityCode(), dto.getEntityId(), metricCode,
                            actualVersion, echoMetricDTO.getValue(), snapshotTs);
                }
            } catch (Exception e) {
                log.error("[snap] failed to write metric: entityCode={}, entityId={}, metricCode={}",
                        dto.getEntityCode(), dto.getEntityId(), metricCode, e);
            }
        }

        return insertedOpLog.getId();
    }
}
