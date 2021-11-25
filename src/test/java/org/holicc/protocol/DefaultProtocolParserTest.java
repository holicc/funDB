package org.holicc.protocol;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultProtocolParserTest {

    private final ProtocolParser parser = new DefaultProtocolParser();

    @ParameterizedTest
    @ValueSource(strings = {"+OK\r\n", "+PONG\r\n"})
    void testParseSimpleString(String req) throws ProtocolParseException {
        String ans = req.substring(1, req.length() - 2);
        RedisValue<String> parse = parser.parse(req.getBytes(StandardCharsets.UTF_8), 0);
        String value = parse.getValue();
        assertEquals(ans, value);
    }

    @ParameterizedTest
    @ValueSource(strings = {":1\r\n", ":10123\r\n", ":0\r\n", ":-1\r\n", ":-11122333\r\n"})
    void testParseInteger(String req) throws ProtocolParseException {
        Integer ans = Integer.parseInt(req.substring(1, req.length() - 2));
        RedisValue<Integer> parse = parser.parse(req.getBytes(StandardCharsets.UTF_8), 0);
        Integer value = parse.getValue();
        assertEquals(ans, value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-Error message\r\n", "-ERR unknown command 'foobar'\r\n", "-WRONGTYPE Operation against a key holding the wrong kind of value\r\n"})
    void testParseError(String req) throws ProtocolParseException {
        RedisError error = new RedisError(req.substring(1, req.length() - 2));
        RedisValue<RedisError> parse = parser.parse(req.getBytes(StandardCharsets.UTF_8), 0);
        assertEquals(error, parse.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"$6\r\nfoobar\r\n", "$12\r\nfoobarfoobar\r\n", "$0\r\n\r\n"})
    void testParseBulkString(String req) throws ProtocolParseException {
        String ans = req.substring(req.indexOf('\n') + 1, req.lastIndexOf('\r'));
        RedisValue<byte[]> parse = parser.parse(req.getBytes(StandardCharsets.UTF_8), 0);
        byte[] value = parse.getValue();
        assertEquals(ans, new String(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "*3\r\n$3\r\nfoo\r\n$3\r\nbar\r\n:1\r\n"
    })
    void testParseArray(String req) throws ProtocolParseException {

        RedisValue<List<RedisValue>> parse = parser.parse(req.getBytes(StandardCharsets.UTF_8), 0);
        List<RedisValue> value = parse.getValue();
        assertEquals("foo", new String((byte[]) value.get(0).getValue()));
        assertEquals("bar", new String((byte[]) value.get(1).getValue()));
        assertEquals(1, (Integer) value.get(2).getValue());

    }
}