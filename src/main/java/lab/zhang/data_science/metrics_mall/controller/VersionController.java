package lab.zhang.data_science.metrics_mall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.model.MetricVersion;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricVersionDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricVersionQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricVersionVO;
import lab.zhang.data_science.metrics_mall.service.MetricService;
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
public class VersionController extends BaseV1Controller {

    @Autowired
    private MetricService metricService;

    @Autowired
    private MetricVersionService metricVersionService;

    @Autowired
    private MetricVersionStructMapper metricVersionStructMapper;


    /**
     * List metric versions by query criteria.
     *
     * @param metricId metric id
     * @return metric version list response
     */
    @Operation(summary = "List metric versions", description = "List metric versions by query criteria")
    @GetMapping("/metrics/{metricId}/versions")
    public ApiResponse<List<MetricVersionVO>> listByMetricId(@NotNull @PathVariable Long metricId) {
        log.info("[metric_version] list, param: metricId={}", metricId);

        List<MetricVersion> modelList = metricVersionService.listByMetricId(metricId);
        List<MetricVersionVO> resultList = metricVersionStructMapper.modelToVoBatch(modelList);

        return ApiResponse.success(resultList);
    }

    /**
     * Get a metric version by query criteria.
     *
     * @param metricId metric id
     * @param version  metric version
     * @return metric version response
     */
    @Operation(summary = "Get a metric version", description = "Get a metric version by query criteria")
    @GetMapping("/metrics/{metricId}/versions/{version}")
    public ApiResponse<MetricVersionVO> getByMetricIdAndVersion(@NotNull @PathVariable Long metricId,
                                                                @NotNull @PathVariable Integer version) {
        log.info("[metric_version] get, param: metricId={}, version={}", metricId, version);
        MetricVersion model = metricVersionService.getByMetricIdAndVersion(metricId, version);
        return ApiResponse.success(metricVersionStructMapper.modelToVo(model));
    }

    /**
     * Create a new metric version.
     *
     * @param qo metric version query object
     * @return success response
     */
    @Operation(summary = "Create metric version", description = "Create a new metric version")
    @PostMapping("/metrics/{metricId}/versions")
    public ApiResponse<Boolean> create(@NotNull @PathVariable Long metricId,
                                       @Valid @RequestBody MetricVersionQO qo) {
        log.info("[metric_version] create param, metricId={}, qo={}", metricId, qo);
        // validate arguments
        MetricMetaDAO metricMetaDAO = metricService.getMetricMetaDaoById(metricId);
        if (metricMetaDAO == null) {
            throw new IllegalArgumentException("[metric_version] create failed, metric meta not found: metricId=" + metricId);
        }
        // prepare data
        if (qo.getMetricCode() == null) {
            qo.setMetricCode(metricMetaDAO.getCode());
        }

        MetricVersionDTO dto = metricVersionStructMapper.qoToDto(qo);
        // query
        boolean result = metricVersionService.create(dto);

        return ApiResponse.success(result);
    }

    /**
     * Update an existing metric version.
     *
     * @param qo metric version query object
     * @return success response
     */
    @Operation(summary = "Update metric version", description = "Update an existing metric version")
    @PutMapping("/metrics/{metricId}/versions")
    public ApiResponse<Boolean> update(@NotNull @PathVariable Long metricId,
                                       @Valid @RequestBody MetricVersionQO qo) {
        log.info("[metric_version] update, param: metricId={}, qo={}", metricId, qo);
        // validate arguments
        MetricMetaDAO metricMetaDAO = metricService.getMetricMetaDaoById(metricId);
        if (metricMetaDAO == null) {
            throw new IllegalArgumentException("[metric_version] update failed, metric meta not found: metricId=" + metricId);
        }
        // prepare data
        if (qo.getMetricCode() == null) {
            qo.setMetricCode(metricMetaDAO.getCode());
        }

        MetricVersionDTO dto = metricVersionStructMapper.qoToDto(qo);
        // query
        boolean result = metricVersionService.update(dto);

        return ApiResponse.success(result);
    }

    /**
     * Delete a metric version by id.
     *
     * @param metricId metric id
     * @param version  metric version
     * @return success response
     */
    @Operation(summary = "Delete metric version", description = "Delete a metric version by id")
    @DeleteMapping("/metrics/{metricId}/versions/{version}")
    public ApiResponse<Boolean> delete(@NotNull @PathVariable Long metricId,
                                       @NotNull @PathVariable Integer version) {
        log.info("[metric_version] delete, param: metricId={}, version={}", metricId, version);
        return ApiResponse.success(metricVersionService.deleteByMetricIdAndVersion(metricId, version));
    }
}

