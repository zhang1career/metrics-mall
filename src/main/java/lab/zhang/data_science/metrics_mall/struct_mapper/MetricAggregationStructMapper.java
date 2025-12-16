package lab.zhang.data_science.metrics_mall.struct_mapper;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import lab.zhang.data_science.metrics_mall.model.MetricAggregation;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricAggregationDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricAggregationDTO.FieldConditionDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricAggregationDTO.OrderByDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricAggregationQO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricAggregationQO.FieldConditionQO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricAggregationQO.OrderByQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.AggregationVO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricAggregationVO;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static lab.zhang.data_science.metrics_mall.util.TimeUtil.parseSecondsFromExpression;

/**
 * Mapper for converting MetricAggregation related objects.
 *
 * @author Rongjin Zhang
 */
@MapperConfig(
        componentModel = "spring",
        uses = {
                MetricStructMapper.class
        })
public interface MetricAggregationStructMapper {

    /**
     * Parse start time from MetricAggregationQO.
     * @param qo metric aggregation QO
     * @return start time as LocalDateTime
     */
    default LocalDateTime parseStartTimeFromQo(MetricAggregationQO qo) {
        String dateStr = qo.getTimeRange().getStart();
        Date date = DateUtil.parse(dateStr);
        return LocalDateTimeUtil.of(date);
    }

    /**
     * Parse stop time from MetricAggregationQO.
     * @param qo metric aggregation QO
     * @return stop time as LocalDateTime
     */
    default LocalDateTime parseStopTimeFromQo(MetricAggregationQO qo) {
        String dateStr = qo.getTimeRange().getStop();
        Date date = DateUtil.parse(dateStr);
        return LocalDateTimeUtil.of(date);
    }


    /**
     * Parse interval in seconds from MetricAggregationQO.
     * @param qo metric aggregation QO
     * @return interval in seconds
     */
    default Long parseSecondsFromQo(MetricAggregationQO qo) {
        return parseSecondsFromExpression(qo.getInterval());
    }


    /**
     * Convert FieldConditionQO to FieldConditionDTO.
     * @param qo field condition QO
     * @return field condition DTO
     */
    @Mapping(target = "value", expression = "java(TypedValue.of(qo.getValue()))")
    FieldConditionDTO condQoToDto(FieldConditionQO qo);

    /**
     * Convert list of FieldConditionQO to list of FieldConditionDTO.
     * @param qoList list of field condition QO
     * @return list of field condition DTO
     */
    List<FieldConditionDTO> condQoToDtoBatch(List<FieldConditionQO> qoList);


    /**
     * Convert OrderByQO to OrderByDTO.
     * @param qo order by QO
     * @return order by DTO
     */
    OrderByDTO orderByQoToDto(OrderByQO qo);

    /**
     * Convert list of OrderByQO to list of OrderByDTO.
     * @param qoList list of order by QO
     * @return list of order by DTO
     */
    List<OrderByDTO> orderByQoToDtoBatch(List<OrderByQO> qoList);


    /**
     * Convert MetricAggregationQO to MetricAggregationDTO.
     *
     * @param qo metric aggregation QO
     * @return metric aggregation DTO
     */
    @Mapping(target = "metricCodeSet", expression = "java(new HashSet<>(qo.getMetricCodes()))")
    @Mapping(target = "startTime", expression = "java(parseStartTimeFromQo(qo))")
    @Mapping(target = "stopTime", expression = "java(parseStopTimeFromQo(qo))")
    @Mapping(target = "intervalInSeconds", expression = "java(parseSecondsFromQo(qo))")
    @Mapping(target = "groupByList", source = "groupBy")
    @Mapping(target = "filterList", expression = "java(condQoToDtoBatch(qo.getFilters()))")
    @Mapping(target = "orderByList", expression = "java(orderByQoToDtoBatch(qo.getOrderBys()))")
    MetricAggregationDTO qoToDto(MetricAggregationQO qo);


    /**
     * Convert MetricAggregation model to list of AggregationVO.
     *
     * @param model metric aggregation model
     * @return list of aggregation VO
     */
    default List<AggregationVO> modelToAggregationVoBatch(MetricAggregation model) {
        return model.getValueMap().entrySet().stream()
                .map(entry -> {
                    AggregationVO vo = new AggregationVO();
                    vo.setDimensionMap(entry.getKey());
                    vo.setValueMap(entry.getValue());
                    return vo;
                })
                .collect(Collectors.toList());
    }


    /**
     * Convert MetricAggregation to MetricAggregationVO.
     *
     * @param model metric aggregation model
     * @return metric aggregation VO
     */
    @Mapping(target = "meta", expression = "java(metricStructMapper.modelToPrettyBriefVoMap(model.getMetricMap()))")
    @Mapping(target = "rows", expression = "java(modelToAggregationVoBatch(model))")
    MetricAggregationVO modelToVo(MetricAggregation model);
}

