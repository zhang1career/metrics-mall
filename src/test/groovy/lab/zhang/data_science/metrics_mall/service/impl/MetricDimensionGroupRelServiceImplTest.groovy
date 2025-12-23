package lab.zhang.data_science.metrics_mall.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import lab.zhang.data_science.metrics_mall.mapper.MetricDimensionGroupRelMapper
import lab.zhang.data_science.metrics_mall.model.MetricDimensionGroupRel
import lab.zhang.data_science.metrics_mall.pojo.dao.y_group.MetricDimensionGroupRelDAO
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionGroupRelDTO
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricDimensionGroupRelStructMapper
import spock.lang.Specification

/**
 * Test for YGroupServiceImpl.
 *
 * @author Rongjin Zhang
 */
class MetricDimensionGroupRelServiceImplTest extends Specification {

    MetricDimensionGroupRelMapper yGroupMapper = Mock()
    MetricDimensionGroupRelStructMapper yGroupStructMapper = Mock()
    MetricDimensionGroupRelRelServiceImpl service

    def setup() {
        service = new MetricDimensionGroupRelRelServiceImpl(yGroupMapper, yGroupStructMapper)
    }

    def "test get success"() {
        given:
        def id = 1L
        def dao = MetricDimensionGroupRelDAO.builder().id(id).mid(100L).dids("1,2,3").build()
        def model = MetricDimensionGroupRel.builder().id(id).mid(100L).dids("1,2,3").build()

        when:
        def result = service.get(id)

        then:
        1 * yGroupMapper.selectById(id) >> dao
        1 * yGroupStructMapper.daoToModel(dao) >> model
        result == model
    }

    def "test get with null id"() {
        when:
        def result = service.get(null)

        then:
        0 * yGroupMapper.selectById(_)
        result == null
    }

    def "test list success"() {
        given:
        def daoList = [MetricDimensionGroupRelDAO.builder().id(1L).mid(100L).dids("1,2,3").build()]
        def modelList = [MetricDimensionGroupRel.builder().id(1L).mid(100L).dids("1,2,3").build()]

        when:
        def result = service.list()

        then:
        1 * yGroupMapper.selectList(null) >> daoList
        1 * yGroupStructMapper.daoToModelBatch(daoList) >> modelList
        result == modelList
    }

    def 'test listByMetricId success'() {
        given:
        def mid = 100L
        def daoList = [MetricDimensionGroupRelDAO.builder().id(1L).mid(mid).dids("1,2,3").build()]
        def modelList = [MetricDimensionGroupRel.builder().id(1L).mid(mid).dids("1,2,3").build()]

        when:
        def result = service.listByMetricId(mid)

        then:
        1 * yGroupMapper.selectList(_ as LambdaQueryWrapper) >> daoList
        1 * yGroupStructMapper.daoToModelBatch(daoList) >> modelList
        result == modelList
    }

    def 'test listByMid with null metricId'() {
        when:
        def result = service.listByMetricId(null)

        then:
        0 * yGroupMapper.selectList(_)
        result == []
    }

    def "test count success"() {
        when:
        def result = service.count()

        then:
        1 * yGroupMapper.selectCount(null) >> 5L
        result == 5L
    }

    def "test insert success"() {
        given:
        def dto = MetricDimensionGroupRelDTO.builder().metricId(100L).dimensionIdList("1,2,3").build()
        def dao = MetricDimensionGroupRelDAO.builder().mid(100L).dids("1,2,3").build()
        dao.id = 1L

        when:
        def result = service.insert(dto)

        then:
        1 * yGroupStructMapper.dtoToDao(dto) >> dao
        1 * yGroupMapper.insert(_ as MetricDimensionGroupRelDAO) >> 1
        result
        dto.id == 1L
    }

    def "test insert with null dto"() {
        when:
        service.insert(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("dto cannot be null")
    }

    def "test insert with null mid"() {
        given:
        def dto = MetricDimensionGroupRelDTO.builder().dimensionIdList("1,2,3").build()

        when:
        service.insert(dto)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("mid cannot be null")
    }

    def "test update success"() {
        given:
        def dto = MetricDimensionGroupRelDTO.builder().id(1L).metricId(100L).dimensionIdList("1,2,3,4").build()
        def existingDao = MetricDimensionGroupRelDAO.builder().id(1L).mid(100L).dids("1,2,3").build()
        def dao = MetricDimensionGroupRelDAO.builder().id(1L).mid(100L).dids("1,2,3,4").build()

        when:
        def result = service.update(dto)

        then:
        1 * yGroupMapper.selectById(1L) >> existingDao
        1 * yGroupStructMapper.dtoToDao(dto) >> dao
        1 * yGroupMapper.updateById(_ as MetricDimensionGroupRelDAO) >> 1
        result
    }

    def "test update with null dto"() {
        when:
        service.update(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("dto cannot be null")
    }

    def "test update with null id"() {
        given:
        def dto = MetricDimensionGroupRelDTO.builder().metricId(100L).dimensionIdList("1,2,3").build()

        when:
        service.update(dto)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("id cannot be null")
    }

    def "test update with non-existent id"() {
        given:
        def dto = MetricDimensionGroupRelDTO.builder().id(999L).metricId(100L).dimensionIdList("1,2,3").build()

        when:
        service.update(dto)

        then:
        1 * yGroupMapper.selectById(999L) >> null
        def e = thrown(IllegalStateException)
        e.message.contains("yGroup does not exist")
    }

    def "test delete success"() {
        given:
        def id = 1L

        when:
        def result = service.delete(id)

        then:
        1 * yGroupMapper.deleteById(id) >> 1
        result
    }

    def "test delete with null id"() {
        when:
        def result = service.delete(null)

        then:
        0 * yGroupMapper.deleteById(_)
        !result
    }
}

