package org.holicc.cmd.impl;

import org.holicc.cmd.FunDBCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;

public record SortedSetCommand(
        DataBase db
) implements FunDBCommand {

    @Command(name = "ZADD", minimumArgs = 3, description = "https://redis.io/commands/zadd")
    public int zadd(String key, float score, String value) {

    }


}
