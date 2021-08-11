package org.holicc.cmd.impl;

import org.holicc.cmd.JedisCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

public class KeysCommand implements JedisCommand {


    @Command(name = "TTL", minimumArgs = 1, description = "https://redis.io/commands/ttl")
    public long ttl(DataBase db, String key) {
        DataEntry entry = db.getEntry(key);
        if (entry == null) return -2;
        if (entry.getTtl() == null) return -1;
        return Duration.between(LocalDateTime.now(), entry.getTtl()).toSeconds();
    }

    @Command(name = "KEYS", minimumArgs = 1, description = "https://redis.io/commands/keys")
    public Set<String> keys(DataBase db, String pattern) {
        return db.keys(pattern);
    }
}
