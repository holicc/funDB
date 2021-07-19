package org.holicc.db;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class LocalDataBase implements DataBase {

    private Map<String, String> map = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> getGlobalSet() {
        return map;
    }

}
