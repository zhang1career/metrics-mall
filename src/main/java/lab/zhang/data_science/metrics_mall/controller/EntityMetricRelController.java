package lab.zhang.data_science.metrics_mall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.pojo.dao.x.EntityMetricRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.EntityMetricRelDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.EntityMetricRelQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.EntityMetricRelVO;
import lab.zhang.data_science.metrics_mall.service.EntityMetricRelService;
import lab.zhang.data_science.metrics_mall.struct_mapper.EntityMetricRelStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity Metric Relation controller.
 *
 * @author Rongjin Zhang
 */
@Tag(name = "EntityMetricRel", description = "Entity Metric Relation management APIs")
@RestController
@RequiredArgsConstructor
@Slf4j
public class EntityMetricRelController extends BaseV1Controller {

    @Autowired
    private EntityMetricRelService entityMetricRelService;

    @Autowired
    private EntityMetricRelStructMapper entityMetricRelStructMapper;


    /**
     * Create entity metric relation.
     *
     * @param qo entity metric relation query object
     * @return success response
     */
    @Operation(summary = "Create entity metric relation",
            description = "Associate a metric with an entity. One entity_meta and one metric_meta can only have one relation")
    @PostMapping("/entity_metric_rels")
    public ApiResponse<Boolean> create(@Valid @RequestBody EntityMetricRelQO qo) {
        log.info("[entity_metric_rel] create, param: entityCode={}, metricCode={}, alias={}",
                qo.getEntityCode(), qo.getMetricCode(), qo.getAlias());

        EntityMetricRelDTO dto = entityMetricRelStructMapper.qoToDto(qo);

        // query
        boolean result = entityMetricRelService.create(dto);

        return ApiResponse.success(result);
    }

    /**
     * Get entity metric relation by entity meta id and metric meta id.
     *
     * @param entityMetaId entity meta id
     * @param metricMetaId metric meta id
     * @return entity metric relation response
     */
    @Operation(summary = "Get entity metric relation",
            description = "Get entity metric relation by entity meta id and metric meta id")
    @GetMapping("/entity_metric_rels")
    public ApiResponse<EntityMetricRelVO> get(
            @RequestParam Long entityMetaId,
            @RequestParam Long metricMetaId) {
        log.info("[entity_metric_rel] get, param: entityMetaId={}, metricMetaId={}",
                entityMetaId, metricMetaId);
        EntityMetricRelDAO dao = entityMetricRelService.get(entityMetaId, metricMetaId);
        if (dao == null) {
            return ApiResponse.success(null);
        }
        EntityMetricRelVO vo = entityMetricRelStructMapper.daoToVo(dao);
        return ApiResponse.success(vo);
    }

    /**
     * List entity metric relations by entity meta id.
     *
     * @param entityMetaId entity meta id
     * @return entity metric relation list response
     */
    @Operation(summary = "List entity metric relations by entity",
            description = "List entity metric relations by entity meta id")
    @GetMapping("/entity_metric_rels/entity/{entityMetaId}")
    public ApiResponse<List<EntityMetricRelVO>> listByEntityMetaId(@PathVariable Long entityMetaId) {
        log.info("[entity_metric_rel] listByEntityMetaId, param: entityMetaId={}", entityMetaId);
        List<EntityMetricRelDAO> daoList = entityMetricRelService.listByEntityMetaId(entityMetaId);
        List<EntityMetricRelVO> voList = daoList.stream()
                .map(entityMetricRelStructMapper::daoToVo)
                .collect(Collectors.toList());
        return ApiResponse.success(voList);
    }

    /**
     * List entity metric relations by metric meta id.
     *
     * @param metricMetaId metric meta id
     * @return entity metric relation list response
     */
    @Operation(summary = "List entity metric relations by metric",
            description = "List entity metric relations by metric meta id")
    @GetMapping("/entity_metric_rels/metric/{metricMetaId}")
    public ApiResponse<List<EntityMetricRelVO>> listByMetricMetaId(@PathVariable Long metricMetaId) {
        log.info("[entity_metric_rel] listByMetricMetaId, param: metricMetaId={}", metricMetaId);
        List<EntityMetricRelDAO> daoList = entityMetricRelService.listByMetricMetaId(metricMetaId);
        List<EntityMetricRelVO> voList = daoList.stream()
                .map(entityMetricRelStructMapper::daoToVo)
                .collect(Collectors.toList());
        return ApiResponse.success(voList);
    }

    /**
     * List all entity metric relations.
     *
     * @return entity metric relation list response
     */
    @Operation(summary = "List all entity metric relations",
            description = "List all entity metric relations")
    @GetMapping("/entity_metric_rels/list")
    public ApiResponse<List<EntityMetricRelVO>> list() {
        log.info("[entity_metric_rel] list");
        List<EntityMetricRelDAO> daoList = entityMetricRelService.list();
        List<EntityMetricRelVO> voList = entityMetricRelStructMapper.daoToVoBatch(daoList);
        return ApiResponse.success(voList);
    }

    /**
     * Update entity metric relation.
     *
     * @param qo entity metric relation query object
     * @return success response
     */
    @Operation(summary = "Update entity metric relation",
            description = "Update an existing entity metric relation.")
    @PutMapping("/entity_metric_rels")
    public ApiResponse<Boolean> update(@Valid @RequestBody EntityMetricRelQO qo) {
        log.info("[entity_metric_rel] update, param: entityCode={}, metricCode={}, alias={}",
                qo.getEntityCode(), qo.getMetricCode(), qo.getAlias());

        EntityMetricRelDTO dto = entityMetricRelStructMapper.qoToDto(qo);

        // query
        boolean result = entityMetricRelService.update(dto);

        return ApiResponse.success(result);
    }

    /**
     * Delete entity metric relation by entity meta id and metric meta id.
     *
     * @param entityMetaId entity meta id
     * @param metricMetaId metric meta id
     * @return success response
     */
    @Operation(summary = "Delete entity metric relation",
            description = "Delete entity metric relation by entity meta id and metric meta id")
    @DeleteMapping("/entity_metric_rels")
    public ApiResponse<Boolean> delete(
            @RequestParam Long entityMetaId,
            @RequestParam Long metricMetaId) {
        log.info("[entity_metric_rel] delete, param: entityMetaId={}, metricMetaId={}",
                entityMetaId, metricMetaId);
        boolean result = entityMetricRelService.delete(entityMetaId, metricMetaId);
        return ApiResponse.success(result);
    }
}

