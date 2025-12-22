package lab.zhang.data_science.metrics_mall.cache.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lab.zhang.data_science.metrics_mall.cache.MetricSnapshotCacheService;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.EchoMetricDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Metric snapshot cache service implementation.
 *
 * @author Rongjin Zhang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricSnapshotCacheServiceRedisImpl implements MetricSnapshotCacheService {

    @Value("${metrics_mall.cache.key.prefix:mx}")
    private String keyPrefix;

    private static final String KEY_SEPARATOR = ":";

    private static final String VERSION_PREFIX = "v";

    private static final String DIMENSION_SEPARATOR = ",";

    private static final String DIMENSION_KEY_VALUE_SEPARATOR = "_";

    @Value("${metrics_mall.snap.cache.timeout:86400}")
    private Long timeoutInSeconds;

    @Value("${metrics_mall.snap.cache.value.history.depth:7}")
    private int maxHistoryDepth;

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    private DefaultRedisScript<String> updateMetricSnapshotScript;

    @PostConstruct
    public void init() {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/update_metric_snapshot.lua")));
        script.setResultType(String.class);
        updateMetricSnapshotScript = script;
    }


    @Override
    public EchoMetricDAO get(String entityCode,
                             Long entityId,
                             String metricCode,
                             Integer version,
                             Map<String, TypedValue> dimensionMap) {
        if (StrUtil.isBlank(entityCode)) {
            log.warn("[cache] get echoMetric, invalid entity for get operation");
            return null;
        }
        if (StrUtil.isBlank(metricCode)) {
            log.warn("[cache] get echoMetric, metric code is empty");
            return null;
        }
        if (version == null) {
            log.warn("[cache] get echoMetric, version is null");
            return null;
        }

        String key = buildKey(entityCode, entityId, metricCode, version, dimensionMap);
        String value = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isBlank(value)) {
            if (log.isDebugEnabled()) {
                log.debug("[cache] get echoMetric, cache miss: key={}", key);
            }
            return null;
        }

        try {
            return objectMapper.readValue(value, EchoMetricDAO.class);
        } catch (Exception e) {
            log.error("[cache] failed to parse cached value: key={}, value={}", key, value, e);
            return null;
        }
    }

    @Override
    public void put(String entityCode,
                    Long entityId,
                    String metricCode,
                    Integer version,
                    Map<String, TypedValue> dimensionMap,
                    Long snapshotTs,
                    Integer sourceType,
                    String value) {
        if (StrUtil.isBlank(entityCode)) {
            log.warn("[cache] put echoMetric, invalid entity for put operation");
            return;
        }
        if (StrUtil.isBlank(metricCode)) {
            log.warn("[cache] put echoMetric, metricCode is empty");
            return;
        }
        if (version == null) {
            log.warn("[cache] put echoMetric, version is null");
            return;
        }
        if (snapshotTs == null) {
            log.warn("[cache] put echoMetric, snapshotTs is null");
            return;
        }
        if (sourceType == null) {
            log.warn("[cache] put echoMetric, sourceType is null");
            return;
        }
        if (StrUtil.isBlank(value)) {
            log.warn("[cache] put echoMetric, value is empty");
            return;
        }

        String key = buildKey(entityCode, entityId, metricCode, version, dimensionMap);

        try {
            List<String> keys = Collections.singletonList(key);
            String result = stringRedisTemplate.execute(
                    updateMetricSnapshotScript,
                    keys,
                    value,
                    String.valueOf(snapshotTs),
                    String.valueOf(sourceType),
                    String.valueOf(maxHistoryDepth),
                    String.valueOf(timeoutInSeconds)
            );
            if (log.isDebugEnabled()) {
                log.debug("[cache] cache updated atomically: key={}, result={}", key, result);
            }
        } catch (Exception e) {
            log.error("[cache] failed to put cache atomically: key={}", key, e);
        }
    }

    /**
     * Build Redis key according to the schema:
     * mx:(entity.meta.code):{entity.id}:{metricCode}:v{version}:{dimensionMap_serialized}
     */
    private String buildKey(String entityCode,
                            Long entityId,
                            String metricCode,
                            Integer version,
                            Map<String, TypedValue> dimensionMap) {
        StringBuilder keyBuilder = new StringBuilder(keyPrefix).append(KEY_SEPARATOR);
        keyBuilder.append(entityCode).append(KEY_SEPARATOR);
        keyBuilder.append(entityId).append(KEY_SEPARATOR);
        keyBuilder.append(metricCode).append(KEY_SEPARATOR);
        keyBuilder.append(VERSION_PREFIX).append(version);

        if (dimensionMap != null && !dimensionMap.isEmpty()) {
            keyBuilder.append(KEY_SEPARATOR);
            String dimensionStr = serializeDimensionMap(dimensionMap);
            keyBuilder.append(dimensionStr);
        }

        String key = keyBuilder.toString();
        if (log.isDebugEnabled()) {
            log.debug("[cache] built cache key: {}", key);
        }

        return key;
    }

    /**
     * Serialize dimension map to string.
     * Format: {entry.key1}_{entry.value1},{entry.key2}_{entry.value2},...
     * Dimensions are sorted by code to ensure consistent key generation.
     */
    private String serializeDimensionMap(Map<String, TypedValue> dimensionMap) {
        return dimensionMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    String dimCode = entry.getKey();
                    TypedValue dimValue = entry.getValue();
                    String valueStr = dimValue != null && dimValue.getValue() != null
                            ? dimValue.getValue().toString()
                            : StrUtil.EMPTY;
                    return dimCode + DIMENSION_KEY_VALUE_SEPARATOR + valueStr;
                })
                .collect(Collectors.joining(DIMENSION_SEPARATOR));
    }
}

