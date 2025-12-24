package lab.zhang.data_science.metrics_mall.controller

import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.handler.GlobalExceptionHandler
import lab.zhang.data_science.metrics_mall.model.Dimension
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDimensionRelDAO
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionRelDTO
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricDimensionRelQO
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricDimensionRelVO
import lab.zhang.data_science.metrics_mall.service.MetricDimensionRelService
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricDimensionRelStructMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Functional test for MetricDimensionRelController.
 *
 * @author Rongjin Zhang
 */
class MetricDimensionRelControllerTest extends Specification {

    MockMvc mockMvc
    MetricDimensionRelService metricDimensionRelService = Mock()
    MetricDimensionRelStructMapper metricDimensionRelStructMapper = Mock()
    MetricDimensionRelController controller
    ObjectMapper objectMapper = new ObjectMapper()

    private static final Long METRIC_META_ID = 1L
    private static final String METRIC_CODE = "consume_amount"
    private static final Long DIMENSION_ID = 2L
    private static final String DIMENSION_CODE = "location"
    private static final Integer IS_HOT = 1
    private static final String VALIDATION = "{\"min\":0,\"max\":100}"

    def setup() {
        controller = new MetricDimensionRelController()
        controller.metricDimensionRelService = metricDimensionRelService
        controller.metricDimensionRelStructMapper = metricDimensionRelStructMapper
        def validator = new LocalValidatorFactoryBean()
        validator.afterPropertiesSet()
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build()
    }

