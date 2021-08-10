package org.holicc.cmd.impl;

import org.holicc.cmd.JedisCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.parser.RedisValue;
import org.holicc.server.Response;

import java.util.List;
import java.util.Set;

public class KeysCommand implements JedisCommand {


    @Command(name = "TTL")
    public Response ttl(DataBase db, List<RedisValue> args) {
        return null;
    }

    @Command(name = "KEYS", minimumArgs = 1, description = "https://redis.io/commands/keys")
    public Set<String> keys(DataBase db, String pattern) {
        return db.keys(pattern);
    }
}
