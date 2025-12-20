package lab.zhang.data_science.metrics_mall.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import lab.zhang.data_science.metrics_mall.mapper.EntityMetaMapper
import lab.zhang.data_science.metrics_mall.model.EntityMeta
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetaDAO
import lab.zhang.data_science.metrics_mall.struct_mapper.EntityMetaStructMapper
import spock.lang.Specification

/**
 * Test for EntityMetaServiceImpl.
 *
 * @author Rongjin Zhang
 */
class EntityMetaServiceImplTest extends Specification {

    EntityMetaMapper entityMetaMapper = Mock()
    EntityMetaStructMapper entityMetaStructMapper = Mock()
    EntityMetaServiceImpl service

    def setup() {
        service = new EntityMetaServiceImpl(entityMetaMapper, entityMetaStructMapper)
    }

    def "test get success"() {
        given:
        def id = 1
        def dao = EntityMetaDAO.builder().id(id).build()
        def model = EntityMeta.builder().id(id).build()

        when:
        def result = service.get(id)

        then:
        1 * entityMetaMapper.selectById(id) >> dao
        1 * entityMetaStructMapper.daoToModelNew(dao) >> model
        result == model
    }

    def "test getByCode success"() {
        given:
        def code = "user"
        def dao = EntityMetaDAO.builder().code(code).build()
        def model = EntityMeta.builder().code(code).build()

        when:
        def result = service.getByCode(code)

        then:
        1 * entityMetaMapper.selectOne(_ as LambdaQueryWrapper) >> dao
        1 * entityMetaStructMapper.daoToModelNew(dao) >> model
        result == model
    }

    def "test list success"() {
        given:
        def daoList = [EntityMetaDAO.builder().id(1).build()]
        def modelList = [EntityMeta.builder().id(1).build()]

        when:
        def result = service.list()

        then:
        1 * entityMetaMapper.selectList(null) >> daoList
        1 * entityMetaStructMapper.daoToModelBatch(daoList) >> modelList
        result == modelList
    }

    def "test count success"() {
        when:
        def result = service.count()

        then:
        1 * entityMetaMapper.selectCount(null) >> 5L
        result == 5L
    }

    def "test insert success"() {
        given:
        def model = EntityMeta.builder().code("user").build()
        def dao = EntityMetaDAO.builder().code("user").build()

        when:
        def result = service.insert(model)

        then:
        1 * entityMetaStructMapper.modelToDao(model) >> dao
        1 * entityMetaMapper.insert(_ as EntityMetaDAO) >> 1
        result
    }

    def "test update success"() {
        given:
        def model = EntityMeta.builder().id(1).code("user").build()
        def dao = EntityMetaDAO.builder().id(1).code("user").build()

        when:
        def result = service.update(model)

        then:
        1 * entityMetaStructMapper.modelToDao(model) >> dao
        1 * entityMetaMapper.updateById(_ as EntityMetaDAO) >> 1
        result
    }

    def "test delete success"() {
        given:
        def id = 1

        when:
        def result = service.delete(id)

        then:
        1 * entityMetaMapper.deleteById(id) >> 1
        result
    }
}

