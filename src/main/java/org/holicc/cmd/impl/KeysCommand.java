package org.holicc.cmd.impl;

import org.holicc.cmd.JedisCommand;
import org.holicc.db.DataBase;
import org.holicc.parser.RedisValue;
import org.holicc.server.Response;

public class KeysCommand implements JedisCommand {

    private final String[] cmds = {
            "TTL",
    };

    @Override
    public Response execute(DataBase db, RedisValue args) {
        return null;
    }

    @Override
    public String[] supportCommands() {
        return new String[0];
    }
}
