package org.holicc.cmd.impl;

import org.holicc.cmd.FunDBCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public record ListsCommand(DataBase db) implements FunDBCommand {

    @Command(name = "LPUSH", minimumArgs = 2, persistence = true, description = "https://redis.io/commands/lpush")
    public int lpush(String key, String... value) {
        DataEntry entry = db.getEntry(key);
        LinkedList<String> list;
        if (entry != null) {
            list = entry.getValue();
        } else {
            list = new LinkedList<>();
            entry = new DataEntry(key, list);
        }
        for (String val : value) {
            list.push(val);
        }
        //
        db.persistInMemory(entry);
        return list.size();
    }

    @Command(name = "LRANGE", minimumArgs = 3, description = "https://redis.io/commands/lrange")
    public List<String> lrange(String key, int start, int end) {
        DataEntry entry = db.getEntry(key);
        if (entry == null || start > end) return List.of();
        LinkedList<String> list = entry.getValue();
        if (start < 0) start = list.size() + start;
        if (end < 0) end = list.size() + end - 1;
        else if (end >= list.size()) end = list.size() - 1;
        if (start > end) return List.of();
        if (start == end) return List.of(list.get(start));
        return list.subList(start, end + 1);
    }

    @Command(name = "LLEN", description = "https://redis.io/commands/llen")
    public int llen(String key) {
        DataEntry entry = db.getEntry(key);
        if (entry == null) return 0;
        LinkedList<String> list = entry.getValue();
        return list.size();
    }

    @Command(name = "LPOP", persistence = true, description = "https://redis.io/commands/lpop")
    public List<String> lpop(String key, int count) {
        DataEntry entry = db.getEntry(key);
        if (entry == null || count <= 0) return List.of();
        LinkedList<String> list = entry.getValue();
        if (list == null || list.isEmpty()) return null;
        List<String> r = new ArrayList<>();
        while (count-- > 0 && !list.isEmpty()) {
            r.add(list.pollLast());
        }
        // should remove the key if lists is empty
        if (list.isEmpty()) db.delEntry(key);
        return r;
    }
}
