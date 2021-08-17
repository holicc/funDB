package org.holicc.cmd.impl;

import org.holicc.cmd.JedisCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.server.JedisServer;
import org.holicc.server.ServerConfig;

public class ConfigCommand implements JedisCommand {


    @Command(name = "CONFIG", subCommand = "GET", minimumArgs = 1, description = "https://redis.io/commands/config-get")
    public String configGet(ServerConfig config, String name) {
        return config.isExists(name);
    }

    @Command(name = "CONFIG", subCommand = "SET", minimumArgs = 2, description = "https://redis.io/commands/config-set")
    public String configSet(ServerConfig config, String name, String... args) {
        return "OK";
    }

    @Command(name = "SAVE", minimumArgs = 2, description = "https://redis.io/commands/save")
    public void save() {

    }
}
