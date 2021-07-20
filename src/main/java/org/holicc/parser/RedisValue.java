package org.holicc.parser;

import java.util.List;
import java.util.Objects;

public class RedisValue<T> {

    private DefaultProtocolParser.Word word;

    private T value;

    private RedisValue() {
    }

    public RedisValue(DefaultProtocolParser.Word word, T value) {
        this.word = word;
        this.value = value;
    }

    public static <T> RedisValue<T> nullValue() {
        return new RedisValue<>();
    }

    public DefaultProtocolParser.Word getWord() {
        return word;
    }

    public T getValue() {
        return Objects.nonNull(value) ? value : null;
    }

    public String getValueAsString() {
        if (value instanceof byte[]) return new String((byte[]) value);
        return value.toString();
    }

    public String getCommand() {
        if (value instanceof byte[]) {
            return new String((byte[]) value);
        }
        if (value instanceof List) {
            return ((List<RedisValue>) value).get(0).getCommand();
        }
        return value.toString();
    }


    @Override
    public String toString() {
        return "RedisValue{" +
                "word=" + word +
                ", value=" + value +
                '}';
    }
}
