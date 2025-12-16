package lab.zhang.data_science.metrics_mall.controller

import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.common.TypedValue
import lab.zhang.data_science.metrics_mall.controller.v1.MetricAggregationController
import lab.zhang.data_science.metrics_mall.model.MetricAggregation
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricAggregationDTO
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricAggregationQO
import lab.zhang.data_science.metrics_mall.pojo.vo.AggregationVO
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricAggregationVO
import lab.zhang.data_science.metrics_mall.pojo.vo.BriefMetricVO.PrettyBriefMetricVO
import lab.zhang.data_science.metrics_mall.service.MetricAggregationService
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricAggregationStructMapper
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
 * Functional test for MetricAggregationController.
 *
 * @author Rongjin Zhang
 */
@WebMvcTest(controllers = [MetricAggregationController.class])
class MetricAggregationControllerTest extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    MetricAggregationService metricAggregationService = Mock()

    @SpringBean
    MetricAggregationStructMapper metricAggregationStructMapper = Mock()

    @Autowired
    ObjectMapper objectMapper

    private static final String API_KEY = "test-api-key"

    def "test query aggregation success"() {
        given:
        def aggregationQO = MetricAggregationQO.builder()
                .metricCodes(["pay_amount", "pay_user_cnt"])
                .timeRange(MetricAggregationQO.TimeRangeQO.builder()
                        .start("2024-05-20 00:00:00")
                        .stop("2024-05-21 00:00:00")
                        .build())
                .interval("1h")
                .groupBy(["city", "os"])
                .filters([MetricAggregationQO.FieldConditionQO.builder()
                        .field("channel")
                        .op("=")
                        .value("tiktok")
                        .build()])
                .orderBys([MetricAggregationQO.OrderByQO.builder()
                        .field("pay_amount")
                        .sort("desc")
                        .build()])
                .limit(100)
                .build()

        def meta = [
                "pay_amount": PrimeMetric.builder()
                        .name("支付金额")
                        .unit("CNY")
                        .precision(2)
                        .build(),
                "pay_user_cnt": PrimeMetric.builder()
                        .name("支付人数")
                        .unit("人")
                        .precision(0)
                        .build()
        ]

        def dimMap = ["bucket_time": TypedValue.of("2024-05-20 10:00:00"),
                      "city": TypedValue.of("Beijing"),
                      "os": TypedValue.of("iOS")]
        def valueMap = ["pay_amount": TypedValue.of(5000.00),
                              "pay_user_cnt": TypedValue.of(200.0)]
        def dimensionValueMap = [(dimMap): valueMap]

        def result = new MetricAggregation(meta, dimensionValueMap)

        def dto = MetricAggregationDTO.builder().build()

        def metaVO = [
                "pay_amount": PrettyBriefMetricVO.builder()
                        .name("支付金额")
                        .unit("CNY")
                        .precision(2)
                        .build(),
                "pay_user_cnt": PrettyBriefMetricVO.builder()
                        .name("支付人数")
                        .unit("人")
                        .precision(0)
                        .build()
        ]
        def vo = MetricAggregationVO.builder()
                .meta(metaVO)
                .rows([AggregationVO.builder()
                        .dimensionMap(dimMap)
                        .valueMap(valueMap)
                        .build()])
                .build()

        metricAggregationStructMapper.qoToDto(_ as MetricAggregationQO) >> dto
        metricAggregationService.queryAggregation(_ as MetricAggregationDTO) >> result
        metricAggregationStructMapper.modelToVo(_ as MetricAggregation) >> vo

        when:
        def response = mockMvc.perform(post("/api/v1/m_agg")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggregationQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.meta.pay_amount.name').value("支付金额"))
                .andExpect(jsonPath('$.data.meta.pay_amount.unit').value("CNY"))
                .andExpect(jsonPath('$.data.meta.pay_amount.precision').value(2))
                .andExpect(jsonPath('$.data.rows[0].bucket_time').value("2024-05-20 10:00:00"))
                .andExpect(jsonPath('$.data.rows[0].city').value("Beijing"))
                .andExpect(jsonPath('$.data.rows[0].os').value("iOS"))
                .andExpect(jsonPath('$.data.rows[0].pay_amount').value(5000.0))
                .andExpect(jsonPath('$.data.rows[0].pay_user_cnt').value(200.0))
    }

    def "test query aggregation multiple rows"() {
        given:
        def aggregationQO = MetricAggregationQO.builder()
                .metricCodes(["pay_amount"])
                .timeRange(MetricAggregationQO.TimeRangeQO.builder()
                        .start("2024-05-20 00:00:00")
                        .stop("2024-05-21 00:00:00")
                        .build())
                .interval("1h")
                .build()

        def meta = [
                "pay_amount": PrimeMetric.builder()
                        .name("支付金额")
                        .unit("CNY")
                        .precision(2)
                        .build()
        ]

        def multiDimMap1 = ["bucket_time": TypedValue.of("2024-05-20 10:00:00"),
                        "city": TypedValue.of("Beijing"),
                        "os": TypedValue.of("iOS")]
        def multiMetricValueMap1 = ["pay_amount": TypedValue.of(5000.00)]
        def multiDimMap2 = ["bucket_time": TypedValue.of("2024-05-20 11:00:00"),
                        "city": TypedValue.of("Shanghai"),
                        "os": TypedValue.of("iOS")]
        def multiMetricValueMap2 = ["pay_amount": TypedValue.of(3000.00)]
        def valueMap = [(multiDimMap1): multiMetricValueMap1, (multiDimMap2): multiMetricValueMap2]

        def result = new MetricAggregation(meta, valueMap)

        def dto = MetricAggregationDTO.builder().build()
        def multiTestDimMap1 = ["bucket_time": TypedValue.of("2024-05-20 10:00:00"),
                       "city": TypedValue.of("Beijing"),
                       "os": TypedValue.of("iOS")]
        def multiTestMetricMap1 = ["pay_amount": TypedValue.of(5000.00)]
        def multiTestDimMap2 = ["bucket_time": TypedValue.of("2024-05-20 11:00:00"),
                       "city": TypedValue.of("Shanghai"),
                       "os": TypedValue.of("iOS")]
        def multiTestMetricMap2 = ["pay_amount": TypedValue.of(3000.00)]
        def vo = MetricAggregationVO.builder()
                .meta([:])
                .rows([
                        AggregationVO.builder()
                                .dimensionMap(multiTestDimMap1)
                                .valueMap(multiTestMetricMap1)
                                .build(),
                        AggregationVO.builder()
                                .dimensionMap(multiTestDimMap2)
                                .valueMap(multiTestMetricMap2)
                                .build()
                ])
                .build()

        metricAggregationStructMapper.qoToDto(_ as MetricAggregationQO) >> dto
        metricAggregationService.queryAggregation(_ as MetricAggregationDTO) >> result
        metricAggregationStructMapper.modelToVo(_ as MetricAggregation) >> vo

        when:
        def response = mockMvc.perform(post("/api/v1/m_agg")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggregationQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.rows.length()').value(2))
                .andExpect(jsonPath('$.data.rows[0].pay_amount').value(5000.0))
                .andExpect(jsonPath('$.data.rows[1].pay_amount').value(3000.0))
    }

    def "test query aggregation validation error missing metric codes"() {
        given:
        def aggregationQO = MetricAggregationQO.builder()
                .timeRange(MetricAggregationQO.TimeRangeQO.builder()
                        .start("2024-05-20 00:00:00")
                        .stop("2024-05-21 00:00:00")
                        .build())
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/m_agg")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggregationQO)))

        then:
        response.andExpect(status().isBadRequest())
    }

    def "test query aggregation validation error missing time range"() {
        given:
        def aggregationQO = MetricAggregationQO.builder()
                .metricCodes(["pay_amount"])
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/m_agg")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggregationQO)))

        then:
        response.andExpect(status().isBadRequest())
    }

    def "test query aggregation validation error missing start time"() {
        given:
        def aggregationQO = MetricAggregationQO.builder()
                .metricCodes(["pay_amount"])
                .timeRange(MetricAggregationQO.TimeRangeQO.builder()
                        .stop("2024-05-21 00:00:00")
                        .build())
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/m_agg")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aggregationQO)))

        then:
        response.andExpect(status().isBadRequest())
    }
}

