package org.holicc.server;

import org.holicc.cmd.CommandScanner;
import org.holicc.cmd.JedisCommand;
import org.holicc.db.DataBase;
import org.holicc.parser.DefaultProtocolParser;
import org.holicc.parser.ProtocolParseException;
import org.holicc.parser.ProtocolParser;
import org.holicc.parser.RedisValue;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class JedisServer {
    private String host;
    private int port;

    private DataBase db;

    private CommandScanner scanner = new CommandScanner();
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

    public static JedisServer build() {
        return server;
    }


    public void run(String host, int port) throws Exception {
        Map<String, JedisCommand> cmds = scanner.scan();
        //
        this.host = host;
        this.port = port;
        System.out.println(BANNER);
        //
        SocketAddress address = new InetSocketAddress(this.host, this.port);
        ServerSocket socket = new ServerSocket();
        socket.bind(address);

        try {
            Socket accept;
            while ((accept = socket.accept()) != null) {
                BufferedInputStream reader = new BufferedInputStream(accept.getInputStream());
                int size = reader.available();
                byte[] data = reader.readNBytes(size);
                try (OutputStream out = accept.getOutputStream()) {
                    RedisValue redisValue = parser.parse(data, 0);
                    String command = redisValue.getCommand().toUpperCase(Locale.ROOT);
                    JedisCommand cmd = cmds.get(command);
                    if (cmd != null) {
                        cmd.execute(db, redisValue).write(out);
                    } else {
                        out.write("-Error Unknown Command".getBytes(StandardCharsets.UTF_8));
                    }
                    out.flush();
                } catch (ProtocolParseException e) {
                    e.printStackTrace();
                    reader.close();
                    accept.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
