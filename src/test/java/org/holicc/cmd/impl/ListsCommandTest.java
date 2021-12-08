package org.holicc.cmd.impl;

import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.db.LocalDataBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

class ListsCommandTest {

    DataBase db = new LocalDataBase();
    ListsCommand command = new ListsCommand(db);

    @BeforeEach
    void prepareData() {
        db.persistInMemory(new DataEntry("a", new LinkedList<>(List.of("1", "2", "3", "4", "5"))));
    }

    @ParameterizedTest
    @MethodSource
    void testLPush(int except, String key, String... value) {
        int index = command.lpush(key, value);
        Assertions.assertEquals(except, index);
    }

    static Stream<Arguments> testLPush() {
        return Stream.of(
                Arguments.of(1, "b", new String[]{"1"}),
                Arguments.of(3, "b", new String[]{"1", "2", "3"})
        );
    }

    @ParameterizedTest
    @MethodSource
    void testLrange(String key, int start, int end, List<String> except) {
        List<String> actual = command.lrange(key, start, end);
        Assertions.assertLinesMatch(except, actual);
    }

    static Stream<Arguments> testLrange() {
        return Stream.of(
                Arguments.of("a", 0, 1, List.of("1", "2")),
                Arguments.of("a", -1, 1, List.of()),
                Arguments.of("a", -100, 1, List.of("1", "2")),
                Arguments.of("b", -1, 1, List.of()),
                Arguments.of("a", 10, 1, List.of()),
                Arguments.of("a", -2, 2, List.of()),
                Arguments.of("a", 1, 3, List.of("2", "3", "4")),
                Arguments.of("a", -4, 3, List.of("2", "3", "4")),
                Arguments.of("a", 0, 10, List.of("1", "2", "3", "4", "5")),
                Arguments.of("a", 0, 5, List.of("1", "2", "3", "4", "5"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testLpop(String key, int count, List<String> except) {
        List<String> list = command.lpop(key, count);
        Assertions.assertLinesMatch(except, list);
    }

    static Stream<Arguments> testLpop() {
        return Stream.of(
                Arguments.of("a", -1, List.of()),
                Arguments.of("a", 0, List.of()),
                Arguments.of("a", 2, List.of("5", "4")),
                Arguments.of("a", 6, List.of("5", "4", "3", "2", "1"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testRPush(int except, String key, String... value) {
        int index = command.rpush(key, value);
        Assertions.assertEquals(except, index);
    }

    static Stream<Arguments> testRPush() {
        return Stream.of(
                Arguments.of(1, "b", new String[]{"1"}),
                Arguments.of(3, "b", new String[]{"1", "2", "3"})
        );
    }

}