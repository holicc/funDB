package org.holicc.protocol;


import java.nio.ByteBuffer;
import java.util.Optional;

public interface ProtocolParser {

    char INTEGER_VALUE = ':';
    char SIMPLE_STRING_VALUE = '+';
    char BULK_STRING_VALUE = '$';
    char ERROR_VALUE = '-';
    char ARRAY_VALUE = '*';

    RedisValue EMPTY_VALUE = new RedisValue("", "", Optional.empty(), Optional.empty());

    RedisValue parse(byte[] buffer) throws ProtocolParseException;

}
