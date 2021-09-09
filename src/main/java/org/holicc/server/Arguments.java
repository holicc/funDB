package org.holicc.server;

import java.util.*;

/**
 * O(1) put and get
 */
public class Arguments {

    private final Map<Class<?>, LinkedList<Object>> map = new HashMap<>();

    public void put(Class<?> key, Object o) {
        if (map.containsKey(key)) {
            map.get(key).add(o);
        } else {
            LinkedList<Object> q = new LinkedList<>();
            q.add(o);
            map.put(key, q);
        }
    }

    public Object get(Class<?> type) {
        if (!map.containsKey(type)) return null;
        return map.get(type).pop();
    }
}
