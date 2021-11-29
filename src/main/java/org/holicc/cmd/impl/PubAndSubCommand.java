package org.holicc.cmd.impl;

import org.holicc.cmd.FunDBCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.server.PubSubServer;
import org.holicc.server.Response;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public record PubAndSubCommand(
        PubSubServer server
) implements FunDBCommand {

    @Command(name = "SUBSCRIBE", description = "https://redis.io/topics/pubsub")
    public List<Object> subscribe(SocketChannel connection, String... keys) throws SocketException {
        List<Object> r = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            if (server.subscribe(keys[i], connection)) {
                r.add("subscribe");
                r.add(keys[i]);
                r.add(i + 1);
            }
        }
        return r;
    }

    @Command(name = "PUBLISH", minimumArgs = 2, description = "https://redis.io/topics/pubsub")
    public int publish(String key, String message) throws IOException {
        return server.publish(key, Response.ArrayReply(List.of("message", key, message))) ? 1 : 0;
    }
}
