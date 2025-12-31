package lab.zhang.data_science.metrics_mall.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import lab.zhang.data_science.metrics_mall.mapper.MetricDimensionRelMapper
import lab.zhang.data_science.metrics_mall.mapper.MetricMetaMapper
import lab.zhang.data_science.metrics_mall.model.Dimension
import lab.zhang.data_science.metrics_mall.pojo.dao.DimensionDAO
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDimensionRelDAO
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionRelDTO
import lab.zhang.data_science.metrics_mall.service.DimensionService
import lab.zhang.data_science.metrics_mall.service.MetricService
import spock.lang.Specification

/**
 * Test for MetricDimensionRelServiceImpl.
 *
 * @author Rongjin Zhang
 */
class MetricDimensionRelServiceImplTest extends Specification {

    MetricDimensionRelServiceImpl service

    MetricService metricService = Mock()
    DimensionService dimensionService = Mock()
    MetricMetaMapper metricMetaMapper = Mock()
    MetricDimensionRelMapper metricDimensionRelMapper = Mock()

    private static final Long METRIC_META_ID = 1L
    private static final String METRIC_CODE = "consumer_amount"
    private static final Long DIMENSION_ID = 2L
    private static final String DIMENSION_CODE = "location"
    private static final Integer IS_HOT = 1
    private static final String VALIDATION = "{\"min\":0,\"max\":100}"

    def setup() {
        service = new MetricDimensionRelServiceImpl()
        service.metricService = metricService
        service.dimensionService = dimensionService
        service.metricDimensionRelMapper = metricDimensionRelMapper
    }

    def "test create success"() {
        given:
        def relDTO = MetricDimensionRelDTO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode(DIMENSION_CODE)
                .isHot(IS_HOT)
                .validation(VALIDATION)
                .build()
        def metricMetaDAO = MetricMetaDAO.builder()
                .id(METRIC_META_ID)
                .code(METRIC_CODE)
                .build()
        def dimension = Dimension.builder()
                .id(DIMENSION_ID)
                .code(DIMENSION_CODE)
                .build()

        when:
        def result = service.create(relDTO)

        then:
        1 * metricService.getMetricMetaDaoByCode(_) >> metricMetaDAO
        1 * dimensionService.getByCode(_) >> dimension
        1 * metricDimensionRelMapper.selectOne(_) >> null
        1 * metricDimensionRelMapper.insert(_ as MetricDimensionRelDAO) >> 1
        result
    }

    def "test create with null metricMetaId should throw exception"() {
        given:
        def relDTO = MetricDimensionRelDTO.builder()
                .metricCode(null)
                .dimensionCode(DIMENSION_CODE)
                .isHot(IS_HOT)
                .validation(VALIDATION)
                .build()

        when:
        service.create(relDTO)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("creating failed, metric meta not found")
    }

    def "test create with metric meta not found should throw exception"() {
        given:
        def relDTO = MetricDimensionRelDTO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode(DIMENSION_CODE)
                .isHot(IS_HOT)
                .validation(VALIDATION)
                .build()

        when:
        service.create(relDTO)

        then:
        1 * metricService.getMetricMetaDaoByCode(_) >> null
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("creating failed, metric meta not found")
    }

    def "test create with dimension not found should throw exception"() {
        given:
        def relDTO = MetricDimensionRelDTO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode(DIMENSION_CODE)
                .isHot(IS_HOT)
                .validation(VALIDATION)
                .build()
        def metricMetaDAO = MetricMetaDAO.builder()
                .id(METRIC_META_ID)
                .build()

        when:
        service.create(relDTO)

        then:
        1 * metricService.getMetricMetaDaoByCode(_) >> metricMetaDAO
        1 * dimensionService.getByCode(_) >> null
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("creating failed, dimension not found")
    }

