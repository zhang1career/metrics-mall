package lab.zhang.data_science.metrics_mall.struct_mapper;

import cn.hutool.core.map.MapUtil;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.model.Metric;
import lab.zhang.data_science.metrics_mall.model.SampledValue;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDAO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricVO;
import lab.zhang.data_science.metrics_mall.util.TimeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Mapper for converting Entity to Model.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface MetricStructMapper {

    /**
     * Convert MetricModel to MetricDAO.
     *
     * @param dto metric DTO
     * @return MetricDAO
     */
    @Mapping(target = "metricType", expression = "java(model.getMetricType() != null ? model.getMetricType().getId() : null)")
    @Mapping(target = "valueType", expression = "java(model.getValueType() != null ? model.getValueType().getId() : null)")
    @Mapping(target = "aggregationType", expression = "java(model.getAggregationType() != null ? model.getAggregationType().getId() : null)")
    MetricDAO dtoToDao(MetricDTO dto);


    Metric daoToModel(MetricDAO dao);

    /**
     * Convert list of MetricDAO to list of MetricModel.
     *
     * @param daoList metric entity list
     * @return MetricModel list
     */
    List<Metric> daoToModelBatch(List<MetricDAO> daoList);


    /**
     * Convert MetricModel to MetricAggregationResponseDTO.MetricMetaDTO.
     *
     * @param model metric model
     * @return MetricAggregationResponseDTO.MetricMetaDTO
     */
    @Mapping(target = "metricType", expression = "java(model.getMetricType() != null ? model.getMetricType().getId() : null)")
    @Mapping(target = "valueType", expression = "java(model.getValueType() != null ? model.getValueType().getId() : null)")
    @Mapping(target = "aggregationType", expression = "java(model.getAggregationType() != null ? model.getAggregationType().getId() : null)")
    MetricVO modelToVo(Metric model);


    /**
     * Convert list of MetricModel to metric value indexed by metric code.
     *
     * @param modelList metric model list
     * @return map of metric code to metric value
     */
    default Map<String, TypedValue> modelToSampleValue(List<Metric> modelList, LocalDateTime snapshot) {
        if (modelList == null) {
            return MapUtil.empty();
        }
        return modelList.stream()
                .map(mode -> modelToHistoryValues(mode, snapshot))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(SampledValue::getCode, SampledValue::getValue));
    }

    /**
     * Convert list of MetricModel to metric sample time indexed by metric code.
     *
     * @param modelList metric model list
     * @param snapshot  the snapshot time
     * @return map of metric code to sample time in milliseconds
     */
    default Map<String, Long> modelToSampleTime(List<Metric> modelList, LocalDateTime snapshot) {
        if (modelList == null) {
            return MapUtil.empty();
        }
        return modelList.stream()
                .map(mode -> modelToHistoryValues(mode, snapshot))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        SampledValue::getCode,
                        sv -> TimeUtil.dateTimeToTimestamp(sv.getSampleTime())));
    }

    /**
     * Get the latest sampled value before or at the given snapshot time.
     *
     * @param model    the metric model
     * @param snapshot the snapshot time
     * @return the latest sampled value before or at the snapshot time, or null if none found
     */
    default SampledValue modelToHistoryValues(Metric model, LocalDateTime snapshot) {
        if (model == null || model.getHistoryValueList() == null) {
            return null;
        }
        return model.getHistoryValueList().stream()
                .filter(sv -> !sv.getSampleTime().isAfter(snapshot))
                .max(Comparator.comparing(SampledValue::getSampleTime))
                .orElse(null);
    }
}

