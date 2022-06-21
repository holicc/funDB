package org.holicc;

import org.holicc.server.FunDBServer;
import org.holicc.server.ServerConfig;

public class FunDBApplication {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            new FunDBServer(new ServerConfig()).run();
        } else {
            new FunDBServer(ServerConfig.parse(args[0])).run();
        }
    }
}
