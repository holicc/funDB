package org.holicc.cmd.impl;

import org.holicc.cmd.FunDBCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.cmd.exception.CommandException;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.db.DataPolicy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

public record StringCommand(DataBase db) implements FunDBCommand {


    @Command(name = "SET", persistence = true, minimumArgs = 2, description = "https://redis.io/commands/set")
    public String set(String key, String value, String... options) throws CommandException {
        DataPolicy policy = DataPolicy.DEFAULT;
        LocalDateTime ttl = null;
        // parse Options
        if (options != null && options.length >= 1) {
            String option = options[0].toUpperCase(Locale.ROOT);
            switch (option) {
                case "EX" -> ttl = LocalDateTime.now().plusSeconds(Long.parseLong(options[1]));
                case "PX" -> ttl = LocalDateTime.now().plusSeconds(Long.parseLong(options[1]) / 1000);
                case "EXAT" -> ttl = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(options[1]) * 1000L), ZoneId.systemDefault());
                case "PXAT" -> ttl = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(options[1])), ZoneId.systemDefault());
                case "NX" -> policy = DataPolicy.PUT_IF_ABSENT;
                case "XX" -> policy = DataPolicy.PUT_IF_EXISTS;
            }
        }
        // to db
        DataEntry entry = new DataEntry(key, value, ttl, policy);
        if (policy == DataPolicy.PUT_IF_EXISTS && db.getEntry(entry.getKey()) != null) {
            DataEntry oldEntry = db.persistInMemory(entry);
            return oldEntry.getValue();
        } else if (policy == DataPolicy.PUT_IF_ABSENT) {
            if (db.getEntry(entry.getKey()) == null) {
                db.persistInMemory(entry);
            }
            return "OK";
        } else {
            db.persistInMemory(entry);
            return "OK";
        }
    }

    @Command(name = "GET", minimumArgs = 1, description = "https://redis.io/commands/get")
    public String get(String key) throws CommandException {
        if (key == null || key.equals("")) throw new CommandException("empty key");
        DataEntry entry = db.getEntry(key);
        if (entry == null) return null;
        return entry.getValue();
    }
}
