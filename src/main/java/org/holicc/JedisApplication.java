package org.holicc;

import org.holicc.server.JedisServer;

import java.io.IOException;

public class JedisApplication {
    public static void main(String[] args) throws IOException {
        JedisServer.build()
                .run("127.0.0.1", 7891);
    }
}
