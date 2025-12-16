package lab.zhang.data_science.metrics_mall.controller

import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.controller.v1.EventController
import lab.zhang.data_science.metrics_mall.model.EventAcceptanceResult
import lab.zhang.data_science.metrics_mall.pojo.dto.EventDTO
import lab.zhang.data_science.metrics_mall.pojo.qo.EventBatchQO
import lab.zhang.data_science.metrics_mall.pojo.qo.EventQO
import lab.zhang.data_science.metrics_mall.pojo.vo.EventResponseVO
import lab.zhang.data_science.metrics_mall.service.EventService
import lab.zhang.data_science.metrics_mall.struct_mapper.EventStructMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Functional test for EventController.
 *
 * @author Rongjin Zhang
 */
@WebMvcTest(controllers = [EventController.class])
class EventDTOControllerTest extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    EventService eventService = Mock()

    @SpringBean
    EventStructMapper eventStructMapper = Mock()

    @Autowired
    ObjectMapper objectMapper

    private static final String API_KEY = "test-api-key"

    def "test report success"() {
        given:
        def eventBatchQO = EventBatchQO.builder()
                .events([EventQO.builder()
                        .metricName("user_credit_score")
                        .timestamp(1701234567890L)
                        .dim(createDimMap("user_id", "12345", "region", "us-west", "device", "ios"))
                        .value(850.0)
                        .build()])
                .build()

        def result = new EventAcceptanceResult(1, 0)
        def eventDTOList = [EventDTO.builder().build()]
        def responseVO = EventResponseVO.builder()
                .accepted(1)
                .rejected(0)
                .build()

        eventStructMapper.qoToDtoBatch(_ as List<EventQO>) >> eventDTOList
        eventService.processEventBatch(_ as List<EventDTO>) >> result
        eventStructMapper.modelToVo(1, 0) >> responseVO

        when:
        def response = mockMvc.perform(post("/api/v1/events")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventBatchQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.accepted').value(1))
                .andExpect(jsonPath('$.data.rejected').value(0))
    }

    def "test report events multiple"() {
        given:
        def eventBatchQO = EventBatchQO.builder()
                .events([
                        EventQO.builder()
                                .metricName("user_credit_score")
                                .timestamp(1701234567890L)
                                .dim(createDimMap("user_id", "12345"))
                                .value(850.0)
                                .build(),
                        EventQO.builder()
                                .metricName("coin_balance")
                                .timestamp(1701234567891L)
                                .dim(createDimMap("user_id", "12345", "city", "Beijing"))
                                .value(1050.5)
                                .build()
                ])
                .build()

        def result = new EventAcceptanceResult(2, 0)
        def eventDTOList = [EventDTO.builder().build(), EventDTO.builder().build()]
        def responseVO = EventResponseVO.builder()
                .accepted(2)
                .rejected(0)
                .build()

        eventStructMapper.qoToDtoBatch(_ as List<EventQO>) >> eventDTOList
        eventService.processEventBatch(_ as List<EventDTO>) >> result
        eventStructMapper.modelToVo(2, 0) >> responseVO

        when:
        def response = mockMvc.perform(post("/api/v1/events")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventBatchQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.accepted').value(2))
                .andExpect(jsonPath('$.data.rejected').value(0))
    }

    def "test report validation error missing metric name"() {
        given:
        def eventBatchQO = EventBatchQO.builder()
                .events([EventQO.builder()
                        .timestamp(1701234567890L)
                        .value(850.0)
                        .build()])
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/events")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventBatchQO)))

        then:
        response.andExpect(status().isBadRequest())
    }

    def "test report validation error missing timestamp"() {
        given:
        def eventBatchQO = EventBatchQO.builder()
                .events([EventQO.builder()
                        .metricName("user_credit_score")
                        .value(850.0)
                        .build()])
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/events")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventBatchQO)))

        then:
        response.andExpect(status().isBadRequest())
    }

    def "test report validation error missing value"() {
        given:
        def eventBatchQO = EventBatchQO.builder()
                .events([EventQO.builder()
                        .metricName("user_credit_score")
                        .timestamp(1701234567890L)
                        .build()])
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/events")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventBatchQO)))

        then:
        response.andExpect(status().isBadRequest())
    }

    def "test report events validation error empty"() {
        given:
        def eventBatchQO = EventBatchQO.builder()
                .events([])
                .build()

        def eventDTOList = []
        def responseVO = EventResponseVO.builder()
                .accepted(0)
                .rejected(0)
                .build()

        eventStructMapper.qoToDtoBatch(_ as List<EventQO>) >> eventDTOList
        eventService.processEventBatch(_ as List<EventDTO>) >> new EventAcceptanceResult(0, 0)
        eventStructMapper.modelToVo(0, 0) >> responseVO

        when:
        def response = mockMvc.perform(post("/api/v1/events")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventBatchQO)))

        then:
        response.andExpect(status().isBadRequest())
    }

    /**
     * Create dimension map for testing.
     *
     * @param keyValues key-value pairs
     * @return dimension map
     */
    private static Map<String, String> createDimMap(String... keyValues) {
        Map<String, String> dimMap = [:]
        for (int i = 0; i < keyValues.length; i += 2) {
            dimMap.put(keyValues[i], keyValues[i + 1])
        }
        return dimMap
    }
}

