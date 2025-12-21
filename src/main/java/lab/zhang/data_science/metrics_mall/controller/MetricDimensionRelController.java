package lab.zhang.data_science.metrics_mall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDimensionRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionRelDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricDimensionRelQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricDimensionRelVO;
import lab.zhang.data_science.metrics_mall.service.MetricDimensionRelService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricDimensionRelStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Metric Dimension Relation controller.
 *
 * @author Rongjin Zhang
 */
@Tag(name = "MetricDimensionRel", description = "Metric Dimension Relation management APIs")
@RestController
@RequiredArgsConstructor
@Slf4j
public class MetricDimensionRelController extends BaseV1Controller {

    @Autowired
    private MetricDimensionRelService metricDimensionRelService;

    @Autowired
    private MetricDimensionRelStructMapper metricDimensionRelStructMapper;


    /**
     * Get metric dimension relation by metric meta id and dimension id.
     *
     * @param metricMetaId metric meta id
     * @param dimensionId  dimension id
     * @return metric dimension relation response
     */
    @Operation(summary = "Get metric dimension relation",
            description = "Get metric dimension relation by metric meta id and dimension id")
    @GetMapping("/metric_dimension_rels")
    public ApiResponse<MetricDimensionRelVO> get(
            @RequestParam Long metricMetaId,
            @RequestParam Long dimensionId) {
        log.info("[metric_dimension_rel] get, param: metricMetaId={}, dimensionId={}",
                metricMetaId, dimensionId);
        MetricDimensionRelDAO dao = metricDimensionRelService.get(metricMetaId, dimensionId);
        if (dao == null) {
            return ApiResponse.success(null);
        }
        MetricDimensionRelVO vo = metricDimensionRelStructMapper.daoToVo(dao);
        return ApiResponse.success(vo);
    }

    /**
     * List metric dimension relations by metric meta id.
     *
     * @param metricMetaId metric meta id
     * @return metric dimension relation list response
     */
    @Operation(summary = "List metric dimension relations by metric",
            description = "List metric dimension relations by metric meta id")
    @GetMapping("/metric_dimension_rels/metric/{metricMetaId}")
    public ApiResponse<List<MetricDimensionRelVO>> listByMetricMetaId(@PathVariable Long metricMetaId) {
        log.info("[metric_dimension_rel] listByMetricMetaId, param: metricMetaId={}", metricMetaId);
        List<MetricDimensionRelDAO> daoList = metricDimensionRelService.listByMetricMetaId(metricMetaId);
        List<MetricDimensionRelVO> voList = daoList.stream()
                .map(metricDimensionRelStructMapper::daoToVo)
                .collect(Collectors.toList());
        return ApiResponse.success(voList);
    }

    /**
     * List metric dimension relations by dimension id.
     *
     * @param dimensionId dimension id
     * @return metric dimension relation list response
     */
    @Operation(summary = "List metric dimension relations by dimension",
            description = "List metric dimension relations by dimension id")
    @GetMapping("/metric_dimension_rels/dimension/{dimensionId}")
    public ApiResponse<List<MetricDimensionRelVO>> listByDimensionId(@PathVariable Long dimensionId) {
        log.info("[metric_dimension_rel] listByDimensionId, param: dimensionId={}", dimensionId);
        List<MetricDimensionRelDAO> daoList = metricDimensionRelService.listByDimensionId(dimensionId);
        List<MetricDimensionRelVO> voList = daoList.stream()
                .map(metricDimensionRelStructMapper::daoToVo)
                .collect(Collectors.toList());
        return ApiResponse.success(voList);
    }

    /**
     * List all metric dimension relations.
     *
     * @return metric dimension relation list response
     */
    @Operation(summary = "List all metric dimension relations",
            description = "List all metric dimension relations")
    @GetMapping("/metric_dimension_rels/list")
    public ApiResponse<List<MetricDimensionRelVO>> list() {
        log.info("[metric_dimension_rel] list");
        List<MetricDimensionRelDAO> daoList = metricDimensionRelService.list();
        List<MetricDimensionRelVO> voList = metricDimensionRelStructMapper.daoToVoBatch(daoList);
        return ApiResponse.success(voList);
    }

    /**
     * Create metric dimension relation.
     *
     * @param qo metric dimension relation query object
     * @return success response
     */
    @Operation(summary = "Create metric dimension relation",
            description = "Associate a dimension with a metric. One metric_meta and one dimension can only have one relation. " +
                    "Either metricMetaId/dimensionId or metricCode/dimensionCode can be provided.")
    @PostMapping("/metric_dimension_rels")
    public ApiResponse<Boolean> create(@Valid @RequestBody MetricDimensionRelQO qo) {
        log.info("[metric_dimension_rel] create, param: metricCode={}, dimensionCode={}, isHot={}",
                qo.getMetricCode(), qo.getDimensionCode(), qo.getIsHot());

        MetricDimensionRelDTO dto = metricDimensionRelStructMapper.qoToDto(qo);

        // query
        boolean result = metricDimensionRelService.create(dto);

        return ApiResponse.success(result);
    }

    /**
     * Update metric dimension relation.
     *
     * @param qo metric dimension relation query object
     * @return success response
     */
    @Operation(summary = "Update metric dimension relation",
            description = "Update an existing metric dimension relation. " +
                    "Either metricMetaId/dimensionId or metricCode/dimensionCode can be provided.")
    @PutMapping("/metric_dimension_rels")
    public ApiResponse<Boolean> update(@Valid @RequestBody MetricDimensionRelQO qo) {
        log.info("[metric_dimension_rel] update, param: metricCode={}, dimensionCode={}, isHot={}",
                qo.getMetricCode(), qo.getDimensionCode(), qo.getIsHot());

        MetricDimensionRelDTO dto = metricDimensionRelStructMapper.qoToDto(qo);

        // query
        boolean result = metricDimensionRelService.update(dto);

        return ApiResponse.success(result);
    }

    /**
     * Delete metric dimension relation by metric meta id and dimension id.
     *
     * @param metricMetaId metric meta id
     * @param dimensionId  dimension id
     * @return success response
     */
    @Operation(summary = "Delete metric dimension relation",
            description = "Delete metric dimension relation by metric meta id and dimension id")
    @DeleteMapping("/metric_dimension_rels")
    public ApiResponse<Boolean> delete(
            @RequestParam Long metricMetaId,
            @RequestParam Long dimensionId) {
        log.info("[metric_dimension_rel] delete, param: metricMetaId={}, dimensionId={}",
                metricMetaId, dimensionId);
        boolean result = metricDimensionRelService.delete(metricMetaId, dimensionId);
        return ApiResponse.success(result);
    }
}

