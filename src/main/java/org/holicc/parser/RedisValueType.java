package org.holicc.parser;

import java.util.Objects;

class RedisValueType<T> {

    private DefaultProtocolParser.Word word;

    private T value;

    public RedisValueType(DefaultProtocolParser.Word word, T value) {
        this.word = word;
        this.value = value;
    }

    public T getValue() {
        return Objects.nonNull(value) ? value : null;
    }
}
