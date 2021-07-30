package org.holicc.cmd.impl;

import org.holicc.cmd.JedisCommand;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.db.DataPolicy;
import org.holicc.parser.RedisValue;
import org.holicc.server.Response;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class StringCommand implements JedisCommand {

    private final String[] cmds = {
            "SET",
            "GET"
    };

    @Override
    public Response execute(DataBase db, RedisValue redisValue) {
        Object v = redisValue.getValue();
        if (v instanceof List) {
            List<RedisValue> args = (List<RedisValue>) v;
            String name = args.remove(0).getValueAsString();
            return switch (name.toUpperCase(Locale.ROOT)) {
                case "SET" -> setFunction(db, args);
                case "GET" -> getFunction(db, args);
                default -> throw new IllegalStateException("Unexpected value: " + name.toUpperCase(Locale.ROOT));
            };
        }
        throw new IllegalStateException("wrong number args");
    }

    @Override
    public String[] supportCommands() {
        return this.cmds;
    }

    private Response setFunction(DataBase db, List<RedisValue> args) {
        if (args.size() <= 1) return Response.NullBulkResponse();
        String key = args.remove(0).getValueAsString();
        String value = args.remove(0).getValueAsString();
        DataPolicy policy = DataPolicy.DEFAULT;
        long ttl = 0;
        // parse Options
        if (args.size() >= 1) {
            Duration now = Duration.ofMillis(System.currentTimeMillis());
            String option = args.remove(0).getValueAsString().toUpperCase(Locale.ROOT);
            String time = args.remove(0).getValueAsString();
            switch (option) {
                case "EX" -> ttl = now.plus(Duration.ofSeconds(Integer.parseInt(time))).toMillis();
                case "EXAT" -> ttl = Integer.parseInt(time) * 1000L;
                case "PX" -> ttl = now.plus(Duration.ofMillis(Integer.parseInt(time))).toMillis();
                case "PXAT" -> ttl = Long.parseLong(time);
                case "NX" -> policy = DataPolicy.PUT_IF_ABSENT;
                case "XX" -> policy = DataPolicy.PUT_IF_EXISTS;
            }
        }
        // to db
        DataEntry entry = new DataEntry(key, ttl, value, policy);
        db.persistInMemory(entry);
        return Response.Ok();
    }

    private Response getFunction(DataBase db, List<RedisValue> arg) {
        DataEntry entry = db.getEntry(arg.remove(0).getValueAsString());
        if (entry == null) return Response.NullBulkResponse();
        return Response.BulkStringReply(entry.getValue().toString());
    }
}
