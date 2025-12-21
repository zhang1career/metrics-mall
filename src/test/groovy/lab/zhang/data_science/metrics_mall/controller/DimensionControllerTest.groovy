package lab.zhang.data_science.metrics_mall.controller

import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.handler.GlobalExceptionHandler
import lab.zhang.data_science.metrics_mall.model.Dimension
import lab.zhang.data_science.metrics_mall.pojo.dto.DimensionDTO
import lab.zhang.data_science.metrics_mall.pojo.qo.DimensionQO
import lab.zhang.data_science.metrics_mall.pojo.vo.DimensionVO
import lab.zhang.data_science.metrics_mall.service.DimensionService
import lab.zhang.data_science.metrics_mall.struct_mapper.DimensionStructMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Functional test for DimensionController.
 *
 * @author Rongjin Zhang
 */
class DimensionControllerTest extends Specification {

    MockMvc mockMvc
    DimensionService dimensionService = Mock()
    DimensionStructMapper dimensionStructMapper = Mock()
    DimensionController controller
    ObjectMapper objectMapper = new ObjectMapper()

    def setup() {
        controller = new DimensionController()
        controller.dimensionService = dimensionService
        controller.dimensionStructMapper = dimensionStructMapper
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build()
    }

    def "test get by id success"() {
        given:
        def id = 1L
        def model = Dimension.builder().id(id).build()
        def vo = DimensionVO.builder().id(id).build()

        when:
        def response = mockMvc.perform(get("/api/v1/dims/${id}"))

        then:
        1 * dimensionService.get(id) >> model
        1 * dimensionStructMapper.modelToVo(model) >> vo
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.id').value(id))
    }

    def "test get by code success"() {
        given:
        def code = "city"
        def model = Dimension.builder().code(code).build()
        def vo = DimensionVO.builder().code(code).build()

        when:
        def response = mockMvc.perform(get("/api/v1/dims/code/${code}"))

        then:
        1 * dimensionService.getByCode(code) >> model
        1 * dimensionStructMapper.modelToVo(model) >> vo
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.code').value(code))
    }

    def "test list success"() {
        given:
        def modelList = [Dimension.builder().id(1L).build()]
        def voList = [DimensionVO.builder().id(1L).build()]

        when:
        def response = mockMvc.perform(get("/api/v1/dims"))

        then:
        1 * dimensionService.list() >> modelList
        1 * dimensionStructMapper.modelToVoBatch(modelList) >> voList
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data[0].id').value(1))
    }

    def "test count success"() {
        when:
        def response = mockMvc.perform(get("/api/v1/dims/count"))

        then:
        1 * dimensionService.count() >> 10L
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(10))
    }

    def "test insert success"() {
        given:
        def qo = DimensionQO.builder().code("city").name("City").build()
        def dto = DimensionDTO.builder().code("city").name("City").build()

        when:
        def response = mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * dimensionStructMapper.qoToDto(_ as DimensionQO) >> dto
        1 * dimensionService.insert(dto) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test update success"() {
        given:
        def qo = DimensionQO.builder().id(1L).code("city").name("City").build()
        def dto = DimensionDTO.builder().id(1L).code("city").name("City").build()

        when:
        def response = mockMvc.perform(put("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * dimensionStructMapper.qoToDto(_ as DimensionQO) >> dto
        1 * dimensionService.update(dto) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test delete success"() {
        given:
        def id = 1L

        when:
        def response = mockMvc.perform(delete("/api/v1/dims/${id}"))

        then:
        1 * dimensionService.delete(id) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }
}

