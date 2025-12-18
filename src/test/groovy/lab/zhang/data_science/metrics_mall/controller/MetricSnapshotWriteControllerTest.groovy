package lab.zhang.data_science.metrics_mall.controller

import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.controller.v1.MetricSnapshotController
import lab.zhang.data_science.metrics_mall.handler.GlobalExceptionHandler
import lab.zhang.data_science.metrics_mall.pojo.qo.EchoMetricQO
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricSnapshotWriteQO
import lab.zhang.data_science.metrics_mall.service.MetricSnapshotService
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricSnapshotStructMapper
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricStructMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Functional test for MetricSnapshotController write operations.
 *
 * @author Rongjin Zhang
 */
class MetricSnapshotWriteControllerTest extends Specification {

    MockMvc mockMvc

    MetricSnapshotService metricSnapshotService = Mock()

    MetricSnapshotStructMapper metricSnapshotStructMapper = Mock()

    MetricStructMapper metricStructMapper = Mock()

    MetricSnapshotController controller

    ObjectMapper objectMapper = new ObjectMapper()

    private static final String API_KEY = "test-api-key"

    def setup() {
        controller = new MetricSnapshotController()
        controller.metricSnapshotService = metricSnapshotService
        controller.metricSnapshotStructMapper = metricSnapshotStructMapper
        controller.metricStructMapper = metricStructMapper
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build()
    }

    def "test write snapshot success"() {
        given:
        def writeQO = MetricSnapshotWriteQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics([EchoMetricQO.builder()
                                  .code("coin_balance")
                                  .alias("balance")
                                  .v(1)
                                  .dims(createDimMap("city", "Beijing"))
                                  .value(1050.5)
                                  .build()])
                .snapshotTs(1715000001000L)
                .build()

        def writeResult = 1L

        metricSnapshotStructMapper.writeQoToDto(_ as MetricSnapshotWriteQO, _) >> {
            def dto = new lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotWriteDTO()
            dto.setEntityCode("user")
            dto.setEntityId(12345678L)
            return dto
        }
        metricSnapshotService.writeSnapshot(_ as lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotWriteDTO) >> writeResult

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap/write")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(writeQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(1))
    }

    def "test write snapshot with invalid parameter mapping"() {
        given:
        def writeQO = MetricSnapshotWriteQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics([])
                .build()

        metricSnapshotStructMapper.writeQoToDto(_ as MetricSnapshotWriteQO, _) >> null

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap/write")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(writeQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
    }

    def "test write snapshot multiple metrics"() {
        given:
        def writeQO = MetricSnapshotWriteQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics([
                        EchoMetricQO.builder()
                                .code("coin_balance")
                                .alias("balance")
                                .v(1)
                                .value(1050.5)
                                .build(),
                        EchoMetricQO.builder()
                                .code("level")
                                .alias("level")
                                .v(1)
                                .value(10)
                                .build()
                ])
                .build()

        def writeResult = 1L

        metricSnapshotStructMapper.writeQoToDto(_ as MetricSnapshotWriteQO, _) >> {
            def dto = new lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotWriteDTO()
            dto.setEntityCode("user")
            dto.setEntityId(12345678L)
            return dto
        }
        metricSnapshotService.writeSnapshot(_ as lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotWriteDTO) >> writeResult

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap/write")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(writeQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(1))
    }

    def "test write snapshot with partial rejection"() {
        given:
        def writeQO = MetricSnapshotWriteQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics([
                        EchoMetricQO.builder()
                                .code("coin_balance")
                                .alias("balance")
                                .v(1)
                                .value(1050.5)
                                .build(),
                        EchoMetricQO.builder()
                                .code("invalid_metric")
                                .alias("invalid")
                                .v(1)
                                .value(100)
                                .build()
                ])
                .build()

        def writeResult = 1L

        metricSnapshotStructMapper.writeQoToDto(_ as MetricSnapshotWriteQO, _) >> {
            def dto = new lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotWriteDTO()
            dto.setEntityCode("user")
            dto.setEntityId(12345678L)
            return dto
        }
        metricSnapshotService.writeSnapshot(_ as lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotWriteDTO) >> writeResult

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap/write")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(writeQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(1))
    }

    def "test write snapshot validation error missing entity code"() {
        given:
        def writeQO = MetricSnapshotWriteQO.builder()
                .eid(12345678L)
                .metrics([EchoMetricQO.builder()
                                  .code("coin_balance")
                                  .alias("balance")
                                  .value(1050.5)
                                  .build()])
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap/write")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(writeQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
    }

    def "test write snapshot validation error missing entity id"() {
        given:
        def writeQO = MetricSnapshotWriteQO.builder()
                .ec("user")
                .metrics([EchoMetricQO.builder()
                                  .code("coin_balance")
                                  .alias("balance")
                                  .value(1050.5)
                                  .build()])
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap/write")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(writeQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
    }

    def "test write snapshot validation error missing metric code"() {
        given:
        def writeQO = MetricSnapshotWriteQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics([EchoMetricQO.builder()
                                  .alias("balance")
                                  .value(1050.5)
                                  .build()])
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap/write")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(writeQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
    }

    def "test write snapshot validation error missing metric value"() {
        given:
        def writeQO = MetricSnapshotWriteQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics([EchoMetricQO.builder()
                                  .code("coin_balance")
                                  .alias("balance")
                                  .build()])
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap/write")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(writeQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
    }

    /**
     * Create dimension map for testing.
     *
     * @param keyValues key-value pairs
     * @return dimension map
     */
    private static Map<String, Object> createDimMap(String... keyValues) {
        Map<String, Object> dimMap = [:]
        for (int i = 0; i < keyValues.length; i += 2) {
            dimMap.put(keyValues[i], keyValues[i + 1])
        }
        return dimMap
    }
}

