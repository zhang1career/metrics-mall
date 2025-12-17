package lab.zhang.data_science.metrics_mall.service.impl;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.model.MetricAggregation;
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricAggregationDTO;
import lab.zhang.data_science.metrics_mall.service.MetricAggregationService;
import lab.zhang.data_science.metrics_mall.service.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Metric aggregation service implementation.
 *
 * @author Rongjin Zhang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricAggregationServiceImpl implements MetricAggregationService {

    private final MetricService metricService;

    @Override
    public MetricAggregation queryAggregation(MetricAggregationDTO queryModel) {
        if (queryModel == null) {
            return new MetricAggregation(Collections.emptyMap(), Collections.emptyMap());
        }

        Set<String> metricCodeSet = queryModel.getMetricCodeSet();
        if (CollectionUtils.isEmpty(metricCodeSet)) {
            return new MetricAggregation(Collections.emptyMap(), Collections.emptyMap());
        }

        // Get metric metadata
        Map<String, PrimeMetric> metricMap = metricService.getPrimeMetricByCodeBatch(
                metricCodeSet.stream().toList());

        if (metricMap.isEmpty()) {
            log.warn("No metrics found for codes: {}", metricCodeSet);
            return new MetricAggregation(Collections.emptyMap(), Collections.emptyMap());
        }

        // TODO: Query ClickHouse for aggregation data
        // In MVP stage, return empty aggregation results
        // The actual ClickHouse query should be implemented based on:
        // - startTime, stopTime, intervalInSeconds
        // - groupByList, filterList, orderByList, limit
        // - metric codes and aggregation types

        log.info("Query aggregation: metricCodes={}, startTime={}, stopTime={}, interval={}s",
                metricCodeSet, queryModel.getStartTime(), queryModel.getStopTime(),
                queryModel.getIntervalInSeconds());

        // Return empty aggregation results for now
        Map<Map<String, TypedValue>, Map<String, TypedValue>> dimensionValueMap = 
                Collections.emptyMap();

        return new MetricAggregation(metricMap, dimensionValueMap);
    }
}

