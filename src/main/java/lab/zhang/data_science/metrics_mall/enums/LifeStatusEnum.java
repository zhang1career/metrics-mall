package lab.zhang.data_science.metrics_mall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LifeStatusEnum {
    OFFLINE(0),
    DEV(1),
    TEST(2),
    GRAY(3),
    ONLINE(4),
    DEPRECATED(5),
    ;


    private final Integer id;


    /**
     * Get enum by id.
     * @param id life status id
     * @return LifeStatusEnum
     */
    public static LifeStatusEnum fromId(Integer id) {
        if (id == null) {
            return null;
        }
        for (LifeStatusEnum type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}
