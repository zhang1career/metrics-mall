package lab.zhang.data_science.metrics_mall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum MetadataTableEnum {
    UNDEFINED(0, ""),
    ENTITY_META(1, "entity_meta"),
    METRIC_META(2, "metric_meta"),
    METRIC_VERSION(3, "metric_version"),
    METRIC_LINEAGE(4, "metric_lineage"),
    DIMENSION(5, "dim"),
    ENTIRY_METRIC_RELATION(6, "x"),
    METRIC_DIMENSION_RELATION(7, "y"),
    OP_LOG(8, "op_log"),
    OP_LOG_DETAIL(9, "op_log_detail"),
    ;


    private final Integer id;
    private final String tableName;


    public static MetadataTableEnum fromId(Integer id) {
        if (id == null) {
            return null;
        }
        for (MetadataTableEnum type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}
