package lab.zhang.data_science.metrics_mall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Aggregation type enumeration.
 *
 * @author Rongjin Zhang
 * 
 */
@Getter
@AllArgsConstructor
public enum AggregationTypeEnum {
    
    /**
     * Sum aggregation.
     */
    SUM(0, "sum"),
    
    /**
     * Average aggregation.
     */
    AVG(1, "avg"),
    
    /**
     * Maximum aggregation.
     */
    MAX(2, "max"),
    
    /**
     * Minimum aggregation.
     */
    MIN(3, "min"),
    
    /**
     * Count aggregation.
     */
    COUNT(4, "count");


    private final Integer id;
    private final String name;

    
    /**
     * Get enum by id.
     *
     * @param id aggregation type id
     * @return AggregationTypeEnum
     */
    public static AggregationTypeEnum fromId(Integer id) {
        if (id == null) {
            return null;
        }
        for (AggregationTypeEnum type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}

