package org.holicc.server;

import org.holicc.db.DataBase;
import org.holicc.parser.DefaultProtocolParser;
import org.holicc.parser.ProtocolParseException;
import org.holicc.parser.ProtocolParser;
import org.holicc.parser.RedisValueType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class JedisServer {
    private String host;
    private int port;

    private DataBase db;
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
                try {
                    RedisValueType<Object> result = parser.parse(data, 0);

                } catch (ProtocolParseException e) {
                    reader.close();
                    accept.close();
                }
                //todo
                OutputStream out = accept.getOutputStream();
                out.write("test".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
