package org.holicc.cmd.impl;

import org.holicc.cmd.FunDBCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public record SetsCommand(DataBase db) implements FunDBCommand {

    @Command(name = "SADD", minimumArgs = 2, description = "https://redis.io/commands/sadd")
    public int sadd(String key, String... value) {
        Optional<DataEntry> entry = db.getEntry(key);
        if (entry.isEmpty()) {
            Set<String> set = new HashSet<>();
            Collections.addAll(set, value);
            db.persistInMemory(new DataEntry(key, set));
            return value.length;
        } else {
            DataEntry dataEntry = entry.get();
            Set<String> set = dataEntry.getValue();
            int count = 0;
            for (String val : value) {
                if (set.add(val)) count++;
            }
            return count;
        }
    }
}
