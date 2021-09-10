package org.holicc.datastruct;

public class SortedSet<T> {

    transient int size = 0;

    transient Node<T> head;

    transient Node<T> tail;

    transient int level = 0;

    private static class Level {
        Node forward;
        int span;
    }

    private static class Node<T> {
        Level[] levels;
        Node<T> backward;
        float score;
        T val;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {

    }

    public void add() {

    }

    public T get() {

    }
}
