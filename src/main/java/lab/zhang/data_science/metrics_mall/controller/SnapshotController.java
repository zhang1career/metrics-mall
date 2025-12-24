package lab.zhang.data_science.metrics_mall.controller;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.components.RequestContext;
import lab.zhang.data_science.metrics_mall.enums.OpEventEnum;
import lab.zhang.data_science.metrics_mall.model.EntityMeta;
import lab.zhang.data_science.metrics_mall.model.MetricSnapshot;
import lab.zhang.data_science.metrics_mall.pojo.dao.x.EntityMetricRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.x.EntityMetricRelResultDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.EchoMetricQO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricSnapshotQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricSnapshotVO;
import lab.zhang.data_science.metrics_mall.service.EntityMetricRelService;
import lab.zhang.data_science.metrics_mall.service.EntityService;
import lab.zhang.data_science.metrics_mall.service.MetricSnapshotService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricSnapshotStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Metric snapshot controller for handling GMS queries and writes.
 *
 * @author Rongjin Zhang
 */
@Tag(name = "Metric Snapshot", description = "GMS (Get Metric Snapshot) APIs")
@RestController
@RequiredArgsConstructor
@Slf4j
public class SnapshotController extends BaseV1Controller {

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private EntityService entityService;

    @Autowired
    private EntityMetricRelService xService;

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
        log.info("[snap] query, param: entityCode={}, entityId={}, metrics={}, snapshot={}, atomic={}",
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

        // validate entity meta
        EntityMeta entityMeta = entityService.getByCode(qo.getEc());
        if (entityMeta == null) {
            throw new IllegalArgumentException("[snap] writing failed, entityMeta not found, entityCode=" + qo.getEc());
        }
        // validate and map alias
        Map<Integer, String> aliasMap = new HashMap<>();
        for (int i = 0; i < qo.getMetrics().size(); i++) {
            EchoMetricQO metricQO = qo.getMetrics().get(i);
            if (metricQO == null) {
                continue;
            }
            // skip if metricCode is specified
            if (!StrUtil.isBlank(metricQO.getCode())) {
                continue;
            }
            // skip if alias is not specified
            if (StrUtil.isBlank(metricQO.getAlias())) {
                continue;
            }
            // collect alias
            aliasMap.put(i, metricQO.getAlias());
        }
        // query entity-metric relations by alias list
        Map<String, EntityMetricRelResultDAO> aliasXMap = xService.mapByAliasBatch(entityMeta.getId(), aliasMap.values());
        // rewrite metric codes by alias
        for (Map.Entry<Integer, String> entry : aliasMap.entrySet()) {
            Integer index = entry.getKey();
            String alias = entry.getValue();
            EntityMetricRelResultDAO xDAO = aliasXMap.get(alias);
            if (xDAO == null) {
                log.warn("[snap] writing skipped, entity-metric relation not found by alias: entityCode={}, alias={}", qo.getEc(), alias);
                continue;
            }
            if (qo.getMetrics().get(index) == null) {
                continue;
            }
            // rewrite metric code
            qo.getMetrics().get(index).setCode(xDAO.getMetricCode());
        }

        MetricSnapshotDTO dto = metricSnapshotStructMapper.qoToDto(qo);
        // prepare request context after validation passes
        requestContext.setEvent(OpEventEnum.CREATE_METRIC_SNAPSHOT);

        BigInteger receiptId = metricSnapshotService.writeSnapshotExternal(dto);
        if (receiptId == null) {
            throw new IllegalArgumentException("[snap] writing failed, no receipt generated");
        }

        return ApiResponse.success(receiptId);
    }
}
