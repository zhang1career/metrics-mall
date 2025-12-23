package lab.zhang.data_science.metrics_mall.controller

import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.handler.GlobalExceptionHandler
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetaDAO
import lab.zhang.data_science.metrics_mall.pojo.dto.EntityMetaDTO
import lab.zhang.data_science.metrics_mall.pojo.qo.EntityMetaQO
import lab.zhang.data_science.metrics_mall.pojo.vo.EntityMetaVO
import lab.zhang.data_science.metrics_mall.service.EntityService
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
class EntityControllerTest extends Specification {

    MockMvc mockMvc
    EntityService entityService = Mock()
    EntityMetaStructMapper entityMetaStructMapper = Mock()
    EntityController controller
    ObjectMapper objectMapper = new ObjectMapper()

    def setup() {
        controller = new EntityController()
        controller.entityService = entityService
        controller.entityMetaStructMapper = entityMetaStructMapper
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build()
    }

    def "test get by id success"() {
        given:
        def id = 1
        def dao = EntityMetaDAO.builder().id(id).build()
        def vo = EntityMetaVO.builder().id(id).build()

        when:
        def response = mockMvc.perform(get("/api/v1/entity_metas/${id}"))

        then:
        1 * entityService.getDao(id) >> dao
        1 * entityMetaStructMapper.daoToVo(dao) >> vo
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.id').value(id))
    }

    def "test get by code success"() {
        given:
        def code = "user"
        def dao = EntityMetaDAO.builder().code(code).build()
        def vo = EntityMetaVO.builder().code(code).build()

        when:
        def response = mockMvc.perform(get("/api/v1/entity_metas/code/${code}"))

        then:
        1 * entityService.getDaoByCode(code) >> dao
        1 * entityMetaStructMapper.daoToVo(dao) >> vo
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.code').value(code))
    }

    def "test list success"() {
        given:
        def daoList = [EntityMetaDAO.builder().id(1).build()]
        def voList = [EntityMetaVO.builder().id(1).build()]

        when:
        def response = mockMvc.perform(get("/api/v1/entity_metas"))

        then:
        1 * entityService.listDao() >> daoList
        1 * entityMetaStructMapper.daoToVoBatch(daoList) >> voList
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data[0].id').value(1))
    }

    def "test count success"() {
        when:
        def response = mockMvc.perform(get("/api/v1/entity_metas/count"))

        then:
        1 * entityService.count() >> 10L
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(10))
    }

    def "test insert success"() {
        given:
        def qo = EntityMetaQO.builder().code("user").name("User").build()
        def dto = EntityMetaDTO.builder().code("user").name("User").build()

        when:
        def response = mockMvc.perform(post("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * entityMetaStructMapper.qoToDto(_ as EntityMetaQO) >> dto
        1 * entityService.insert(dto) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test update success"() {
        given:
        def qo = EntityMetaQO.builder().code("user").name("User").build()
        def dto = EntityMetaDTO.builder().code("user").name("User").build()

        when:
        def response = mockMvc.perform(put("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * entityMetaStructMapper.qoToDto(_ as EntityMetaQO) >> dto
        1 * entityService.update(dto) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test delete success"() {
        given:
        def id = 1

        when:
        def response = mockMvc.perform(delete("/api/v1/entity_metas/${id}"))

        then:
        1 * entityService.delete(id) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }
}

