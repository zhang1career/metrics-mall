package lab.zhang.data_science.metrics_mall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Operation event enum.
 *
 * @author Rongjin Zhang
 */
@Getter
@AllArgsConstructor
public enum OpEventEnum {
    UNDEFINED(0, "undefined event"),

    // entity
    CREATE_ENTITY_META(101, "create entity meta"),
    UPDATE_ENTITY_META(102, "update entity meta"),
    DELETE_ENTITY_META(103, "delete entity meta"),

    // metric
    CREATE_METRIC_META(111, "create metric meta"),
    UPDATE_METRIC_META(112, "update metric meta"),
    DELETE_METRIC_META(113, "delete metric meta"),

    // metric version
    CREATE_METRIC_VERSION(121, "create metric version"),
    UPTATE_METRIC_VERSION(122, "update metric version"),
    DELETE_METRIC_VERSION(123, "delete metric version"),
    // set main version
    CHANGE_METRIC_VERSION_MAIN(131, "change metric main version"),
    // change life status
    CHANGE_METRIC_VERSION_LIFE_STATUS_OFFLINE(141, "change metric version life status to offline"),
    CHANGE_METRIC_VERSION_LIFE_STATUS_DEV(142, "change metric version life status to development"),
    CHANGE_METRIC_VERSION_LIFE_STATUS_TEST(143, "change metric version life status to testing"),
    CHANGE_METRIC_VERSION_LIFE_STATUS_GRAY(144, "change metric version life status to gray"),
    CHANGE_METRIC_VERSION_LIFE_STATUS_ONLINE(145, "change metric version life status to online"),
    CHANGE_METRIC_VERSION_LIFE_STATUS_DEPRECATED(146, "change metric version life status to deprecated"),

    // dimension
    CREATE_DIMENSION(151, "create dimension"),
    UPDATE_DIMENSION(152, "update dimension"),
    DELETE_DIMENSION(153, "delete dimension"),

    // relationship of entity and metric
    CREATE_METRIC_OF_ENTITY(201, "create a metric for an entity"),
    UPDATE_METRIC_OF_ENTITY(202, "update a metric for an entity"),
    DELETE_METRIC_OF_ENTITY(203, "delete a metric from an entity"),

    // relationship of metric and dimension
    CREATE_DIMENSION_OF_METRIC(211, "create a dimension for a metric"),
    UPDATE_DIMENSION_OF_METRIC(212, "update a dimension for a metric"),
    DELETE_DIMENSION_OF_METRIC(213, "delete a dimension from a metric"),

    // snapshot
    CREATE_METRIC_SNAPSHOT(301, "create metric snapshot"),

    ;


    /**
     * Event value. Note: not final because we assign auto ids in static block.
     */
    private Integer id;

    /**
     * Event name.
     */
    private final String name;


    /**
     * Get enum by value.
     *
     * @param value event value
     * @return OpEventEnum, null if not found
     */
    public static OpEventEnum fromId(Integer value) {
        if (value == null) {
            return null;
        }
        for (OpEventEnum event : OpEventEnum.values()) {
            if (event.getId().equals(value)) {
                return event;
            }
        }
        return null;
    }
}
