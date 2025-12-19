package lab.zhang.data_science.metrics_mall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum SnapshotSourceTypeEnum {
    EXTERNAL(0, "external"),
    INTERNAL(1, "internal")
    ;


    private final Integer id;

    private final String tableName;


    public static SnapshotSourceTypeEnum fromId(Integer id) {
        if (id == null) {
            return null;
        }
        for (SnapshotSourceTypeEnum type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}
