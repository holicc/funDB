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
        DataEntry fieldsEntry = new DataEntry(field, value);
        db.getEntry(key).ifPresentOrElse(
                entry -> {
                    Map<String, DataEntry> map = entry.getValue();
                    map.put(field, fieldsEntry);
                },
                () -> {
                    Map<String, DataEntry> map = new HashMap<>();
                    DataEntry entry = new DataEntry(key, map);
                    map.put(field, fieldsEntry);
                    db.persistInMemory(entry);
                });
        return 1;
    }


    @Command(name = "HGET", minimumArgs = 2, description = "https://redis.io/commands/hget")
    public String hget(String key, String field) {
        return db.getEntry(key).map(entry -> {
            Map<String, DataEntry> map = entry.getValue();
            if (map.containsKey(field)) return (String) map.get(field).getValue();
            else return null;
        }).orElse(null);
    }
}
