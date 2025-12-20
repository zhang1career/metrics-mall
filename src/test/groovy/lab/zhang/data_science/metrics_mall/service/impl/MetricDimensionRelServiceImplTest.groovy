package lab.zhang.data_science.metrics_mall.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import lab.zhang.data_science.metrics_mall.mapper.DimensionMapper
import lab.zhang.data_science.metrics_mall.mapper.MetricDimensionRelMapper
import lab.zhang.data_science.metrics_mall.mapper.MetricMetaMapper
import lab.zhang.data_science.metrics_mall.pojo.dao.DimensionDAO
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDimensionRelDAO
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO
import spock.lang.Specification

/**
 * Test for MetricDimensionRelServiceImpl.
 *
 * @author Rongjin Zhang
 */
class MetricDimensionRelServiceImplTest extends Specification {

    MetricDimensionRelMapper metricDimensionRelMapper = Mock()
    MetricMetaMapper metricMetaMapper = Mock()
    DimensionMapper dimensionMapper = Mock()

    MetricDimensionRelServiceImpl service

    private static final Long METRIC_META_ID = 1L
    private static final Long DIMENSION_ID = 2L
    private static final Integer IS_HOT = 1
    private static final String VALIDATION = "{\"min\":0,\"max\":100}"

    def setup() {
        service = new MetricDimensionRelServiceImpl()
        service.metricDimensionRelMapper = metricDimensionRelMapper
        service.metricMetaMapper = metricMetaMapper
        service.dimensionMapper = dimensionMapper
    }

    def "test create success"() {
        given:
        def metricMetaDAO = MetricMetaDAO.builder().id(METRIC_META_ID).build()
        def dimensionDAO = DimensionDAO.builder().id(DIMENSION_ID).build()

        when:
        def result = service.create(METRIC_META_ID, DIMENSION_ID, IS_HOT, VALIDATION)

        then:
        1 * metricMetaMapper.selectById(METRIC_META_ID) >> metricMetaDAO
        1 * dimensionMapper.selectById(DIMENSION_ID) >> dimensionDAO
        1 * metricDimensionRelMapper.selectOne(_ as LambdaQueryWrapper) >> null
        1 * metricDimensionRelMapper.insert(_ as MetricDimensionRelDAO) >> 1
        result
    }

    def "test create with null metricMetaId should throw exception"() {
        when:
        service.create(null, DIMENSION_ID, IS_HOT, VALIDATION)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metricMetaId and dimensionId cannot be null")
        0 * metricMetaMapper.selectById(_)
    }

    def "test create with null isHot should throw exception"() {
        when:
        service.create(METRIC_META_ID, DIMENSION_ID, null, VALIDATION)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("isHot cannot be null")
        0 * metricMetaMapper.selectById(_)
    }

    def "test create with metric meta not found should throw exception"() {
        when:
        service.create(METRIC_META_ID, DIMENSION_ID, IS_HOT, VALIDATION)

        then:
        1 * metricMetaMapper.selectById(METRIC_META_ID) >> null
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metric meta not found")
    }

    def "test create with dimension not found should throw exception"() {
        given:
        def metricMetaDAO = MetricMetaDAO.builder().id(METRIC_META_ID).build()

        when:
        service.create(METRIC_META_ID, DIMENSION_ID, IS_HOT, VALIDATION)

        then:
        1 * metricMetaMapper.selectById(METRIC_META_ID) >> metricMetaDAO
        1 * dimensionMapper.selectById(DIMENSION_ID) >> null
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("dimension not found")
    }

    def "test create with existing relation should throw exception"() {
        given:
        def metricMetaDAO = MetricMetaDAO.builder().id(METRIC_META_ID).build()
        def dimensionDAO = DimensionDAO.builder().id(DIMENSION_ID).build()
        def existingRel = new MetricDimensionRelDAO()
        existingRel.setMetricMetaId(METRIC_META_ID)
        existingRel.setDimensionId(DIMENSION_ID)

        when:
        service.create(METRIC_META_ID, DIMENSION_ID, IS_HOT, VALIDATION)

        then:
        1 * metricMetaMapper.selectById(METRIC_META_ID) >> metricMetaDAO
        1 * dimensionMapper.selectById(DIMENSION_ID) >> dimensionDAO
        1 * metricDimensionRelMapper.selectOne(_ as LambdaQueryWrapper) >> existingRel
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("relation already exists")
    }

