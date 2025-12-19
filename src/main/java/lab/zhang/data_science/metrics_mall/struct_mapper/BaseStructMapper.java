package lab.zhang.data_science.metrics_mall.struct_mapper;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.enums.AggregationTypeEnum;
import lab.zhang.data_science.metrics_mall.enums.MetricTypeEnum;
import lab.zhang.data_science.metrics_mall.enums.OpEventEnum;
import lab.zhang.data_science.metrics_mall.enums.SnapshotSourceTypeEnum;

import java.time.LocalDateTime;
import java.util.Date;

import static lab.zhang.data_science.metrics_mall.constant.NumConst.ZERO_L;
import static lab.zhang.data_science.metrics_mall.util.TimeUtil.parseSecondsFromExpression;

public interface BaseStructMapper {
    // time

    /**
     * Map Long timestamp to Date.
     *
     * @param timestamp timestamp in milliseconds
     * @return Date object
     */
    default Date mapTimestampToDate(Long timestamp) {
        return timestamp != null ? new Date(timestamp) : null;
    }

    /**
     * Map Date to Long timestamp.
     *
     * @param date Date object
     * @return timestamp in milliseconds
     */
    default Long mapDateToTimestamp(Date date) {
        return date != null ? date.getTime() : ZERO_L;
    }

    /**
     * Map String date representation to LocalDateTime.
     * @param dateStr date string
     * @return LocalDateTime object
     */
    default LocalDateTime mapDateStrToLocalDateTime(String dateStr) {
        Date date = DateUtil.parse(dateStr);
        return LocalDateTimeUtil.of(date);
    }

    /**
     * Map LocalDateTime to String date representation.
     * @param exp time expression
     * @return seconds
     */
    default Long mapExpressionToSeconds(String exp) {
        return parseSecondsFromExpression(exp);
    }

    // enums

    /**
     * Map Integer to OpEventEnum.
     *
     * @param value event value
     * @return OpEventEnum
     */
    default OpEventEnum mapEvent(Integer value) {
        return OpEventEnum.fromId(value);
    }
    /**
     * Map OpEventEnum to Integer.
     *
     * @param event event enum
     * @return event value
     */
    default Integer mapEventToInt(OpEventEnum event) {
        return event != null ? event.getId() : null;
    }

    /**
     * Map Integer to SnapshotSourceTypeEnum.
     * @param value source type value
     * @return SnapshotSourceTypeEnum
     */
    default SnapshotSourceTypeEnum mapSourceType(Integer value) {
        return SnapshotSourceTypeEnum.fromId(value);
    }
    /**
     * Map SnapshotSourceTypeEnum to Integer.
     * @param sourceType snapshot source type enum
     * @return source type value
     */
    default Integer mapSourceTypeToInt(SnapshotSourceTypeEnum sourceType) {
        return sourceType != null ? sourceType.getId() : null;
    }

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


    // TypedValue

    /**
     * Map Object to TypedValue.
     * @param valueObj object value
     * @return TypedValue
     */
    default TypedValue mapObjToTypedValue(Object valueObj) {
        return TypedValue.of(valueObj);
    }

}
