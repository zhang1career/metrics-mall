package lab.zhang.data_science.metrics_mall.config;

import lab.zhang.data_science.metrics_mall.enums.EnvEnum;
import lab.zhang.data_science.metrics_mall.enums.LifeStatusEnum;
import lab.zhang.data_science.metrics_mall.util.EnvUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Life status configuration class
 * Manages the mapping between environment types and allowed life statuses
 *
 * @author Rongjin Zhang
 */
@Slf4j
@Component
public class MetricVersionLifeStatusConfig {

    @Autowired
    private EnvUtil envUtil;

    /**
     * Mapping from environment enum to allowed life statuses
     */
    private static final Map<EnvEnum, Set<LifeStatusEnum>> METRIC_VERSION_LIFE_STATUS_MAP =
            new HashMap<>() {{
                put(EnvEnum.DEV, new HashSet<>(Arrays.asList(LifeStatusEnum.DEV)));
                put(EnvEnum.TEST, new HashSet<>(Arrays.asList(LifeStatusEnum.TEST)));
                put(EnvEnum.GRAY, new HashSet<>(Arrays.asList(LifeStatusEnum.GRAY, LifeStatusEnum.ONLINE, LifeStatusEnum.DEPRECATED)));
                put(EnvEnum.PROD, new HashSet<>(Arrays.asList(LifeStatusEnum.ONLINE, LifeStatusEnum.DEPRECATED)));
            }};

    /**
     * Get allowed metric versions' life statuses based on current environment
     *
     * @return set of allowed life statuses for current environment
     */
    public Set<LifeStatusEnum> getAvailableLifeStatuses() {
        EnvEnum env = envUtil.getEnvEnum();
        Set<LifeStatusEnum> allowedStatuses = METRIC_VERSION_LIFE_STATUS_MAP.get(env);
        if (allowedStatuses == null) {
            throw new IllegalStateException("No life statuses configured for environment: " + env);
        }
        return allowedStatuses;
    }


    /**
     * State transition configuration map
     * Maps each status to the set of allowed target statuses for transition
     */
    private static final Map<LifeStatusEnum, Set<LifeStatusEnum>> METRIC_VERSION_LIFE_STATUS_CHANGE_MAP =
            new HashMap<>() {{
                put(LifeStatusEnum.OFFLINE, new HashSet<>(Arrays.asList(LifeStatusEnum.DEV, LifeStatusEnum.TEST)));
                put(LifeStatusEnum.DEV, new HashSet<>(Arrays.asList(LifeStatusEnum.OFFLINE, LifeStatusEnum.TEST)));
                put(LifeStatusEnum.TEST, new HashSet<>(Arrays.asList(LifeStatusEnum.OFFLINE, LifeStatusEnum.GRAY)));
                put(LifeStatusEnum.GRAY, new HashSet<>(Arrays.asList(LifeStatusEnum.OFFLINE, LifeStatusEnum.ONLINE)));
                put(LifeStatusEnum.ONLINE, new HashSet<>(Arrays.asList(LifeStatusEnum.OFFLINE)));
            }};

    /**
     * Get allowed life status transitions for a given current status
     *
     * @param currentStatus current life status
     * @return set of allowed target life statuses for transition
     */
    public Set<LifeStatusEnum> getAvailableLifeStatusTransitions(LifeStatusEnum currentStatus) {
        Set<LifeStatusEnum> allowedTransitions = METRIC_VERSION_LIFE_STATUS_CHANGE_MAP.get(currentStatus);
        if (allowedTransitions == null) {
            throw new IllegalStateException("No life status transitions configured for status: " + currentStatus);
        }
        return allowedTransitions;
    }
}

