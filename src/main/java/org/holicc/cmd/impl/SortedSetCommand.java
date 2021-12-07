package org.holicc.cmd.impl;

import org.holicc.cmd.FunDBCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.datastruct.SortNode;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;

import java.util.Objects;
import java.util.TreeSet;

/**
 * TreeSet instead
 */
public record SortedSetCommand(
        DataBase db
) implements FunDBCommand {

    @Command(name = "ZADD", minimumArgs = 3, description = "https://redis.io/commands/zadd")
    public int zadd(String key, SortNode... nodes) {
        int r = 0;
        DataEntry entry = db.getEntry(key).orElse(new DataEntry(key, new TreeSet<>()));
        TreeSet<SortNode> sortedSet = entry.getValue();
        for (SortNode node : nodes) {
            if (sortedSet.add(node)) r++;
        }
        return r;
    }


}
