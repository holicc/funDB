package org.holicc.cmd.impl;

import org.holicc.datastruct.SortNode;
import org.holicc.db.DataBase;
import org.holicc.db.LocalDataBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class SortedSetCommandTest {

    DataBase db = new LocalDataBase();
    SortedSetCommand command = new SortedSetCommand(db);

    @MethodSource
    @ParameterizedTest
    void zadd(String key, SortNode[] nodes, int except) {
        int actual = command.zadd(key, nodes);
        Assertions.assertEquals(except, actual);
    }

    static Stream<Arguments> zadd() {
        return Stream.of(
                Arguments.of("myzset", new SortNode[]{new SortNode("one", 1)}, 1),
                Arguments.of("myzset", new SortNode[]{new SortNode("one", 1), new SortNode("one", 1)}, 1),
                Arguments.of("myzset", new SortNode[]{new SortNode("one", 1), new SortNode("two", 2), new SortNode("three", 3)}, 3)
        );
    }
}