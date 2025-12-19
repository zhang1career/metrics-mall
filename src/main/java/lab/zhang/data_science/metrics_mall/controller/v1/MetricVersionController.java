package lab.zhang.data_science.metrics_mall.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.model.MetricVersion;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricVersionQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricVersionVO;
import lab.zhang.data_science.metrics_mall.service.MetricVersionService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricVersionStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Metric version controller for CRUD operations.
 *
 * @author Rongjin Zhang
 * @date 2025-12-19
 */
@Tag(name = "MetricVersion", description = "Metric version management APIs")
@RestController
@RequiredArgsConstructor
@Slf4j
public class MetricVersionController extends BaseV1Controller {

    @Autowired
    private MetricVersionService metricVersionService;

    @Autowired
    private MetricVersionStructMapper metricVersionStructMapper;

    /**
     * Get metric version by id.
     *
     * @param id metric version id
     * @return metric version response
     */
    @Operation(summary = "Get metric version", description = "Get metric version by id")
    @GetMapping("/metric_versions/{id}")
    public ApiResponse<MetricVersionVO> get(@PathVariable Long id) {
        log.info("[metric_version] get, id: {}", id);
        MetricVersion model = metricVersionService.get(id);
        return ApiResponse.success(metricVersionStructMapper.modelToVo(model));
    }

    /**
     * List metric versions by query criteria.
     *
     * @param qo metric version query object
     * @return metric version list response
     */
    @Operation(summary = "List metric versions", description = "List metric versions by query criteria")
    @GetMapping("/metric_versions")
    public ApiResponse<List<MetricVersionVO>> list(MetricVersionQO qo) {
        log.info("[metric_version] list, param: {}", qo);
        MetricVersion queryModel = metricVersionStructMapper.qoToModel(qo);
        List<MetricVersion> modelList = metricVersionService.list(queryModel);
        return ApiResponse.success(metricVersionStructMapper.modelToVoBatch(modelList));
    }

    /**
     * Insert a new metric version.
     *
     * @param qo metric version query object
     * @return success response
     */
    @Operation(summary = "Insert metric version", description = "Insert a new metric version")
    @PostMapping("/metric_versions")
    public ApiResponse<Boolean> insert(@Valid @RequestBody MetricVersionQO qo) {
        log.info("[metric_version] insert, param: {}", qo);
        MetricVersion model = metricVersionStructMapper.qoToModel(qo);
        return ApiResponse.success(metricVersionService.insert(model));
    }

    /**
     * Update an existing metric version.
     *
     * @param id primary key id
     * @param qo metric version query object
     * @return success response
     */
    @Operation(summary = "Update metric version", description = "Update an existing metric version")
    @PutMapping("/metric_versions/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody MetricVersionQO qo) {
        log.info("[metric_version] update, id: {}, param: {}", id, qo);
        MetricVersion model = metricVersionStructMapper.qoToModel(qo);
        model.setId(id);
        return ApiResponse.success(metricVersionService.update(model));
    }

    /**
     * Delete a metric version by id.
     *
     * @param id metric version id
     * @return success response
     */
    @Operation(summary = "Delete metric version", description = "Delete a metric version by id")
    @DeleteMapping("/metric_versions/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        log.info("[metric_version] delete, id: {}", id);
        return ApiResponse.success(metricVersionService.delete(id));
    }
}

