package org.holicc.protocol;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record RedisValue(
        String command,
        String key,
        Object value,
        Optional<String[]> options
) {

    public <T> T into(Class<?> parameterType) {
        return (T) value;
    }

    public int size() {
        if (value instanceof List) {
            return ((List<?>) value).size();
        } else {
            return Objects.isNull(value) ? 0 : 1;
        }
    }

    public boolean isEmpty() {
        return command == null;
    }
}
