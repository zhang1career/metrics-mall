package lab.zhang.data_science.metrics_mall.service.impl;

import cn.hutool.core.util.StrUtil;
import lab.zhang.data_science.metrics_mall.cache.MetricSnapshotCacheService;
import lab.zhang.data_science.metrics_mall.components.RequestContext;
import lab.zhang.data_science.metrics_mall.config.MetricVersionLifeStatusConfig;
import lab.zhang.data_science.metrics_mall.enums.LifeStatusEnum;
import lab.zhang.data_science.metrics_mall.model.Entity;
import lab.zhang.data_science.metrics_mall.model.MetricSnapshot;
import lab.zhang.data_science.metrics_mall.model.OpLog;
import lab.zhang.data_science.metrics_mall.model.metric.AlphaMetric;
import lab.zhang.data_science.metrics_mall.model.metric.BetaMetric;
import lab.zhang.data_science.metrics_mall.model.metric.EchoMetric;
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private MetricVersionLifeStatusConfig lifeStatusConfig;

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

    @Value("${metrics_mall.snap.publish.delay:60}")
    private Long snapshotPublishDelay;

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
        if (dto == null) {
            throw new IllegalArgumentException("[snap] writing failed, metric snapshot write dto is null");
        }

        // validate entity meta
        Entity entity = entityService.getEntityByCode(dto.getEntityCode(), dto.getEntityId());
        if (entity == null) {
            throw new IllegalArgumentException("[snap] writing failed, entity not found, entityCode=" + dto.getEntityCode());
        }

        // validate metric list
        List<EchoMetricDTO> metricList = dto.getMetricList();
        if (CollectionUtils.isEmpty(metricList)) {
            throw new IllegalArgumentException("[snap] writing failed, metric list is empty");
        }
        for (EchoMetricDTO echoMetricDTO : metricList) {
            if (echoMetricDTO == null) {
                throw new IllegalArgumentException("[snap] writing failed, metric write dto is null");
            }
            if (StrUtil.isBlank(echoMetricDTO.getCode())) {
                throw new IllegalArgumentException("[snap] writing failed, metric code is empty");
            }
            if (StrUtil.isBlank(echoMetricDTO.getValue())) {
                throw new IllegalArgumentException("[snap] writing failed, metric value is empty: metricCode=" + echoMetricDTO.getCode());
            }
        }

        // metric code list
        List<String> metricCodeList = metricList.stream()
                .filter(Objects::nonNull)
                .map(EchoMetricDTO::getCode)
                .filter(Objects::nonNull)
                .toList();
        // required version map
        Map<String, Integer> requiredVersionMap = new HashMap<>();
        for (EchoMetricDTO echoMetricDTO : metricList) {
            if (echoMetricDTO != null) {
                requiredVersionMap.put(echoMetricDTO.getCode(), echoMetricDTO.getVersion());
            }
        }
        // life status set
        Set<LifeStatusEnum> availableLifeStatusSet = lifeStatusConfig.getWritableMetricVersionLifeStatuses();

        Map<String, Integer> chosenVersionMap = metricService.chooseVersionBatch(metricCodeList, requiredVersionMap, availableLifeStatusSet);
        Map<String, Integer> acutalVersionMap = new HashMap<>();
        for (String code : metricCodeList) {
            if (!chosenVersionMap.containsKey(code) || chosenVersionMap.get(code) == null) {
                throw new IllegalArgumentException(String.format("[snap] writing failed, no version found, code=%s, versionMap=%s",
                        code, chosenVersionMap));
            }
            Integer nearestVersion = chosenVersionMap.get(code);
            // choose main version by default
            if (requiredVersionMap.get(code) == null) {
                acutalVersionMap.put(code, nearestVersion);
                continue;
            }
            // nearest version must match required one strictly
            Integer requiredVersion = requiredVersionMap.get(code);
            if (!nearestVersion.equals(requiredVersion)) {
                throw new IllegalArgumentException("[snap] writing failed, version not match required one strictly, would you want to try version=" + nearestVersion);
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
            throw new IllegalArgumentException("[snap] writing failed, metric dimension relations are empty");
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
                            "[snap] writing failed, dimension is not hot, cannot write: metricCode=%s, dimensionCode=%s",
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

        // ignore dto snapshotTs, to protect history data from tampered
        Long snapshotTs = System.currentTimeMillis() + (snapshotPublishDelay * 1000);

        // batch validate metric codes exist
        List<String> metricCodeListForValidation = metricList.stream()
                .filter(Objects::nonNull)
                .map(EchoMetricDTO::getCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!CollectionUtils.isEmpty(metricCodeListForValidation)) {
            Map<String, PrimeMetric> metricMap = metricService.getPrimeMetricByCodeBatch(metricCodeListForValidation);
            List<String> missingCodes = metricCodeListForValidation.stream()
                    .filter(code -> !metricMap.containsKey(code))
                    .toList();
            if (!missingCodes.isEmpty()) {
                throw new IllegalArgumentException("[snap] writing failed, metric codes not found: " + String.join(", ", missingCodes));
            }
        }

        // prepare metric list for batch write
        List<EchoMetricDTO> preparedMetricList = new ArrayList<>();
        for (EchoMetricDTO echoMetricDTO : metricList) {
            if (echoMetricDTO == null) {
                continue;
            }
            String metricCode = echoMetricDTO.getCode();
            Integer actualVersion = acutalVersionMap.get(metricCode);

            // clone EchoMetricDTO and update version and snapshotTs
            EchoMetricDTO clonedMetric = EchoMetricDTO.builder()
                    .code(echoMetricDTO.getCode())
                    .version(actualVersion)
                    .dimensionMap(echoMetricDTO.getDimensionMap())
                    .value(echoMetricDTO.getValue())
                    .snapshotTs(snapshotTs)
                    .sourceType(echoMetricDTO.getSourceType())
                    .build();
            preparedMetricList.add(clonedMetric);
        }

        // batch write to cache
        if (!CollectionUtils.isEmpty(preparedMetricList)) {
            try {
                cacheService.putBatch(
                        dto.getEntityCode(),
                        dto.getEntityId(),
                        preparedMetricList
                );

                if (log.isDebugEnabled()) {
                    log.debug("[snap] writing done, metrics written in batch: entityCode={}, entityId={}, metricCount={}, snapshotTs={}",
                            dto.getEntityCode(), dto.getEntityId(), preparedMetricList.size(), snapshotTs);
                }
            } catch (Exception e) {
                log.error("[snap] writing failed in batch, entityCode={}, entityId={}",
                        dto.getEntityCode(), dto.getEntityId(), e);
                throw e;
            }
        }

        return insertedOpLog.getId();
    }
}
