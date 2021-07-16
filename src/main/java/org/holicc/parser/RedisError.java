package org.holicc.parser;

import java.util.Objects;

public record RedisError(String error) {

    public static RedisError nullBulkString() {
        return new RedisError("Null Bulk String");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedisError that = (RedisError) o;
        return Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error);
    }
}
