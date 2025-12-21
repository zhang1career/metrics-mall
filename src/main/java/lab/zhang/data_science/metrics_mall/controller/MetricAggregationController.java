package lab.zhang.data_science.metrics_mall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.model.MetricAggregation;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricAggregationDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricAggregationQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricAggregationVO;
import lab.zhang.data_science.metrics_mall.service.MetricAggregationService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricAggregationStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Metric aggregation controller for handling GMA queries.
 *
 * @author Rongjin Zhang
 */
@Slf4j
@Tag(name = "Metric Aggregation", description = "GMA (Get Metric Aggregate) APIs")
@RestController
@RequiredArgsConstructor
public class MetricAggregationController extends BaseV1Controller {

    @Autowired
    private MetricAggregationService metricAggregationService;

    @Autowired
    private MetricAggregationStructMapper metricAggregationStructMapper;

    /**
     * Query metric aggregation.
     *
     * @param apiKey API key from request header
     * @param qo     metric aggregation query object
     * @return metric aggregation response
     */
    @Operation(summary = "Get metric aggregation", description = "Query historical metric aggregation results (GMA)")
    @PostMapping("/m_agg")
    public ApiResponse<MetricAggregationVO> queryAggregation(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestHeader(value = "X-Request-ID", required = false) String requestIdStr,
            @RequestParam(value = "traceId", required = false) String traceIdStr,
            @Valid @RequestBody MetricAggregationQO qo) {
        log.info("[agg] param: qo={}", qo);

        // Convert QO to Model
        MetricAggregationDTO dto = metricAggregationStructMapper.qoToDto(qo);

        // Call service layer
        MetricAggregation model = metricAggregationService.queryAggregation(dto);

        // Convert Model to DTO
        MetricAggregationVO vo = metricAggregationStructMapper.modelToVo(model);

        return ApiResponse.success(vo);
    }
}

