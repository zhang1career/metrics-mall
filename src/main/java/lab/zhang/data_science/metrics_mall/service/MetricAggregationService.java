package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.model.MetricAggregation;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricAggregationDTO;

/**
 * Metric service interface.
 *
 * @author Rongjin Zhang
 */
public interface MetricAggregationService {
    /**
     * Query metric aggregation results.
     *
     * @param queryModel metric aggregation query model
     * @return metric aggregation result containing metadata and rows
     */
    MetricAggregation queryAggregation(MetricAggregationDTO queryModel);
}

