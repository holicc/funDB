package org.holicc.cmd.impl;

import org.holicc.cmd.FunDBCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.datastruct.SortNode;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * TreeSet instead
 */
public record SortedSetCommand(DataBase db) implements FunDBCommand {

    @Command(name = "ZADD", minimumArgs = 3, description = "https://redis.io/commands/zadd")
    public int zadd(String key, SortNode[] nodes) {
        int r = 0;
        DataEntry entry = db.getEntry(key).orElseGet(() -> {
            DataEntry e = new DataEntry(key, new TreeSet<>());
            db.persistInMemory(e);
            return e;
        });
        TreeSet<SortNode> sortedSet = entry.getValue();
        for (SortNode node : nodes) {
            if (sortedSet.add(node)) r++;
        }
        return r;
    }

    @Command(name = "ZPOPMIN", description = "https://redis.io/commands/zpopmin")
    public List<SortNode> zpopmin(String key, int count) {
        Optional<DataEntry> entry = db.getEntry(key);
        if (entry.isPresent()) {
            TreeSet<SortNode> sortNodes = entry.get().getValue();
            if (sortNodes.isEmpty()) return List.of();
            if (count == 0) return List.of(Objects.requireNonNull(sortNodes.pollLast()));
            List<SortNode> list = new ArrayList<>(count);
            for (int i = 0; !sortNodes.isEmpty() && i < count; i++) {
                list.add(sortNodes.pollLast());
            }
            return list;
        }
        return List.of();
    }
}
