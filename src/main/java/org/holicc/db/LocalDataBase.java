package org.holicc.db;

import org.holicc.util.Wildcard;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LocalDataBase implements DataBase {

    private final Map<String, DataEntry> entryMap = new HashMap<>();

    @Override
    public void persistInMemory(DataEntry entry) {
        entryMap.put(entry.getKey(), entry);
    }

    @Override
    public DataEntry getEntry(String key) {
        if (!entryMap.containsKey(key)) return null;
        DataEntry entry = entryMap.get(key);
        long ttl = entry.getTtl();
        if (ttl == 0 || ttl > System.currentTimeMillis()) {
            return entry;
        } else {
            DataEntry remove = entryMap.remove(key);
            // TODO ObjectPool recycle
            return null;
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        if (pattern == null || pattern.equals("")) return Set.of();
        Wildcard wildcard = Wildcard.compile(pattern);
        return entryMap.keySet().stream()
                .filter(wildcard::isMatch)
                .collect(Collectors.toSet());
    }


}
