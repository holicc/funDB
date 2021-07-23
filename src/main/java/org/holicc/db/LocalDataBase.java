package org.holicc.db;

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
        return entryMap.get(key);
    }
}
