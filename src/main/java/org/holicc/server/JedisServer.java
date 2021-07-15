package org.holicc.server;

import org.holicc.db.DataBase;

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

    public static JedisServer database(DataBase db) {
        server.db = db;
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
                System.out.println(new String(data));

                OutputStream out = accept.getOutputStream();
                out.write("test".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
