package lab.zhang.data_science.metrics_mall.controller;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.model.Dimension;
import lab.zhang.data_science.metrics_mall.model.MetricDimensionGroupRel;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionGroupRelDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricDimensionGroupRelQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricDimensionGroupRelVO;
import lab.zhang.data_science.metrics_mall.service.DimensionService;
import lab.zhang.data_science.metrics_mall.service.MetricDimensionGroupRelService;
import lab.zhang.data_science.metrics_mall.service.MetricService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricDimensionGroupRelStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * YGroup controller for CRUD operations.
 *
 * @author Rongjin Zhang
 */
@Tag(name = "YGroup", description = "YGroup management APIs")
@RestController
@RequiredArgsConstructor
@Slf4j
public class MetricDimensionGroupRelController extends BaseV1Controller {

    @Autowired
    private MetricService metricService;

    @Autowired
    private DimensionService dimensionService;

    @Autowired
    private MetricDimensionGroupRelService metricDimensionGroupRelService;

    @Autowired
    private MetricDimensionGroupRelStructMapper metricDimensionGroupRelStructMapper;

    /**
     * List all yGroups.
     *
     * @return yGroup list response
     */
    @Operation(summary = "List yGroups", description = "List all yGroups")
    @GetMapping("/metric_dim_group_rels")
    public ApiResponse<List<MetricDimensionGroupRelVO>> list() {
        log.info("[y_group] list");
        List<MetricDimensionGroupRel> modelList = metricDimensionGroupRelService.list();
        return ApiResponse.success(metricDimensionGroupRelStructMapper.modelToVoBatch(modelList));
    }

    /**
     * List yGroups by metric id.
     *
     * @param metricId metric id
     * @return yGroup list response
     */
    @Operation(summary = "List yGroups by metric id", description = "List yGroups by metric id")
    @GetMapping("/metric_dim_group_rels/metric/{metricId}")
    public ApiResponse<List<MetricDimensionGroupRelVO>> listByMetricId(@PathVariable Long metricId) {
        log.info("[y_group] listByMetricId, param: metricId={}", metricId);
        List<MetricDimensionGroupRel> modelList = metricDimensionGroupRelService.listByMetricId(metricId);
        return ApiResponse.success(metricDimensionGroupRelStructMapper.modelToVoBatch(modelList));
    }

    /**
     * Get yGroup by id.
     *
     * @param id yGroup id
     * @return yGroup response
     */
    @Operation(summary = "Get yGroup", description = "Get yGroup by id")
    @GetMapping("/metric_dim_group_rels/{id}")
    public ApiResponse<MetricDimensionGroupRelVO> get(@PathVariable Long id) {
        log.info("[y_group] get, param: id={}", id);
        MetricDimensionGroupRel model = metricDimensionGroupRelService.get(id);
        return ApiResponse.success(metricDimensionGroupRelStructMapper.modelToVo(model));
    }

    /**
     * Count all yGroups.
     *
     * @return yGroup count response
     */
    @Operation(summary = "Count yGroups", description = "Count all yGroups")
    @GetMapping("/metric_dim_group_rels/count")
    public ApiResponse<Long> count() {
        log.info("[y_group] count");
        return ApiResponse.success(metricDimensionGroupRelService.count());
    }

    /**
     * Create a new yGroup.
     *
     * @param qo yGroup query object
     * @return success response
     */
    @Operation(summary = "Create yGroup", description = "Create a new yGroup")
    @PostMapping("/metric_dim_group_rels")
    public ApiResponse<Boolean> create(@Valid @RequestBody MetricDimensionGroupRelQO qo) {
        log.info("[y_group] create, param: {}", qo);

        // validate metric code
        MetricMetaDAO metricMetaDAO = metricService.getMetricMetaDaoByCode(qo.getMetricCode());
        if (metricMetaDAO == null) {
            throw new IllegalArgumentException("[y_group] creating failed, metric meta not found by code: " + qo.getMetricCode());
        }
        Long metricId = metricMetaDAO.getId();
        if (metricId == null) {
            throw new IllegalArgumentException("[y_group] creating failed, metric meta id is null by code: " + qo.getMetricCode());
        }
        qo.setMetricId(metricId);

        // validate dimension codes
        String dimensionCodesStr = qo.getDimensionCodes();
        if (StrUtil.isBlank(dimensionCodesStr)) {
            throw new IllegalArgumentException("[y_group] creating failed, dimensionCodes cannot be blank");
        }
        List<String> dimensionCodeList = metricDimensionGroupRelStructMapper.explode(dimensionCodesStr);
        Map<String, Dimension> dimensionCodeMap = dimensionService.mapByCodeBatch(dimensionCodeList);
        if (dimensionCodeMap == null) {
            throw new IllegalArgumentException("[y_group] creating failed, dimensionList is null by codes: " + dimensionCodesStr);
        }
        List<String> dimensionIdList = dimensionCodeList.stream()
                .map(code -> dimensionCodeMap.get(code).getId().toString())
                .toList();
        String dimensionIdsStr = metricDimensionGroupRelStructMapper.implode(dimensionIdList);
        qo.setDimensionIds(dimensionIdsStr);

        MetricDimensionGroupRelDTO dto = metricDimensionGroupRelStructMapper.qoToDto(qo);
        return ApiResponse.success(metricDimensionGroupRelService.insert(dto));
    }

    /**
     * Update an existing yGroup.
     *
     * @param qo yGroup query object
     * @return success response
     * todo: 需要再设计一下更新逻辑
     */
    @Operation(summary = "Update yGroup", description = "Update an existing yGroup")
    @PutMapping("/metric_dim_group_rels")
    public ApiResponse<Boolean> update(@Valid @RequestBody MetricDimensionGroupRelQO qo) {
        log.info("[y_group] update, param: {}", qo);
        MetricDimensionGroupRelDTO dto = metricDimensionGroupRelStructMapper.qoToDto(qo);
        return ApiResponse.success(metricDimensionGroupRelService.update(dto));
    }

    /**
     * Delete a yGroup by id.
     *
     * @param id yGroup id
     * @return success response
     */
    @Operation(summary = "Delete yGroup", description = "Delete a yGroup by id")
    @DeleteMapping("/metric_dim_group_rels/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        log.info("[y_group] delete, id: {}", id);
        return ApiResponse.success(metricDimensionGroupRelService.delete(id));
    }
}

