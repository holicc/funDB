package org.holicc.cmd.impl;

import org.holicc.cmd.JedisCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.cmd.exception.CommandException;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.db.DataPolicy;

import java.util.Locale;

public class StringCommand implements JedisCommand {

    @Command(name = "SET", minimumArgs = 2, description = "https://redis.io/commands/set")
    public String set(DataBase db, String key, String value, String... options) {
        DataPolicy policy = DataPolicy.DEFAULT;
        long ttl = 0;
        // parse Options
        if (options != null && options.length >= 1) {
            String option = options[0].toUpperCase(Locale.ROOT);
            long time = Long.parseLong(options[1]);
            long now = System.currentTimeMillis();
            switch (option) {
                case "EX" -> ttl = time * 1000L + now;
                case "EXAT" -> ttl = time * 1000L;
                case "PX" -> ttl = now + time;
                case "PXAT" -> ttl = time;
                case "NX" -> policy = DataPolicy.PUT_IF_ABSENT;
                case "XX" -> policy = DataPolicy.PUT_IF_EXISTS;
            }
        }
        // to db
        DataEntry entry = new DataEntry(key, value, ttl, policy);
        db.persistInMemory(entry);
        return "";
    }

    @Command(name = "GET", minimumArgs = 1, description = "https://redis.io/commands/get")
    public String get(DataBase db, String key) throws CommandException {
        if (key == null || key.equals("")) throw new CommandException("empty key");
        DataEntry entry = db.getEntry(key);
        if (entry == null) return null;
        return entry.getValue().toString();
    }
}
