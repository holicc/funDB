package org.holicc.parser;

public class RedisError {
    private String error;

    public RedisError(String error) {
        this.error = error;
    }

    public static RedisError nullBulkString() {
        return new RedisError("Null Bulk String");
    }
}