    def "test create success"() {
        given:
        def qo = MetricDimensionRelQO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode(DIMENSION_CODE)
                .isHot(IS_HOT)
                .validation(VALIDATION)
                .build()
        def dto = MetricDimensionRelDTO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode(DIMENSION_CODE)
                .isHot(IS_HOT)
                .validation(VALIDATION)
                .build()
        when:
        def response = mockMvc.perform(post("/api/v1/metric_dim_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * metricDimensionRelStructMapper.qoToDto(qo) >> dto
        1 * metricDimensionRelService.create(dto) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test create validation failure"() {
        given:
        def qo = MetricDimensionRelQO.builder()
                .dimensionCode(DIMENSION_CODE)
                .isHot(IS_HOT)
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/metric_dim_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
                .andExpect(jsonPath('$.errmsg').exists())
    }

    def "test get success"() {
        given:
        def dao = new MetricDimensionRelDAO()
        dao.setMetricMetaId(METRIC_META_ID)
        dao.setDimensionId(DIMENSION_ID)
        dao.setIsHot(IS_HOT)
        dao.setValidation(VALIDATION)
        def vo = MetricDimensionRelVO.builder()
                .metricMetaId(METRIC_META_ID)
                .dimensionId(DIMENSION_ID)
                .isHot(IS_HOT)
                .validation(VALIDATION)
                .build()

        when:
        def response = mockMvc.perform(get("/api/v1/metric_dim_rels")
                .param("metricMetaId", METRIC_META_ID.toString())
                .param("dimensionId", DIMENSION_ID.toString()))

        then:
        1 * metricDimensionRelService.get(METRIC_META_ID, DIMENSION_ID) >> dao
        1 * metricDimensionRelStructMapper.daoToVo(dao) >> vo
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.metricMetaId').value(METRIC_META_ID))
                .andExpect(jsonPath('$.data.dimensionId').value(DIMENSION_ID))
    }

    def "test get not found should return null"() {
        when:
        def response = mockMvc.perform(get("/api/v1/metric_dim_rels")
                .param("metricMetaId", METRIC_META_ID.toString())
                .param("dimensionId", DIMENSION_ID.toString()))

        then:
        1 * metricDimensionRelService.get(METRIC_META_ID, DIMENSION_ID) >> null
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').isEmpty())
    }

    def "test listByMetricMetaId success"() {
        given:
        def dao1 = new MetricDimensionRelDAO()
        dao1.setMetricMetaId(METRIC_META_ID)
        dao1.setDimensionId(2L)
        def dao2 = new MetricDimensionRelDAO()
        dao2.setMetricMetaId(METRIC_META_ID)
        dao2.setDimensionId(3L)
        def daoList = [dao1, dao2]
        def vo1 = MetricDimensionRelVO.builder()
                .metricMetaId(METRIC_META_ID)
                .dimensionId(2L)
                .build()
        def vo2 = MetricDimensionRelVO.builder()
                .metricMetaId(METRIC_META_ID)
                .dimensionId(3L)
                .build()

        when:
        def response = mockMvc.perform(get("/api/v1/metric_dim_rels/metric/${METRIC_META_ID}"))

        then:
        1 * metricDimensionRelService.listByMetricMetaId(METRIC_META_ID) >> daoList
        1 * metricDimensionRelStructMapper.daoToVo(dao1) >> vo1
        1 * metricDimensionRelStructMapper.daoToVo(dao2) >> vo2
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data[0].metricMetaId').value(METRIC_META_ID))
                .andExpect(jsonPath('$.data[1].metricMetaId').value(METRIC_META_ID))
    }

    def "test listByDimensionId success"() {
        given:
        def dao1 = new MetricDimensionRelDAO()
        dao1.setMetricMetaId(1L)
        dao1.setDimensionId(DIMENSION_ID)
        def daoList = [dao1]
        def vo1 = MetricDimensionRelVO.builder()
                .metricMetaId(1L)
                .dimensionId(DIMENSION_ID)
                .build()

        when:
        def response = mockMvc.perform(get("/api/v1/metric_dim_rels/dimension/${DIMENSION_ID}"))

        then:
        1 * metricDimensionRelService.listByDimensionId(DIMENSION_ID) >> daoList
        1 * metricDimensionRelStructMapper.daoToVo(dao1) >> vo1
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data[0].dimensionId').value(DIMENSION_ID))
    }

    def "test list success"() {
        given:
        def dao1 = new MetricDimensionRelDAO()
        dao1.setMetricMetaId(METRIC_META_ID)
        dao1.setDimensionId(DIMENSION_ID)
        def daoList = [dao1]
        def vo1 = MetricDimensionRelVO.builder()
                .metricMetaId(METRIC_META_ID)
                .dimensionId(DIMENSION_ID)
                .build()
        def voList = [vo1]

        when:
        def response = mockMvc.perform(get("/api/v1/metric_dim_rels/list"))

        then:
        1 * metricDimensionRelService.list() >> daoList
        1 * metricDimensionRelStructMapper.daoToVoBatch(daoList) >> voList
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data[0].metricMetaId').value(METRIC_META_ID))
    }

    def "test update success"() {
        given:
        def qo = MetricDimensionRelQO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode(DIMENSION_CODE)
                .isHot(0)
                .validation("{\"min\":10,\"max\":200}")
                .build()
        def dto = MetricDimensionRelDTO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode(DIMENSION_CODE)
                .isHot(0)
                .validation("{\"min\":10,\"max\":200}")
                .build()

        when:
        def response = mockMvc.perform(put("/api/v1/metric_dim_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * metricDimensionRelStructMapper.qoToDto(qo) >> dto
        1 * metricDimensionRelService.update(dto) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test update validation failure"() {
        given:
        def qo = MetricDimensionRelQO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode(DIMENSION_CODE)
                .validation(VALIDATION)
                .build()

        when:
        def response = mockMvc.perform(put("/api/v1/metric_dim_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
                .andExpect(jsonPath('$.errmsg').exists())
    }

    def "test delete success"() {
        when:
        def response = mockMvc.perform(delete("/api/v1/metric_dim_rels")
                .param("metricMetaId", METRIC_META_ID.toString())
                .param("dimensionId", DIMENSION_ID.toString()))

        then:
        1 * metricDimensionRelService.delete(METRIC_META_ID, DIMENSION_ID) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }
}

