package lab.zhang.data_science.metrics_mall.service.impl;

import cn.hutool.core.util.StrUtil;
import lab.zhang.data_science.metrics_mall.cache.MetricSnapshotCacheService;
import lab.zhang.data_science.metrics_mall.model.Entity;
import lab.zhang.data_science.metrics_mall.model.MetricSnapshot;
import lab.zhang.data_science.metrics_mall.model.metric.AlphaMetric;
import lab.zhang.data_science.metrics_mall.model.metric.BetaMetric;
import lab.zhang.data_science.metrics_mall.model.metric.EchoMetric;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.EchoMetricDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionRelsDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotWriteDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricWriteDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.metric.EchoMetricDTO;
import lab.zhang.data_science.metrics_mall.service.EntityService;
import lab.zhang.data_science.metrics_mall.service.MetricService;
import lab.zhang.data_science.metrics_mall.service.MetricSnapshotService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private EntityService entityService;

    @Autowired
    private MetricSnapshotCacheService cacheService;

    @Autowired
    private MetricService metricService;

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
        Entity entity = entityService.getEntity(dto.getEntityCode(), dto.getEntityId());
        if (entity == null) {
            log.warn("[snap] entity not found: entityCode={}, entityId={}",
                    dto.getEntityCode(), dto.getEntityId());
            return null;
        }

        // query metrics' information
        Map<String, MetricMetaDAO> metricDAOMap = dto.getMetricList().stream()
                .filter(Objects::nonNull)
                .map(metricDTO ->
                        metricService.getMetricDaoByCode(metricDTO.getCode()))
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
    public Long writeSnapshot(MetricSnapshotWriteDTO dto) {
        // validate metric list
        List<MetricWriteDTO> metricList = dto.getMetricList();
        if (CollectionUtils.isEmpty(metricList)) {
            throw new IllegalArgumentException("[snap] metric list is empty");
        }

        // validate entity meta
        Entity entity = entityService.getEntity(dto.getEntityCode(), dto.getEntityId());
        if (entity == null) {
            log.warn("[snap] write, entity meta is not found: entityCode={}, entityId={}",
                    dto.getEntityCode(), dto.getEntityId());
            return null;
        }

        for (MetricWriteDTO metricWriteDTO : metricList) {
            if (metricWriteDTO == null) {
                throw new IllegalArgumentException("[snap] write, metric write dto is null");
            }
            if (StrUtil.isBlank(metricWriteDTO.getCode())) {
                throw new IllegalArgumentException("[snap] write, metric code is empty");
            }
            if (StrUtil.isBlank(metricWriteDTO.getValue())) {
                throw new IllegalArgumentException(String.format("[snap] write, metric value is empty: metricCode=%s",
                        metricWriteDTO.getCode()));
            }
        }
        List<String> metricCodeList = metricList.stream()
                .filter(Objects::nonNull)
                .map(MetricWriteDTO::getCode)
                .filter(Objects::nonNull)
                .toList();
        Map<String, Integer> requiredVersionMap = new HashMap<>();
        for (MetricWriteDTO metricWriteDTO : metricList) {
            if (metricWriteDTO != null) {
                requiredVersionMap.put(metricWriteDTO.getCode(), metricWriteDTO.getVersion());
            }
        }
        Map<String, Integer> nearestVersionMap = metricService.getNearestVersionBatch(metricCodeList, requiredVersionMap);
        Map<String, Integer> acutalVersionMap = new HashMap<>();
        for (String code : metricCodeList) {
            if (!nearestVersionMap.containsKey(code) || nearestVersionMap.get(code) == null) {
                throw new IllegalArgumentException("[snap] write, no version near before required one");
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
                .map(dto1 -> new MetricDimensionRelsDTO(
                        dto1.getCode(),
                        dto1.getDimensionCodeList()))
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

        // use current timestamp if not provided
        Long snapshotTs = dto.getSnapshotTs();
        if (snapshotTs == null || snapshotTs <= 0) {
            snapshotTs = System.currentTimeMillis();
        }

        // write each metric
        for (MetricWriteDTO metricWriteDTO : metricList) {
            String code = metricWriteDTO.getCode();
            Integer actualVersion = acutalVersionMap.get(code);

            try {
                // validate metric exists
                MetricMetaDAO metricMetaDAO = metricService.getMetricDaoByCode(metricWriteDTO.getCode());
                if (metricMetaDAO == null) {
                    log.warn("[snap] metric not found: metricCode={}", metricWriteDTO.getCode());
                    continue;
                }

                // write to cache
                cacheService.put(
                        dto.getEntityCode(),
                        dto.getEntityId(),
                        metricWriteDTO.getCode(),
                        actualVersion,
                        metricWriteDTO.getDimensionMap(),
                        snapshotTs,
                        metricWriteDTO.getValue()
                );

                if (log.isDebugEnabled()) {
                    log.debug("[snap] metric written: entityCode={}, entityId={}, metricCode={}, version={}, value={}, snapshotTs={}",
                            dto.getEntityCode(), dto.getEntityId(), metricWriteDTO.getCode(),
                            actualVersion, metricWriteDTO.getValue(), snapshotTs);
                }
            } catch (Exception e) {
                log.error("[snap] failed to write metric: entityCode={}, entityId={}, metricCode={}",
                        dto.getEntityCode(), dto.getEntityId(), metricWriteDTO.getCode(), e);
            }
        }

        return System.currentTimeMillis();
    }
}
