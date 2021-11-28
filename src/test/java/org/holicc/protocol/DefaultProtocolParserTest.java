package org.holicc.protocol;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultProtocolParserTest {

    private final ProtocolParser parser = new DefaultProtocolParser();

    @ParameterizedTest
    @ValueSource(strings = {"+OK\r\n", "+PONG\r\n"})
    void testParseSimpleString(String req) throws ProtocolParseException {
        String ans = req.substring(1, req.length() - 2);
        RedisValue parse = parser.parse(req.getBytes(StandardCharsets.UTF_8));
        assertEquals(ans, parse.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {":1\r\n", ":10123\r\n", ":0\r\n", ":-1\r\n", ":-11122333\r\n"})
    void testParseInteger(String req) throws ProtocolParseException {
        RedisValue parse = parser.parse(req.getBytes(StandardCharsets.UTF_8));
        assertEquals(Long.parseLong(req.substring(1, req.length() - 2)),  parse.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-Error message\r\n", "-ERR unknown command 'foobar'\r\n", "-WRONGTYPE Operation against a key holding the wrong kind of value\r\n"})
    void testParseError(String req) throws ProtocolParseException {
        RedisValue parse = parser.parse(req.getBytes(StandardCharsets.UTF_8));
        assertEquals(req.substring(1, req.length() - 2), parse.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"$6\r\nfoobar\r\n", "$12\r\nfoobarfoobar\r\n", "$0\r\n\r\n"})
    void testParseBulkString(String req) throws ProtocolParseException {
        RedisValue parse = parser.parse(req.getBytes(StandardCharsets.UTF_8));
        assertEquals(req.substring(req.indexOf('\n') + 1, req.lastIndexOf('\r')), parse.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "*3\r\n$3\r\nset\r\n$3\r\nbar\r\n:1\r\n"
    })
    void testParseArray(String req) throws ProtocolParseException {
        RedisValue parse = parser.parse(req.getBytes(StandardCharsets.UTF_8));
        assertEquals("set", parse.command());
        assertEquals("bar", parse.key());
        List<Integer> ary = List.of(1);
        Object o = parse.value();
        assertInstanceOf(List.class, o);
        assertTrue(((List<Object>) o).stream().allMatch(i -> ary.contains(1)));
    }
}