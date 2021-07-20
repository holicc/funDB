package org.holicc;

import org.holicc.db.LocalDataBase;
import org.holicc.server.JedisServer;

public class JedisApplication {
    public static void main(String[] args) throws Exception {
        JedisServer.Builder builder = new JedisServer.Builder();

        builder.database(new LocalDataBase())
                .build()
                .run("0.0.0.0", 7891);
    }
}
