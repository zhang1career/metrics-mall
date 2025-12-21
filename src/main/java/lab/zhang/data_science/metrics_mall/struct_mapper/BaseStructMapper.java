package lab.zhang.data_science.metrics_mall.struct_mapper;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lab.zhang.data_science.metrics_mall.enums.*;

import java.time.LocalDateTime;
import java.util.*;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_MS_FORMAT;
import static lab.zhang.data_science.metrics_mall.constant.NumConst.ZERO_L;
import static lab.zhang.data_science.metrics_mall.util.TimeUtil.parseSecondsFromExpression;

public interface BaseStructMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    // string

    default String mapMapToString(Map<?, ?> map) {
        return map != null ? map.toString() : StrUtil.EMPTY;
    }

    default <K> Map<K, Object> getKTypedValueMap(String str) {
        try {
            Map<K, Object> result = OBJECT_MAPPER.readValue(str, Map.class);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse string to map: " + str, e);
        }
    }


    // collections

    /**
     * Map List to Set.
     *
     * @param list input list
     * @param <T>  element type
     * @return set
     */
    default <T> Set<T> mapListToSet(List<T> list) {
        return list != null ? new HashSet<>(list) : null;
    }

    /**
     * Map Set to List.
     *
     * @param set input set
     * @param <T> element type
     * @return list
     */
    default <T> List<T> mapSetToList(Set<T> set) {
        return set != null ? List.copyOf(set) : null;
    }


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
     * Map Date to String date representation.
     * @param date Date object
     * @return date string
     */
    default String mapDateToString(Date date) {
        return date != null ? DateUtil.format(date, NORM_DATETIME_MS_FORMAT) : StrUtil.EMPTY;
    }

    /**
     * Map String date representation to LocalDateTime.
     *
     * @param dateStr date string
     * @return LocalDateTime object
     */
    default LocalDateTime mapDateStrToLocalDateTime(String dateStr) {
        Date date = DateUtil.parse(dateStr);
        return LocalDateTimeUtil.of(date);
    }

    /**
     * Map LocalDateTime to String date representation.
     *
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
     *
     * @param value source type value
     * @return SnapshotSourceTypeEnum
     */
    default SnapshotSourceTypeEnum mapSourceType(Integer value) {
        return SnapshotSourceTypeEnum.fromId(value);
    }

    /**
     * Map SnapshotSourceTypeEnum to Integer.
     *
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

    /**
     * Map Integer to LifeStatusEnum.
     *
     * @param id life status id
     * @return LifeStatusEnum
     */
    default LifeStatusEnum mapLifeStatus(Integer id) {
        return LifeStatusEnum.fromId(id);
    }
    /**
     * Map LifeStatusEnum to Integer.
     *
     * @param lifeStatus life status enum
     * @return life status id
     */
    default Integer mapLifeStatusToInt(LifeStatusEnum lifeStatus) {
        return lifeStatus != null ? lifeStatus.getId() : null;
    }


    // TypedValue

    /**
     * Map Object to TypedValue.
     *
     * @param valueObj object value
     * @return TypedValue
     */
    default TypedValue mapTypedValue(Object valueObj) {
        return TypedValue.of(valueObj);
    }

    /**
     * Map Map<K, Object> to Map<K, TypedValue>.
     *
     * @param valueObjMap map with Object values
     * @param <K>         key type
     * @return map with TypedValue values
     */
    default <K> Map<K, TypedValue> mapTypedValueMap(Map<K, Object> valueObjMap) {
        if (valueObjMap == null) {
            return null;
        }
        Map<K, TypedValue> typedValueMap = new HashMap<>();
        for (Map.Entry<K, Object> entry : valueObjMap.entrySet()) {
            typedValueMap.put(entry.getKey(), mapTypedValue(entry.getValue()));
        }
        return typedValueMap;
    }

    default <K> Map<K, TypedValue> mapStringToTypedValueMap(String str) {
        if (StrUtil.isBlank(str)) {
            return MapUtil.newHashMap();
        }

        Map<K, Object> valueObjMap = getKTypedValueMap(str);
        return mapTypedValueMap(valueObjMap);
    }

    default TypedValue mapTypeToTypedValue(Object rawValue, Integer valueType) {
        return ValueTypeEnum.typedValueOf(rawValue, valueType);
    }
}
