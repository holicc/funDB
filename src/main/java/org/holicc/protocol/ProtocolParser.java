package org.holicc.protocol;


public interface ProtocolParser {

    RedisValue parse(byte[] buffer) throws ProtocolParseException;

}
