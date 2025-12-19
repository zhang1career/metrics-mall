package lab.zhang.data_science.metrics_mall.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricVO;
import lab.zhang.data_science.metrics_mall.service.MetricService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Metric controller for CRUD operations.
 *
 * @author Rongjin Zhang
 * @date 2025-12-19
 */
@Tag(name = "Metric", description = "Metric management APIs")
@RestController
@RequiredArgsConstructor
@Slf4j
public class MetricController extends BaseV1Controller {

    @Autowired
    private MetricService metricService;

    @Autowired
    private MetricStructMapper metricStructMapper;

    /**
     * Get metric by id.
     *
     * @param id metric id
     * @return metric response
     */
    @Operation(summary = "Get metric", description = "Get metric by id")
    @GetMapping("/metrics/{id}")
    public ApiResponse<MetricVO> get(@PathVariable Long id) {
        log.info("[metric] get, id: {}", id);
        PrimeMetric model = metricService.get(id);
        return ApiResponse.success(metricStructMapper.modelToVo(model));
    }

    /**
     * Get metric by code.
     *
     * @param code metric code
     * @return metric response
     */
    @Operation(summary = "Get metric by code", description = "Get metric by code")
    @GetMapping("/metrics")
    public ApiResponse<MetricVO> getByCode(@RequestParam String code) {
        log.info("[metric] getByCode, code: {}", code);
        PrimeMetric model = metricService.getPrimeMetricByCode(code);
        return ApiResponse.success(metricStructMapper.modelToVo(model));
    }

    /**
     * List metrics by query criteria.
     *
     * @param qo metric query object
     * @return metric list response
     */
    @Operation(summary = "List metrics", description = "List metrics by query criteria")
    @GetMapping("/metrics/list")
    public ApiResponse<List<MetricVO>> list(MetricQO qo) {
        log.info("[metric] list, param: {}", qo);
        PrimeMetric queryModel = metricStructMapper.qoToPrimeModel(qo);
        List<PrimeMetric> modelList = metricService.list(queryModel);
        List<MetricVO> voList = modelList.stream()
                .map(metricStructMapper::modelToVo)
                .collect(java.util.stream.Collectors.toList());
        return ApiResponse.success(voList);
    }

    /**
     * Insert a new metric.
     *
     * @param qo metric query object
     * @return success response
     */
    @Operation(summary = "Insert metric", description = "Insert a new metric")
    @PostMapping("/metrics")
    public ApiResponse<Boolean> insert(@Valid @RequestBody MetricQO qo) {
        log.info("[metric] insert, param: {}", qo);
        PrimeMetric model = metricStructMapper.qoToPrimeModel(qo);
        return ApiResponse.success(metricService.insert(model));
    }

    /**
     * Update an existing metric.
     *
     * @param id primary key id
     * @param qo metric query object
     * @return success response
     */
    @Operation(summary = "Update metric", description = "Update an existing metric")
    @PutMapping("/metrics/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody MetricQO qo) {
        log.info("[metric] update, id: {}, param: {}", id, qo);
        PrimeMetric model = metricStructMapper.qoToPrimeModel(qo);
        model.setId(id);
        return ApiResponse.success(metricService.update(model));
    }

    /**
     * Delete a metric by id.
     *
     * @param id metric id
     * @return success response
     */
    @Operation(summary = "Delete metric", description = "Delete a metric by id")
    @DeleteMapping("/metrics/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        log.info("[metric] delete, id: {}", id);
        return ApiResponse.success(metricService.delete(id));
    }
}

