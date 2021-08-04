package org.holicc.cmd.impl;

import org.holicc.cmd.JedisCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.db.DataPolicy;
import org.holicc.parser.RedisValue;
import org.holicc.server.Response;

import java.util.List;
import java.util.Locale;

public class StringCommand implements JedisCommand {

    @Command(name = "SET", minimumArgs = 2, description = "https://redis.io/commands/set")
    public Response set(DataBase db, String key, String value, String... options) {
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
        return Response.Ok();
    }

    @Command(name = "GET")
    public Response get(DataBase db, List<RedisValue> arg) {
        DataEntry entry = db.getEntry(arg.remove(0).getValueAsString());
        if (entry == null) return Response.NullBulkResponse();
        return Response.BulkStringReply(entry.getValue().toString());
    }
}
