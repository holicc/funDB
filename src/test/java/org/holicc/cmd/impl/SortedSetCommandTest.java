package org.holicc.cmd.impl;

import org.holicc.datastruct.SortNode;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.db.LocalDataBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

class SortedSetCommandTest {

    DataBase db = new LocalDataBase();
    SortedSetCommand command = new SortedSetCommand(db);

    @BeforeEach
    void prepareData() {
        TreeSet<SortNode> set = new TreeSet<>();
        set.add(new SortNode("x", 1));
        set.add(new SortNode("z", 2));
        set.add(new SortNode("y", 3));
        db.persistInMemory(new DataEntry("myzset", set));
    }


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

    @MethodSource
    @ParameterizedTest
    void testZpopmin(String key, int count, List<SortNode> except) {
        List<SortNode> actual = command.zpopmin(key, count);
        Assertions.assertEquals(except, actual);
    }

    static Stream<Arguments> testZpopmin() {
        return Stream.of(
                Arguments.of("mylist", 1, List.of()),
                Arguments.of("myzset", 1, List.of(new SortNode("x", 1))),
                Arguments.of("myzset", 2, List.of(new SortNode("x", 1), new SortNode("z", 2)))
        );
    }

}