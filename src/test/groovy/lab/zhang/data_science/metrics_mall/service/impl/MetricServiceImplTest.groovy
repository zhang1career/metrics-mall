package lab.zhang.data_science.metrics_mall.service.impl

import lab.zhang.data_science.metrics_mall.enums.LifeStatusEnum
import lab.zhang.data_science.metrics_mall.mapper.MetricDimensionRelMapper
import lab.zhang.data_science.metrics_mall.mapper.MetricMetaMapper
import lab.zhang.data_science.metrics_mall.mapper.MetricVersionMapper
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO
import lab.zhang.data_science.metrics_mall.pojo.dao.metric_version.ExistenceMetricVersionDAO
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionRelDTO
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricStructMapper
import spock.lang.Specification

import static lab.zhang.data_science.metrics_mall.constant.NumConst.ONE

/**
 * Test for MetricServiceImpl.
 *
 * @author Rongjin Zhang
 */
class MetricServiceImplTest extends Specification {

    MetricMetaMapper metricMapper = Mock()
    MetricDimensionRelMapper metricDimensionRelMapper = Mock()
    MetricVersionMapper metricVersionMapper = Mock()
    MetricStructMapper metricStructMapper = Mock()

    MetricServiceImpl service

    private static final String METRIC_CODE = "coin_balance"

    def setup() {
        service = new MetricServiceImpl()
        service.metricMapper = metricMapper
        service.metricDimensionRelMapper = metricDimensionRelMapper
        service.metricVersionMapper = metricVersionMapper
        service.metricStructMapper = metricStructMapper
    }

    def "test getMetricMetaDaoByCode success"() {
        given:
        def dao = MetricMetaDAO.builder().code(METRIC_CODE).build()

        when:
        def result = service.getMetricMetaDaoByCode(METRIC_CODE)

        then:
        1 * metricMapper.selectOne(_) >> dao
        result == dao
    }

    def "test getMetricMetaDaoByCode blank code"() {
        expect:
        service.getMetricMetaDaoByCode("") == null
    }

    def "test checkHotBatch success"() {
        given:
        def dimensionCodes = ["city", "os"]
        def rels = [
                MetricDimensionRelDTO.builder().dimensionCode("city").isHot(ONE).build(),
                MetricDimensionRelDTO.builder().dimensionCode("os").isHot(0).build()
        ]

        when:
        def result = service.checkHotBatch(METRIC_CODE, dimensionCodes)

        then:
        1 * metricDimensionRelMapper.getIsHotBatch(METRIC_CODE, _ as Set) >> rels
        result.size() == 2
        result.get("city") == true
        result.get("os") == false
    }

    def "test checkHotBatch empty param"() {
        expect:
        service.checkHotBatch("", []) == [:]
    }

    def "test chooseVersionBatch success with exact required version"() {
        given:
        def codes = [METRIC_CODE]
        def required = [(METRIC_CODE): 2]
        def lifeStatus = [LifeStatusEnum.GRAY, LifeStatusEnum.ONLINE, LifeStatusEnum.DEPRECATED]
        def available = [
                ExistenceMetricVersionDAO.builder().metricCode(METRIC_CODE).version(1).build(),
                ExistenceMetricVersionDAO.builder().metricCode(METRIC_CODE).version(2).build(),
                ExistenceMetricVersionDAO.builder().metricCode(METRIC_CODE).version(3).build()
        ]

        when:
        def result = service.chooseVersionBatch(codes, required, lifeStatus as Set<LifeStatusEnum>)

        then:
        1 * metricVersionMapper.getExistenceMetricVersionBatch(_, _) >> available
        result.size() == 1
        result.get(METRIC_CODE) == 2
    }

    def "test chooseVersionBatch no exact match found"() {
        given:
        def codes = [METRIC_CODE]
        def required = [(METRIC_CODE): 2]
        def lifeStatus = [LifeStatusEnum.GRAY, LifeStatusEnum.ONLINE, LifeStatusEnum.DEPRECATED]
        def available = [
                ExistenceMetricVersionDAO.builder().metricCode(METRIC_CODE).version(1).build(),
                ExistenceMetricVersionDAO.builder().metricCode(METRIC_CODE).version(3).build()
        ]

        when:
        def result = service.chooseVersionBatch(codes, required, lifeStatus as Set<LifeStatusEnum>)

        then:
        1 * metricVersionMapper.getExistenceMetricVersionBatch(_, _) >> available
        result.isEmpty()
    }

    def "test chooseVersionBatch success with null required version"() {
        given:
        def codes = [METRIC_CODE]
        def required = [(METRIC_CODE): null]
        def lifeStatus = [LifeStatusEnum.GRAY, LifeStatusEnum.ONLINE, LifeStatusEnum.DEPRECATED]
        def available = [
                ExistenceMetricVersionDAO.builder().metricCode(METRIC_CODE).version(1).isMain(0).build(),
                ExistenceMetricVersionDAO.builder().metricCode(METRIC_CODE).version(2).isMain(ONE).build()
        ]

        when:
        def result = service.chooseVersionBatch(codes, required, lifeStatus as Set<LifeStatusEnum>)

        then:
        1 * metricVersionMapper.getExistenceMetricVersionBatch(_, _) >> available
        result.size() == 1
        result.get(METRIC_CODE) == 2
    }

    def "test chooseVersionBatch no versions found"() {
        given:
        def codes = [METRIC_CODE]
        def required = [(METRIC_CODE): 1]
        def lifeStatus = [LifeStatusEnum.GRAY, LifeStatusEnum.ONLINE, LifeStatusEnum.DEPRECATED]

        when:
        def result = service.chooseVersionBatch(codes, required, lifeStatus as Set<LifeStatusEnum>)

        then:
        1 * metricVersionMapper.getExistenceMetricVersionBatch(_, _) >> []
        result.isEmpty()
    }
}

