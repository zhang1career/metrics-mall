package lab.zhang.data_science.metrics_mall.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import lab.zhang.data_science.metrics_mall.mapper.EntityMetricRelMapper
import lab.zhang.data_science.metrics_mall.mapper.EntityMetaMapper
import lab.zhang.data_science.metrics_mall.mapper.MetricMetaMapper
import lab.zhang.data_science.metrics_mall.pojo.dao.x.EntityMetricRelDAO
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetaDAO
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO
import spock.lang.Specification

/**
 * Test for EntityMetricRelServiceImpl.
 *
 * @author Rongjin Zhang
 */
class EntityMetricRelServiceImplTest extends Specification {

    EntityMetricRelMapper entityMetricRelMapper = Mock()
    EntityMetaMapper entityMetaMapper = Mock()
    MetricMetaMapper metricMetaMapper = Mock()

    EntityMetricRelServiceImpl service

    private static final Integer ENTITY_META_ID = 1
    private static final Long METRIC_META_ID = 2L
    private static final String ALIAS = "test_alias"
    private static final String DATA_URI = "test://data/uri"

    def setup() {
        service = new EntityMetricRelServiceImpl()
        service.entityMetricRelMapper = entityMetricRelMapper
        service.entityMetaMapper = entityMetaMapper
        service.metricMetaMapper = metricMetaMapper
    }

    def "test create success"() {
        given:
        def entityMetaDAO = EntityMetaDAO.builder().id(ENTITY_META_ID).build()
        def metricMetaDAO = MetricMetaDAO.builder().id(METRIC_META_ID).build()

        when:
        def result = service.create(ENTITY_META_ID, METRIC_META_ID, ALIAS, DATA_URI)

        then:
        1 * entityMetaMapper.selectById(ENTITY_META_ID) >> entityMetaDAO
        1 * metricMetaMapper.selectById(METRIC_META_ID) >> metricMetaDAO
        1 * entityMetricRelMapper.selectOne(_ as LambdaQueryWrapper) >> null
        1 * entityMetricRelMapper.insert(_ as EntityMetricRelDAO) >> 1
        result
    }

    def "test create with null entityMetaId should throw exception"() {
        when:
        service.create(null, METRIC_META_ID, ALIAS, DATA_URI)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("entityMetaId and metricMetaId cannot be null")
        0 * entityMetaMapper.selectById(_)
    }

    def "test create with blank alias should throw exception"() {
        when:
        service.create(ENTITY_META_ID, METRIC_META_ID, "", DATA_URI)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("alias cannot be blank")
        0 * entityMetaMapper.selectById(_)
    }

    def "test create with entity meta not found should throw exception"() {
        when:
        service.create(ENTITY_META_ID, METRIC_META_ID, ALIAS, DATA_URI)

        then:
        1 * entityMetaMapper.selectById(ENTITY_META_ID) >> null
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("entity meta not found")
    }

    def "test create with metric meta not found should throw exception"() {
        given:
        def entityMetaDAO = EntityMetaDAO.builder().id(ENTITY_META_ID).build()

        when:
        service.create(ENTITY_META_ID, METRIC_META_ID, ALIAS, DATA_URI)

        then:
        1 * entityMetaMapper.selectById(ENTITY_META_ID) >> entityMetaDAO
        1 * metricMetaMapper.selectById(METRIC_META_ID) >> null
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("metric meta not found")
    }

    def "test create with existing relation should throw exception"() {
        given:
        def entityMetaDAO = EntityMetaDAO.builder().id(ENTITY_META_ID).build()
        def metricMetaDAO = MetricMetaDAO.builder().id(METRIC_META_ID).build()
        def existingRel = new EntityMetricRelDAO()
        existingRel.setEntityMetaId(ENTITY_META_ID)
        existingRel.setMetricMetaId(METRIC_META_ID)

        when:
        service.create(ENTITY_META_ID, METRIC_META_ID, ALIAS, DATA_URI)

        then:
        1 * entityMetaMapper.selectById(ENTITY_META_ID) >> entityMetaDAO
        1 * metricMetaMapper.selectById(METRIC_META_ID) >> metricMetaDAO
        1 * entityMetricRelMapper.selectOne(_ as LambdaQueryWrapper) >> existingRel
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("relation already exists")
    }