    def "test get success"() {
        given:
        def dao = new MetricDimensionRelDAO()
        dao.setMetricMetaId(METRIC_META_ID)
        dao.setDimensionId(DIMENSION_ID)
        dao.setIsHot(IS_HOT)
        dao.setValidation(VALIDATION)

        when:
        def result = service.get(METRIC_META_ID, DIMENSION_ID)

        then:
        1 * metricDimensionRelMapper.selectOne(_ as LambdaQueryWrapper) >> dao
        result != null
        result.metricMetaId == METRIC_META_ID
        result.dimensionId == DIMENSION_ID
    }

    def "test get with null metricMetaId should return null"() {
        when:
        def result = service.get(null, DIMENSION_ID)

        then:
        0 * metricDimensionRelMapper.selectOne(_)
        result == null
    }

    def "test get not found should return null"() {
        when:
        def result = service.get(METRIC_META_ID, DIMENSION_ID)

        then:
        1 * metricDimensionRelMapper.selectOne(_ as LambdaQueryWrapper) >> null
        result == null
    }

    def "test listByMetricMetaId success"() {
        given:
        def dao1 = new MetricDimensionRelDAO()
        dao1.setMetricMetaId(METRIC_META_ID)
        dao1.setDimensionId(2L)
        def dao2 = new MetricDimensionRelDAO()
        dao2.setMetricMetaId(METRIC_META_ID)
        dao2.setDimensionId(3L)
        def daoList = [dao1, dao2]

        when:
        def result = service.listByMetricMetaId(METRIC_META_ID)

        then:
        1 * metricDimensionRelMapper.selectList(_ as LambdaQueryWrapper) >> daoList
        result.size() == 2
        result[0].metricMetaId == METRIC_META_ID
    }

    def "test listByMetricMetaId with null should return empty list"() {
        when:
        def result = service.listByMetricMetaId(null)

        then:
        0 * metricDimensionRelMapper.selectList(_)
        result == []
    }

    def "test listByDimensionId success"() {
        given:
        def dao1 = new MetricDimensionRelDAO()
        dao1.setMetricMetaId(1L)
        dao1.setDimensionId(DIMENSION_ID)
        def daoList = [dao1]

        when:
        def result = service.listByDimensionId(DIMENSION_ID)

        then:
        1 * metricDimensionRelMapper.selectList(_ as LambdaQueryWrapper) >> daoList
        result.size() == 1
        result[0].dimensionId == DIMENSION_ID
    }

    def "test listByDimensionId with null should return empty list"() {
        when:
        def result = service.listByDimensionId(null)

        then:
        0 * metricDimensionRelMapper.selectList(_)
        result == []
    }

    def "test list success"() {
        given:
        def dao1 = new MetricDimensionRelDAO()
        dao1.setMetricMetaId(METRIC_META_ID)
        dao1.setDimensionId(DIMENSION_ID)
        def daoList = [dao1]

        when:
        def result = service.list()

        then:
        1 * metricDimensionRelMapper.selectList(null) >> daoList
        result.size() == 1
    }

    def "test update success"() {
        given:
        def existingRel = new MetricDimensionRelDAO()
        existingRel.setMetricMetaId(METRIC_META_ID)
        existingRel.setDimensionId(DIMENSION_ID)
        def updatedIsHot = 0
        def updatedValidation = "{\"min\":10,\"max\":200}"

        when:
        def result = service.update(METRIC_META_ID, DIMENSION_ID, updatedIsHot, updatedValidation)

        then:
        1 * metricDimensionRelMapper.selectOne(_ as LambdaQueryWrapper) >> existingRel
        1 * metricDimensionRelMapper.update(null, _ as UpdateWrapper) >> 1
        result
    }

    def "test update with null metricMetaId should throw exception"() {
        when:
        service.update(null, DIMENSION_ID, IS_HOT, VALIDATION)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metricMetaId and dimensionId cannot be null")
        0 * metricDimensionRelMapper.selectOne(_)
    }

    def "test update with relation not found should throw exception"() {
        when:
        service.update(METRIC_META_ID, DIMENSION_ID, IS_HOT, VALIDATION)

        then:
        1 * metricDimensionRelMapper.selectOne(_ as LambdaQueryWrapper) >> null
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("relation not found")
    }

    def "test delete success"() {
        when:
        def result = service.delete(METRIC_META_ID, DIMENSION_ID)

        then:
        1 * metricDimensionRelMapper.delete(_ as LambdaQueryWrapper) >> 1
        result
    }

    def "test delete with null metricMetaId should throw exception"() {
        when:
        service.delete(null, DIMENSION_ID)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metricMetaId and dimensionId cannot be null")
        0 * metricDimensionRelMapper.delete(_)
    }

    def "test delete not found should return false"() {
        when:
        def result = service.delete(METRIC_META_ID, DIMENSION_ID)

        then:
        1 * metricDimensionRelMapper.delete(_ as LambdaQueryWrapper) >> 0
        !result
    }
}

