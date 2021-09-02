package org.holicc.cmd.impl;

import org.holicc.cmd.FunDBCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;

import java.util.HashMap;
import java.util.Map;

public record HashCommand(DataBase db) implements FunDBCommand {

    @Command(name = "HSET", persistence = true, minimumArgs = 3, description = "https://redis.io/commands/hset")
    public int hset(String key, String field, Object value) {
        DataEntry entry = db.getEntry(key);
        DataEntry fieldsEntry = new DataEntry(field, value);
        if (entry == null) {
            Map<String, DataEntry> map = new HashMap<>();
            map.put(field, fieldsEntry);
            entry = new DataEntry(key, map);
            db.persistInMemory(entry);
        } else {
            Map<String, DataEntry> map = entry.getValue();
            if (map == null) {
                map = new HashMap<>();
            }
            map.put(field, fieldsEntry);
            entry.setValue(map);
        }
        return 1;
    }


    @Command(name = "HGET", minimumArgs = 2, description = "https://redis.io/commands/hget")
    public String hget(String key, String field) {
        DataEntry entry = db.getEntry(key);
        if (entry == null) return null;
        Map<String, DataEntry> map = entry.getValue();
        if (map.containsKey(field)) return map.get(field).getValue();
        else return null;
    }
}
