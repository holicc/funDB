package org.holicc;

import org.holicc.server.JedisServer;
import org.holicc.server.ServerConfig;

public class JedisApplication {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            new JedisServer(new ServerConfig()).run();
        } else {
            new JedisServer(ServerConfig.parse(args[0])).run();
        }
    }
}
