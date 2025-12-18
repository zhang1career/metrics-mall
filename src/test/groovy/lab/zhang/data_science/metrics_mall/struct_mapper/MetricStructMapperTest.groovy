package lab.zhang.data_science.metrics_mall.struct_mapper

import lab.zhang.data_science.metrics_mall.common.TypedValue
import lab.zhang.data_science.metrics_mall.enums.AggregationTypeEnum
import lab.zhang.data_science.metrics_mall.enums.MetricTypeEnum
import lab.zhang.data_science.metrics_mall.enums.ValueTypeEnum
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.EchoMetricDAO
import lab.zhang.data_science.metrics_mall.pojo.qo.EchoMetricQO
import spock.lang.Specification

/**
 * Test for MetricStructMapper.
 *
 * @author Rongjin Zhang
 */
class MetricStructMapperTest extends Specification {

    MetricStructMapper mapper = new MetricStructMapperImpl()

    def "test mapMetricType"() {
        expect:
        mapper.mapMetricType(0) == MetricTypeEnum.ATOMIC
        mapper.mapMetricType(1) == MetricTypeEnum.DERIVED
        mapper.mapMetricType(2) == MetricTypeEnum.COMPOSITE
    }

    def "test mapAggregationType"() {
        expect:
        mapper.mapAggregationType(0) == AggregationTypeEnum.SUM
        mapper.mapAggregationType(1) == AggregationTypeEnum.AVG
        mapper.mapAggregationType(2) == AggregationTypeEnum.MAX
        mapper.mapAggregationType(3) == AggregationTypeEnum.MIN
        mapper.mapAggregationType(4) == AggregationTypeEnum.COUNT
    }

    def "test dimensionQoToDto success"() {
        given:
        def qo = ["city": "Beijing", "count": 10]

        when:
        def dtoMap = mapper.dimensionQoToDto(qo)

        then:
        dtoMap.size() == 2
        dtoMap.get("city").getValueStr() == "Beijing"
        dtoMap.get("count").getValueStr() == "10"
    }

    def "test dimensionQoToDto with null"() {
        expect:
        mapper.dimensionQoToDto(null).isEmpty()
    }

    def "test echoQoToDto success"() {
        given:
        def qo = EchoMetricQO.builder()
                .code("m1")
                .v(1)
                .dims(["city": "Beijing"])
                .build()
        def snapshotTs = 1000L

        when:
        def dto = mapper.echoQoToDto(qo, snapshotTs)

        then:
        dto != null
        dto.code == "m1"
        dto.version == 1
        dto.snapshotTs == 1000L
        dto.dimensionMap.get("city").getValueStr() == "Beijing"
    }

    def "test echoMetricDaoToModel success"() {
        given:
        def dao = EchoMetricDAO.builder()
                .a("10.5")
                .ts(1000L)
                .h([])
                .build()
        def meta = MetricMetaDAO.builder()
                .code("m1")
                .valueType(ValueTypeEnum.DECIMAL.id)
                .precision(2)
                .unit("USD")
                .build()

        when:
        def model = mapper.echoMetricDaoToModel(dao, meta)

        then:
        model != null
        model.code == "m1"
        model.value.getValueStr() == "10.5"
        model.snapshotTs == 1000L
        model.precision == 2
        model.unit == "USD"
    }

    def "test echoMetricModelToDao success"() {
        given:
        def model = lab.zhang.data_science.metrics_mall.model.metric.EchoMetric.builder()
                .code("m1")
                .value(TypedValue.of(10.5))
                .snapshotTs(1000L)
                .historyList([])
                .build()

        when:
        def dao = mapper.echoMetricModelToDao(model)

        then:
        dao != null
        dao.a == "10.5"
        dao.ts == 1000L
    }
}