    def "test create with existing relation should throw exception"() {
        given:
        def relDTO = MetricDimensionRelDTO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode(DIMENSION_CODE)
                .isHot(IS_HOT)
                .validation(VALIDATION)
                .build()
        def metricMetaDAO = MetricMetaDAO.builder()
                .id(METRIC_META_ID)
                .build()
        def dimension = Dimension.builder()
                .id(DIMENSION_ID)
                .build()
        def existingRel = new MetricDimensionRelDAO()
        existingRel.setMetricMetaId(METRIC_META_ID)
        existingRel.setDimensionId(DIMENSION_ID)

        when:
        service.create(relDTO)

        then:
        1 * metricService.getMetricMetaDaoByCode(_) >> metricMetaDAO
        1 * dimensionService.getByCode(_) >> dimension
        1 * metricDimensionRelMapper.selectOne(_) >> existingRel
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("creating failed, relation already exists")
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
        def updatedIsHot = 0
        def updatedValidation = "{\"min\":10,\"max\":200}"
        def relDTO = MetricDimensionRelDTO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode(DIMENSION_CODE)
                .isHot(updatedIsHot)
                .validation(updatedValidation)
                .build()
        def metricMetaDAO = MetricMetaDAO.builder()
                .id(METRIC_META_ID)
                .build()
        def dimension = Dimension.builder()
                .id(DIMENSION_ID)
                .build()
        def existingRel = new MetricDimensionRelDAO()
        existingRel.setMetricMetaId(METRIC_META_ID)
        existingRel.setDimensionId(DIMENSION_ID)
        def relDAO = new MetricDimensionRelDAO();
        relDAO.setMetricMetaId(METRIC_META_ID);
        relDAO.setDimensionId(DIMENSION_ID);
        relDAO.setIsHot(updatedIsHot);
        relDAO.setValidation(updatedValidation);

        when:
        def result = service.update(relDTO)

        then:
        1 * metricService.getMetricMetaDaoByCode(_) >> metricMetaDAO
        1 * dimensionService.getByCode(_) >> dimension
        1 * metricDimensionRelMapper.selectOne(_) >> existingRel
        1 * metricDimensionRelMapper.updateByPrimaryKey(relDAO) >> 1
    }

    def "test update with null metricMetaId should throw exception"() {
        given:
        def relDTO = MetricDimensionRelDTO.builder()
                .metricCode(null)
                .dimensionCode(DIMENSION_CODE)
                .isHot(IS_HOT)
                .validation(VALIDATION)
                .build()

        when:
        service.update(relDTO)

        then:
        1 * metricService.getMetricMetaDaoByCode(_) >> null
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("updating failed, metric meta not found")
    }

    def "test update with null dimensionId should throw exception"() {
        given:
        def relDTO = MetricDimensionRelDTO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode(null)
                .isHot(IS_HOT)
                .validation(VALIDATION)
                .build()
        def metricMetaDAO = MetricMetaDAO.builder()
                .id(METRIC_META_ID)
                .build()
        when:
        service.update(relDTO)

        then:
        1 * metricService.getMetricMetaDaoByCode(_) >> metricMetaDAO
        1 * dimensionService.getByCode(_) >> null
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("updating failed, dimension not found")
    }

    def "test update with no relation should throw exception"() {
        def updatedIsHot = 0
        def updatedValidation = "{\"min\":10,\"max\":200}"
        def relDTO = MetricDimensionRelDTO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode(DIMENSION_CODE)
                .isHot(updatedIsHot)
                .validation(updatedValidation)
                .build()
        def metricMetaDAO = MetricMetaDAO.builder()
                .id(METRIC_META_ID)
                .build()
        def dimension = Dimension.builder()
                .id(DIMENSION_ID)
                .build()

        when:
        def result = service.update(relDTO)

        then:
        1 * metricService.getMetricMetaDaoByCode(_) >> metricMetaDAO
        1 * dimensionService.getByCode(_) >> dimension
        1 * metricDimensionRelMapper.selectOne(_) >> null
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("updating failed, relation not exists")
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

