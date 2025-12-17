package lab.zhang.data_science.metrics_mall.controller

import cn.hutool.core.date.DateUtil
import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.cache.MetricSnapshotCacheService
import lab.zhang.data_science.metrics_mall.common.TypedValue
import lab.zhang.data_science.metrics_mall.controller.v1.MetricSnapshotController
import lab.zhang.data_science.metrics_mall.model.Entity
import lab.zhang.data_science.metrics_mall.model.MetricSnapshot
import lab.zhang.data_science.metrics_mall.model.metric.EchoMetric
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO
import lab.zhang.data_science.metrics_mall.pojo.qo.EchoMetricQO
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricSnapshotQO
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricSnapshotVO
import lab.zhang.data_science.metrics_mall.service.MetricSnapshotService
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricSnapshotStructMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Functional test for MetricSnapshotController.
 *
 * @author Rongjin Zhang
 */
class MetricSnapshotControllerTest extends Specification {

    MockMvc mockMvc

    MetricSnapshotService metricSnapshotService = Mock()

    MetricSnapshotStructMapper metricSnapshotStructMap = Mock()

    MetricSnapshotCacheService metricSnapshotCacheService = Mock()

    MetricSnapshotController controller

    ObjectMapper objectMapper = new ObjectMapper()

    private static final String API_KEY = "test-api-key"

    def setup() {
        controller = new MetricSnapshotController()
        controller.metricSnapshotService = metricSnapshotService
        controller.metricSnapshotStructMapper = metricSnapshotStructMap
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    def "test query snapshot success"() {
        given:
        def snapshotQO = MetricSnapshotQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics([EchoMetricQO.builder()
                                  .code("coin_balance")
                                  .dims(createDimMap("city", "Beijing"))
                                  .build()])
                .snapshotTs(0L)
                .build()

        def precision2 = 2

        def values = ["coin_balance": 1050.5]
        def ts = ["coin_balance": 1715000001000L]

        def entity = Entity.builder()
                .meta(Entity.EntityMeta.builder().code("user").build())
                .id(12345678L)
                .build()
        def metricList = [PrimeMetric.builder().code("coin_balance").build()]
        def result = MetricSnapshot.builder()
                .entity(entity)
                .metricList(metricList as List<EchoMetric>)
                .snapshotTs(DateUtil.current())
                .build()

        def dto = MetricSnapshotDTO.builder().build()
        def vo = MetricSnapshotVO.builder()
                .entityCode("user")
                .entityId(12345678L)
                .valueMap(["coin_balance": TypedValue.of(1050.5, precision2)])
                .snapshotTsMap(ts)
                .build()

        metricSnapshotStructMap.qoToDto(_ as MetricSnapshotQO) >> dto
        metricSnapshotService.querySnapshot(_ as MetricSnapshotDTO) >> result
        metricSnapshotStructMap.modelToVo(_ as MetricSnapshot) >> vo

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(snapshotQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.ec').value("user"))
                .andExpect(jsonPath('$.data.eid').value(12345678))
                .andExpect(jsonPath('$.data.values.coin_balance').value(1050.50))
                .andExpect(jsonPath('$.data._ts.coin_balance').value(1715000001000L))
    }

    def "test query snapshot multiple metrics"() {
        given:
        def snapshotQO = MetricSnapshotQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics([
                        EchoMetricQO.builder()
                                .code("coin_balance")
                                .dims(createDimMap("city", "Beijing"))
                                .build(),
                        EchoMetricQO.builder()
                                .code("risk_score")
                                .dims(createDimMap("os", "ios"))
                                .build()
                ])
                .snapshotTs(0L)
                .build()

        def precision1 = 1

        def values = [
                "coin_balance": 1050.5,
                "risk_score"  : 0.1
        ]

        def ts = [
                "coin_balance": 1715000001000L,
                "risk_score"  : 1715000005000L
        ]

        def entity = Entity.builder()
                .meta(Entity.EntityMeta.builder().code("user").build())
                .id(12345678L)
                .build()
        def metricList = [
                PrimeMetric.builder().code("coin_balance").build(),
                PrimeMetric.builder().code("risk_score").build()
        ]
        def result = MetricSnapshot.builder()
                .entity(entity)
                .metricList(metricList as List<EchoMetric>)
                .snapshotTs(DateUtil.current())
                .build()

        def dto = MetricSnapshotDTO.builder().build()
        def vo = MetricSnapshotVO.builder()
                .entityCode("user")
                .entityId(12345678L)
                .valueMap([
                        "coin_balance": TypedValue.of(1050.5, precision1),
                        "risk_score"  : TypedValue.of(0.1, precision1)
                ])
                .snapshotTsMap(ts)
                .build()

        metricSnapshotStructMap.qoToDto(_ as MetricSnapshotQO) >> dto
        metricSnapshotService.querySnapshot(_ as MetricSnapshotDTO) >> result
        metricSnapshotStructMap.modelToVo(_ as MetricSnapshot) >> vo

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(snapshotQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.values.coin_balance').value(1050.5))
                .andExpect(jsonPath('$.data.values.risk_score').value(0.1))
    }

    def "test query snapshot without timestamp"() {
        given:
        def snapshotQO = MetricSnapshotQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics([EchoMetricQO.builder()
                                  .code("coin_balance")
                                  .build()])
                .build()

        def precision0 = 0

        def values = ["coin_balance": 1050.5]

        def entity = Entity.builder()
                .meta(Entity.EntityMeta.builder().code("user").build())
                .id(12345678L)
                .build()
        def metricList = [PrimeMetric.builder().code("coin_balance").build()]
        def result = MetricSnapshot.builder()
                .entity(entity)
                .metricList(metricList as List<EchoMetric>)
                .snapshotTs(DateUtil.current())
                .build()

        def dto = MetricSnapshotDTO.builder().build()
        def vo = MetricSnapshotVO.builder()
                .entityCode("user")
                .entityId(12345678L)
                .valueMap(["coin_balance": TypedValue.of(1050.5, precision0)])
                .snapshotTsMap(null)
                .build()

        metricSnapshotStructMap.qoToDto(_ as MetricSnapshotQO) >> dto
        metricSnapshotService.querySnapshot(_ as MetricSnapshotDTO) >> result
        metricSnapshotStructMap.modelToVo(_ as MetricSnapshot) >> vo

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(snapshotQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.values.coin_balance').value(1051))
    }

    def "test query snapshot validation error missing entity code"() {
        given:
        def snapshotQO = MetricSnapshotQO.builder()
                .eid(12345678L)
                .metrics([EchoMetricQO.builder()
                                  .code("coin_balance")
                                  .build()])
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(snapshotQO)))

        then:
        response.andExpect(status().isBadRequest())
    }

    def "test query snapshot validation error missing entity id"() {
        given:
        def snapshotQO = MetricSnapshotQO.builder()
                .ec("user")
                .metrics([EchoMetricQO.builder()
                                  .code("coin_balance")
                                  .build()])
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/m_snap")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(snapshotQO)))

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

