package org.holicc.cmd.impl;

import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.db.DataPolicy;
import org.holicc.db.LocalDataBase;
import org.holicc.parser.DefaultProtocolParser;
import org.holicc.parser.ProtocolParseException;
import org.holicc.server.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

class StringCommandTest {

    StringCommand command = new StringCommand();
    DefaultProtocolParser parser = new DefaultProtocolParser();
    DataBase dataBase = new LocalDataBase();

    @ParameterizedTest
    @MethodSource
    void set(String cmd, DataEntry except) throws ProtocolParseException {
        String[] cmds = cmd.split(" ");
        String[] options = null;
        if (cmds.length > 3) {
            options = Arrays.copyOfRange(cmds, 3, cmds.length - 1);
        }
        Response response = command.set(dataBase, cmds[1], cmds[2], options);
        //
        Assertions.assertEquals("+OK\r\n", new String(response.toBytes()));
        //
        DataEntry entry = dataBase.getEntry(cmds[1]);
        Assertions.assertEquals(except.getKey(), entry.getKey());
        Assertions.assertEquals(except.getTtl(), entry.getTtl());
        Assertions.assertEquals(except.getValue(), entry.getValue());
        Assertions.assertEquals(except.getPolicy(), entry.getPolicy());
    }

    static Stream<Arguments> set() {
        return Stream.of(
                Arguments.of("set a 1", new DataEntry("a", "1")),
                Arguments.of("set a 1 EX 1", new DataEntry("a", "1", System.currentTimeMillis() + 1000L, DataPolicy.DEFAULT)),
                Arguments.of("set a 1 PX 2000", new DataEntry("a", "1", System.currentTimeMillis() + 2000, DataPolicy.DEFAULT))
        );
    }

}