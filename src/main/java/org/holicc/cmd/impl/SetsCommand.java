package org.holicc.cmd.impl;

import org.holicc.cmd.FunDBCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;

import java.util.*;
import java.util.stream.Collectors;

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

    @Command(name = "SPOP", description = "https://redis.io/commands/spop")
    public List<String> spop(String key, int count) {
        return db.getEntry(key).map(entry -> {
            Set<String> set = entry.getValue();
            Iterator<String> iterator = set.iterator();
            int i = count == 0 ? 1 : count;
            List<String> r = new ArrayList<>();
            while (iterator.hasNext() && i-- > 0) {
                String next = iterator.next();
                if (set.remove(next)) r.add(next);
            }
            return r;
        }).orElse(List.of());
    }
}
