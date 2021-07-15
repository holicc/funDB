package org.holicc.parser;

public interface ProtocolParser {

    RedisValueType parse(byte[] data) throws ProtocolParseException;

}
