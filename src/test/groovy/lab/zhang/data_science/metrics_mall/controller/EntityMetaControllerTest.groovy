package lab.zhang.data_science.metrics_mall.controller

import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.controller.v1.EntityMetaController
import lab.zhang.data_science.metrics_mall.handler.GlobalExceptionHandler
import lab.zhang.data_science.metrics_mall.model.EntityMeta
import lab.zhang.data_science.metrics_mall.pojo.qo.EntityMetaQO
import lab.zhang.data_science.metrics_mall.pojo.vo.EntityMetaVO
import lab.zhang.data_science.metrics_mall.service.EntityMetaService
import lab.zhang.data_science.metrics_mall.struct_mapper.EntityMetaStructMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Functional test for EntityMetaController.
 *
 * @author Rongjin Zhang
 */
class EntityMetaControllerTest extends Specification {

    MockMvc mockMvc
    EntityMetaService entityMetaService = Mock()
    EntityMetaStructMapper entityMetaStructMapper = Mock()
    EntityMetaController controller
    ObjectMapper objectMapper = new ObjectMapper()

    def setup() {
        controller = new EntityMetaController()
        controller.entityMetaService = entityMetaService
        controller.entityMetaStructMapper = entityMetaStructMapper
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build()
    }

    def "test get by id success"() {
        given:
        def id = 1
        def model = EntityMeta.builder().id(id).build()
        def vo = EntityMetaVO.builder().id(id).build()

        when:
        def response = mockMvc.perform(get("/api/v1/entity-metas/${id}"))

        then:
        1 * entityMetaService.get(id) >> model
        1 * entityMetaStructMapper.modelToVo(model) >> vo
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.id').value(id))
    }

    def "test get by code success"() {
        given:
        def code = "user"
        def model = EntityMeta.builder().code(code).build()
        def vo = EntityMetaVO.builder().code(code).build()

        when:
        def response = mockMvc.perform(get("/api/v1/entity-metas/code/${code}"))

        then:
        1 * entityMetaService.getByCode(code) >> model
        1 * entityMetaStructMapper.modelToVo(model) >> vo
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.code').value(code))
    }

    def "test list success"() {
        given:
        def modelList = [EntityMeta.builder().id(1).build()]
        def voList = [EntityMetaVO.builder().id(1).build()]

        when:
        def response = mockMvc.perform(get("/api/v1/entity-metas"))

        then:
        1 * entityMetaService.list() >> modelList
        1 * entityMetaStructMapper.modelToVoBatch(modelList) >> voList
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data[0].id').value(1))
    }

    def "test count success"() {
        when:
        def response = mockMvc.perform(get("/api/v1/entity-metas/count"))

        then:
        1 * entityMetaService.count() >> 10L
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(10))
    }

    def "test insert success"() {
        given:
        def qo = EntityMetaQO.builder().code("user").name("User").build()
        def model = EntityMeta.builder().code("user").name("User").build()

        when:
        def response = mockMvc.perform(post("/api/v1/entity-metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * entityMetaStructMapper.qoToModel(_ as EntityMetaQO) >> model
        1 * entityMetaService.insert(model) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test update success"() {
        given:
        def qo = EntityMetaQO.builder().id(1).code("user").name("User").build()
        def model = EntityMeta.builder().id(1).code("user").name("User").build()

        when:
        def response = mockMvc.perform(put("/api/v1/entity-metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * entityMetaStructMapper.qoToModel(_ as EntityMetaQO) >> model
        1 * entityMetaService.update(model) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test delete success"() {
        given:
        def id = 1

        when:
        def response = mockMvc.perform(delete("/api/v1/entity-metas/${id}"))

        then:
        1 * entityMetaService.delete(id) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }
}

