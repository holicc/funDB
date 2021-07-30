package org.holicc.db;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalDataBase implements DataBase {

    private final Map<String, DataEntry> entryMap = new ConcurrentHashMap<>();

    @Override
    public void persistInMemory(DataEntry entry) {
        entryMap.put(entry.getKey(), entry);
    }

    @Override
    public DataEntry getEntry(String key) {
        if (!entryMap.containsKey(key)) return null;
        DataEntry entry = entryMap.get(key);
        long ttl = entry.getTtl();
        if (ttl > 0 && ttl > System.currentTimeMillis()) {
            return entry;
        } else {
            DataEntry remove = entryMap.remove(key);
            // TODO ObjectPool recycle
            return null;
        }
    }
}
