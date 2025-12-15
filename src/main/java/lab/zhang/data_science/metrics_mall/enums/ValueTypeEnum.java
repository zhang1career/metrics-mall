package lab.zhang.data_science.metrics_mall.enums;

import lombok.Getter;

/**
 * Value type enumeration.
 *
 * @author Rongjin Zhang
 * 
 */
@Getter
public enum ValueTypeEnum {
    STRING(0, "STR"),
    INTEGER(1, "INT"),
    LONG(2, "LONG"),
    DECIMAL(3, "FLOAT"),
    BOOLEAN(4, "BOOL"),
    DATE(5, "DATE"),
    OBJECT(6, "OBJ");

    
    private final Integer id;
    private final String name;


    ValueTypeEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
    
    /**
     * Get enum by id.
     *
     * @param id value type id
     * @return ValueTypeEnum
     */
    public static ValueTypeEnum fromId(Integer id) {
        if (id == null) {
            return null;
        }
        for (ValueTypeEnum type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}

