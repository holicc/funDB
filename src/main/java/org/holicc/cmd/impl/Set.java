package org.holicc.cmd.impl;

import org.holicc.cmd.JedisCommand;
import org.holicc.db.DataBase;
import org.holicc.parser.RedisValue;
import org.holicc.server.Response;
import org.holicc.cmd.annotation.Command;

import java.util.List;

@Command(name = "SET",
        minArgSize = 2,
        allowValueTypes = {String.class, Integer.class}
)
public class Set implements JedisCommand {

    private List<RedisValue> args;

    @Override
    public Response execute(DataBase db) {
        return null;
    }
}
