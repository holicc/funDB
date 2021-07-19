package org.holicc.parser;


public interface ProtocolParser {

    <T> RedisValue<T> parse(byte[] data, int pos) throws ProtocolParseException;
}
