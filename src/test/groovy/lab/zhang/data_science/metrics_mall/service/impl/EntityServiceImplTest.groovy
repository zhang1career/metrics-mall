package lab.zhang.data_science.metrics_mall.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import lab.zhang.data_science.metrics_mall.mapper.EntityMetaMapper
import lab.zhang.data_science.metrics_mall.model.Entity.EntityMeta
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetaDAO
import lab.zhang.data_science.metrics_mall.struct_mapper.EntityMetaStructMapper
import spock.lang.Specification

/**
 * Test for EntityServiceImpl.
 *
 * @author Rongjin Zhang
 */
class EntityServiceImplTest extends Specification {

    EntityMetaMapper entityMetaMapper = Mock()
    EntityMetaStructMapper entityMetaStructMapper = Mock()

    EntityServiceImpl service

    private static final String ENTITY_CODE = "user"
    private static final Long ENTITY_ID = 12345678L

    def setup() {
        service = new EntityServiceImpl()
        service.entityMetaMapper = entityMetaMapper
        service.entityMetaStructMapper = entityMetaStructMapper
    }

    def "test getEntityByCode success"() {
        given:
        def dao = EntityMetaDAO.builder().code(ENTITY_CODE).name("User").build()
        def meta = EntityMeta.builder().code(ENTITY_CODE).name("User").build()

        when:
        def result = service.getEntityByCode(ENTITY_CODE, ENTITY_ID)

        then:
        1 * entityMetaMapper.selectOne(_ as LambdaQueryWrapper) >> dao
        1 * entityMetaStructMapper.daoToModel(dao) >> meta
        result != null
        result.meta.code == ENTITY_CODE
        result.id == ENTITY_ID
    }

    def "test getEntityByCode not found"() {
        when:
        def result = service.getEntityByCode(ENTITY_CODE, ENTITY_ID)

        then:
        1 * entityMetaMapper.selectOne(_ as LambdaQueryWrapper) >> null
        0 * entityMetaStructMapper.daoToModel(_)
        result == null
    }
}

