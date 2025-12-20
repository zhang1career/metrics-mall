package lab.zhang.data_science.metrics_mall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Environment type enumeration
 *
 * @author Rongjin Zhang
 */
@Getter
@AllArgsConstructor
public enum EnvEnum {

    DEV(0, "Developing Environment"),

    TEST(1, "Testing Environment"),

    PROD(2, "Production Environment"),

    GRAY(3, "Gray Environment");


    private final Integer id;

    private final String name;


    /**
     * Get EnvironmentEnum by ID
     *
     * @param id enumeration ID
     * @return EnvironmentEnum enum, or null if not found
     */
    public static EnvEnum fromId(Integer id) {
        if (id == null) {
            return null;
        }
        for (EnvEnum env : values()) {
            if (env.id.equals(id)) {
                return env;
            }
        }
        return null;
    }
}

