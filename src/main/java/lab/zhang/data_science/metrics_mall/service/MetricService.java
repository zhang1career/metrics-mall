package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.model.*;
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricAggregationDTO;

import java.util.List;
import java.util.Map;

/**
 * Metric service interface.
 *
 * @author Rongjin Zhang
 * 
 */
public interface MetricService {
    
    /**
     * Get metric model by code.
     *
     * @param code metric code
     * @return metric model, null if not found
     */
    PrimeMetric getByCode(String code);
    
    /**
     * Get metric models by codes.
     *
     * @param codes metric codes list
     * @return metric model map, key is metric code, value is metric model
     */
    Map<String, PrimeMetric> getByCodes(List<String> codes);
    
    /**
     * Validate metric codes exist.
     *
     * @param codes metric codes list
     * @return true if all codes exist, false otherwise
     */
    boolean validateCodesExist(List<String> codes);

    /**
     * Query metric aggregation results.
     *
     * @param queryModel metric aggregation query model
     * @return metric aggregation result containing metadata and rows
     */
    MetricAggregation queryAggregation(MetricAggregationDTO queryModel);

    /**
     * Get metric models by codes.
     *
     * @param metricCodes metric codes list
     * @return metric model map, key is metric code, value is metric model
     */
    Map<String, PrimeMetric> getMetricModelsByCodes(List<String> metricCodes);
}

