package org.holicc.util;

public record Pair<K, V>(K key, V val) {

    public static <K, V> Pair<K, V> of(K k, V v) {
        return new Pair<>(k, v);
    }
}
