package lab.zhang.data_science.metrics_mall.struct_mapper

import lab.zhang.data_science.metrics_mall.common.TypedValue
import lab.zhang.data_science.metrics_mall.enums.SnapshotSourceTypeEnum
import lab.zhang.data_science.metrics_mall.model.Entity
import lab.zhang.data_science.metrics_mall.model.MetricSnapshot
import lab.zhang.data_science.metrics_mall.model.metric.BetaMetric
import lab.zhang.data_science.metrics_mall.pojo.dto.metric.EchoMetricDTO
import lab.zhang.data_science.metrics_mall.pojo.qo.EchoMetricQO
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricSnapshotQO
import spock.lang.Specification

/**
 * Test for MetricSnapshotStructMapper.
 *
 * @author Rongjin Zhang
 */
class MetricSnapshotStructMapperTest extends Specification {

    MetricSnapshotStructMapper mapper = new MetricSnapshotStructMapperImpl()
    MetricStructMapper metricStructMapper = Mock()

    def "test MetricSnapshot qoToDto success"() {
        given:
        def qo = MetricSnapshotQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics([EchoMetricQO.builder().code("m1").alias("a1").build()])
                .snapshotTs(1000L)
                .isAtomic(1)
                .build()

        when:
        def dto = mapper.qoToDto(qo, metricStructMapper, SnapshotSourceTypeEnum.EXTERNAL)

        then:
        1 * metricStructMapper.echoQoToDtoBatch(qo.metrics, qo.snapshotTs, SnapshotSourceTypeEnum.EXTERNAL) >> []
        dto != null
        dto.entityCode == "user"
        dto.entityId == 12345678L
        dto.snapshotTs == 1000L
        dto.isAtomic == true
    }

    def "test qoToDto with null"() {
        expect:
        mapper.qoToDto(null, metricStructMapper, SnapshotSourceTypeEnum.EXTERNAL) == null
    }

    def "test modelToVo success"() {
        given:
        def entity = Entity.builder()
                .meta(Entity.EntityMeta.builder().code("user").build())
                .id(12345678L)
                .build()
        def metricList = [
                BetaMetric.builder().code("m1").value(TypedValue.of(10.5)).snapshotTs(1000L).build()
        ]
        def model = MetricSnapshot.builder()
                .entity(entity)
                .metricList(metricList)
                .snapshotTs(1000L)
                .build()

        when:
        def vo = mapper.modelToVo(model, metricStructMapper)

        then:
        vo != null
        vo.entityCode == "user"
        vo.entityId == 12345678L
        vo.valueMap.get("m1").getValueStr() == "10.5"
        vo.snapshotTsMap.get("m1") == 1000L
    }

    def "test modelToVo with null"() {
        expect:
        mapper.modelToVo(null, metricStructMapper) == null
    }

    def "test EchoMetric qoToDto success"() {
        given:
        def echoQo = EchoMetricQO.builder()
                .code("m1")
                .alias("a1")
                .v(1)
                .dims(["city": "Beijing"])
                .value(10.5)
                .build()
        def qo = MetricSnapshotQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics([echoQo])
                .snapshotTs(1000L)
                .build()
        def expectedMetricDto = EchoMetricDTO.builder()
                .code("m1")
                .version(1)
                .value("10.5")
                .dimensionMap(["city": TypedValue.of("Beijing")])
                .snapshotTs(1000L)
                .build()

        when:
        def dto = mapper.qoToDto(qo, metricStructMapper, SnapshotSourceTypeEnum.EXTERNAL)

        then:
        1 * metricStructMapper.echoQoToDtoBatch(qo.metrics, qo.snapshotTs, SnapshotSourceTypeEnum.EXTERNAL) >> [expectedMetricDto]
        dto != null
        dto.entityCode == "user"
        dto.entityId == 12345678L
        dto.snapshotTs == 1000L
        dto.metricList.size() == 1
        dto.metricList[0].code == "m1"
        dto.metricList[0].version == 1
    }

    def "test betaModelToValueMap success"() {
        given:
        def metricList = [
                BetaMetric.builder().code("m1").value(TypedValue.of(10.5)).build(),
                BetaMetric.builder().code("m2").value(TypedValue.of("abc")).build()
        ]

        when:
        def valueMap = mapper.betaModelToValueMap(metricList)

        then:
        valueMap.size() == 2
        valueMap.get("m1").getValueStr() == "10.5"
        valueMap.get("m2").getValueStr() == "abc"
    }

    def "test betaModelToTimestampMap success"() {
        given:
        def metricList = [
                BetaMetric.builder().code("m1").snapshotTs(1000L).build(),
                BetaMetric.builder().code("m2").snapshotTs(2000L).build()
        ]

        when:
        def tsMap = mapper.betaModelToTimestampMap(metricList)

        then:
        tsMap.size() == 2
        tsMap.get("m1") == 1000L
        tsMap.get("m2") == 2000L
    }
}

