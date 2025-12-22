package lab.zhang.data_science.metrics_mall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.components.RequestContext;
import lab.zhang.data_science.metrics_mall.enums.OpEventEnum;
import lab.zhang.data_science.metrics_mall.enums.SnapshotSourceTypeEnum;
import lab.zhang.data_science.metrics_mall.model.MetricSnapshot;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricSnapshotQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricSnapshotVO;
import lab.zhang.data_science.metrics_mall.service.MetricSnapshotService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricSnapshotStructMapper;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

/**
 * Metric snapshot controller for handling GMS queries and writes.
 *
 * @author Rongjin Zhang
 */
@Tag(name = "Metric Snapshot", description = "GMS (Get Metric Snapshot) APIs")
@RestController
@RequiredArgsConstructor
@Slf4j
public class MetricSnapshotController extends BaseV1Controller {

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private MetricSnapshotService metricSnapshotService;

    @Autowired
    private MetricSnapshotStructMapper metricSnapshotStructMapper;

    @Autowired
    private MetricStructMapper metricStructMapper;

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
        log.info("[snap] query, param: entityCode={}, entityId={}, metrics={}, snapshot={}, atomic={}",
                qo.getEc(), qo.getEid(), qo.getMetrics(), qo.getSnapshotTs(), qo.getIsAtomic());

        MetricSnapshotDTO dto = metricSnapshotStructMapper.qoToDto(qo, metricStructMapper, SnapshotSourceTypeEnum.EXTERNAL);
        if (dto == null) {
            throw new IllegalArgumentException("[snap] invalid parameter");
        }
        // query
        MetricSnapshot model = metricSnapshotService.querySnapshot(dto);
        if (model == null) {
            throw new IllegalArgumentException("[snap] metric not found");
        }

        MetricSnapshotVO vo = metricSnapshotStructMapper.modelToVo(model, metricStructMapper);

        return ApiResponse.success(vo);
    }

    /**
     * Write metric snapshot.
     *
     * @param apiKey API key from request header
     * @param qo     metric snapshot write query object
     * @return metric snapshot write response
     */
    @Operation(summary = "Write metric snapshot", description = "Write metric snapshot values to cache")
    @PostMapping("/m_snap/write")
    public ApiResponse<BigInteger> writeSnapshot(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestHeader(value = "X-Request-ID", required = false) String requestIdStr,
            @RequestParam(value = "traceId", required = false) String traceIdStr,
            @Valid @RequestBody MetricSnapshotQO qo) {
        log.info("[snap] write, param: entityCode={}, entityId={}, metrics={}, snapshotTs={}",
                qo.getEc(), qo.getEid(), qo.getMetrics(), qo.getSnapshotTs());

        MetricSnapshotDTO dto = metricSnapshotStructMapper.qoToDto(qo, metricStructMapper, SnapshotSourceTypeEnum.EXTERNAL);
        // prepare request context after validation passes
        requestContext.setEvent(OpEventEnum.CREATE_METRIC_SNAPSHOT);

        BigInteger receiptId = metricSnapshotService.writeSnapshot(dto);
        if (receiptId == null) {
            throw new IllegalArgumentException("[snap] writing failed, no receipt generated");
        }

        return ApiResponse.success(receiptId);
    }
}
