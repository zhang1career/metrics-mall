package lab.zhang.data_science.metrics_mall.struct_mapper

import lab.zhang.data_science.metrics_mall.model.Dimension
import lab.zhang.data_science.metrics_mall.pojo.dao.DimensionDAO
import lab.zhang.data_science.metrics_mall.pojo.qo.DimensionQO
import spock.lang.Specification

import java.util.Date

/**
 * Test for DimensionStructMapper.
 *
 * @author Rongjin Zhang
 */
class DimensionStructMapperTest extends Specification {

    DimensionStructMapper structMapper = new DimensionStructMapperImpl()

    def "test qoToModel"() {
        given:
        def qo = DimensionQO.builder()
                .id(1L)
                .code("city")
                .name("City")
                .validation("[\"Beijing\", \"Shanghai\"]")
                .build()

        when:
        def model = structMapper.qoToModel(qo)

        then:
        model.id == 1L
        model.code == "city"
        model.name == "City"
        model.validation == "[\"Beijing\", \"Shanghai\"]"
    }

    def "test daoToModel"() {
        given:
        def now = System.currentTimeMillis()
        def dao = DimensionDAO.builder()
                .id(1L)
                .code("city")
                .name("City")
                .validation("[\"Beijing\", \"Shanghai\"]")
                .build()
        dao.ct = now
        dao.ut = now

        when:
        def model = structMapper.daoToModel(dao)

        then:
        model.id == 1L
        model.code == "city"
        model.name == "City"
        model.validation == "[\"Beijing\", \"Shanghai\"]"
        model.createTime.time == now
        model.updateTime.time == now
    }

    def "test modelToDao"() {
        given:
        def now = new Date()
        def model = Dimension.builder()
                .id(1L)
                .code("city")
                .name("City")
                .validation("[\"Beijing\", \"Shanghai\"]")
                .build()
        model.createTime = now
        model.updateTime = now

        when:
        def dao = structMapper.modelToDao(model)

        then:
        dao.id == 1L
        dao.code == "city"
        dao.name == "City"
        dao.validation == "[\"Beijing\", \"Shanghai\"]"
        dao.ct == now.time
        dao.ut == now.time
    }

    def "test modelToVo"() {
        given:
        def now = new Date()
        def model = Dimension.builder()
                .id(1L)
                .code("city")
                .name("City")
                .validation("[\"Beijing\", \"Shanghai\"]")
                .build()
        model.createTime = now
        model.updateTime = now

        when:
        def vo = structMapper.modelToVo(model)

        then:
        vo.id == 1L
        vo.code == "city"
        vo.name == "City"
        vo.validation == "[\"Beijing\", \"Shanghai\"]"
        vo.ct == now.time
        vo.ut == now.time
    }
}

