package lab.zhang.data_science.metrics_mall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.EntityMetaDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.EntityMetaQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.EntityMetaVO;
import lab.zhang.data_science.metrics_mall.service.EntityService;
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
public class EntityController extends BaseV1Controller {

    @Autowired
    private EntityService entityService;

    @Autowired
    private EntityMetaStructMapper entityMetaStructMapper;

    /**
     * Get entity meta by id.
     *
     * @param id entity meta id
     * @return entity meta response
     */
    @Operation(summary = "Get entity meta", description = "Get entity meta by id")
    @GetMapping("/entity_metas/{id}")
    public ApiResponse<EntityMetaVO> get(@PathVariable Integer id) {
        log.info("[entity_meta] get, id: {}", id);
        EntityMetaDAO dao = entityService.getDao(id);
        if (dao == null) {
            return ApiResponse.success(null);
        }
        return ApiResponse.success(entityMetaStructMapper.daoToVo(dao));
    }

    /**
     * Get entity meta by code.
     *
     * @param code entity meta code
     * @return entity meta response
     */
    @Operation(summary = "Get entity meta by code", description = "Get entity meta by code")
    @GetMapping("/entity_metas/code/{code}")
    public ApiResponse<EntityMetaVO> getByCode(@PathVariable String code) {
        log.info("[entity_meta] getByCode, code: {}", code);
        EntityMetaDAO dao = entityService.getDaoByCode(code);
        if (dao == null) {
            return ApiResponse.success(null);
        }
        return ApiResponse.success(entityMetaStructMapper.daoToVo(dao));
    }

    /**
     * List all entity metas.
     *
     * @return entity meta list response
     */
    @Operation(summary = "List entity metas", description = "List all entity metas")
    @GetMapping("/entity_metas")
    public ApiResponse<List<EntityMetaVO>> list() {
        log.info("[entity_meta] list");
        List<EntityMetaDAO> daoList = entityService.listDao();
        return ApiResponse.success(entityMetaStructMapper.daoToVoBatch(daoList));
    }

    /**
     * Count all entity metas.
     *
     * @return entity meta count response
     */
    @Operation(summary = "Count entity metas", description = "Count all entity metas")
    @GetMapping("/entity_metas/count")
    public ApiResponse<Long> count() {
        log.info("[entity_meta] count");
        return ApiResponse.success(entityService.count());
    }

    /**
     * Insert a new entity meta.
     *
     * @param qo entity meta query object
     * @return success response
     */
    @Operation(summary = "Insert entity meta", description = "Insert a new entity meta")
    @PostMapping("/entity_metas")
    public ApiResponse<Boolean> insert(@Valid @RequestBody EntityMetaQO qo) {
        log.info("[entity_meta] insert, param: {}", qo);
        EntityMetaDTO dto = entityMetaStructMapper.qoToDto(qo);
        return ApiResponse.success(entityService.insert(dto));
    }

    /**
     * Update an existing entity meta.
     *
     * @param qo entity meta query object
     * @return success response
     */
    @Operation(summary = "Update entity meta", description = "Update an existing entity meta")
    @PutMapping("/entity_metas")
    public ApiResponse<Boolean> update(@Valid @RequestBody EntityMetaQO qo) {
        log.info("[entity_meta] update, param: {}", qo);
        EntityMetaDTO dto = entityMetaStructMapper.qoToDto(qo);
        return ApiResponse.success(entityService.update(dto));
    }

    /**
     * Delete an entity meta by id.
     *
     * @param id entity meta id
     * @return success response
     */
    @Operation(summary = "Delete entity meta", description = "Delete an entity meta by id")
    @DeleteMapping("/entity_metas/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        log.info("[entity_meta] delete, id: {}", id);
        return ApiResponse.success(entityService.delete(id));
    }
}

