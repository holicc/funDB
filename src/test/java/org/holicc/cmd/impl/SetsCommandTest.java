package org.holicc.cmd.impl;

import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.db.LocalDataBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SetsCommandTest {

    DataBase db = new LocalDataBase();
    SetsCommand command = new SetsCommand(db);

    @BeforeEach
    void prepareData() {
        db.persistInMemory(new DataEntry("myset", new HashSet<>(List.of("test_A"))));
    }

    @MethodSource
    @ParameterizedTest
    void testSpop(String key, int count, List<String> except) {
        List<String> actual = command.spop(key, count);
        assertEquals(except, actual);
    }

    static Stream<Arguments> testSpop() {
        return Stream.of(
                Arguments.of("myset_a", 0, List.of()),
                Arguments.of("myset", 0, List.of("test_A"))
        );
    }

    @MethodSource
    @ParameterizedTest
    void testSadd(String key, String value, int except) {
        int actual = command.sadd(key, value);
        assertEquals(except, actual);
    }

    static Stream<Arguments> testSadd() {
        return Stream.of(
                Arguments.of("myset", "test_A", 0),
                Arguments.of("myset", "test_B", 1)
        );
    }
}