package lab.zhang.data_science.metrics_mall.controller

import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.handler.GlobalExceptionHandler
import lab.zhang.data_science.metrics_mall.model.Dimension
import lab.zhang.data_science.metrics_mall.model.MetricDimensionGroupRel
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionGroupRelDTO
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricDimensionGroupRelQO
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricDimensionGroupRelVO
import lab.zhang.data_science.metrics_mall.service.DimensionService
import lab.zhang.data_science.metrics_mall.service.MetricDimensionGroupRelService
import lab.zhang.data_science.metrics_mall.service.MetricService
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricDimensionGroupRelStructMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Functional test for YGroupController.
 *
 * @author Rongjin Zhang
 */
class MetricDimensionGroupRelControllerTest extends Specification {

    MockMvc mockMvc
    MetricService metricService = Mock()
    DimensionService dimensionService = Mock()
    MetricDimensionGroupRelService yGroupService = Mock()
    MetricDimensionGroupRelStructMapper yGroupStructMapper = Mock()
    MetricDimensionGroupRelController controller
    ObjectMapper objectMapper = new ObjectMapper()

    def setup() {
        controller = new MetricDimensionGroupRelController()
        controller.metricService = metricService
        controller.dimensionService = dimensionService
        controller.metricDimensionGroupRelService = yGroupService
        controller.metricDimensionGroupRelStructMapper = yGroupStructMapper
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build()
    }

    def "test get by id success"() {
        given:
        def id = 1L
        def model = MetricDimensionGroupRel.builder()
                .id(id)
                .metricId(100L)
                .dimensionIdList(["1", "2", "3"])
                .build()
        def vo = MetricDimensionGroupRelVO.builder()
                .id(id)
                .metricId(100L)
                .dimensionIds("1,2,3")
                .build()

        when:
        def response = mockMvc.perform(get("/api/v1/metric_dim_group_rels/${id}"))

        then:
        1 * yGroupService.get(id) >> model
        1 * yGroupStructMapper.modelToVo(model) >> vo
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.id').value(id))
    }

    def "test list success"() {
        given:
        def modelList = [
                MetricDimensionGroupRel.builder()
                        .id(1L)
                        .metricId(100L)
                        .dimensionIdList(["1", "2", "3"])
                        .build()
        ]
        def voList = [
                MetricDimensionGroupRelVO.builder()
                        .id(1L)
                        .metricId(100L)
                        .dimensionIds("1,2,3")
                        .build()
        ]

        when:
        def response = mockMvc.perform(get("/api/v1/metric_dim_group_rels"))

        then:
        1 * yGroupService.list() >> modelList
        1 * yGroupStructMapper.modelToVoBatch(modelList) >> voList
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data[0].id').value(1))
    }

    def "test listByMid success"() {
        given:
        def mid = 100L
        def modelList = [
                MetricDimensionGroupRel.builder()
                        .id(1L)
                        .metricId(mid)
                        .dimensionIdList(["1", "2", "3"])
                        .build()
        ]
        def voList = [
                MetricDimensionGroupRelVO.builder()
                        .id(1L)
                        .metricId(mid)
                        .dimensionIds("1,2,3")
                        .build()
        ]

        when:
        def response = mockMvc.perform(get("/api/v1/metric_dim_group_rels/metric/${mid}"))

        then:
        1 * yGroupService.listByMetricId(mid) >> modelList
        1 * yGroupStructMapper.modelToVoBatch(modelList) >> voList
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data[0].id').value(1))
    }

    def "test count success"() {
        when:
        def response = mockMvc.perform(get("/api/v1/metric_dim_group_rels/count"))

        then:
        1 * yGroupService.count() >> 10L
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(10))
    }

    def 'test create success'() {
        given:
        def qo = MetricDimensionGroupRelQO.builder()
                .metricCode("consume_amount")
                .dimensionCodes("location,device_type,user_age")
                .build()
        def dim1 = Dimension.builder()
                .id(1)
                .code("location")
                .build()
        def dim2 = Dimension.builder()
                .id(2)
                .code("device_type")
                .build()
        def dim3 = Dimension.builder()
                .id(3)
                .code("user_age")
                .build()
        def dao = MetricMetaDAO.builder()
                .id(100L)
                .code("consume_amount")
                .build()
        def dto = MetricDimensionGroupRelDTO.builder()
                .metricId(100L)
                .dimensionIdList(["1", "2", "3"])
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/metric_dim_group_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * metricService.getMetricMetaDaoByCode(_) >> dao
        1 * yGroupStructMapper.explode(_) >> ["location","device_type","user_age"]
        1 * dimensionService.mapByCodeBatch(_) >> ["location": dim1, "device_type": dim2, "user_age": dim3]
        1 * yGroupStructMapper.qoToDto(_ as MetricDimensionGroupRelQO) >> dto
        1 * yGroupService.insert(dto) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test update success"() {
        given:
        def qo = MetricDimensionGroupRelQO.builder()
                .metricId(100L)
                .dimensionIds("1,2,3,4")
                .build()
        def dto = MetricDimensionGroupRelDTO.builder()
                .id(1L)
                .metricId(100L)
                .dimensionIdList(["1", "2", "3", "4"])
                .build()

        when:
        def response = mockMvc.perform(put("/api/v1/metric_dim_group_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * yGroupStructMapper.qoToDto(_ as MetricDimensionGroupRelQO) >> dto
        1 * yGroupService.update(dto) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test delete success"() {
        given:
        def id = 1L

        when:
        def response = mockMvc.perform(delete("/api/v1/metric_dim_group_rels/${id}"))

        then:
        1 * yGroupService.delete(id) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }
}

