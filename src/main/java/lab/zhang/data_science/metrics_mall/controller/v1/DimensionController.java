package lab.zhang.data_science.metrics_mall.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.model.Dimension;
import lab.zhang.data_science.metrics_mall.pojo.qo.DimensionQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.DimensionVO;
import lab.zhang.data_science.metrics_mall.service.DimensionService;
import lab.zhang.data_science.metrics_mall.struct_mapper.DimensionStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dimension controller for CRUD operations.
 *
 * @author Rongjin Zhang
 */
@Tag(name = "Dimension", description = "Dimension management APIs")
@RestController
@RequiredArgsConstructor
@Slf4j
public class DimensionController extends BaseV1Controller {

    @Autowired
    private DimensionService dimensionService;

    @Autowired
    private DimensionStructMapper dimensionStructMapper;

    /**
     * Get dimension by id.
     *
     * @param id dimension id
     * @return dimension response
     */
    @Operation(summary = "Get dimension", description = "Get dimension by id")
    @GetMapping("/dims/{id}")
    public ApiResponse<DimensionVO> get(@PathVariable Long id) {
        log.info("[dim] get, id: {}", id);
        Dimension model = dimensionService.get(id);
        return ApiResponse.success(dimensionStructMapper.modelToVo(model));
    }

    /**
     * Get dimension by code.
     *
     * @param code dimension code
     * @return dimension response
     */
    @Operation(summary = "Get dimension by code", description = "Get dimension by code")
    @GetMapping("/dims/code/{code}")
    public ApiResponse<DimensionVO> getByCode(@PathVariable String code) {
        log.info("[dim] getByCode, code: {}", code);
        Dimension model = dimensionService.getByCode(code);
        return ApiResponse.success(dimensionStructMapper.modelToVo(model));
    }

    /**
     * List all dimensions.
     *
     * @return dimension list response
     */
    @Operation(summary = "List dimensions", description = "List all dimensions")
    @GetMapping("/dims")
    public ApiResponse<List<DimensionVO>> list() {
        log.info("[dim] list");
        List<Dimension> modelList = dimensionService.list();
        return ApiResponse.success(dimensionStructMapper.modelToVoBatch(modelList));
    }

    /**
     * Count all dimensions.
     *
     * @return dimension count response
     */
    @Operation(summary = "Count dimensions", description = "Count all dimensions")
    @GetMapping("/dims/count")
    public ApiResponse<Long> count() {
        log.info("[dim] count");
        return ApiResponse.success(dimensionService.count());
    }

    /**
     * Insert a new dimension.
     *
     * @param qo dimension query object
     * @return success response
     */
    @Operation(summary = "Insert dimension", description = "Insert a new dimension")
    @PostMapping("/dims")
    public ApiResponse<Boolean> insert(@Valid @RequestBody DimensionQO qo) {
        log.info("[dim] insert, param: {}", qo);
        Dimension model = dimensionStructMapper.qoToModel(qo);
        return ApiResponse.success(dimensionService.insert(model));
    }

    /**
     * Update an existing dimension.
     *
     * @param qo dimension query object
     * @return success response
     */
    @Operation(summary = "Update dimension", description = "Update an existing dimension")
    @PutMapping("/dims")
    public ApiResponse<Boolean> update(@Valid @RequestBody DimensionQO qo) {
        log.info("[dim] update, param: {}", qo);
        if (qo.getId() == null) {
            return ApiResponse.error(400, "Dimension id is required for update");
        }
        Dimension model = dimensionStructMapper.qoToModel(qo);
        return ApiResponse.success(dimensionService.update(model));
    }

    /**
     * Delete a dimension by id.
     *
     * @param id dimension id
     * @return success response
     */
    @Operation(summary = "Delete dimension", description = "Delete a dimension by id")
    @DeleteMapping("/dims/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        log.info("[dim] delete, id: {}", id);
        return ApiResponse.success(dimensionService.delete(id));
    }
}

