package org.holicc.datastruct;

/**
 * to implement sortedSet
 * <p>
 * SortedSets = TreeSet() + SortNode
 */
public record SortNode(
        String value,
        float score
) implements Comparable<SortNode> {

    @Override
    public int compareTo(SortNode o) {
        return Float.compare(o.score(), score());
    }
}
