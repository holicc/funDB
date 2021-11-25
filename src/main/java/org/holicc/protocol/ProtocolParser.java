package org.holicc.protocol;


public interface ProtocolParser {

    <T> RedisValue<T> parse(byte[] data, int pos) throws ProtocolParseException;
}
