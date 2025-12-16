package lab.zhang.data_science.metrics_mall.pojo.vo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lab.zhang.data_science.metrics_mall.common.TypedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Map;

/**
 * Aggregation row DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = AggregationVO.AggregationVOSerializer.class)
public class  AggregationVO {
    /**
     * Dimension values map, key is dimension code, value is dimension value.
     */
    private Map<String, TypedValue> dimensionMap;

    /**
     * Metric values map, key is metric code, value is metric value.
     */
    private Map<String, TypedValue> valueMap;


    /**
     * Custom serializer for AggregationVO.
     * Flattens dims map entries and metricCode/metricValue to the top level of JSON.
     *
     * @author Rongjin Zhang
     */
    static class AggregationVOSerializer extends JsonSerializer<AggregationVO> {

        @Override
        public void serialize(AggregationVO value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            gen.writeStartObject();

            // Flatten dimensionMap entries to top level
            Map<String, TypedValue> dimensionMap = value.getDimensionMap();
            if (dimensionMap != null) {
                for (Map.Entry<String, TypedValue> entry : dimensionMap.entrySet()) {
                    String dimKey = entry.getKey();
                    TypedValue dimValue = entry.getValue();
                    gen.writeFieldName(dimKey);
                    if (dimValue != null) {
                        serializers.findValueSerializer(TypedValue.class).serialize(dimValue, gen, serializers);
                    } else {
                        gen.writeNull();
                    }
                }
            }

            // Flatten valueMap entries to top level
            Map<String, TypedValue> valueMap = value.getValueMap();
            if (valueMap != null) {
                for (Map.Entry<String, TypedValue> entry : valueMap.entrySet()) {
                    String valKey = entry.getKey();
                    TypedValue valValue = entry.getValue();
                    gen.writeFieldName(valKey);
                    if (valValue != null) {
                        serializers.findValueSerializer(TypedValue.class).serialize(valValue, gen, serializers);
                    } else {
                        gen.writeNull();
                    }
                }
            }

            gen.writeEndObject();
        }
    }
}
