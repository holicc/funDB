package org.holicc.cmd.impl;

import org.holicc.cmd.FunDBCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.util.Pair;

import java.util.*;

/**
 * LinkedList instead
 */
public record ListsCommand(DataBase db) implements FunDBCommand {

    @Command(name = "LPUSH", minimumArgs = 2, persistence = true, description = "https://redis.io/commands/lpush")
    public int lpush(String key, String... value) {
        DataEntry entry = db.getEntry(key).orElse(new DataEntry(key, new LinkedList<>()));
        LinkedList<String> list = entry.getValue();
        for (String val : value) {
            list.push(val);
        }
        db.persistInMemory(entry);
        return list.size();
    }

    @Command(name = "RPUSH", minimumArgs = 2, persistence = true, description = "https://redis.io/commands/rpush")
    public int rpush(String key, String... value) {
        DataEntry entry = db.getEntry(key).orElse(new DataEntry(key, new LinkedList<>()));
        LinkedList<String> list = entry.getValue();
        for (String val : value) {
            list.offerLast(val);
        }
        db.persistInMemory(entry);
        return list.size();
    }

    @Command(name = "LRANGE", minimumArgs = 3, description = "https://redis.io/commands/lrange")
    public List<String> lrange(String key, int start, int end) {
        Optional<DataEntry> entry = db.getEntry(key);
        if (entry.isPresent()) {
            LinkedList<String> list = entry.get().getValue();
            if (list.isEmpty() || start >= list.size()) return List.of();
            if (start < 0) start = -start > list.size() ? -start % list.size() : list.size() + start;
            if (start == end) return List.of(list.get(start));
            if (start > end) return List.of();
            if (end >= list.size()) end = list.size();
            else end += 1;
            return list.subList(start, end);
        }
        return List.of();
    }

    @Command(name = "LLEN", description = "https://redis.io/commands/llen")
    public int llen(String key) {
        return db.getEntry(key).map(entry -> {
            LinkedList<String> list = entry.getValue();
            return list.size();
        }).orElse(0);
    }

    @Command(name = "LPOP", persistence = true, description = "https://redis.io/commands/lpop")
    public List<String> lpop(String key, int count) {
        return db.getEntry(key).map(entry -> {
            LinkedList<String> list = entry.getValue();
            if (list.isEmpty()) return null;
            if (count == 0) return List.of(list.pop());
            if (list.size() <= count) {
                db.delEntry(key);
                Collections.reverse(list);
                return list;
            }
            List<String> r = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                r.add(list.pollLast());
            }
            return r;
        }).orElse(List.of());
    }
}
