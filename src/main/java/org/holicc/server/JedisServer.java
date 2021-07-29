package org.holicc.server;

import org.holicc.cmd.JedisCommand;
import org.holicc.db.DataBase;
import org.holicc.parser.DefaultProtocolParser;
import org.holicc.parser.ProtocolParser;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
        //
        Selector selector = Selector.open();
        //
        InetSocketAddress address = new InetSocketAddress(host, port);
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.register(selector, channel.validOps());
        channel.bind(address);
        Logger.info("server start at: {}:{} ...", this.host, this.port);
        while (selector.isOpen()) {
            int selected = selector.select(key -> {
                try {
                    if (key.isAcceptable()) {
                        register(selector, channel);
                    } else if (key.isReadable()) {
                        onRead(key);
                    } else if (key.isWritable()) {
                        onWrite(key);
                    }
                } catch (Exception e) {
                    key.cancel();
                    Logger.error(e);
                }
            }, 3 * 1000L);
        }
    }


    private void register(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
        SocketChannel client = serverSocketChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    private void onRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(512);
        int size = channel.read(buffer);
        // peer socket closed
        if (size == -1) {
            channel.close();
        }
        if (size > 0) {
            //
            byte[] array = buffer.array();
            Logger.debug("data {} ", new String(array));
            //
            channel.register(key.selector(), SelectionKey.OP_WRITE);
        }
    }

    private void onWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer wrap = ByteBuffer.wrap("+OK\r\n".getBytes(StandardCharsets.UTF_8));
        channel.write(wrap);
        channel.register(key.selector(), SelectionKey.OP_READ);
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
