package org.holicc.parser;

import java.util.Objects;

public class RedisValueType<T> {

    private DefaultProtocolParser.Word word;

    private T value;

    private RedisValueType() {
    }

    public RedisValueType(DefaultProtocolParser.Word word, T value) {
        this.word = word;
        this.value = value;
    }

    public static <T> RedisValueType<T> nullValue() {
        return new RedisValueType<>();
    }

    public DefaultProtocolParser.Word getWord() {
        return word;
    }

    public T getValue() {
        return Objects.nonNull(value) ? value : null;
    }

    @Override
    public String toString() {
        return "RedisValueType{" +
                "word=" + word +
                ", value=" + value +
                '}';
    }
}
