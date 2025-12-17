package lab.zhang.data_science.metrics_mall.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.model.MetricSnapshot;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricSnapshotQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricSnapshotVO;
import lab.zhang.data_science.metrics_mall.service.MetricSnapshotService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricSnapshotStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Metric snapshot controller for handling GMS queries.
 *
 * @author Rongjin Zhang
 */
@Tag(name = "Metric Snapshot", description = "GMS (Get Metric Snapshot) APIs")
@RestController
@RequiredArgsConstructor
@Slf4j
public class MetricSnapshotController extends BaseController {

    @Autowired
    private MetricSnapshotService metricSnapshotService;

    @Autowired
    private MetricSnapshotStructMapper metricSnapshotStructMapper;

    /**
     * Query metric snapshot.
     *
     * @param apiKey API key from request header
     * @param qo     metric snapshot query object
     * @return metric snapshot response
     */
    @Operation(summary = "Get metric snapshot", description = "Query real-time metric snapshot values (GMS)")
    @PostMapping("/m_snap")
    public ApiResponse<MetricSnapshotVO> querySnapshot(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestHeader(value = "X-Request-ID", required = false) String requestIdStr,
            @RequestParam(value = "traceId", required = false) String traceIdStr,
            @Valid @RequestBody MetricSnapshotQO qo) {
        log.info("[snap] param: entityCode={}, entityId={}, metrics={}, snapshot={}, atomic={}",
                qo.getEc(), qo.getEid(), qo.getMetrics(), qo.getSnapshotTs(), qo.getIsAtomic());

        MetricSnapshotDTO dto = metricSnapshotStructMapper.qoToDto(qo);
        if (dto == null) {
            throw new IllegalArgumentException("[snap] invalid parameter");
        }
        // query
        MetricSnapshot model = metricSnapshotService.querySnapshot(dto);
        if (model == null) {
            throw new IllegalArgumentException("[snap] metric not found");
        }

        MetricSnapshotVO vo = metricSnapshotStructMapper.modelToVo(model);

        return ApiResponse.success(vo);
    }
}
