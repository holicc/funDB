package org.holicc.cmd.impl;

import org.holicc.cmd.JedisCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.parser.RedisValue;
import org.holicc.server.Response;

import java.util.List;

public class KeysCommand implements JedisCommand {


    @Command(name = "TTL")
    public Response ttl(DataBase db, List<RedisValue> args) {
        return null;
    }

    @Command(name = "KEYS", minimumArgs = 1)
    public Response keys(DataBase db, List<RedisValue> args) {
        return null;
    }
}
