package lab.zhang.data_science.metrics_mall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Metric type enumeration.
 *
 * @author Rongjin Zhang
 * 
 */
@Getter
@AllArgsConstructor
public enum MetricTypeEnum {
    
    /**
     * Atomic metric.
     */
    ATOMIC(0, "ATOMIC"),
    
    /**
     * Derived metric.
     */
    DERIVED(1, "DERIVED"),
    
    /**
     * Composite metric.
     */
    COMPOSITE(2, "COMPOSITE");


    private final Integer id;
    private final String name;

    
    /**
     * Get enum by id.
     *
     * @param id metric type id
     * @return MetricTypeEnum
     */
    public static MetricTypeEnum fromId(Integer id) {
        if (id == null) {
            return null;
        }
        for (MetricTypeEnum type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}

