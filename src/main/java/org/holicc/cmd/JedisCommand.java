package org.holicc.cmd;

import org.holicc.db.DataBase;
import org.holicc.parser.RedisValue;
import org.holicc.server.Response;

import java.util.List;

public interface JedisCommand {

    Response execute(DataBase db);
}
