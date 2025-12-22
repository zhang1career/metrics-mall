package lab.zhang.data_science.metrics_mall.cache.impl

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.common.TypedValue
import lab.zhang.data_science.metrics_mall.enums.SnapshotSourceTypeEnum
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.AlphaMetricDAO
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.EchoMetricDAO
import lab.zhang.data_science.metrics_mall.pojo.dto.metric.EchoMetricDTO
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.data.redis.core.script.DefaultRedisScript
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
        ReflectionTestUtils.setField(cacheService, "keyPrefix", "mx")
        ReflectionTestUtils.setField(cacheService, "timeoutInSeconds", 86400L)
        ReflectionTestUtils.setField(cacheService, "maxHistoryDepth", 7)
        stringRedisTemplate.opsForValue() >> valueOperations
        
        // Initialize scripts
        def updateMetricSnapshotBatchScript = new DefaultRedisScript<List>()
        updateMetricSnapshotBatchScript.setResultType(List.class)
        ReflectionTestUtils.setField(cacheService, "updateMetricSnapshotBatchScript", updateMetricSnapshotBatchScript)
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

    def "test putBatch success new metric"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(dimensionMap)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        1 * stringRedisTemplate.execute(_ as DefaultRedisScript, [key], _) >> ['{"a":"' + METRIC_VALUE + '","ts":' + SNAPSHOT_TS + ',"s":0,"h":[]}']
    }

    def "test putBatch success update existing metric"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(dimensionMap)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        1 * stringRedisTemplate.execute(_ as DefaultRedisScript, [key], _) >> ['{"a":"' + METRIC_VALUE + '","ts":' + SNAPSHOT_TS + ',"s":0,"h":[{"a":"1000.0","ts":1715000000000,"s":0}]}']
    }

    def "test putBatch history management"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(dimensionMap)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        1 * stringRedisTemplate.execute(_ as DefaultRedisScript, [key], _) >> ['{"a":"' + METRIC_VALUE + '","ts":' + SNAPSHOT_TS + ',"s":0,"h":[{"a":"1000.0","ts":1715000000000,"s":0}]}']
    }

    def "test putBatch history exceeds max depth"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(dimensionMap)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        1 * stringRedisTemplate.execute(_ as DefaultRedisScript, [key], _) >> ['{"a":"' + METRIC_VALUE + '","ts":' + SNAPSHOT_TS + ',"s":0,"h":[]}']
    }

    def "test putBatch null entity code"() {
        given:
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(null)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(null, ENTITY_ID, [metricDTO])

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("invalid entity for put operation")
        0 * stringRedisTemplate.execute(_, _, _)
    }

    def "test putBatch empty entity code"() {
        given:
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(null)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch("", ENTITY_ID, [metricDTO])

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("invalid entity for put operation")
        0 * stringRedisTemplate.execute(_, _, _)
    }

    def "test putBatch null metric code"() {
        given:
        def metricDTO = EchoMetricDTO.builder()
                .code(null)
                .version(VERSION)
                .dimensionMap(null)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metricCode is empty")
        0 * stringRedisTemplate.execute(_, _, _)
    }

    def "test putBatch empty metric code"() {
        given:
        def metricDTO = EchoMetricDTO.builder()
                .code("")
                .version(VERSION)
                .dimensionMap(null)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metricCode is empty")
        0 * stringRedisTemplate.execute(_, _, _)
    }

    def "test putBatch null version"() {
        given:
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(null)
                .dimensionMap(null)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("version is null")
        0 * stringRedisTemplate.execute(_, _, _)
    }

    def "test putBatch null snapshot ts"() {
        given:
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(null)
                .snapshotTs(null)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("snapshotTs is null")
        0 * stringRedisTemplate.execute(_, _, _)
    }

    def "test putBatch null value"() {
        given:
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(null)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(null)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("value is empty")
        0 * stringRedisTemplate.execute(_, _, _)
    }

    def "test putBatch empty value"() {
        given:
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(null)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value("")
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("value is empty")
        0 * stringRedisTemplate.execute(_, _, _)
    }

    def "test putBatch null entityId"() {
        given:
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(null)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, null, [metricDTO])

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("entityId is null")
        0 * stringRedisTemplate.execute(_, _, _)
    }

    def "test putBatch empty metric list"() {
        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [])

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metricList is empty")
        0 * stringRedisTemplate.execute(_, _, _)
    }

    def "test putBatch null metric list"() {
        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, null)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metricList is empty")
        0 * stringRedisTemplate.execute(_, _, _)
    }

    def "test putBatch null metricDTO in list"() {
        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [null])

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metricDTO is null")
        0 * stringRedisTemplate.execute(_, _, _)
    }

    def "test putBatch null sourceType"() {
        given:
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(null)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(null)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("sourceType is null")
        0 * stringRedisTemplate.execute(_, _, _)
    }

    def "test putBatch redis exception"() {
        given:
        def dimensionMap = createDimensionMap("city", "Beijing")
        def key = buildExpectedKey(dimensionMap)
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(dimensionMap)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        1 * stringRedisTemplate.execute(_ as DefaultRedisScript, [key], _) >> {
            throw new RuntimeException("Redis error")
        }
        def exception = thrown(RuntimeException)
        exception.message.contains("putBatch echoMetric failed")
    }

    def "test putBatch dimension with null value"() {
        given:
        def dimensionMap = [:]
        dimensionMap.put("city", TypedValue.nullValue())
        def key = buildExpectedKey(dimensionMap as Map<String, TypedValue>)
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(dimensionMap as Map<String, TypedValue>)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        1 * stringRedisTemplate.execute(_ as DefaultRedisScript, [key], _) >> ['{"a":"' + METRIC_VALUE + '","ts":' + SNAPSHOT_TS + ',"s":0,"h":[]}']
    }

    def "test putBatch dimension sorted order"() {
        given:
        def dimensionMap = [:]
        dimensionMap.put("zebra", TypedValue.of("z"))
        dimensionMap.put("apple", TypedValue.of("a"))
        def key = buildExpectedKey(dimensionMap as Map<String, TypedValue>)
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE)
                .version(VERSION)
                .dimensionMap(dimensionMap as Map<String, TypedValue>)
                .snapshotTs(SNAPSHOT_TS)
                .sourceType(SnapshotSourceTypeEnum.EXTERNAL)
                .value(METRIC_VALUE)
                .build()

        when:
        cacheService.putBatch(ENTITY_CODE, ENTITY_ID, [metricDTO])

        then:
        1 * stringRedisTemplate.execute(_ as DefaultRedisScript, [key], _) >> ['{"a":"' + METRIC_VALUE + '","ts":' + SNAPSHOT_TS + ',"s":0,"h":[]}']
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

