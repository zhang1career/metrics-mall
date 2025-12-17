package lab.zhang.data_science.metrics_mall.struct_mapper;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.enums.AggregationTypeEnum;
import lab.zhang.data_science.metrics_mall.enums.MetricTypeEnum;
import lab.zhang.data_science.metrics_mall.enums.ValueTypeEnum;
import lab.zhang.data_science.metrics_mall.model.metric.AlphaMetric;
import lab.zhang.data_science.metrics_mall.model.metric.EchoMetric;
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.AlphaMetricDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.metric.EchoMetricDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.EchoMetricDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.EchoMetricQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.BriefMetricVO.PrettyBriefMetricVO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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


    //==================== PrimeMetric Model ====================

    /**
     * Convert MetricDAO to MetricModel.
     *
     * @param dao metric entity
     * @return MetricModel
     */
    @Mapping(target = "metricType", expression = "java(mapMetricType(dao.getMetricType()))")
    @Mapping(target = "aggregationType", expression = "java(mapAggregationType(dao.getAggregationType()))")
    PrimeMetric daoToPrimeModel(MetricDAO dao);

    /**
     * Convert list of MetricDAO to list of MetricModel.
     *
     * @param daoList metric entity list
     * @return MetricModel list
     */
    List<PrimeMetric> daoToPrimeModelBatch(List<MetricDAO> daoList);


    //==================== Dimension ====================

    /**
     * Convert dimension query object to DTO map.
     *
     * @param qo dimension query object
     * @return dimension DTO map
     */
    default Map<String, TypedValue> dimensionQoToDto(Map<String, Object> qo) {
        if (qo == null) {
            return MapUtil.empty();
        }
        return qo.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> TypedValue.of(entry.getValue())
                ));
    }


    //==================== AlphaMetric DAO ====================

    /**
     * Convert AlphaMetricDAO to AlphaMetric.
     *
     * @param dao alpha metric DAO
     * @return alpha metric model
     */
    @Mapping(target = "value", expression = "java(lab.zhang.data_science.metrics_mall.common.TypedValue.of(dao.getA()))")
    @Mapping(target = "snapshotTs", source = "ts")
    AlphaMetric alphaMetricDaoToModel(AlphaMetricDAO dao);

    /**
     * Convert list of AlphaMetricDAO to list of AlphaMetric.
     *
     * @param daoList alpha metric DAO list
     * @return alpha metric model list
     */
    List<AlphaMetric> alphaMetricDaoToModelBatch(List<AlphaMetricDAO> daoList);


    /**
     * Convert AlphaMetric to AlphaMetricDAO.
     *
     * @param model alpha metric model
     * @return alpha metric DAO
     */
    @Mapping(target = "a", expression = "java(model.getValue() != null ? model.getValue().getValueStr() : null)")
    @Mapping(target = "ts", source = "snapshotTs")
    AlphaMetricDAO alphaMetricModelToDao(AlphaMetric model);

    /**
     * Convert list of AlphaMetric to list of AlphaMetricDAO.
     *
     * @param modelList alpha metric model list
     * @return alpha metric DAO list
     */
    List<AlphaMetricDAO> alphaMetricModelToDaoBatch(List<AlphaMetric> modelList);


    //==================== EchoMetric DTO ====================

    /**
     * Convert EchoMetricQO to EchoMetricDTO.
     *
     * @param qo                echo metric query object
     * @param snapshotTs snapshot timestamp
     * @return echo metric DTO
     */
    @Mapping(target = "version", source = "qo.v")
    @Mapping(target = "dimensionMap", expression = "java(dimensionQoToDto(qo.getDims()))")
    @Mapping(target = "snapshotTs", source = "snapshotTs")
    EchoMetricDTO echoQoToDto(EchoMetricQO qo, Long snapshotTs);

    /**
     * Convert list of EchoMetricQO to list of EchoMetricDTO.
     *
     * @param qoList            list of echo metric query objects
     * @param snapshotTs snapshot timestamp
     * @return list of echo metric DTOs
     */
    default List<EchoMetricDTO> echoQoToDtoBatch(List<EchoMetricQO> qoList, Long snapshotTs) {
        if (qoList == null) {
            return ListUtil.empty();
        }
        return qoList.stream()
                .map(qo -> echoQoToDto(qo, snapshotTs))
                .collect(Collectors.toList());
    }


    /**
     * Convert list of MetricModel to metric value indexed by metric code.
     *
     * @param modelList         metric model list
     * @param snapshotTs the snapshot time
     * @return map of metric code to metric value
     */
    default Map<String, TypedValue> echoModelToSnapshotValue(List<EchoMetric> modelList, Long snapshotTs) {
        if (modelList == null) {
            return MapUtil.empty();
        }
        return modelList.stream()
                .map(mode -> mode.getBackNearest(snapshotTs))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AlphaMetric::getCode, AlphaMetric::getValue));
    }

    /**
     * Convert list of MetricModel to metric sample time indexed by metric code.
     *
     * @param modelList         metric model list
     * @param snapshotTs the snapshot time
     * @return map of metric code to sample time in milliseconds
     */
    default Map<String, Long> echoModelToSnapshotTime(List<EchoMetric> modelList, Long snapshotTs) {
        if (modelList == null) {
            return MapUtil.empty();
        }
        return modelList.stream()
                .map(mode -> mode.getBackNearest(snapshotTs))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AlphaMetric::getCode, AlphaMetric::getSnapshotTs));
    }


    //==================== EchoMetric Model ====================

    default TypedValue typedValueOf(Object rawValue, Integer valueType) {
        return ValueTypeEnum.typedValueOf(rawValue, valueType);
    }

    /**
     * Convert EchoMetricDAO to EchoMetric.
     *
     * @param echo echo metric DAO
     * @param meta metric DAO
     * @return echo metric model
     */
    @Mapping(target = "value", expression = "java(typedValueOf(echo.getA(), meta.getValueType()))")
    @Mapping(target = "snapshotTs", source = "echo.ts")
    @Mapping(target = "historyList", source = "echo.h")
    @Mapping(target = "code", source = "meta.code")
    @Mapping(target = "precision", source = "meta.precision")
    @Mapping(target = "unit", source = "meta.unit")
    @Mapping(target = "validationMap", source = "meta.validation")
    EchoMetric echoMetricDaoToModel(EchoMetricDAO echo, MetricDAO meta);


    //==================== EchoMetric DAO ====================

    /**
     * Convert EchoMetric to EchoMetricDAO.
     *
     * @param model echo metric model
     * @return echo metric DAO
     */
    @Mapping(target = "a", expression = "java(model.getValue() != null ? model.getValue().getValueStr() : null)")
    @Mapping(target = "ts", source = "snapshotTs")
    @Mapping(target = "h", source = "historyList")
    EchoMetricDAO echoMetricModelToDao(EchoMetric model);

    /**
     * Convert list of EchoMetric to list of EchoMetricDAO.
     *
     * @param modelList echo metric model list
     * @return echo metric DAO list
     */
    List<EchoMetricDAO> echoMetricModelToDaoBatch(List<EchoMetric> modelList);


    //==================== PrettyBriefMetric VO ====================

    /**
     * Convert Metric model to MetricPrettyBriefVO.
     *
     * @param model metric model
     * @return MetricPrettyBriefVO
     */
    @Mapping(target = "aggregationTypeStr", expression = "java(model.getAggregationType() != null ? model.getAggregationType().getName() : cn.hutool.core.util.StrUtil.EMPTY)")
    PrettyBriefMetricVO modelToPrettyBriefVo(PrimeMetric model);

    /**
     * Convert map of Metric model to map of MetricPrettyBriefVO.
     * @param modelMap metric model map
     * @return map of MetricPrettyBriefVO
     */
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


    //==================== Metric VO ====================

    /**
     * Convert MetricModel to MetricAggregationResponseDTO.MetricMetaDTO.
     *
     * @param model metric model
     * @return MetricAggregationResponseDTO.MetricMetaDTO
     */
    @Mapping(target = "metricType", expression = "java(model.getMetricType() != null ? model.getMetricType().getId() : null)")
    @Mapping(target = "valueType", expression = "java(model.getValue() != null && model.getValue().getType() != null ? model.getValue().getType().getId() : null)")
    @Mapping(target = "aggregationType", expression = "java(model.getAggregationType() != null ? model.getAggregationType().getId() : null)")
    @Mapping(target = "validation", ignore = true)
    MetricVO modelToVo(PrimeMetric model);
}

