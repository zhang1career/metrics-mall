package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.model.MetricSnapshot;
import lab.zhang.data_science.metrics_mall.model.metric.BetaMetric;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricSnapshotQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricSnapshotVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

/**
 * Mapper for converting MetricSnapshot related objects.
 *
 * @author Rongjin Zhang
 */
@Mapper(
        componentModel = "spring",
        uses = {
                MetricStructMapper.class,
        })
public interface MetricSnapshotStructMapper {

    /**
     * Convert MetricSnapshotQO to MetricSnapshotDTO.
     *
     * @param qo metric snapshot query object
     * @return metric snapshot data transfer object
     */
    @Mapping(target = "entityCode", source = "ec")
    @Mapping(target = "entityId", source = "eid")
    @Mapping(target = "metricList", expression = "java(metricStructMapper.echoQoToDtoBatch(qo.getMetrics(), qo.getSnapshotTs()))")
    @Mapping(target = "snapshotTs", source = "snapshotTs")
    @Mapping(target = "isAtomic", expression = "java(qo.getIsAtomic() != null && qo.getIsAtomic().equals(1))")
    MetricSnapshotDTO qoToDto(MetricSnapshotQO qo);


    /**
     * Convert BetaMetric list to value map.
     *
     * @param metricList beta metric list
     * @return value map
     */
    default Map<String, TypedValue> betaModelToValueMap(List<BetaMetric> metricList) {
        if (metricList == null) {
            return java.util.Collections.emptyMap();
        }
        return metricList.stream()
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(
                        BetaMetric::getCode,
                        BetaMetric::getValue
                ));
    }

    /**
     * Convert BetaMetric list to timestamp map.
     *
     * @param metricList beta metric list
     * @return timestamp map
     */
    default Map<String, Long> betaModelToTimestampMap(List<BetaMetric> metricList) {
        if (metricList == null) {
            return java.util.Collections.emptyMap();
        }
        return metricList.stream()
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(
                        BetaMetric::getCode,
                        BetaMetric::getSnapshotTs
                ));
    }

    /**
     * Convert MetricSnapshot to MetricSnapshotVO.
     *
     * @param model              metric snapshot result model
     * @return metric snapshot response VO
     */
    default MetricSnapshotVO modelToVo(MetricSnapshot model) {
        if (model == null) {
            return null;
        }
        return MetricSnapshotVO.builder()
                .entityCode(model.getEntity().getMeta().getCode())
                .entityId(model.getEntity().getId())
                .valueMap(betaModelToValueMap(model.getMetricList()))
                .snapshotTsMap(betaModelToTimestampMap(model.getMetricList()))
                .build();
    }
}
