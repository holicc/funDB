package org.holicc.server;

import org.holicc.cmd.CommandScanner;
import org.holicc.cmd.annotation.Command;
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
import java.util.HashMap;

public class JedisServer {
    private String host;
    private int port;

    private DataBase db;
    private CommandScanner scanner=new CommandScanner();
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


    public static JedisServer build() {
        return server;
    }


    public void run(String host, int port) throws IOException {
        HashMap<String, Command> cmdMap = scanner.scan();
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
                    System.out.println(redisValue.getValue().toString());
                    out.write("+OK".getBytes(StandardCharsets.UTF_8));
                } catch (ProtocolParseException e) {
                    reader.close();
                    accept.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
