package lab.zhang.data_science.metrics_mall.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lab.zhang.data_science.metrics_mall.enums.ValueTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import static lab.zhang.data_science.metrics_mall.constant.NumConst.ZERO;

/**
 * TypedValue - Typed value wrapper class
 *
 * @author Rongjin Zhang
 */
@Data
@NoArgsConstructor
@JsonSerialize(using = TypedValue.TypedValueSerializer.class)
public class TypedValue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Default precision for decimal values.
     */
    private static final Integer PRECISION_DEFAULT = 2;

    /**
     * Create TypedValue from raw Object value.
     * Decimal values use default precision.
     *
     * @param value raw value
     * @return TypedValue instance
     */
    public static TypedValue of(Object value) {
        if (value == null) {
            return nullValue();
        } else if (value instanceof String) {
            return new TypedValue(value, ValueTypeEnum.STRING);
        } else if (value instanceof Integer) {
            return new TypedValue(value, ValueTypeEnum.INTEGER);
        } else if (value instanceof Long) {
            return new TypedValue(value, ValueTypeEnum.LONG);
        } else if (value instanceof Double || value instanceof Float) {
            return new TypedValue(value, ValueTypeEnum.DECIMAL, PRECISION_DEFAULT);
        } else if (value instanceof BigDecimal) {
            return new TypedValue(value, ValueTypeEnum.DECIMAL, PRECISION_DEFAULT);
        } else if (value instanceof Boolean) {
            return new TypedValue(value, ValueTypeEnum.BOOLEAN);
        } else {
            return new TypedValue(value, ValueTypeEnum.OBJECT);
        }
    }

    /**
     * Create TypedValue from raw Object value with specified precision for decimal types.
     *
     * @param value     raw value
     * @param precision precision for decimal types
     * @return TypedValue instance
     */
    public static TypedValue of(Object value, Integer precision) {
        if (value == null) {
            return nullValue();
        } else if (value instanceof String) {
            return new TypedValue(value, ValueTypeEnum.STRING);
        } else if (value instanceof Integer) {
            return new TypedValue(value, ValueTypeEnum.INTEGER);
        } else if (value instanceof Long) {
            return new TypedValue(value, ValueTypeEnum.LONG);
        } else if (value instanceof Double || value instanceof Float) {
            return new TypedValue(value, ValueTypeEnum.DECIMAL, precision);
        } else if (value instanceof BigDecimal) {
            return new TypedValue(value, ValueTypeEnum.DECIMAL, precision);
        } else if (value instanceof Boolean) {
            return new TypedValue(value, ValueTypeEnum.BOOLEAN);
        } else {
            return new TypedValue(value, ValueTypeEnum.OBJECT);
        }
    }

    public static TypedValue of(Object value, ValueTypeEnum type, Integer precision) {
        return new TypedValue(value, type, precision);
    }

    public static TypedValue nullValue() {
        return new TypedValue(null, ValueTypeEnum.OBJECT);
    }

    public static TypedValue trueValue() {
        return new TypedValue(true, ValueTypeEnum.BOOLEAN);
    }

    public static TypedValue falseValue() {
        return new TypedValue(false, ValueTypeEnum.BOOLEAN);
    }


    /**
     * Actual value
     */
    private Object value;

    /**
     * Value type
     */
    private ValueTypeEnum type;

    /**
     * Precision for decimal value.
     */
    private Integer precision;


    @JsonCreator
    public TypedValue(@JsonProperty("value") Object value,
                      @JsonProperty("type") ValueTypeEnum type,
                      Integer precision) {
        this.value = value;
        this.type = type;
        this.precision = precision;
    }

    @JsonCreator
    public TypedValue(@JsonProperty("value") Object value,
                      @JsonProperty("type") ValueTypeEnum type) {
        this.value = value;
        this.type = type;
        this.precision = ZERO;
    }

    /**
     * Get value automatically based on internal type information.
     * This method intelligently converts the value based on the stored ValueType
     * and returns the most appropriate type.
     * <p>
     * For example:
     * TypedValue tv = new TypedValue(100, ValueType.INTEGER);
     * Object result = tv.getValue();  // Returns Integer(100), not Object
     * Integer intValue = (Integer) result;  // Safe cast
     *
     * @return the value converted to the most appropriate type based on internal type information
     */
    public Object getValue() {
        if (value == null) {
            return null;
        }

        // Automatically convert based on internal type information
        if (type == null) {
            return value;  // If type is not set, return raw value
        }

        return switch (type) {
            case STRING -> getStringValue();
            case INTEGER -> getIntegerValue();
            case LONG -> getLongValue();
            case DECIMAL -> getDecimalValue();
            case BOOLEAN -> getBooleanValue();
            default -> value;  // Return raw value for complex types
        };
    }

    /**
     * Get value as String
     *
     * @return String representation of the value
     */
    public String getValueStr() {
        Object val = getValue();
        return val != null ? val.toString() : null;
    }

    /**
     * Get value and convert by type
     * These methods are used internally by getValue() for type conversion
     */
    private String getStringValue() {
        return value != null ? value.toString() : null;
    }

    private Integer getIntegerValue() {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    private Long getLongValue() {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }

    private Double getDecimalValue() {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }

    private Boolean getBooleanValue() {
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * Custom serializer for TypedValue.
     * Serializes TypedValue as just the value, not as an object.
     *
     * @author Rongjin Zhang
     */
    static class TypedValueSerializer extends JsonSerializer<TypedValue> {

        @Override
        public void serialize(TypedValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            Object actualValue = value.getValue();
            if (actualValue == null) {
                gen.writeNull();
                return;
            }

            if (value.getType() == ValueTypeEnum.DECIMAL && value.getPrecision() != null) {
                BigDecimal decimalValue;
                if (actualValue instanceof BigDecimal) {
                    decimalValue = (BigDecimal) actualValue;
                } else if (actualValue instanceof Number) {
                    decimalValue = new BigDecimal(actualValue.toString());
                } else {
                    decimalValue = new BigDecimal(actualValue.toString());
                }
                decimalValue = decimalValue.setScale(value.getPrecision(), java.math.RoundingMode.HALF_UP);
                String formattedValue = decimalValue.toPlainString();
                gen.writeString(formattedValue);
            } else {
                gen.writeObject(actualValue);
            }
        }
    }
}

