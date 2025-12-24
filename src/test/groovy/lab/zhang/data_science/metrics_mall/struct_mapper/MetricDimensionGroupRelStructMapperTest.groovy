package lab.zhang.data_science.metrics_mall.struct_mapper


import lab.zhang.data_science.metrics_mall.model.MetricDimensionGroupRel
import lab.zhang.data_science.metrics_mall.pojo.dao.y_group.MetricDimensionGroupRelDAO
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionGroupRelDTO
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricDimensionGroupRelQO
import spock.lang.Specification

/**
 * Test for YGroupStructMapper.
 *
 * @author Rongjin Zhang
 */
class MetricDimensionGroupRelStructMapperTest extends Specification {

    MetricDimensionGroupRelStructMapper structMapper = new MetricDimensionGroupRelStructMapperImpl()

    def "test qoToDto"() {
        given:
        def qo = MetricDimensionGroupRelQO.builder()
                .metricId(100L)
                .dimensionIds("1,2,3")
                .build()

        when:
        def dto = structMapper.qoToDto(qo)

        then:
        dto.metricId == 100L
        dto.dimensionIdList == ["1", "2", "3"]
    }

    def "test daoToModel"() {
        given:
        def dao = MetricDimensionGroupRelDAO.builder()
                .id(1L)
                .mid(100L)
                .dids("1,2,3")
                .build()

        when:
        def model = structMapper.daoToModel(dao)

        then:
        model.id == 1L
        model.metricId == 100L
        model.dimensionIdList == ["1", "2", "3"]
    }

    def "test dtoToDao"() {
        given:
        def dto = MetricDimensionGroupRelDTO.builder()
                .id(1L)
                .metricId(100L)
                .dimensionIdList(["1", "2", "3"])
                .build()

        when:
        def dao = structMapper.dtoToDao(dto)

        then:
        dao.id == 1L
        dao.mid == 100L
        dao.dids == "1,2,3"
    }

    def "test modelToVo"() {
        given:
        def model = MetricDimensionGroupRel.builder()
                .id(1L)
                .metricId(100L)
                .dimensionIdList(["1", "2", "3"])
                .build()

        when:
        def vo = structMapper.modelToVo(model)

        then:
        vo.id == 1L
        vo.metricId == 100L
        vo.dimensionIds == "1,2,3"
    }

    def "test daoToModelBatch"() {
        given:
        def dao1 = MetricDimensionGroupRelDAO.builder()
                .id(1L)
                .mid(100L)
                .dids("1,2,3")
                .build()
        def dao2 = MetricDimensionGroupRelDAO.builder()
                .id(2L)
                .mid(200L)
                .dids("4,5,6")
                .build()
        def daoList = [dao1, dao2]

        when:
        def modelList = structMapper.daoToModelBatch(daoList)

        then:
        modelList.size() == 2
        modelList[0].id == 1L
        modelList[0].metricId == 100L
        modelList[1].id == 2L
        modelList[1].metricId == 200L
    }

    def "test modelToVoBatch"() {
        given:
        def model1 = MetricDimensionGroupRel.builder()
                .id(1L)
                .metricId(100L)
                .dimensionIdList(["1", "2", "3"])
                .build()
        def model2 = MetricDimensionGroupRel.builder()
                .id(2L)
                .metricId(200L)
                .dimensionIdList(["1", "2", "3"])
                .build()
        def modelList = [model1, model2]

        when:
        def voList = structMapper.modelToVoBatch(modelList)

        then:
        voList.size() == 2
        voList[0].id == 1L
        voList[0].metricId == 100L
        voList[1].id == 2L
        voList[1].metricId == 200L
    }
}

