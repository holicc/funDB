package org.holicc.cmd.impl;

import org.holicc.cmd.exception.CommandException;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.db.DataPolicy;
import org.holicc.db.LocalDataBase;
import org.holicc.parser.ProtocolParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.holicc.cmd.impl.TestUtils.equalsEntry;

class StringCommandTest {

    StringCommand command = new StringCommand();
    DataBase dataBase = new LocalDataBase();

    @ParameterizedTest
    @MethodSource
    void set(String cmd, DataEntry except) throws ProtocolParseException {
        String[] cmds = cmd.split(" ");
        String[] options = null;
        if (cmds.length > 3) {
            options = Arrays.copyOfRange(cmds, 3, cmds.length);
        }
        String response = command.set(dataBase, cmds[1], cmds[2], options);
        //
        Assertions.assertNotNull(response);
        //
        DataEntry entry = dataBase.getEntry(cmds[1]);
        equalsEntry(except, entry);
    }


    static Stream<Arguments> set() {
        return Stream.of(
                Arguments.of("set a 1", new DataEntry("a", "1")),
                Arguments.of("set a 1 EX 1", new DataEntry("a", "1", System.currentTimeMillis() + 1000L, DataPolicy.DEFAULT)),
                Arguments.of("set a 1 PX 2000", new DataEntry("a", "1", System.currentTimeMillis() + 2000, DataPolicy.DEFAULT))
        );
    }

    @ParameterizedTest
    @MethodSource
    void get(String cmd, DataEntry except) throws CommandException {
        String[] cmds = cmd.split(" ");
        dataBase.persistInMemory(except);
        String response = command.get(dataBase, cmds[1]);
        Assertions.assertEquals(except.getValue(), response);
    }

    static Stream<Arguments> get() {
        return Stream.of(
                Arguments.of("get a", new DataEntry("a", "1"))
        );
    }


}