package lab.zhang.data_science.metrics_mall.service.impl

import lab.zhang.data_science.metrics_mall.cache.MetricSnapshotCacheService
import lab.zhang.data_science.metrics_mall.common.TypedValue
import lab.zhang.data_science.metrics_mall.model.Entity
import lab.zhang.data_science.metrics_mall.model.metric.AlphaMetric
import lab.zhang.data_science.metrics_mall.model.metric.EchoMetric
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.EchoMetricDAO
import lab.zhang.data_science.metrics_mall.pojo.dto.metric.EchoMetricDTO
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO
import lab.zhang.data_science.metrics_mall.service.EntityService
import lab.zhang.data_science.metrics_mall.service.MetricService
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricStructMapper
import spock.lang.Specification

/**
 * Test for MetricSnapshotServiceImpl.
 *
 * @author Rongjin Zhang
 */
class MetricSnapshotServiceImplTest extends Specification {

    EntityService entityService = Mock()
    MetricSnapshotCacheService cacheService = Mock()
    MetricService metricService = Mock()
    MetricStructMapper metricStructMapper = Mock()

    MetricSnapshotServiceImpl service

    private static final String ENTITY_CODE = "user"
    private static final Long ENTITY_ID = 12345678L
    private static final String METRIC_CODE_1 = "coin_balance"
    private static final String METRIC_CODE_2 = "level"
    private static final Integer VERSION = 1
    private static final Long SNAPSHOT_TS = 1715000001000L
    private static final String METRIC_VALUE_1 = "1050.5"
    private static final String METRIC_VALUE_2 = "10"

    def setup() {
        service = new MetricSnapshotServiceImpl()
        service.entityService = entityService
        service.cacheService = cacheService
        service.metricService = metricService
        service.metricStructMapper = metricStructMapper
    }

    def "test querySnapshot with empty metric list should throw exception"() {
        given:
        def dto = MetricSnapshotDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([])
                .build()

        when:
        service.querySnapshot(dto)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "[snap] metric list is empty"
        0 * entityService.getEntity(_, _)
    }

    def "test querySnapshot with null metric list should throw exception"() {
        given:
        def dto = MetricSnapshotDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList(null)
                .build()

        when:
        service.querySnapshot(dto)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "[snap] metric list is empty"
        0 * entityService.getEntity(_, _)
    }

