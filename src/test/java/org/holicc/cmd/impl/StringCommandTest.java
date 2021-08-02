package org.holicc.cmd.impl;

import org.holicc.db.DataBase;
import org.holicc.db.LocalDataBase;
import org.holicc.parser.DefaultProtocolParser;
import org.holicc.parser.ProtocolParseException;
import org.holicc.parser.RedisValue;
import org.holicc.server.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.List;

class StringCommandTest {

    StringCommand command = new StringCommand();
    DefaultProtocolParser parser = new DefaultProtocolParser();
    DataBase dataBase = new LocalDataBase();

    @ParameterizedTest
    @ValueSource(strings = {
            "set a 1",
            "set a abc",

    })
    void set(String cmd) throws ProtocolParseException {
        RedisValue redisValue = parser.parse(cmd.getBytes(StandardCharsets.UTF_8), 0);
        Object value = redisValue.getValue();
        List<RedisValue> args = (List<RedisValue>) value;
        Response response = command.set(dataBase, args.subList(1, args.size()));
        Assertions.assertArrayEquals("-OK\r\n".getBytes(StandardCharsets.UTF_8), response.toBytes());
    }


}