    def "test get success"() {
        given:
        def dao = new EntityMetricRelDAO()
        dao.setEntityMetaId(ENTITY_META_ID)
        dao.setMetricMetaId(METRIC_META_ID)
        dao.setAlias(ALIAS)
        dao.setDataUri(DATA_URI)

        when:
        def result = service.get(ENTITY_META_ID, METRIC_META_ID)

        then:
        1 * entityMetricRelMapper.selectOne(_ as LambdaQueryWrapper) >> dao
        result != null
        result.entityMetaId == ENTITY_META_ID
        result.metricMetaId == METRIC_META_ID
    }

    def "test get with null entityMetaId should return null"() {
        when:
        def result = service.get(null, METRIC_META_ID)

        then:
        0 * entityMetricRelMapper.selectOne(_)
        result == null
    }

    def "test get not found should return null"() {
        when:
        def result = service.get(ENTITY_META_ID, METRIC_META_ID)

        then:
        1 * entityMetricRelMapper.selectOne(_ as LambdaQueryWrapper) >> null
        result == null
    }

    def "test listByEntityMetaId success"() {
        given:
        def dao1 = new EntityMetricRelDAO()
        dao1.setEntityMetaId(ENTITY_META_ID)
        dao1.setMetricMetaId(2L)
        def dao2 = new EntityMetricRelDAO()
        dao2.setEntityMetaId(ENTITY_META_ID)
        dao2.setMetricMetaId(3L)
        def daoList = [dao1, dao2]

        when:
        def result = service.listByEntityMetaId(ENTITY_META_ID)

        then:
        1 * entityMetricRelMapper.selectList(_ as LambdaQueryWrapper) >> daoList
        result.size() == 2
        result[0].entityMetaId == ENTITY_META_ID
    }

    def "test listByEntityMetaId with null should return empty list"() {
        when:
        def result = service.listByEntityMetaId(null)

        then:
        0 * entityMetricRelMapper.selectList(_)
        result == []
    }

    def "test listByMetricMetaId success"() {
        given:
        def dao1 = new EntityMetricRelDAO()
        dao1.setEntityMetaId(1L)
        dao1.setMetricMetaId(METRIC_META_ID)
        def daoList = [dao1]

        when:
        def result = service.listByMetricMetaId(METRIC_META_ID)

        then:
        1 * entityMetricRelMapper.selectList(_ as LambdaQueryWrapper) >> daoList
        result.size() == 1
        result[0].metricMetaId == METRIC_META_ID
    }

    def "test list success"() {
        given:
        def dao1 = new EntityMetricRelDAO()
        dao1.setEntityMetaId(ENTITY_META_ID)
        dao1.setMetricMetaId(METRIC_META_ID)
        def daoList = [dao1]

        when:
        def result = service.list()

        then:
        1 * entityMetricRelMapper.selectList(null) >> daoList
        result.size() == 1
    }

    def "test update success"() {
        given:
        def existingRel = new EntityMetricRelDAO()
        existingRel.setEntityMetaId(ENTITY_META_ID)
        existingRel.setMetricMetaId(METRIC_META_ID)
        def updatedAlias = "updated_alias"
        def updatedDataUri = "updated://data/uri"

        when:
        def result = service.update(ENTITY_META_ID, METRIC_META_ID, updatedAlias, updatedDataUri)

        then:
        1 * entityMetricRelMapper.selectOne(_ as LambdaQueryWrapper) >> existingRel
        1 * entityMetricRelMapper.update(null, _ as UpdateWrapper) >> 1
        result
    }

    def "test update with null entityMetaId should throw exception"() {
        when:
        service.update(null, METRIC_META_ID, ALIAS, DATA_URI)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("entityMetaId and metricMetaId cannot be null")
        0 * entityMetricRelMapper.selectOne(_)
    }

    def "test update with relation not found should throw exception"() {
        when:
        service.update(ENTITY_META_ID, METRIC_META_ID, ALIAS, DATA_URI)

        then:
        1 * entityMetricRelMapper.selectOne(_ as LambdaQueryWrapper) >> null
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("relation not found")
    }

    def "test delete success"() {
        when:
        def result = service.delete(ENTITY_META_ID, METRIC_META_ID)

        then:
        1 * entityMetricRelMapper.delete(_ as LambdaQueryWrapper) >> 1
        result
    }

    def "test delete with null entityMetaId should throw exception"() {
        when:
        service.delete(null, METRIC_META_ID)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("entityMetaId and metricMetaId cannot be null")
        0 * entityMetricRelMapper.delete(_)
    }

    def "test delete not found should return false"() {
        when:
        def result = service.delete(ENTITY_META_ID, METRIC_META_ID)

        then:
        1 * entityMetricRelMapper.delete(_ as LambdaQueryWrapper) >> 0
        !result
    }
}

