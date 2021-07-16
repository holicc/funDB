package org.holicc.parser;

public interface ProtocolParser {

    <T> RedisValueType<T> parse(byte[] data,int pos) throws ProtocolParseException;

}
