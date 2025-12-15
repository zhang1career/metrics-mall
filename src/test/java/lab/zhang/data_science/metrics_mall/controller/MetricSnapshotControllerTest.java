package lab.zhang.data_science.metrics_mall.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.zhang.data_science.metrics_mall.controller.v1.MetricSnapshotController;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO;
import lab.zhang.data_science.metrics_mall.model.MetricSnapshot;
import lab.zhang.data_science.metrics_mall.pojo.qo.VersionedMetricDimensionQO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricSnapshotQO;
import lab.zhang.data_science.metrics_mall.service.MetricSnapshotService;
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
 * Functional test for MetricSnapshotController.
 *
 * @author Rongjin Zhang
 * 
 */
@WebMvcTest(MetricSnapshotController.class)
class MetricSnapshotControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MetricSnapshotService metricSnapshotService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String API_KEY = "test-api-key";
    
    @BeforeEach
    void setUp() {
        // Setup common test data
    }
    
    @Test
    void testQuerySnapshot_Success() throws Exception {
        // Given
        MetricSnapshotQO snapshotQO = MetricSnapshotQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics(Arrays.asList(
                        VersionedMetricDimensionQO.builder()
                                .code("coin_balance")
                                .dims(createDimMap("city", "Beijing"))
                                .build()
                ))
                .snapshotTs(0L)
                .build();
        
        Map<String, Double> values = new HashMap<>();
        values.put("coin_balance", 1050.5);
        
        Map<String, Long> ts = new HashMap<>();
        ts.put("coin_balance", 1715000001000L);
        
        MetricSnapshot result =
                new MetricSnapshot("user", 12345678L, values, ts);
        
        when(metricSnapshotService.querySnapshot(any(MetricSnapshotDTO.class))).thenReturn(result);
        
        // When & Then
        mockMvc.perform(post("/api/v1/m_snap")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(snapshotQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.ec").value("user"))
                .andExpect(jsonPath("$.data.eid").value(12345678))
                .andExpect(jsonPath("$.data.values.coin_balance").value(1050.5))
                .andExpect(jsonPath("$.data.ts.coin_balance").value(1715000001000L));
    }
    
    @Test
    void testQuerySnapshot_MultipleMetrics() throws Exception {
        // Given
        MetricSnapshotQO snapshotQO = MetricSnapshotQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics(Arrays.asList(
                        VersionedMetricDimensionQO.builder()
                                .code("coin_balance")
                                .dims(createDimMap("city", "Beijing"))
                                .build(),
                        VersionedMetricDimensionQO.builder()
                                .code("risk_score")
                                .dims(createDimMap("os", "ios"))
                                .build()
                ))
                .snapshotTs(0L)
                .build();
        
        Map<String, Double> values = new HashMap<>();
        values.put("coin_balance", 1050.5);
        values.put("risk_score", 0.1);
        
        Map<String, Long> ts = new HashMap<>();
        ts.put("coin_balance", 1715000001000L);
        ts.put("risk_score", 1715000005000L);
        
        MetricSnapshot result =
                new MetricSnapshot("user", 12345678L, values, ts);
        
        when(metricSnapshotService.querySnapshot(any(MetricSnapshotDTO.class))).thenReturn(result);
        
        // When & Then
        mockMvc.perform(post("/api/v1/m_snap")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(snapshotQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.values.coin_balance").value(1050.5))
                .andExpect(jsonPath("$.data.values.risk_score").value(0.1));
    }
    
    @Test
    void testQuerySnapshot_WithoutTimestamp() throws Exception {
        // Given
        MetricSnapshotQO snapshotQO = MetricSnapshotQO.builder()
                .ec("user")
                .eid(12345678L)
                .metrics(Arrays.asList(
                        VersionedMetricDimensionQO.builder()
                                .code("coin_balance")
                                .build()
                ))
                .build();
        
        Map<String, Double> values = new HashMap<>();
        values.put("coin_balance", 1050.5);
        
        MetricSnapshot result =
                new MetricSnapshot("user", 12345678L, values, null);
        
        when(metricSnapshotService.querySnapshot(any(MetricSnapshotDTO.class))).thenReturn(result);
        
        // When & Then
        mockMvc.perform(post("/api/v1/m_snap")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(snapshotQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.values.coin_balance").value(1050.5));
    }
    
    @Test
    void testQuerySnapshot_ValidationError_MissingEntityCode() throws Exception {
        // Given
        MetricSnapshotQO snapshotQO = MetricSnapshotQO.builder()
                .eid(12345678L)
                .metrics(Arrays.asList(
                        VersionedMetricDimensionQO.builder()
                                .code("coin_balance")
                                .build()
                ))
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/m_snap")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(snapshotQO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testQuerySnapshot_ValidationError_MissingEntityId() throws Exception {
        // Given
        MetricSnapshotQO snapshotQO = MetricSnapshotQO.builder()
                .ec("user")
                .metrics(Arrays.asList(
                        VersionedMetricDimensionQO.builder()
                                .code("coin_balance")
                                .build()
                ))
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/m_snap")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(snapshotQO)))
                .andExpect(status().isBadRequest());
    }
    
    /**
     * Create dimension map for testing.
     *
     * @param keyValues key-value pairs
     * @return dimension map
     */
    private Map<String, String> createDimMap(String... keyValues) {
        Map<String, String> dimMap = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            dimMap.put(keyValues[i], keyValues[i + 1]);
        }
        return dimMap;
    }
}

