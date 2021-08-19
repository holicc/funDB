package org.holicc.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ServerConfigTest {

    @Test
    void parse() throws IOException, IllegalAccessException {

        ServerConfig config = ServerConfig.parse("/Users/joe/Desktop/jedis/src/main/resources/redis.conf");

        Assertions.assertNotNull(config);
    }
}