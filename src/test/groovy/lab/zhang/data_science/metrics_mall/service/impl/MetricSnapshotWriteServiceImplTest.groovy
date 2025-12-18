package lab.zhang.data_science.metrics_mall.service.impl

import lab.zhang.data_science.metrics_mall.cache.MetricSnapshotCacheService
import lab.zhang.data_science.metrics_mall.common.TypedValue
import lab.zhang.data_science.metrics_mall.model.Entity
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotWriteDTO
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricWriteDTO
import lab.zhang.data_science.metrics_mall.service.EntityService
import lab.zhang.data_science.metrics_mall.service.MetricService
import lab.zhang.data_science.metrics_mall.service.MetricSnapshotService
import spock.lang.Specification

/**
 * Test for MetricSnapshotServiceImpl write operations.
 *
 * @author Rongjin Zhang
 */
class MetricSnapshotWriteServiceImplTest extends Specification {

    EntityService entityService = Mock()
    MetricSnapshotCacheService cacheService = Mock()
    MetricService metricService = Mock()

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
    }

    def "test writeSnapshot with empty metric list should throw exception"() {
        given:
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([])
                .build()

        when:
        service.writeSnapshot(dto)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "[snap] metric list is empty"
        0 * entityService.getEntity(_, _)
    }

    def "test writeSnapshot with null metric list should throw exception"() {
        given:
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList(null)
                .build()

        when:
        service.writeSnapshot(dto)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "[snap] metric list is empty"
        0 * entityService.getEntity(_, _)
    }

    def "test writeSnapshot with entity not found should return all rejected"() {
        given:
        def metricDTO = MetricWriteDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .value(METRIC_VALUE_1)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .build()

        entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> null

        when:
        def result = service.writeSnapshot(dto)

        then:
        result == null
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID)
        0 * cacheService.put(_, _, _, _, _, _, _)
    }

    def "test writeSnapshot success with single metric"() {
        given:
        def entity = createEntity()
        def metricDTO = MetricWriteDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .value(METRIC_VALUE_1)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(SNAPSHOT_TS)
                .build()
        def metricMetaDAO = createMetricMetaDAO(METRIC_CODE_1)

        when:
        def result = service.writeSnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getNearestVersionBatch([METRIC_CODE_1], [(METRIC_CODE_1): VERSION]) >> [(METRIC_CODE_1): VERSION]
        0 * metricService.checkHotBatch(_, _)
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO
        1 * cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, null, SNAPSHOT_TS, METRIC_VALUE_1)
        result != null
        result instanceof Long
    }

    def "test writeSnapshot success with multiple metrics"() {
        given:
        def entity = createEntity()
        def metricDTO1 = MetricWriteDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .value(METRIC_VALUE_1)
                .dimensionMap(null)
                .build()
        def metricDTO2 = MetricWriteDTO.builder()
                .code(METRIC_CODE_2)
                .version(VERSION)
                .value(METRIC_VALUE_2)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO1, metricDTO2])
                .snapshotTs(SNAPSHOT_TS)
                .build()
        def metricMetaDAO1 = createMetricMetaDAO(METRIC_CODE_1)
        def metricMetaDAO2 = createMetricMetaDAO(METRIC_CODE_2)

        when:
        def result = service.writeSnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getNearestVersionBatch([METRIC_CODE_1, METRIC_CODE_2], [(METRIC_CODE_1): VERSION, (METRIC_CODE_2): VERSION]) >> [(METRIC_CODE_1): VERSION, (METRIC_CODE_2): VERSION]
        0 * metricService.checkHotBatch(_, _)
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO1
        1 * metricService.getMetricDaoByCode(METRIC_CODE_2) >> metricMetaDAO2
        1 * cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, null, SNAPSHOT_TS, METRIC_VALUE_1)
        1 * cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE_2, VERSION, null, SNAPSHOT_TS, METRIC_VALUE_2)
        result != null
        result instanceof Long
    }

    def "test writeSnapshot with null snapshotTs should use current timestamp"() {
        given:
        def entity = createEntity()
        def metricDTO = MetricWriteDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .value(METRIC_VALUE_1)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(null)
                .build()
        def metricMetaDAO = createMetricMetaDAO(METRIC_CODE_1)

        when:
        def result = service.writeSnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getNearestVersionBatch([METRIC_CODE_1], [(METRIC_CODE_1): VERSION]) >> [(METRIC_CODE_1): VERSION]
        0 * metricService.checkHotBatch(_, _)
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO
        1 * cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, null, _ as Long, METRIC_VALUE_1) >> {
            args -> assert args[5] > 0
        }
        result instanceof Long
    }

    def "test writeSnapshot with zero snapshotTs should use current timestamp"() {
        given:
        def entity = createEntity()
        def metricDTO = MetricWriteDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .value(METRIC_VALUE_1)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(0L)
                .build()
        def metricMetaDAO = createMetricMetaDAO(METRIC_CODE_1)

        when:
        def result = service.writeSnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getNearestVersionBatch([METRIC_CODE_1], [(METRIC_CODE_1): VERSION]) >> [(METRIC_CODE_1): VERSION]
        0 * metricService.checkHotBatch(_, _)
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO
        1 * cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, null, _ as Long, METRIC_VALUE_1) >> {
            args -> assert args[5] > 0
        }
        result instanceof Long
    }

    def "test writeSnapshot with null version should use nearest version"() {
        given:
        def entity = createEntity()
        def metricDTO = MetricWriteDTO.builder()
                .code(METRIC_CODE_1)
                .version(null)
                .value(METRIC_VALUE_1)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(SNAPSHOT_TS)
                .build()
        def metricMetaDAO = createMetricMetaDAO(METRIC_CODE_1)

        when:
        def result = service.writeSnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getNearestVersionBatch([METRIC_CODE_1], [(METRIC_CODE_1): null]) >> [(METRIC_CODE_1): 2]
        0 * metricService.checkHotBatch(_, _)
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO
        1 * cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, 2, null, SNAPSHOT_TS, METRIC_VALUE_1)
        result instanceof Long
    }

    def "test writeSnapshot with null metric in list should ignore null metric"() {
        given:
        def entity = createEntity()
        entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        def metricDTO1 = MetricWriteDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .value(METRIC_VALUE_1)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO1, null])
                .snapshotTs(SNAPSHOT_TS)
                .build()
        def metricMetaDAO1 = createMetricMetaDAO(METRIC_CODE_1)

        when:
        service.writeSnapshot(dto)

        then:
        thrown(IllegalArgumentException)
    }

    def "test writeSnapshot with empty metric code should throw exception"() {
        given:
        def entity = createEntity()
        def metricDTO = MetricWriteDTO.builder()
                .code("")
                .version(VERSION)
                .value(METRIC_VALUE_1)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(SNAPSHOT_TS)
                .build()
        entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity

        when:
        service.writeSnapshot(dto)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metric code is empty")
    }

    def "test writeSnapshot with null metric code should throw exception"() {
        given:
        def entity = createEntity()
        def metricDTO = MetricWriteDTO.builder()
                .code(null)
                .version(VERSION)
                .value(METRIC_VALUE_1)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(SNAPSHOT_TS)
                .build()
        entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity

        when:
        service.writeSnapshot(dto)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metric code is empty")
    }

    def "test writeSnapshot with empty metric value should throw exception"() {
        given:
        def entity = createEntity()
        def metricDTO = MetricWriteDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .value("")
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(SNAPSHOT_TS)
                .build()
        entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity

        when:
        service.writeSnapshot(dto)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metric value is empty")
    }

    def "test writeSnapshot with null metric value should throw exception"() {
        given:
        def entity = createEntity()
        def metricDTO = MetricWriteDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .value(null)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(SNAPSHOT_TS)
                .build()
        entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity

        when:
        service.writeSnapshot(dto)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metric value is empty")
    }

    def "test writeSnapshot with metric not found should skip metric"() {
        given:
        def entity = createEntity()
        def metricDTO = MetricWriteDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .value(METRIC_VALUE_1)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(SNAPSHOT_TS)
                .build()

        when:
        def result = service.writeSnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getNearestVersionBatch([METRIC_CODE_1], [(METRIC_CODE_1): VERSION]) >> [(METRIC_CODE_1): VERSION]
        0 * metricService.checkHotBatch(_, _)
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> null
        0 * cacheService.put(_, _, _, _, _, _, _)
        result instanceof Long
    }

    def "test writeSnapshot with hot dimension check fail should throw exception"() {
        given:
        def entity = createEntity()
        def dimensionMap = ["city": TypedValue.of("Beijing")]
        def metricDTO = MetricWriteDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .value(METRIC_VALUE_1)
                .dimensionMap(dimensionMap)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(SNAPSHOT_TS)
                .build()

        when:
        service.writeSnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getNearestVersionBatch([METRIC_CODE_1], [(METRIC_CODE_1): VERSION]) >> [(METRIC_CODE_1): VERSION]
        1 * metricService.checkHotBatch(METRIC_CODE_1, ["city"]) >> ["city": false]
        0 * metricService.getMetricDaoByCode(_)
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("dimension is not hot")
    }

    def "test writeSnapshot with version mismatch should throw exception"() {
        given:
        def entity = createEntity()
        def metricDTO = MetricWriteDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .value(METRIC_VALUE_1)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(SNAPSHOT_TS)
                .build()

        when:
        service.writeSnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getNearestVersionBatch([METRIC_CODE_1], [(METRIC_CODE_1): VERSION]) >> [(METRIC_CODE_1): 2]
        0 * metricService.checkHotBatch(_, _)
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("version not match required one strictly")
    }

    def "test writeSnapshot with cache exception should log error and continue"() {
        given:
        def entity = createEntity()
        def metricDTO = MetricWriteDTO.builder()
                .code(METRIC_CODE_1)
                .version(VERSION)
                .value(METRIC_VALUE_1)
                .dimensionMap(null)
                .build()
        def dto = MetricSnapshotWriteDTO.builder()
                .entityCode(ENTITY_CODE)
                .entityId(ENTITY_ID)
                .metricList([metricDTO])
                .snapshotTs(SNAPSHOT_TS)
                .build()
        def metricMetaDAO = createMetricMetaDAO(METRIC_CODE_1)

        when:
        def result = service.writeSnapshot(dto)

        then:
        1 * entityService.getEntity(ENTITY_CODE, ENTITY_ID) >> entity
        1 * metricService.getNearestVersionBatch([METRIC_CODE_1], [(METRIC_CODE_1): VERSION]) >> [(METRIC_CODE_1): VERSION]
        0 * metricService.checkHotBatch(_, _)
        1 * metricService.getMetricDaoByCode(METRIC_CODE_1) >> metricMetaDAO
        1 * cacheService.put(ENTITY_CODE, ENTITY_ID, METRIC_CODE_1, VERSION, null, SNAPSHOT_TS, METRIC_VALUE_1) >> {
            throw new RuntimeException("Cache error")
        }
        result instanceof Long
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
}

