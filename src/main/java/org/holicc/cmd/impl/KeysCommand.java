package org.holicc.cmd.impl;

import org.holicc.cmd.FunDBCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

public record KeysCommand(DataBase db) implements FunDBCommand {

    @Command(name = "TTL", minimumArgs = 1, description = "https://redis.io/commands/ttl")
    public long ttl(String key) {
        DataEntry entry = db.getEntry(key);
        if (entry == null) return -2;
        if (entry.getTtl() == null) return -1;
        return Duration.between(LocalDateTime.now(), entry.getTtl()).toSeconds();
    }

    @Command(name = "KEYS", minimumArgs = 1, description = "https://redis.io/commands/keys")
    public Set<String> keys(String pattern) {
        return db.keys(pattern);
    }

    @Command(name = "EXISTS", description = "https://redis.io/commands/exists")
    public int exists(String... keys) {
        int count = 0;
        for (String key : keys) {
            if (db.getEntry(key) != null) count++;
        }
        return count;
    }

    @Command(name = "EXPIRE", description = "https://redis.io/commands/expire")
    public int expire(String key, int second, String... options) {
        DataEntry entry = db.getEntry(key);
        if (entry == null) return 0;
        entry.setTtl(LocalDateTime.now().plusSeconds(second));
        return 1;
    }

}
