package org.holicc.cmd.impl;

import org.holicc.cmd.JedisCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.db.DataEntry;
import org.holicc.db.DataPolicy;
import org.holicc.parser.RedisValue;
import org.holicc.server.Response;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Command(name = "SET")
public class SetCommand implements JedisCommand {

    private static final int MIN_ARG_NUM = 2;

    @Override
    public Response execute(DataBase db, RedisValue redisValue) {
        Object v = redisValue.getValue();
        if (v instanceof List && ((List<?>) v).size() >= MIN_ARG_NUM) {
            List<RedisValue> arg = (List<RedisValue>) v;
            String key = (String) arg.remove(1).getValueAsString();
            Object value = arg.remove(1).getValue();
            DataPolicy policy = DataPolicy.DEFAULT;
            long ttl = 0;
            // parse Options
            if (arg.size() >= 2) {
                Duration now = Duration.ofMillis(System.currentTimeMillis());
                String option = arg.remove(1).getValueAsString().toUpperCase(Locale.ROOT);
                String time = arg.remove(1).getValueAsString();
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
        return Response.NullBulkResponse();
    }


}
