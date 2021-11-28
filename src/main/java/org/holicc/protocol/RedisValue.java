package org.holicc.protocol;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record RedisValue(
        String command,
        String key,
        Optional<Object> value,
        Optional<String[]> options
) {

    public int size() {
        int addKey  = Objects.isNull(key)?0:1;
        return value.map(v -> {
            if (v instanceof List) {
                return ((List<?>) v).size();
            }
            return 0;
        }).orElse(0)+addKey;
    }

    public boolean isEmpty() {
        return command == null;
    }
}
