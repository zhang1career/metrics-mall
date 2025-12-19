package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.model.MetricSnapshot;
import lab.zhang.data_science.metrics_mall.model.metric.BetaMetric;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricSnapshotQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricSnapshotVO;
import org.mapstruct.Mapper;

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
     * @param qo                 metric snapshot query object
     * @param metricStructMapper metric struct mapper
     * @return metric snapshot data transfer object
     */
    default MetricSnapshotDTO qoToDto(MetricSnapshotQO qo, MetricStructMapper metricStructMapper) {
        if (qo == null) {
            return null;
        }
        return MetricSnapshotDTO.builder()
                .entityCode(qo.getEc())
                .entityId(qo.getEid())
                .metricList(metricStructMapper.echoQoToDtoBatch(qo.getMetrics(), qo.getSnapshotTs()))
                .snapshotTs(qo.getSnapshotTs())
                .isAtomic(qo.getIsAtomic() != null && qo.getIsAtomic().equals(1))
                .build();
    }


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
     * @param metricStructMapper metric struct mapper
     * @return metric snapshot response VO
     */
    default MetricSnapshotVO modelToVo(MetricSnapshot model, MetricStructMapper metricStructMapper) {
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
