package org.holicc.server;

import org.holicc.cmd.JedisCommand;
import org.holicc.db.DataBase;
import org.holicc.parser.DefaultProtocolParser;
import org.holicc.parser.ProtocolParser;
import org.holicc.parser.RedisValue;
import org.tinylog.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class JedisServer {

    private String host;
    private int port;

    private DataBase db;

    private Map<String, JedisCommand> cmds = new HashMap<>();

    private ProtocolParser parser = new DefaultProtocolParser();
    private static final JedisServer server = new JedisServer();

    private static final String BANNER = """
                
             ▄▄▄██▀▀▀▓█████ ▓█████▄  ██▓  ██████
               ▒██   ▓█   ▀ ▒██▀ ██▌▓██▒▒██    ▒
               ░██   ▒███   ░██   █▌▒██▒░ ▓██▄
            ▓██▄██▓  ▒▓█  ▄ ░▓█▄   ▌░██░  ▒   ██▒
             ▓███▒   ░▒████▒░▒████▓ ░██░▒██████▒▒
             ▒▓▒▒░   ░░ ▒░ ░ ▒▒▓  ▒ ░▓  ▒ ▒▓▒ ▒ ░
             ▒ ░▒░    ░ ░  ░ ░ ▒  ▒  ▒ ░░ ░▒  ░ ░
             ░ ░ ░      ░    ░ ░  ░  ▒ ░░  ░  ░
             ░   ░      ░  ░   ░     ░        ░
                             ░
            """;

    private JedisServer() {

    }

    public static class Builder {

        private DataBase db;

        public Builder database(DataBase db) {
            this.db = db;
            return this;
        }

        public JedisServer build() {
            JedisServer server = new JedisServer();
            server.db = db;
            return server;
        }
    }


    public void run(String host, int port) throws Exception {
        registerCmd();
        this.host = host;
        this.port = port;
        System.out.println(BANNER);
        InetSocketAddress address = new InetSocketAddress(host, port);
        ServerSocket socket = new ServerSocket();
        socket.setReuseAddress(true);
        socket.bind(address);
        int i = 0;
        Logger.info("server start at: {}:{} ...", this.host, this.port);
        try {
            Socket accept;
            while ((accept = socket.accept()) != null) {
                i++;
                System.out.println(i);
                BufferedInputStream reader = new BufferedInputStream(accept.getInputStream());
                int size = reader.available();
                if (size > 0) {
                    byte[] data = reader.readNBytes(size);
                    System.out.println(new String(data));
                    try (OutputStream out = accept.getOutputStream()) {
                        RedisValue redisValue = parser.parse(data, 0);
                        String command = redisValue.getCommand().toUpperCase(Locale.ROOT);
                        JedisCommand cmd = cmds.get(command);
                        if (cmd != null) {
                            cmd.execute(db, redisValue).write(out);
                        } else {
                            Response.Error("unknown command").write(out);
                        }
                    } catch (Exception e) {
                        Logger.error(e);
                        reader.close();
                        accept.close();
                    }
                }
            }
        } catch (IOException e) {
            Logger.error(e);
            socket.close();
        }
    }

    private void registerCmd() {
        Stream.of(
                JedisCommand.ALL_COMMAND
        ).forEach(cmd -> {
            for (String c : cmd.supportCommands()) {
                cmds.put(c, cmd);
            }
        });
    }
}
