package lab.zhang.data_science.metrics_mall.controller

import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.components.RequestContext
import lab.zhang.data_science.metrics_mall.enums.OpEventEnum
import lab.zhang.data_science.metrics_mall.handler.GlobalExceptionHandler
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO
import lab.zhang.data_science.metrics_mall.pojo.qo.EchoMetricQO
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricSnapshotQO
import lab.zhang.data_science.metrics_mall.service.EntityMetricRelService
import lab.zhang.data_science.metrics_mall.service.EntityService
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

    RequestContext requestContext = Mock()

    EntityService entityService = Mock()

    EntityMetricRelService entityMetricRelService = Mock()

    MetricSnapshotService metricSnapshotService = Mock()

    MetricSnapshotStructMapper metricSnapshotStructMapper = Mock()

    SnapshotController controller

    ObjectMapper objectMapper = new ObjectMapper()

    private static final String API_KEY = "test-api-key"

    def setup() {
        controller = new SnapshotController()
        controller.requestContext = requestContext
        controller.entityService = entityService
        controller.xService = entityMetricRelService
        controller.metricSnapshotService = metricSnapshotService
        controller.metricSnapshotStructMapper = metricSnapshotStructMapper
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build()
    }

    def "test write snapshot success"() {
        given:
        def writeQO = MetricSnapshotQO.builder()
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

        def writeResult = BigInteger.valueOf(10000001L)

        metricSnapshotStructMapper.qoToDto(_ as MetricSnapshotQO) >> {
            def dto = new MetricSnapshotDTO()
            dto.setEntityCode("user")
            dto.setEntityId(12345678L)
            return dto
        }
        metricSnapshotService.writeSnapshot(_ as MetricSnapshotDTO) >> writeResult

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap/write")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(writeQO)))

        then:
        1 * requestContext.setEvent(OpEventEnum.CREATE_METRIC_SNAPSHOT)
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(10000001))
    }

    def "test write snapshot with entity not found"() {
        given:
        def writeQO = MetricSnapshotQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics([EchoMetricQO.builder()
                                  .code("coin_balance")
                                  .alias("balance")
                                  .v(1)
                                  .value(1050.5)
                                  .build()])
                .build()

        metricSnapshotStructMapper.qoToDto(_ as MetricSnapshotQO) >> {
            def dto = new MetricSnapshotDTO()
            dto.setEntityCode("user")
            dto.setEntityId(12345678L)
            return dto
        }
        metricSnapshotService.writeSnapshot(_ as MetricSnapshotDTO) >> null

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap/write")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(writeQO)))

        then:
        1 * requestContext.setEvent(OpEventEnum.CREATE_METRIC_SNAPSHOT)
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
    }

    def "test write snapshot multiple metrics"() {
        given:
        def writeQO = MetricSnapshotQO.builder()
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

        def writeResult = BigInteger.valueOf(10000002L)

        metricSnapshotStructMapper.qoToDto(_ as MetricSnapshotQO) >> {
            def dto = new MetricSnapshotDTO()
            dto.setEntityCode("user")
            dto.setEntityId(12345678L)
            return dto
        }
        metricSnapshotService.writeSnapshot(_ as MetricSnapshotDTO) >> writeResult

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap/write")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(writeQO)))

        then:
        1 * requestContext.setEvent(OpEventEnum.CREATE_METRIC_SNAPSHOT)
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(10000002))
    }

    def "test write snapshot with partial rejection"() {
        given:
        def writeQO = MetricSnapshotQO.builder()
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

        def writeResult = BigInteger.valueOf(10000003L)

        metricSnapshotStructMapper.qoToDto(_ as MetricSnapshotQO) >> {
            def dto = new MetricSnapshotDTO()
            dto.setEntityCode("user")
            dto.setEntityId(12345678L)
            return dto
        }
        metricSnapshotService.writeSnapshot(_ as MetricSnapshotDTO) >> writeResult

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap/write")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(writeQO)))

        then:
        1 * requestContext.setEvent(OpEventEnum.CREATE_METRIC_SNAPSHOT)
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(10000003))
    }

    def "test write snapshot validation error missing entity code"() {
        given:
        def writeQO = MetricSnapshotQO.builder()
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
        0 * requestContext.setEvent(_)
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
    }

    def "test write snapshot validation error missing entity id"() {
        given:
        def writeQO = MetricSnapshotQO.builder()
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
        0 * requestContext.setEvent(_)
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
    }

    def "test write snapshot validation error missing metric code"() {
        given:
        def writeQO = MetricSnapshotQO.builder()
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
        0 * requestContext.setEvent(_)
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
    }

    def "test write snapshot validation error missing metric value"() {
        given:
        def writeQO = MetricSnapshotQO.builder()
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
        0 * requestContext.setEvent(_)
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

