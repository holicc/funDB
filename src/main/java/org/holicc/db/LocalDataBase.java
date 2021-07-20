package org.holicc.db;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalDataBase implements DataBase {

    private Map<String, Object> set = new ConcurrentHashMap<>();

    private Map<String, Long> ttlMap = new ConcurrentHashMap<>();


    @Override
    public void persistInMemory(DataEntry entry) {
        String key = entry.getKey();
        long ttl = entry.getTtl();
        if (ttl > 0) ttlMap.put(key, entry.getTtl());
        //
        set.put(key, entry.getValue());
    }
}
