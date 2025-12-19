package lab.zhang.data_science.metrics_mall.cache.impl

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.common.TypedValue
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.AlphaMetricDAO
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.EchoMetricDAO
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

import java.util.stream.Collectors

/**
 * Test for MetricSnapshotCacheServiceRedisImpl.
 *
 * @author Rongjin Zhang
 */
class MetricSnapshotCacheServiceRedisImplTest extends Specification {

    StringRedisTemplate stringRedisTemplate = Mock()

    ObjectMapper objectMapper = Mock()

    MetricSnapshotCacheServiceRedisImpl cacheService

    ValueOperations<String, String> valueOperations = Mock()

    private static final String ENTITY_CODE = "user"
    private static final Long ENTITY_ID = 12345678L
    private static final String METRIC_CODE = "coin_balance"
    private static final Integer VERSION = 1
    private static final Long SNAPSHOT_TS = 1715000001000L
    private static final String METRIC_VALUE = "1050.5"

    def setup() {
        cacheService = new MetricSnapshotCacheServiceRedisImpl(stringRedisTemplate, objectMapper)
        ReflectionTestUtils.setField(MetricSnapshotCacheServiceRedisImpl.class, "KEY_PREFIX", "mx")
        ReflectionTestUtils.setField(MetricSnapshotCacheServiceRedisImpl.class, "MAX_HISTORY_DEPTH", 10)
        stringRedisTemplate.opsForValue() >> valueOperations
    }

