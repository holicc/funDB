package org.holicc.datastruct;

import java.util.Objects;

/**
 * to implement sortedSet
 * <p>
 * SortedSets = TreeSet() + SortNode
 */
public record SortNode(
        String value,
        float score
) implements Comparable<SortNode> {

    public static SortNode of(String value, float score) {
        return new SortNode(value, score);
    }

    public int compareTo(SortNode o) {
        return Integer.compare(o.hashCode(), hashCode());
    }


    @Override
    public int hashCode() {
        return Objects.hash(value, score);
    }
}
