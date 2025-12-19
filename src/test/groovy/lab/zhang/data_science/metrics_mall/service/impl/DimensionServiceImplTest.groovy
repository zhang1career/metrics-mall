package lab.zhang.data_science.metrics_mall.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import lab.zhang.data_science.metrics_mall.mapper.DimensionMapper
import lab.zhang.data_science.metrics_mall.model.Dimension
import lab.zhang.data_science.metrics_mall.pojo.dao.DimensionDAO
import lab.zhang.data_science.metrics_mall.struct_mapper.DimensionStructMapper
import spock.lang.Specification

/**
 * Test for DimensionServiceImpl.
 *
 * @author Rongjin Zhang
 */
class DimensionServiceImplTest extends Specification {

    DimensionMapper dimensionMapper = Mock()
    DimensionStructMapper dimensionStructMapper = Mock()
    DimensionServiceImpl service

    def setup() {
        service = new DimensionServiceImpl(dimensionMapper, dimensionStructMapper)
    }

    def "test get success"() {
        given:
        def id = 1L
        def dao = DimensionDAO.builder().id(id).build()
        def model = Dimension.builder().id(id).build()

        when:
        def result = service.get(id)

        then:
        1 * dimensionMapper.selectById(id) >> dao
        1 * dimensionStructMapper.daoToModel(dao) >> model
        result == model
    }

    def "test getByCode success"() {
        given:
        def code = "city"
        def dao = DimensionDAO.builder().code(code).build()
        def model = Dimension.builder().code(code).build()

        when:
        def result = service.getByCode(code)

        then:
        1 * dimensionMapper.selectOne(_ as LambdaQueryWrapper) >> dao
        1 * dimensionStructMapper.daoToModel(dao) >> model
        result == model
    }

    def "test list success"() {
        given:
        def daoList = [DimensionDAO.builder().id(1L).build()]
        def modelList = [Dimension.builder().id(1L).build()]

        when:
        def result = service.list()

        then:
        1 * dimensionMapper.selectList(null) >> daoList
        1 * dimensionStructMapper.daoToModelBatch(daoList) >> modelList
        result == modelList
    }

    def "test count success"() {
        when:
        def result = service.count()

        then:
        1 * dimensionMapper.selectCount(null) >> 5L
        result == 5L
    }

    def "test insert success"() {
        given:
        def model = Dimension.builder().code("city").build()
        def dao = DimensionDAO.builder().code("city").build()

        when:
        def result = service.insert(model)

        then:
        1 * dimensionStructMapper.modelToDao(model) >> dao
        1 * dimensionMapper.insert(_ as DimensionDAO) >> 1
        result
    }

    def "test update success"() {
        given:
        def model = Dimension.builder().id(1L).code("city").build()
        def dao = DimensionDAO.builder().id(1L).code("city").build()

        when:
        def result = service.update(model)

        then:
        1 * dimensionStructMapper.modelToDao(model) >> dao
        1 * dimensionMapper.updateById(_ as DimensionDAO) >> 1
        result
    }

    def "test delete success"() {
        given:
        def id = 1L

        when:
        def result = service.delete(id)

        then:
        1 * dimensionMapper.deleteById(id) >> 1
        result
    }
}

