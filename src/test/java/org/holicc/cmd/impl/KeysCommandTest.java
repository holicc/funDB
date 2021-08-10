package org.holicc.cmd.impl;

import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.db.LocalDataBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
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
        dataBase.persistInMemory(new DataEntry("other", 3));
    }

    @ParameterizedTest
    @MethodSource
    void keys(String key, Set<String> except) {
        Set<String> response = command.keys(dataBase, key);
        Assertions.assertEquals(response.size(), except.size());
        Assertions.assertTrue(response.containsAll(except));
    }

    static Stream<Arguments> keys() {
        return Stream.of(
                Arguments.of("", Set.of()),
                Arguments.of("h\\*llo", Set.of()),
                Arguments.of("h\\[a-z]llo", Set.of()),
                Arguments.of("h?llo", Set.of("hello", "hallo", "hbllo")),
                Arguments.of("h*llo", Set.of("hello", "hallo", "hbllo", "heeello")),
                Arguments.of("h[ae]llo", Set.of("hello", "hallo")),
                Arguments.of("h[^e]llo", Set.of("hallo", "hbllo")),
                Arguments.of("h[a-e]llo", Set.of("hello", "hallo", "hbllo"))
        );
    }


}