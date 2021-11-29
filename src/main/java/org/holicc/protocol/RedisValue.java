package org.holicc.protocol;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record RedisValue(
        String command,
        String key,
        Object value,
        String[] options
) {

    public int size() {
        int addKey = Objects.isNull(key) ? 0 : 1;
        if (value instanceof List) {
            return ((List<?>) value).size() + addKey;
        }
        return addKey;
    }

    public Object value() {
        if (value instanceof List && ((List<?>) value).size() == 1) {
            return ((List<?>) value).get(0);
        }
        return value;
    }

    public boolean isEmpty() {
        return command == null;
    }
}
