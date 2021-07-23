package org.holicc.cmd;

import org.holicc.cmd.impl.StringCommand;
import org.holicc.db.DataBase;
import org.holicc.parser.RedisValue;
import org.holicc.server.Response;

public interface JedisCommand {

    JedisCommand[] ALL_COMMAND = {
            new StringCommand(),
    };

    Response execute(DataBase db, RedisValue args);

    String[] supportCommands();
}
