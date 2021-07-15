package org.holicc.parser;

class RedisValueType {

    private DefaultProtocolParser.Word word;

    private Object value;

    public RedisValueType(DefaultProtocolParser.Word word, Object value) {
        this.word = word;
        this.value = value;
    }

}