    def "test querySnapshot with entity not found should return null"() {
        given:
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .build()

        entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> null

        when:
        def result = service.querySnapshot(dto)

        then:
        result == null
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID)
        0 * cacheService.get(_, _, _, _, _)
    }

    def "test querySnapshot with snapshotTs null should return current snapshot"() {
        given:
        def entity = createEntity()
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(null)
                .build()
        def metricMetaDAO = createMetricMetaDAO(METRIC_CODE_1)
        def echoMetricMetaDAO = createEchoMetricDAO(METRIC_VALUE_1, SNAPSHOT_TS)
        def echoMetric = createEchoMetric(METRIC_CODE_1, METRIC_VALUE_1, SNAPSHOT_TS)

        when:
        def result = service.querySnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO
        1 * cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, null) >> echoMetricMetaDAO
        1 * metricStructMapper.echoMetricDaoToModel(echoMetricMetaDAO, metricMetaDAO) >> echoMetric
        result != null
        result.entity == entity
        result.snapshotTs != null
        result.snapshotTs > 0
        result.metricList.size() == 1
        result.metricList[0].code == METRIC_CODE_1
        result.metricList[0].value.getValueStr() == METRIC_VALUE_1
        result.metricList[0].snapshotTs == SNAPSHOT_TS
    }

    def "test querySnapshot with snapshotTs zero should return current snapshot"() {
        given:
        def entity = createEntity()
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(0L)
                .build()
        def metricMetaDAO = createMetricMetaDAO(METRIC_CODE_1)
        def echoMetricMetaDAO = createEchoMetricDAO(METRIC_VALUE_1, SNAPSHOT_TS)
        def echoMetric = createEchoMetric(METRIC_CODE_1, METRIC_VALUE_1, SNAPSHOT_TS)

        when:
        def result = service.querySnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO
        1 * cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, null) >> echoMetricMetaDAO
        1 * metricStructMapper.echoMetricDaoToModel(echoMetricMetaDAO, metricMetaDAO) >> echoMetric
        result != null
        result.entity == entity
        result.snapshotTs != null
        result.snapshotTs > 0
        result.metricList.size() == 1
        result.metricList[0].code == METRIC_CODE_1
        result.metricList[0].value.getValueStr() == METRIC_VALUE_1
    }

    def "test querySnapshot with snapshotTs should return filtered snapshot"() {
        given:
        def entity = createEntity()
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .dimensionMap(null)
                .build()
        def targetSnapshotTs = SNAPSHOT_TS + 1000L
        def dto = MetricSnapshotDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(targetSnapshotTs)
                .build()
        def metricMetaDAO = createMetricMetaDAO(METRIC_CODE_1)
        def echoMetricMetaDAO = createEchoMetricDAO(METRIC_VALUE_1, SNAPSHOT_TS)
        def echoMetric = createEchoMetricWithHistory(METRIC_CODE_1, METRIC_VALUE_1, SNAPSHOT_TS, targetSnapshotTs)

        when:
        def result = service.querySnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO
        1 * cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, null) >> echoMetricMetaDAO
        1 * metricStructMapper.echoMetricDaoToModel(echoMetricMetaDAO, metricMetaDAO) >> echoMetric
        result != null
        result.entity == entity
        result.snapshotTs == targetSnapshotTs
        result.metricList.size() == 1
        result.metricList[0].code == METRIC_CODE_1
        result.metricList[0].snapshotTs <= targetSnapshotTs
    }

    def "test querySnapshot with multiple metrics should return all metrics"() {
        given:
        def entity = createEntity()
        def metricDTO1 = EchoMetricDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .dimensionMap(null)
                .build()
        def metricDTO2 = EchoMetricDTO.builder()
                .code(METRIC_CODE_2)
                .version(VERSION)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO1, metricDTO2])
                .snapshotTs(null)
                .build()
        def metricMetaDAO1 = createMetricMetaDAO(METRIC_CODE_1)
        def metricMetaDAO2 = createMetricMetaDAO(METRIC_CODE_2)
        def echoMetricMetaDAO1 = createEchoMetricDAO(METRIC_VALUE_1, SNAPSHOT_TS)
        def echoMetricMetaDAO2 = createEchoMetricDAO(METRIC_VALUE_2, SNAPSHOT_TS)
        def echoMetric1 = createEchoMetric(METRIC_CODE_1, METRIC_VALUE_1, SNAPSHOT_TS)
        def echoMetric2 = createEchoMetric(METRIC_CODE_2, METRIC_VALUE_2, SNAPSHOT_TS)

        when:
        def result = service.querySnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO1
        1 * metricService.getMetricDaoByCode(METRIC_CODE_2) >> metricMetaDAO2
        1 * cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, null) >> echoMetricMetaDAO1
        1 * cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE_2, VERSION, null) >> echoMetricMetaDAO2
        1 * metricStructMapper.echoMetricDaoToModel(echoMetricMetaDAO1, metricMetaDAO1) >> echoMetric1
        1 * metricStructMapper.echoMetricDaoToModel(echoMetricMetaDAO2, metricMetaDAO2) >> echoMetric2
        result != null
        result.entity == entity
        result.metricList.size() == 2
        result.metricList.find { it.code == METRIC_CODE_1 } != null
        result.metricList.find { it.code == METRIC_CODE_2 } != null
    }

    def "test querySnapshot with null metric in list should filter out"() {
        given:
        def entity = createEntity()
        def metricDTO1 = EchoMetricDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO1, null])
                .snapshotTs(null)
                .build()
        def metricMetaDAO1 = createMetricMetaDAO(METRIC_CODE_1)
        def echoMetricMetaDAO1 = createEchoMetricDAO(METRIC_VALUE_1, SNAPSHOT_TS)
        def echoMetric1 = createEchoMetric(METRIC_CODE_1, METRIC_VALUE_1, SNAPSHOT_TS)

        when:
        def result = service.querySnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO1
        1 * cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, null) >> echoMetricMetaDAO1
        1 * metricStructMapper.echoMetricDaoToModel(echoMetricMetaDAO1, metricMetaDAO1) >> echoMetric1
        result != null
        result.metricList.size() == 1
        result.metricList[0].code == METRIC_CODE_1
    }

    def "test querySnapshot with metric not found in service should filter out"() {
        given:
        def entity = createEntity()
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(null)
                .build()

        when:
        def result = service.querySnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> null
        0 * cacheService.get(_, _, _, _, _)
        result != null
        result.metricList.isEmpty()
    }

    def "test querySnapshot with cache miss should handle null"() {
        given:
        def entity = createEntity()
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(null)
                .build()
        def metricMetaDAO = createMetricMetaDAO(METRIC_CODE_1)

        when:
        def result = service.querySnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO
        1 * cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, null) >> null
        1 * metricStructMapper.echoMetricDaoToModel(null, metricMetaDAO) >> null
        result != null
        result.metricList.isEmpty()
    }

    def "test querySnapshot with snapshotTs and no matching history should filter out"() {
        given:
        def entity = createEntity()
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .dimensionMap(null)
                .build()
        def targetSnapshotTs = SNAPSHOT_TS - 1000L
        def dto = MetricSnapshotDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(targetSnapshotTs)
                .build()
        def metricMetaDAO = createMetricMetaDAO(METRIC_CODE_1)
        def echoMetricMetaDAO = createEchoMetricDAO(METRIC_VALUE_1, SNAPSHOT_TS)
        def echoMetric = createEchoMetric(METRIC_CODE_1, METRIC_VALUE_1, SNAPSHOT_TS)

        when:
        def result = service.querySnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO
        1 * cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, null) >> echoMetricMetaDAO
        1 * metricStructMapper.echoMetricDaoToModel(echoMetricMetaDAO, metricMetaDAO) >> echoMetric
        result != null
        result.entity == entity
        result.snapshotTs == targetSnapshotTs
        result.metricList.isEmpty()
    }

    def "test querySnapshot with dimension map"() {
        given:
        def entity = createEntity()
        def dimensionMap = ["city": TypedValue.of("Beijing"), "os": TypedValue.of("ios")]
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .dimensionMap(dimensionMap)
                .build()
        def dto = MetricSnapshotDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(null)
                .build()
        def metricMetaDAO = createMetricMetaDAO(METRIC_CODE_1)
        def echoMetricMetaDAO = createEchoMetricDAO(METRIC_VALUE_1, SNAPSHOT_TS)
        def echoMetric = createEchoMetric(METRIC_CODE_1, METRIC_VALUE_1, SNAPSHOT_TS)

        when:
        def result = service.querySnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO
        1 * cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, dimensionMap) >> echoMetricMetaDAO
        1 * metricStructMapper.echoMetricDaoToModel(echoMetricMetaDAO, metricMetaDAO) >> echoMetric
        result != null
        result.metricList.size() == 1
    }

    def "test querySnapshot with precision and unit"() {
        given:
        def entity = createEntity()
        def metricDTO = EchoMetricDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(null)
                .build()
        def metricMetaDAO = createMetricMetaDAOWithPrecisionAndUnit(METRIC_CODE_1, 2, "USD")
        def echoMetricMetaDAO = createEchoMetricDAO(METRIC_VALUE_1, SNAPSHOT_TS)
        def echoMetric = createEchoMetricWithPrecisionAndUnit(METRIC_CODE_1, METRIC_VALUE_1, SNAPSHOT_TS, 2, "USD")

        when:
        def result = service.querySnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO
        1 * cacheService.get(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, null) >> echoMetricMetaDAO
        1 * metricStructMapper.echoMetricDaoToModel(echoMetricMetaDAO, metricMetaDAO) >> echoMetric
        result != null
        result.metricList.size() == 1
        result.metricList[0].precision == 2
        result.metricList[0].unit == "USD"
    }

    /**
     * Create entity for testing.
     */
    private static Entity createEntity() {
        def entityMeta = Entity.EntityMeta.builder()
                .id(1)
                .code(ENTITY_CODE)
                .name("User")
                .description("User entity")
                .build()
        return Entity.builder()
                .meta(entityMeta)
                .id(ENTITY_ID)
                .build()
    }

    /**
     * Create MetricMetaDAO for testing.
     */
    private static MetricMetaDAO createMetricMetaDAO(String code) {
        return MetricMetaDAO.builder()
                .id(10000001L)
                .code(code)
                .name("Test Metric")
                .description("Test metric description")
                .metricType(0)
                .valueType(3)
                .precision(2)
                .aggregationType(0)
                .unit("")
                .build()
    }

    /**
     * Create MetricMetaDAO with precision and unit for testing.
     */
    private static MetricMetaDAO createMetricMetaDAOWithPrecisionAndUnit(String code, Integer precision, String unit) {
        return MetricMetaDAO.builder()
                .id(10000001L)
                .code(code)
                .name("Test Metric")
                .description("Test metric description")
                .metricType(0)
                .valueType(3)
                .precision(precision)
                .aggregationType(0)
                .unit(unit)
                .build()
    }

    /**
     * Create EchoMetricDAO for testing.
     */
    private static EchoMetricDAO createEchoMetricDAO(String value, Long snapshotTs) {
        return EchoMetricDAO.builder()
                .a(value)
                .ts(snapshotTs)
                .h([])
                .build()
    }

    /**
     * Create EchoMetric for testing.
     */
    private static EchoMetric createEchoMetric(String code, String value, Long snapshotTs) {
        return EchoMetric.builder()
                .code(code)
                .value(TypedValue.of(value))
                .snapshotTs(snapshotTs)
                .precision(2)
                .unit("")
                .historyList([])
                .build()
    }

    /**
     * Create EchoMetric with precision and unit for testing.
     */
    private static EchoMetric createEchoMetricWithPrecisionAndUnit(String code, String value, Long snapshotTs, Integer precision, String unit) {
        return EchoMetric.builder()
                .code(code)
                .value(TypedValue.of(value))
                .snapshotTs(snapshotTs)
                .precision(precision)
                .unit(unit)
                .historyList([])
                .build()
    }

    /**
     * Create EchoMetric with history for testing.
     */
    private static EchoMetric createEchoMetricWithHistory(String code, String value, Long snapshotTs, Long targetSnapshotTs) {
        def historyList = [
                AlphaMetric.builder()
                        .code(code)
                        .value(TypedValue.of(value))
                        .snapshotTs(snapshotTs)
                        .build(),
                AlphaMetric.builder()
                        .code(code)
                        .value(TypedValue.of("1200.0"))
                        .snapshotTs(targetSnapshotTs)
                        .build()
        ]
        return EchoMetric.builder()
                .code(code)
                .value(TypedValue.of(value))
                .snapshotTs(snapshotTs)
                .precision(2)
                .unit("")
                .historyList(historyList)
                .build()
    }
}

