package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.enums.LifeStatusEnum;
import lab.zhang.data_science.metrics_mall.model.MetricAggregation;
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricAggregationDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricMetaDTO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Metric service interface.
 *
 * @author Rongjin Zhang
 */
public interface MetricService {

    /**
     * Get metric model by code.
     *
     * @param code metric code
     * @return metric model, null if not found
     */
    PrimeMetric getPrimeMetricByCode(String code);

    /**
     * Get metric models by codes.
     *
     * @param codes metric codes list
     * @return metric model map, key is metric code, value is metric model
     */
    Map<String, PrimeMetric> getPrimeMetricByCodeBatch(List<String> codes);

    /**
     * Get metric DAO by id.
     *
     * @param id metric id
     * @return metric DAO, null if not found
     */
    MetricMetaDAO getMetricMetaDaoById(Long id);

    /**
     * Get metric DAO by code.
     *
     * @param code metric code
     * @return metric DAO, null if not found
     */
    MetricMetaDAO getMetricMetaDaoByCode(String code);

    /**
     * Validate metric code format.
     * @param code metric code
     * @return pair of validation result and message. If valid, first is true and second is empty string.
     */
    Pair<Boolean, String> validateCode(String code);

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
     * Get is_hot by metric code.
     *
     * @param metricCode metric code
     * @param dimensionCodeList dimension codes list
     * @return whether the dimension is hot
     */
    Map<String, Boolean> checkHotBatch(String metricCode, List<String> dimensionCodeList);

    /**
     * Choose metric version by codes.
     *
     * @param metricCodeList metric codes list
     * @param requiredVersionMap required version map, key is metric code, value is required version
     * @param availableLifeStatusSet available life status set
     * @return metric version map, key is metric code, value is chosen version (exact match or main version by default)
     */
    Map<String, Integer> chooseVersionBatch(List<String> metricCodeList,
                                            Map<String, Integer> requiredVersionMap,
                                            Set<LifeStatusEnum> availableLifeStatusSet);

    /**
     * Get metric by id.
     *
     * @param id metric id
     * @return PrimeMetric model, null if not found
     */
    PrimeMetric get(Long id);

    /**
     * List metrics by query criteria.
     *
     * @param dto query criteria
     * @return list of PrimeMetric model
     */
    List<PrimeMetric> list(MetricMetaDTO dto);

    /**
     * Insert a new metric.
     *
     * @param dto metric data transfer object
     * @return true if success
     */
    boolean insert(MetricMetaDTO dto);

    /**
     * Update an existing metric.
     *
     * @param dto metric meta data transfer object
     * @return true if success
     */
    boolean update(MetricMetaDTO dto);

    /**
     * Delete a metric by id.
     *
     * @param id metric id
     * @return true if success
     */
    boolean delete(Long id);
}

