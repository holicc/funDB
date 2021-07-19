package org.holicc;

import org.holicc.server.JedisServer;

import java.io.IOException;

public class JedisApplication {
    public static void main(String[] args) throws Exception {
        JedisServer.build()
                .run("0.0.0.0", 7891);
    }
}
