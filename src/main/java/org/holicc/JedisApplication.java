package org.holicc;

import org.holicc.server.JedisServer;

import java.io.IOException;

public class JedisApplication {
    public static void main(String[] args) throws IOException {
        JedisServer.build()
                .run("192.168.3.3", 7891);
    }
}
