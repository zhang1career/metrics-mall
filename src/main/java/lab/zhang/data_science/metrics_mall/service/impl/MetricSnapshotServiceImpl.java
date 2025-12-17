package lab.zhang.data_science.metrics_mall.service.impl;

import lab.zhang.data_science.metrics_mall.cache.MetricSnapshotCacheService;
import lab.zhang.data_science.metrics_mall.model.Entity;
import lab.zhang.data_science.metrics_mall.model.MetricSnapshot;
import lab.zhang.data_science.metrics_mall.model.metric.AlphaMetric;
import lab.zhang.data_science.metrics_mall.model.metric.BetaMetric;
import lab.zhang.data_science.metrics_mall.model.metric.EchoMetric;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.EchoMetricDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.EchoMetricDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO;
import lab.zhang.data_science.metrics_mall.service.EntityService;
import lab.zhang.data_science.metrics_mall.service.MetricService;
import lab.zhang.data_science.metrics_mall.service.MetricSnapshotService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Metric snapshot service implementation.
 *
 * @author Rongjin Zhang
 */
@Slf4j
@Service
@RequiredArgsConstructor
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
        Map<String, MetricDAO> metricDAOMap = dto.getMetricList().stream()
                .filter(Objects::nonNull)
                .map(metricDTO ->
                        metricService.getMetricDaoByCode(metricDTO.getCode()))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MetricDAO::getCode, metricDAO -> metricDAO));

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
                    MetricDAO metricDAO = metricDAOMap.get(echoMetricDTO.getCode());
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
}
