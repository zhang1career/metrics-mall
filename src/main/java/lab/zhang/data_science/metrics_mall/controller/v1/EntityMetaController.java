package lab.zhang.data_science.metrics_mall.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.model.EntityMeta;
import lab.zhang.data_science.metrics_mall.pojo.qo.EntityMetaQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.EntityMetaVO;
import lab.zhang.data_science.metrics_mall.service.EntityMetaService;
import lab.zhang.data_science.metrics_mall.struct_mapper.EntityMetaStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Entity meta controller for CRUD operations.
 *
 * @author Rongjin Zhang
 */
@Tag(name = "EntityMeta", description = "Entity meta management APIs")
@RestController
@RequiredArgsConstructor
@Slf4j
public class EntityMetaController extends BaseV1Controller {

    @Autowired
    private EntityMetaService entityMetaService;

    @Autowired
    private EntityMetaStructMapper entityMetaStructMapper;

    /**
     * Get entity meta by id.
     *
     * @param id entity meta id
     * @return entity meta response
     */
    @Operation(summary = "Get entity meta", description = "Get entity meta by id")
    @GetMapping("/entity-metas/{id}")
    public ApiResponse<EntityMetaVO> get(@PathVariable Integer id) {
        log.info("[entity_meta] get, id: {}", id);
        EntityMeta model = entityMetaService.get(id);
        return ApiResponse.success(entityMetaStructMapper.modelToVo(model));
    }

    /**
     * Get entity meta by code.
     *
     * @param code entity meta code
     * @return entity meta response
     */
    @Operation(summary = "Get entity meta by code", description = "Get entity meta by code")
    @GetMapping("/entity-metas/code/{code}")
    public ApiResponse<EntityMetaVO> getByCode(@PathVariable String code) {
        log.info("[entity_meta] getByCode, code: {}", code);
        EntityMeta model = entityMetaService.getByCode(code);
        return ApiResponse.success(entityMetaStructMapper.modelToVo(model));
    }

    /**
     * List all entity metas.
     *
     * @return entity meta list response
     */
    @Operation(summary = "List entity metas", description = "List all entity metas")
    @GetMapping("/entity-metas")
    public ApiResponse<List<EntityMetaVO>> list() {
        log.info("[entity_meta] list");
        List<EntityMeta> modelList = entityMetaService.list();
        return ApiResponse.success(entityMetaStructMapper.modelToVoBatch(modelList));
    }

    /**
     * Count all entity metas.
     *
     * @return entity meta count response
     */
    @Operation(summary = "Count entity metas", description = "Count all entity metas")
    @GetMapping("/entity-metas/count")
    public ApiResponse<Long> count() {
        log.info("[entity_meta] count");
        return ApiResponse.success(entityMetaService.count());
    }

    /**
     * Insert a new entity meta.
     *
     * @param qo entity meta query object
     * @return success response
     */
    @Operation(summary = "Insert entity meta", description = "Insert a new entity meta")
    @PostMapping("/entity-metas")
    public ApiResponse<Boolean> insert(@Valid @RequestBody EntityMetaQO qo) {
        log.info("[entity_meta] insert, param: {}", qo);
        EntityMeta model = entityMetaStructMapper.qoToModel(qo);
        return ApiResponse.success(entityMetaService.insert(model));
    }

    /**
     * Update an existing entity meta.
     *
     * @param qo entity meta query object
     * @return success response
     */
    @Operation(summary = "Update entity meta", description = "Update an existing entity meta")
    @PutMapping("/entity-metas")
    public ApiResponse<Boolean> update(@Valid @RequestBody EntityMetaQO qo) {
        log.info("[entity_meta] update, param: {}", qo);
        if (qo.getId() == null) {
            return ApiResponse.error(400, "Entity meta id is required for update");
        }
        EntityMeta model = entityMetaStructMapper.qoToModel(qo);
        return ApiResponse.success(entityMetaService.update(model));
    }

    /**
     * Delete an entity meta by id.
     *
     * @param id entity meta id
     * @return success response
     */
    @Operation(summary = "Delete entity meta", description = "Delete an entity meta by id")
    @DeleteMapping("/entity-metas/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        log.info("[entity_meta] delete, id: {}", id);
        return ApiResponse.success(entityMetaService.delete(id));
    }
}