    def "test get success"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)
        def expectedMetric = EchoMetricDAO.builder()
                .a(METRIC_VALUE)
                .ts(SNAPSHOT_TS)
                .build()
        def jsonValue = '{"a":"1050.5","ts":1715000001000}'

        when:
        def result = cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, dimensionMap)

        then:
        1 * valueOperations.get(key) >> jsonValue
        1 * objectMapper.readValue(jsonValue, EchoMetricDAO.class) >> expectedMetric
        result != null
        result.a == METRIC_VALUE
        result.ts == SNAPSHOT_TS
    }

    def "test get cache miss"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)

        valueOperations.get(key) >> null

        when:
        def result = cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, dimensionMap)

        then:
        result == null
        1 * valueOperations.get(key)
        0 * objectMapper.readValue(_, _)
    }

    def "test get empty value"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)

        valueOperations.get(key) >> ""

        when:
        def result = cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, dimensionMap)

        then:
        result == null
        1 * valueOperations.get(key)
        0 * objectMapper.readValue(_, _)
    }

    def "test get json parse exception"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)
        def invalidJson = "{invalid json}"

        when:
        def result = cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, dimensionMap)

        then:
        1 * valueOperations.get(key) >> invalidJson
        1 * objectMapper.readValue(invalidJson, EchoMetricDAO.class) >> {
            throw new JsonProcessingException("Invalid JSON") {}
        }
        result == null
    }

    def "test get null entity code"() {
        when:
        def result = cacheService.get(null, ENTITY_ID, METRIC_CODE, VERSION, null)

        then:
        result == null
        0 * valueOperations.get(_)
    }

    def "test get empty entity code"() {
        when:
        def result = cacheService.get("", ENTITY_ID, METRIC_CODE, VERSION, null)

        then:
        result == null
        0 * valueOperations.get(_)
    }

    def "test get blank entity code"() {
        when:
        def result = cacheService.get("   ", ENTITY_ID, METRIC_CODE, VERSION, null)

        then:
        result == null
        0 * valueOperations.get(_)
    }

    def "test get null metric code"() {
        when:
        def result = cacheService.get(ENTITY_CODE, ENTITY_ID, null, VERSION, null)

        then:
        result == null
        0 * valueOperations.get(_)
    }

    def "test get empty metric code"() {
        when:
        def result = cacheService.get(ENTITY_CODE, ENTITY_ID, "", VERSION, null)

        then:
        result == null
        0 * valueOperations.get(_)
    }

    def "test get null version"() {
        when:
        def result = cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE, null, null)

        then:
        result == null
        0 * valueOperations.get(_)
    }

    def "test get no dimensions"() {
        given:
        def key = buildExpectedKey(null)
        def expectedMetric = EchoMetricDAO.builder()
                .a(METRIC_VALUE)
                .ts(SNAPSHOT_TS)
                .build()
        def jsonValue = '{"a":"1050.5","ts":1715000001000}'

        when:
        def result = cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, null)

        then:
        1 * valueOperations.get(key) >> jsonValue
        1 * objectMapper.readValue(jsonValue, EchoMetricDAO.class) >> expectedMetric
        result != null
        result.a == METRIC_VALUE
    }

    def "test get multiple dimensions"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing", "os", "ios")
        def key = buildExpectedKey(dimensionMap)
        def expectedMetric = EchoMetricDAO.builder()
                .a(METRIC_VALUE)
                .ts(SNAPSHOT_TS)
                .build()
        def jsonValue = '{"a":"1050.5","ts":1715000001000}'

        when:
        def result = cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, dimensionMap)

        then:
        1 * valueOperations.get(key) >> jsonValue
        1 * objectMapper.readValue(jsonValue, EchoMetricDAO.class) >> expectedMetric
        result != null
        result.a == METRIC_VALUE
    }

    def "test put success new metric"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)

        when:
        cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, dimensionMap, SNAPSHOT_TS, 0, METRIC_VALUE)

        then:
        1 * valueOperations.get(key) >> null
        1 * objectMapper.writeValueAsString(_ as EchoMetricDAO) >> { EchoMetricDAO metric ->
            return '{"a":"' + metric.a + '","ts":' + metric.ts + '}'
        }
        1 * valueOperations.set(key, _)
    }

    def "test put success update existing metric"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)
        def existingJson = '{"a":"1000.0","ts":1715000000000}'
        def existingMetric = EchoMetricDAO.builder()
                .a("1000.0")
                .ts(1715000000000L)
                .build()

        when:
        cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, dimensionMap, SNAPSHOT_TS, 0, METRIC_VALUE)

        then:
        1 * valueOperations.get(key) >> existingJson
        1 * objectMapper.readValue(existingJson, EchoMetricDAO.class) >> existingMetric
        1 * objectMapper.writeValueAsString(_ as EchoMetricDAO) >> { EchoMetricDAO metric ->
            return '{"a":"' + metric.a + '","ts":' + metric.ts + '}'
        }
        1 * valueOperations.set(key, _)
    }

    def "test put history management"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)
        def existingMetric = EchoMetricDAO.builder()
                .a("1000.0")
                .ts(1715000000000L)
                .build()
        def existingJson = '{"a":"1000.0","ts":1715000000000}'

        when:
        cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, dimensionMap, SNAPSHOT_TS, 0, METRIC_VALUE)

        then:
        1 * valueOperations.get(key) >> existingJson
        1 * objectMapper.readValue(existingJson, EchoMetricDAO.class) >> existingMetric
        1 * objectMapper.writeValueAsString(_ as EchoMetricDAO) >> { EchoMetricDAO metric ->
            return '{"a":"' + metric.a + '","ts":' + metric.ts + '}'
        }
        1 * valueOperations.set(key, _)
    }

    def "test put history exceeds max depth"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)
        def history = []
        for (int i = 0; i < 15; i++) {
            history.add(AlphaMetricDAO.builder()
                    .a("value" + i)
                    .ts(1715000000000L + i)
                    .build())
        }
        def existingMetric = EchoMetricDAO.builder()
                .a("1000.0")
                .ts(1715000000000L)
                .h(history)
                .build()
        def existingJson = '{"a":"1000.0","ts":1715000000000}'

        when:
        cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, dimensionMap, SNAPSHOT_TS, 0, METRIC_VALUE)

        then:
        1 * valueOperations.get(key) >> existingJson
        1 * objectMapper.readValue(existingJson, EchoMetricDAO.class) >> existingMetric
        1 * objectMapper.writeValueAsString(_ as EchoMetricDAO) >> { EchoMetricDAO metric ->
            return '{"a":"' + metric.a + '","ts":' + metric.ts + '}'
        }
        1 * valueOperations.set(key, _)
    }

    def "test put null entity code"() {
        when:
        cacheService.put(null, ENTITY_ID, METRIC_CODE, VERSION, null, SNAPSHOT_TS, 0, METRIC_VALUE)

        then:
        0 * valueOperations.get(_)
        0 * valueOperations.set(_, _)
    }

    def "test put empty entity code"() {
        when:
        cacheService.put("", ENTITY_ID, METRIC_CODE, VERSION, null, SNAPSHOT_TS, 0, METRIC_VALUE)

        then:
        0 * valueOperations.get(_)
        0 * valueOperations.set(_, _)
    }

    def "test put null metric code"() {
        when:
        cacheService.put(ENTITY_CODE, ENTITY_ID, null, VERSION, null, SNAPSHOT_TS, 0, METRIC_VALUE)

        then:
        0 * valueOperations.get(_)
        0 * valueOperations.set(_, _)
    }

    def "test put empty metric code"() {
        when:
        cacheService.put(ENTITY_CODE, ENTITY_ID, "", VERSION, null, SNAPSHOT_TS, 0, METRIC_VALUE)

        then:
        0 * valueOperations.get(_)
        0 * valueOperations.set(_, _)
    }

    def "test put null version"() {
        when:
        cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE, null, null, SNAPSHOT_TS, 0, METRIC_VALUE)

        then:
        0 * valueOperations.get(_)
        0 * valueOperations.set(_, _)
    }

    def "test put null snapshot ts"() {
        when:
        cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, null, null, 0, METRIC_VALUE)

        then:
        0 * valueOperations.get(_)
        0 * valueOperations.set(_, _)
    }

    def "test put null value"() {
        when:
        cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, null, SNAPSHOT_TS, 0, null)

        then:
        0 * valueOperations.get(_)
        0 * valueOperations.set(_, _)
    }

    def "test put empty value"() {
        when:
        cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, null, SNAPSHOT_TS, 0, "")

        then:
        0 * valueOperations.get(_)
        0 * valueOperations.set(_, _)
    }

    def "test put json write exception"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)

        when:
        cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, dimensionMap, SNAPSHOT_TS, 0, METRIC_VALUE)

        then:
        1 * valueOperations.get(key) >> null
        1 * objectMapper.writeValueAsString(_ as EchoMetricDAO) >> {
            throw new JsonProcessingException("Write error") {}
        }
        0 * valueOperations.set(_, _)
    }

    def "test put dimension with null value"() {
        given:
        def dimensionMap = [:]
        dimensionMap.put("city", TypedValue.nullValue())
        def key = buildExpectedKey(dimensionMap as Map<String, TypedValue>)

        when:
        cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, dimensionMap as Map<String, TypedValue>, SNAPSHOT_TS, 0, METRIC_VALUE)

        then:
        1 * valueOperations.get(key) >> null
        1 * objectMapper.writeValueAsString(_ as EchoMetricDAO) >> "{}"
        1 * valueOperations.set(key, _)
    }

    def "test put dimension sorted order"() {
        given:
        def dimensionMap = [:]
        dimensionMap.put("zebra", TypedValue.of("z"))
        dimensionMap.put("apple", TypedValue.of("a"))
        def key = buildExpectedKey(dimensionMap as Map<String, TypedValue>)

        when:
        cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE, VERSION, dimensionMap as Map<String, TypedValue>, SNAPSHOT_TS, 0, METRIC_VALUE)

        then:
        1 * valueOperations.get(key) >> null
        1 * objectMapper.writeValueAsString(_ as EchoMetricDAO) >> "{}"
        1 * valueOperations.set(key, _)
    }

    /**
     * Create dimension map for testing.
     *
     * @param keyValues key-value pairs
     * @return dimension map
     */
    private static Map<String, TypedValue> createDimensionMap(String... keyValues) {
        Map<String, TypedValue> dimMap = [:]
        for (int i = 0; i < keyValues.length; i += 2) {
            dimMap.put(keyValues[i], TypedValue.of(keyValues[i + 1]))
        }
        return dimMap
    }

    /**
     * Build expected Redis key.
     *
     * @param dimensionMap dimension map
     * @return expected key
     */
    private static String buildExpectedKey(Map<String, TypedValue> dimensionMap) {
        def keyBuilder = new StringBuilder("mx:")
        keyBuilder.append(ENTITY_CODE).append(":")
        keyBuilder.append(ENTITY_ID).append(":")
        keyBuilder.append(METRIC_CODE).append(":")
        keyBuilder.append("v").append(VERSION)

        if (dimensionMap != null && !dimensionMap.isEmpty()) {
            keyBuilder.append(":")
            def dimensionStr = dimensionMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map({ entry ->
                        def dimCode = entry.getKey()
                        def dimValue = entry.getValue()
                        def valueStr = dimValue != null && dimValue.getValue() != null
                                ? dimValue.getValue().toString()
                                : ""
                        return dimCode + "_" + valueStr
                    })
                    .collect(Collectors.joining(","))
            keyBuilder.append(dimensionStr)
        }

        return keyBuilder.toString()
    }
}

