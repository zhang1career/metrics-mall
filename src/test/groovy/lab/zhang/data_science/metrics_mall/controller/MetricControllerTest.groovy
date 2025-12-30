package lab.zhang.data_science.metrics_mall.controller

import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.enums.AggregationTypeEnum
import lab.zhang.data_science.metrics_mall.enums.MetricTypeEnum
import lab.zhang.data_science.metrics_mall.handler.GlobalExceptionHandler
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricMetaDTO
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricMetaQO
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricVO
import lab.zhang.data_science.metrics_mall.service.MetricService
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricStructMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Functional test for MetricController.
 *
 * @author Rongjin Zhang
 * @date 2025-12-19
 */
class MetricControllerTest extends Specification {

    MockMvc mockMvc
    MetricService metricService = Mock()
    MetricStructMapper metricStructMapper = Mock()
    MetricController controller
    ObjectMapper objectMapper = new ObjectMapper()

    def setup() {
        controller = new MetricController()
        controller.metricService = metricService
        controller.metricStructMapper = metricStructMapper
        def validator = new LocalValidatorFactoryBean()
        validator.afterPropertiesSet()
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build()
    }

    def "test get by id success"() {
        given:
        def id = 1L
        def model = PrimeMetric.builder()
                .id(id)
                .code("test_metric")
                .name("Test Metric")
                .build()
        def vo = MetricVO.builder()
                .id(id)
                .code("test_metric")
                .name("Test Metric")
                .build()

        when:
        def response = mockMvc.perform(get("/api/v1/metrics/${id}"))

        then:
        1 * metricService.get(id) >> model
        1 * metricStructMapper.modelToVo(model) >> vo
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.id').value(id))
                .andExpect(jsonPath('$.data.code').value("test_metric"))
    }

    def "test get by code success"() {
        given:
        def code = "test_metric"
        def model = PrimeMetric.builder()
                .id(1L)
                .code(code)
                .name("Test Metric")
                .build()
        def vo = MetricVO.builder()
                .id(1L)
                .code(code)
                .name("Test Metric")
                .build()

        when:
        def response = mockMvc.perform(get("/api/v1/metrics").param("code", code))

        then:
        1 * metricService.getPrimeMetricByCode(code) >> model
        1 * metricStructMapper.modelToVo(model) >> vo
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.code').value(code))
    }

    def "test list success"() {
        given:
        def modelList = [PrimeMetric.builder()
                .id(1L)
                .code("test_metric")
                .name("Test Metric")
                .build()]
        def voList = [MetricVO.builder()
                .id(1L)
                .code("test_metric")
                .name("Test Metric")
                .build()]

        when:
        def response = mockMvc.perform(get("/api/v1/metrics/list"))

        then:
        1 * metricStructMapper.qoToDto(_ as MetricMetaQO) >> MetricMetaDTO.builder().build()
        1 * metricService.list(_ as MetricMetaDTO) >> modelList
        1 * metricStructMapper.modelToVo(_ as PrimeMetric) >> voList[0]
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data[0].id').value(1))
    }

    def "test insert success"() {
        given:
        def qo = MetricMetaQO.builder()
                .code("test_metric")
                .name("Test Metric")
                .metricType(0)
                .valueType(1)
                .aggregationType(0)
                .build()
        def dto = MetricMetaDTO.builder()
                .code("test_metric")
                .name("Test Metric")
                .metricType(MetricTypeEnum.ATOMIC.getId())
                .aggregationType(AggregationTypeEnum.SUM.getId())
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * metricStructMapper.qoToDto(_ as MetricMetaQO) >> dto
        1 * metricService.insert(dto) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test update success"() {
        given:
        def id = 1L
        def qo = MetricMetaQO.builder()
                .code("test_metric")
                .name("Updated Metric")
                .metricType(0)
                .valueType(1)
                .aggregationType(0)
                .build()
        def dto = MetricMetaDTO.builder()
                .id(id)
                .code("test_metric")
                .name("Updated Metric")
                .metricType(MetricTypeEnum.ATOMIC.getId())
                .aggregationType(AggregationTypeEnum.SUM.getId())
                .build()

        when:
        def response = mockMvc.perform(put("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * metricStructMapper.qoToDto(_ as MetricMetaQO) >> dto
        1 * metricService.update(dto) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test delete success"() {
        given:
        def id = 1L

        when:
        def response = mockMvc.perform(delete("/api/v1/metrics/${id}"))

        then:
        1 * metricService.delete(id) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test insert validation failure"() {
        given:
        def qo = MetricMetaQO.builder()
                .name("Test Metric")
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
                .andExpect(jsonPath('$.errmsg').exists())
    }
}

