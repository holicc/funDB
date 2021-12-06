package org.holicc.cmd.impl;

import org.holicc.cmd.FunDBCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.cmd.exception.CommandException;
import org.holicc.server.ServerConfig;

import java.util.Arrays;
import java.util.List;

public record ConfigCommand(ServerConfig config) implements FunDBCommand {


    @Command(name = "CONFIG", subCommand = "GET", description = "https://redis.io/commands/config-get")
    public List<String> configGet(String name) {
        return config.getProperty(name)
                .map(v -> List.of(v.split(" ")))
                .orElse(List.of());
    }


    @Command(name = "CONFIG", subCommand = "SET", minimumArgs = 2, description = "https://redis.io/commands/config-set")
    public String configSet(String name, String... args) throws CommandException {
        if (config.setProperty(name, args)) {
            return "OK";
        }
        throw new CommandException("Unsupported CONFIG parameter: " + name);
    }

    @Command(name = "SAVE", minimumArgs = 2, description = "https://redis.io/commands/save")
    public void save() {

    }
}
