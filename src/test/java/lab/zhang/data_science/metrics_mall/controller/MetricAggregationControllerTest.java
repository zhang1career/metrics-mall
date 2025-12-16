package lab.zhang.data_science.metrics_mall.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.zhang.data_science.metrics_mall.controller.v1.MetricAggregationController;
import lab.zhang.data_science.metrics_mall.model.Metric;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricAggregationDTO;
import lab.zhang.data_science.metrics_mall.model.MetricAggregation;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricAggregationQO;
import lab.zhang.data_science.metrics_mall.service.MetricService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Functional test for MetricAggregationController.
 *
 * @author Rongjin Zhang
 * 
 */
@WebMvcTest(MetricAggregationController.class)
class MetricAggregationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MetricService metricService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String API_KEY = "test-api-key";
    
    @BeforeEach
    void setUp() {
        // Setup common test data
    }
    
    @Test
    void testQueryAggregation_Success() throws Exception {
        // Given
        MetricAggregationQO aggregationQO = MetricAggregationQO.builder()
                .metricCodes(Arrays.asList("pay_amount", "pay_user_cnt"))
                .timeRange(MetricAggregationQO.TimeRangeQO.builder()
                        .start("2024-05-20 00:00:00")
                        .stop("2024-05-21 00:00:00")
                        .build())
                .interval("1h")
                .groupBy(Arrays.asList("city", "os"))
                .filters(Arrays.asList(
                        MetricAggregationQO.FieldConditionQO.builder()
                                .field("channel")
                                .op("=")
                                .value("tiktok")
                                .build()
                ))
                .orderBy(MetricAggregationQO.OrderByQO.builder()
                        .field("pay_amount")
                        .sort("desc")
                        .build())
                .limit(100)
                .build();
        
        Map<String, Metric> meta = new HashMap<>();
        meta.put("pay_amount", new Metric("支付金额", "CNY", 2));
        meta.put("pay_user_cnt", new Metric("支付人数", "人", 0));
        
        Map<String, String> dims = new HashMap<>();
        dims.put("city", "Beijing");
        dims.put("os", "iOS");
        
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("pay_amount", 5000.00);
        metrics.put("pay_user_cnt", 200.0);
        
        MetricAggregation.AggregationRow row =
                new MetricAggregation.AggregationRow("2024-05-20 10:00:00", dims, metrics);
        
        MetricAggregation result =
                new MetricAggregation(meta, Arrays.asList(row));
        
        when(metricService.queryAggregation(any(MetricAggregationDTO.class))).thenReturn(result);
        
        // When & Then
        mockMvc.perform(post("/api/v1/m_agg")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aggregationQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.meta.pay_amount.name").value("支付金额"))
                .andExpect(jsonPath("$.data.meta.pay_amount.unit").value("CNY"))
                .andExpect(jsonPath("$.data.meta.pay_amount.precision").value(2))
                .andExpect(jsonPath("$.data.rows[0].bucketTime").value("2024-05-20 10:00:00"))
                .andExpect(jsonPath("$.data.rows[0].dims.city").value("Beijing"))
                .andExpect(jsonPath("$.data.rows[0].metrics.pay_amount").value(5000.00));
    }
    
    @Test
    void testQueryAggregation_MultipleRows() throws Exception {
        // Given
        MetricAggregationQO aggregationQO = MetricAggregationQO.builder()
                .metricCodes(Arrays.asList("pay_amount"))
                .timeRange(MetricAggregationQO.TimeRangeQO.builder()
                        .start("2024-05-20 00:00:00")
                        .stop("2024-05-21 00:00:00")
                        .build())
                .interval("1h")
                .build();
        
        Map<String, Metric> meta = new HashMap<>();
        meta.put("pay_amount", new Metric("支付金额", "CNY", 2));
        
        Map<String, String> dims1 = new HashMap<>();
        dims1.put("city", "Beijing");
        
        Map<String, Double> metrics1 = new HashMap<>();
        metrics1.put("pay_amount", 5000.00);
        
        Map<String, String> dims2 = new HashMap<>();
        dims2.put("city", "Shanghai");
        
        Map<String, Double> metrics2 = new HashMap<>();
        metrics2.put("pay_amount", 3000.00);
        
        MetricAggregation.AggregationRow row1 =
                new MetricAggregation.AggregationRow("2024-05-20 10:00:00", dims1, metrics1);
        MetricAggregation.AggregationRow row2 =
                new MetricAggregation.AggregationRow("2024-05-20 11:00:00", dims2, metrics2);
        
        MetricAggregation result =
                new MetricAggregation(meta, Arrays.asList(row1, row2));
        
        when(metricService.queryAggregation(any(MetricAggregationDTO.class))).thenReturn(result);
        
        // When & Then
        mockMvc.perform(post("/api/v1/m_agg")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aggregationQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.rows.length()").value(2))
                .andExpect(jsonPath("$.data.rows[0].metrics.pay_amount").value(5000.00))
                .andExpect(jsonPath("$.data.rows[1].metrics.pay_amount").value(3000.00));
    }
    
    @Test
    void testQueryAggregation_ValidationError_MissingMetricCodes() throws Exception {
        // Given
        MetricAggregationQO aggregationQO = MetricAggregationQO.builder()
                .timeRange(MetricAggregationQO.TimeRangeQO.builder()
                        .start("2024-05-20 00:00:00")
                        .stop("2024-05-21 00:00:00")
                        .build())
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/m_agg")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aggregationQO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testQueryAggregation_ValidationError_MissingTimeRange() throws Exception {
        // Given
        MetricAggregationQO aggregationQO = MetricAggregationQO.builder()
                .metricCodes(Arrays.asList("pay_amount"))
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/m_agg")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aggregationQO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testQueryAggregation_ValidationError_MissingStartTime() throws Exception {
        // Given
        MetricAggregationQO aggregationQO = MetricAggregationQO.builder()
                .metricCodes(Arrays.asList("pay_amount"))
                .timeRange(MetricAggregationQO.TimeRangeQO.builder()
                        .stop("2024-05-21 00:00:00")
                        .build())
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/m_agg")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aggregationQO)))
                .andExpect(status().isBadRequest());
    }
}

