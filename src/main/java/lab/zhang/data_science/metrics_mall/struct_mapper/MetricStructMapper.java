package lab.zhang.data_science.metrics_mall.struct_mapper;

import cn.hutool.core.map.MapUtil;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.enums.AggregationTypeEnum;
import lab.zhang.data_science.metrics_mall.enums.MetricTypeEnum;
import lab.zhang.data_science.metrics_mall.model.metric.BaseMetric;
import lab.zhang.data_science.metrics_mall.model.metric.EchoMetric;
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDAO;
import lab.zhang.data_science.metrics_mall.pojo.vo.BriefMetricVO.PrettyBriefMetricVO;
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
     * Map Integer to MetricTypeEnum.
     *
     * @param id metric type id
     * @return MetricTypeEnum
     */
    default MetricTypeEnum mapMetricType(Integer id) {
        return MetricTypeEnum.fromId(id);
    }

    /**
     * Map Integer to AggregationTypeEnum.
     *
     * @param id aggregation type id
     * @return AggregationTypeEnum
     */
    default AggregationTypeEnum mapAggregationType(Integer id) {
        return AggregationTypeEnum.fromId(id);
    }


    /**
     * Convert MetricDAO to MetricModel.
     *
     * @param dao metric entity
     * @return MetricModel
     */
    @Mapping(target = "metricType", expression = "java(mapMetricType(dao.getMetricType()))")
    @Mapping(target = "aggregationType", expression = "java(mapAggregationType(dao.getAggregationType()))")
    PrimeMetric daoToModel(MetricDAO dao);

    /**
     * Convert list of MetricDAO to list of MetricModel.
     *
     * @param daoList metric entity list
     * @return MetricModel list
     */
    List<PrimeMetric> daoToModelBatch(List<MetricDAO> daoList);


    /**
     * Convert MetricModel to MetricAggregationResponseDTO.MetricMetaDTO.
     *
     * @param model metric model
     * @return MetricAggregationResponseDTO.MetricMetaDTO
     */
    @Mapping(target = "metricType", expression = "java(model.getMetricType() != null ? model.getMetricType().getId() : null)")
    @Mapping(target = "valueType", expression = "java(model.getValue() != null && model.getValue().getType() != null ? model.getValue().getType().getId() : null)")
    @Mapping(target = "aggregationType", expression = "java(model.getAggregationType() != null ? model.getAggregationType().getId() : null)")
    @Mapping(target = "validation", source = "validationMap")
    MetricVO modelToVo(PrimeMetric model);


    /**
     * Convert list of MetricModel to metric value indexed by metric code.
     *
     * @param modelList metric model list
     * @return map of metric code to metric value
     */
    default Map<String, TypedValue> echoModelToSampleValue(List<EchoMetric> modelList, LocalDateTime snapshot) {
        if (modelList == null) {
            return MapUtil.empty();
        }
        return modelList.stream()
                .map(mode -> getBackNearest(mode, snapshot))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(BaseMetric::getCode, BaseMetric::getValue));
    }

    /**
     * Convert list of MetricModel to metric sample time indexed by metric code.
     *
     * @param modelList metric model list
     * @param snapshot  the snapshot time
     * @return map of metric code to sample time in milliseconds
     */
    default Map<String, Long> echoModelToSampleTime(List<EchoMetric> modelList, LocalDateTime snapshot) {
        if (modelList == null) {
            return MapUtil.empty();
        }
        return modelList.stream()
                .map(mode -> getBackNearest(mode, snapshot))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        BaseMetric::getCode,
                        sv -> TimeUtil.dateTimeToTimestamp(sv.getSampleTime())));
    }

    /**
     * Get the latest sampled value before or at the given snapshot time.
     *
     * @param model    the metric model
     * @param snapshot the snapshot time
     * @return the latest sampled value before or at the snapshot time, or null if none found
     */
    default BaseMetric getBackNearest(EchoMetric model, LocalDateTime snapshot) {
        if (model == null || model.getHistoryMetricList() == null) {
            return null;
        }
        return model.getHistoryMetricList().stream()
                .filter(sv -> !sv.getSampleTime().isAfter(snapshot))
                .max(Comparator.comparing(BaseMetric::getSampleTime))
                .orElse(null);
    }


    /**
     * Convert Metric model to MetricPrettyBriefVO.
     *
     * @param model metric model
     * @return MetricPrettyBriefVO
     */
    @Mapping(target = "aggregationTypeStr", expression = "java(model.getAggregationType() != null ? model.getAggregationType().getName() : cn.hutool.core.util.StrUtil.EMPTY)")
    PrettyBriefMetricVO modelToPrettyBriefVo(PrimeMetric model);

    default Map<String, PrettyBriefMetricVO> modelToPrettyBriefVoMap(Map<String, PrimeMetric> modelMap) {
        if (modelMap == null) {
            return MapUtil.empty();
        }
        return modelMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> modelToPrettyBriefVo(entry.getValue())
                ));
    }
}

