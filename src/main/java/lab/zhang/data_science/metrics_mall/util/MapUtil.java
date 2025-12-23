package lab.zhang.data_science.metrics_mall.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


public final class MapUtil {

    @SafeVarargs
    public static <K, V> Map<K, V> mapByField(Function<V, K> keyExtractor,
                                              Function<Object[], Collection<V>> collSupplier,
                                              Object... params) {
        Collection<V> coll = collSupplier.apply(params);

        if (coll == null || coll.isEmpty()) {
            return Collections.emptyMap();
        }

        return coll.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        keyExtractor,
                        Function.identity(),
                        (a, b) -> a
                ));
    }

}
