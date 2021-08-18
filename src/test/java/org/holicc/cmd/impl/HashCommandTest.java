package org.holicc.cmd.impl;

import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.db.LocalDataBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.holicc.cmd.impl.TestUtils.equalsEntry;

class HashCommandTest {

    DataBase dataBase = new LocalDataBase();
    HashCommand command = new HashCommand(dataBase);

    @BeforeEach
    void prepareData() {
        Map<String, DataEntry> map = new HashMap<>();
        map.put("b", new DataEntry("b", "1"));
        map.put("c", new DataEntry("c", "2"));
        map.put("d", new DataEntry("d", "3"));
        dataBase.persistInMemory(new DataEntry("a", map));
    }

    @ParameterizedTest
    @MethodSource
    void hset(String key, String field, Object val, DataEntry except) {
        int r = command.hset(key, field, val);
        Assertions.assertEquals(1, r);

        DataEntry entry = dataBase.getEntry(key);
        Map<String, DataEntry> map = entry.getValue();

        Assertions.assertNotNull(map);
        equalsEntry(except, map.get(field));
    }

    static Stream<Arguments> hset() {
        return Stream.of(
                Arguments.of("a", "b", "1", new DataEntry("b", "1")),
                Arguments.of("a", "c", 2, new DataEntry("c", 2)),
                Arguments.of("a", "d", "3", new DataEntry("d", "3"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void hget(String key, String field, String except) {
        String r = command.hget(key, field);
        Assertions.assertEquals(except, r);
    }

    static Stream<Arguments> hget() {
        return Stream.of(
                Arguments.of("a", "b", "1"),
                Arguments.of("a", "c", "2"),
                Arguments.of("a", "d", "3"),
                Arguments.of("a", "e", null)
        );
    }
}