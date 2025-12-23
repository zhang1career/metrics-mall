package lab.zhang.data_science.metrics_mall.enums;

import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.Getter;

import java.math.BigDecimal;

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

    public static TypedValue typedValueOf(Object valueObj, Integer typeId) {
        ValueTypeEnum valueTypeEnum = fromId(typeId);
        if (valueTypeEnum == null) {
            throw new IllegalArgumentException("Unsupported ValueTypeEnum, id=" + typeId);
        }
        if (valueObj == null) {
            throw new UnsupportedOperationException("The type of null is not supported in ValueTypeEnum.");
        }
        switch (valueTypeEnum) {
            case STRING:
                return new TypedValue(String.valueOf(valueObj), ValueTypeEnum.STRING);
            case INTEGER:
                return new TypedValue(Integer.valueOf(String.valueOf(valueObj)), ValueTypeEnum.INTEGER);
            case LONG:
                return new TypedValue(new BigDecimal(String.valueOf(valueObj)).longValue(), ValueTypeEnum.LONG);
            case DECIMAL:
                return new TypedValue(new BigDecimal(String.valueOf(valueObj)), ValueTypeEnum.DECIMAL);
            case BOOLEAN:
                return new TypedValue(Boolean.valueOf(String.valueOf(valueObj)), ValueTypeEnum.BOOLEAN);
            case DATE:
                return new TypedValue(valueObj, ValueTypeEnum.DATE);
            case OBJECT:
                return new TypedValue(valueObj, ValueTypeEnum.OBJECT);
            default:
                throw new IllegalArgumentException("Unsupported ValueTypeEnum, id=" + typeId);
        }
    }
}

