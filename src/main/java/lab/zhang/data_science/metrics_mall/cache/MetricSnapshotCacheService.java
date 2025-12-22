package lab.zhang.data_science.metrics_mall.cache;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.EchoMetricDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.metric.EchoMetricDTO;

import java.util.List;
import java.util.Map;

/**
 * Metric snapshot cache service.
 * <p>
 * Cache Schema:
 * key = (entity.meta.code, entity.id, metricCode, dimensionMap, version)
 * value = {"a": value, "ts": snapshotTs, "s": 0, "h": [{"a": value1, "ts": snapshotTs1, "s": 1}]}
 * <p>
 * Notice of key construction:
 * - all keys are prefixed with 'mx:' to denote metric snapshot.
 * - the dimensionMap should be sorted by dimension code to ensure consistent key generation.
 * - the version is prefixed with 'v' to avoid ambiguity.
 * - the serializing pattern of dimensionMap is {entry.key1}_{entry.value1},{entry.key2}_{entry.value2},...
 * - the whole serializing pattern is mx:(entity.meta.code):{entity.id}:{metricCode}:v{version}:{dimensionMap_serialized}
 */
public interface MetricSnapshotCacheService {

    /**
     * Get cached metric value
     *
     * @param entityCode   entity meta code
     * @param entityId     entity id
     * @param metricCode   metric code
     * @param version      metric version
     * @param dimensionMap dimension map, key is dimension code, value is dimension value
     * @return cached echo metric, null if not found.
     */
    EchoMetricDAO get(String entityCode,
                      Long entityId,
                      String metricCode,
                      Integer version,
                      Map<String, TypedValue> dimensionMap);

    /**
     * Batch put metric values into cache
     * The write process will overwrite the fields of "a" and "ts", and append the previous value into history list "h".
     * The history list "h" will keep at most 10 items, the oldest items will be removed when exceeding the limit.
     * All parameters in EchoMetricDTO should be validated before calling this method.
     *
     * @param entityCode entity meta code
     * @param entityId   entity id
     * @param metricList list of echo metric DTOs, each DTO should have valid code, version, dimensionMap, snapshotTs, sourceType, and value
     * @throws IllegalArgumentException if any validation fails
     */
    void putBatch(String entityCode,
                  Long entityId,
                  List<EchoMetricDTO> metricList);
}

