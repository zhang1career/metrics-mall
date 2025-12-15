package lab.zhang.data_science.metrics_mall.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.zhang.data_science.metrics_mall.controller.v1.EventController;
import lab.zhang.data_science.metrics_mall.model.EventAcceptanceResult;
import lab.zhang.data_science.metrics_mall.pojo.qo.EventBatchQO;
import lab.zhang.data_science.metrics_mall.pojo.qo.EventQO;
import lab.zhang.data_science.metrics_mall.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Functional test for EventController.
 *
 * @author Rongjin Zhang
 * 
 */
@WebMvcTest(EventController.class)
class EventDTOControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private EventService eventService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String API_KEY = "test-api-key";
    
    @BeforeEach
    void setUp() {
        // Setup common test data
    }
    
    @Test
    void testReport_Success() throws Exception {
        // Given
        EventBatchQO eventBatchQO = EventBatchQO.builder()
                .events(Arrays.asList(
                        EventQO.builder()
                                .metricName("user_credit_score")
                                .timestamp(1701234567890L)
                                .dim(createDimMap("user_id", "12345", "region", "us-west", "device", "ios"))
                                .value(850.0)
                                .build()
                ))
                .build();
        
        EventAcceptanceResult result =
                new EventAcceptanceResult(1, 0);
        
        when(eventService.processEventBatch(anyList())).thenReturn(result);
        
        // When & Then
        mockMvc.perform(post("/api/v1/events")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventBatchQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accepted").value(1))
                .andExpect(jsonPath("$.data.rejected").value(0));
    }
    
    @Test
    void testReportEvents_Multiple() throws Exception {
        // Given
        EventBatchQO eventBatchQO = EventBatchQO.builder()
                .events(Arrays.asList(
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
                ))
                .build();
        
        EventAcceptanceResult result =
                new EventAcceptanceResult(2, 0);
        
        when(eventService.processEventBatch(anyList())).thenReturn(result);
        
        // When & Then
        mockMvc.perform(post("/api/v1/events")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventBatchQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accepted").value(2))
                .andExpect(jsonPath("$.data.rejected").value(0));
    }
    
    @Test
    void testReport_ValidationError_MissingMetricName() throws Exception {
        // Given
        EventBatchQO eventBatchQO = EventBatchQO.builder()
                .events(Arrays.asList(
                        EventQO.builder()
                                .timestamp(1701234567890L)
                                .value(850.0)
                                .build()
                ))
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/events")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventBatchQO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testReport_ValidationError_MissingTimestamp() throws Exception {
        // Given
        EventBatchQO eventBatchQO = EventBatchQO.builder()
                .events(Arrays.asList(
                        EventQO.builder()
                                .metricName("user_credit_score")
                                .value(850.0)
                                .build()
                ))
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/events")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventBatchQO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testReport_ValidationError_MissingValue() throws Exception {
        // Given
        EventBatchQO eventBatchQO = EventBatchQO.builder()
                .events(Arrays.asList(
                        EventQO.builder()
                                .metricName("user_credit_score")
                                .timestamp(1701234567890L)
                                .build()
                ))
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/events")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventBatchQO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testReportEvents_ValidationError_Empty() throws Exception {
        // Given
        EventBatchQO eventBatchQO = EventBatchQO.builder()
                .events(List.of())
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/events")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventBatchQO)))
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

