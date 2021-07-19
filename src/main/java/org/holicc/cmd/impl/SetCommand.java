package org.holicc.cmd.impl;

import org.holicc.cmd.JedisCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.parser.RedisValue;
import org.holicc.server.Response;

import java.util.Map;
import java.util.Set;

@Command(name = "SET")
public class SetCommand implements JedisCommand {

    @Override
    public Response execute(DataBase db, RedisValue args) {



        Map<String,String> globalSet = db.getGlobalSet();
        globalSet.put()

        return;
    }
}
