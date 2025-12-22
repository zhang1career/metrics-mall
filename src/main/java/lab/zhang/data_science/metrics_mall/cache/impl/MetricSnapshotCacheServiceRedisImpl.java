package lab.zhang.data_science.metrics_mall.cache.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lab.zhang.data_science.metrics_mall.cache.MetricSnapshotCacheService;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.EchoMetricDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.metric.EchoMetricDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
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

    private DefaultRedisScript<List> updateMetricSnapshotBatchScript;

    @PostConstruct
    public void init() {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/update_metric_snapshot.lua")));
        script.setResultType(String.class);
        updateMetricSnapshotScript = script;

        DefaultRedisScript<List> batchScript = new DefaultRedisScript<>();
        batchScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/update_metric_snapshot_batch.lua")));
        batchScript.setResultType(List.class);
        updateMetricSnapshotBatchScript = batchScript;
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
    public void putBatch(String entityCode, Long entityId, List<EchoMetricDTO> metricList) {
        // batch validation
        if (StrUtil.isBlank(entityCode)) {
            throw new IllegalArgumentException("[cache] putBatch failed, invalid entity for put operation");
        }
        if (entityId == null) {
            throw new IllegalArgumentException("[cache] putBatch failed, entityId is null");
        }
        if (CollectionUtils.isEmpty(metricList)) {
            throw new IllegalArgumentException("[cache] putBatch failed, metricList is empty");
        }

        List<String> validationErrors = new ArrayList<>();
        Map<String, List<String>> keyArgsMap = new LinkedHashMap<>();

        for (int i = 0; i < metricList.size(); i++) {
            EchoMetricDTO metricDTO = metricList.get(i);
            if (metricDTO == null) {
                validationErrors.add(String.format("metricList[%d]: metricDTO is null", i));
                continue;
            }
            String metricCode = metricDTO.getCode();
            if (StrUtil.isBlank(metricCode)) {
                validationErrors.add(String.format("metricList[%d]: metricCode is empty", i));
                continue;
            }
            Integer version = metricDTO.getVersion();
            if (version == null) {
                validationErrors.add(String.format("metricList[%d]: version is null", i));
                continue;
            }
            Long snapshotTs = metricDTO.getSnapshotTs();
            if (snapshotTs == null) {
                validationErrors.add(String.format("metricList[%d]: snapshotTs is null", i));
                continue;
            }
            if (metricDTO.getSourceType() == null || metricDTO.getSourceType().getId() == null) {
                validationErrors.add(String.format("metricList[%d]: sourceType is null", i));
                continue;
            }
            String value = metricDTO.getValue();
            if (StrUtil.isBlank(value)) {
                validationErrors.add(String.format("metricList[%d]: value is empty", i));
                continue;
            }

            String key = buildKey(entityCode, entityId, metricCode, version, metricDTO.getDimensionMap());
            List<String> args = new ArrayList<>();
            args.add(value);
            args.add(String.valueOf(snapshotTs));
            args.add(String.valueOf(metricDTO.getSourceType().getId()));
            args.add(String.valueOf(maxHistoryDepth));
            args.add(String.valueOf(timeoutInSeconds));
            keyArgsMap.put(key, args);
        }

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("[cache] putBatch echoMetric, validation failed: " + String.join(", ", validationErrors));
        }

        if (keyArgsMap.isEmpty()) {
            log.warn("[cache] putBatch echoMetric, no valid metrics to write");
            return;
        }

        // convert Map to List for execute method
        List<String> keys = new ArrayList<>(keyArgsMap.keySet());
        List<String> args = new ArrayList<>();
        for (List<String> argList : keyArgsMap.values()) {
            args.addAll(argList);
        }

        try {
            List<String> results = stringRedisTemplate.execute(
                    updateMetricSnapshotBatchScript,
                    keys,
                    args.toArray(new String[0])
            );
            if (log.isDebugEnabled()) {
                log.debug("[cache] cache updated atomically in batch: keyCount={}, results={}", keys.size(), results);
            }
        } catch (Exception e) {
            log.error("[cache] failed to put cache atomically in batch: keyCount={}", keys.size(), e);
            throw new RuntimeException("[cache] putBatch echoMetric failed", e);
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

