package org.holicc.cmd.impl;

import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.db.LocalDataBase;
import org.holicc.server.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

class KeysCommandTest {

    KeysCommand command = new KeysCommand();
    DataBase dataBase = new LocalDataBase();

    @BeforeEach
    void setTestData() {
        dataBase.persistInMemory(new DataEntry("hello", 1));
        dataBase.persistInMemory(new DataEntry("heeello", 2));
        dataBase.persistInMemory(new DataEntry("hallo", 2));
        dataBase.persistInMemory(new DataEntry("hbllo", 2));
    }

    @ParameterizedTest
    @MethodSource
    void keys(String key, Response except) {
        Response response = command.keys(dataBase, key);
        System.out.println(response.getMsg());
        Assertions.assertArrayEquals(except.toBytes(), response.toBytes());
    }

    static Stream<Arguments> keys() {
        return Stream.of(
                Arguments.of("", Response.EmptyArrayReply()),
                Arguments.of("h\\*llo", Response.EmptyArrayReply()),
                Arguments.of("h\\[a-z]llo", Response.EmptyArrayReply()),
                Arguments.of("h?llo", Response.ArrayReply(List.of("hello", "hallo", "hbllo"))),
                Arguments.of("h*llo", Response.ArrayReply(List.of("hello", "hallo", "hbllo", "heeello"))),
                Arguments.of("h[ae]llo", Response.ArrayReply(List.of("hello", "hallo"))),
                Arguments.of("h[^e]llo", Response.ArrayReply(List.of("hallo", "hbllo"))),
                Arguments.of("h[a-b]llo", Response.ArrayReply(List.of("hello", "hallo", "hbllo")))
        );
    }


}