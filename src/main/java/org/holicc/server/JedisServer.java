package org.holicc.server;

import org.holicc.cmd.JedisCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.parser.DefaultProtocolParser;
import org.holicc.parser.ProtocolParseException;
import org.holicc.parser.ProtocolParser;
import org.holicc.parser.RedisValue;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class JedisServer {

    private String host;
    private int port;

    private DataBase db;

    private Map<String, JedisCommand> cmds = new HashMap<>();

    private ProtocolParser parser = new DefaultProtocolParser();
    private static final JedisServer server = new JedisServer();

    private static final String BANNER = "    \n" +
            "             ▄▄▄██▀▀▀▓█████ ▓█████▄  ██▓  ██████\n" +
            "               ▒██   ▓█   ▀ ▒██▀ ██▌▓██▒▒██    ▒\n" +
            "               ░██   ▒███   ░██   █▌▒██▒░ ▓██▄\n" +
            "            ▓██▄██▓  ▒▓█  ▄ ░▓█▄   ▌░██░  ▒   ██▒\n" +
            "             ▓███▒   ░▒████▒░▒████▓ ░██░▒██████▒▒\n" +
            "             ▒▓▒▒░   ░░ ▒░ ░ ▒▒▓  ▒ ░▓  ▒ ▒▓▒ ▒ ░\n" +
            "             ▒ ░▒░    ░ ░  ░ ░ ▒  ▒  ▒ ░░ ░▒  ░ ░\n" +
            "             ░ ░ ░      ░    ░ ░  ░  ▒ ░░  ░  ░\n" +
            "             ░   ░      ░  ░   ░     ░        ░\n" +
            "                             ░";

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
        //
        registerCmd();
        //
        Logger.info("server start at: {}:{} ...", this.host, this.port);
        while (selector.isOpen()) {
            int selected = selector.select(key -> {
                try {
                    if (key.isAcceptable()) {
                        register(selector, channel);
                    }
                    if (key.isReadable()) {
                        onRead(key);
                    }
                    if (key.isWritable()) {
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
        client.register(selector, client.validOps());
    }

    private void onRead(SelectionKey key) throws IOException, ProtocolParseException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(512);
        int size = channel.read(buffer);
        // peer socket closed
        if (size == -1) {
            throw new IOException("peer socket closed");
        }
        if (size > 0) {
            //
            byte[] array = buffer.array();
            // TODO parse is slow
            RedisValue parse = parser.parse(array, 0);
            String command = parse.getCommand();
            JedisCommand cmd = cmds.get(command.toUpperCase(Locale.ROOT));
            Response response = Response.Error("unknown command " + command);
            if (cmd != null) {
                response = cmd.execute(db, parse);
                if (response != null) {
                    key.attach(response);
                }
            } else {
                key.attach(response);
            }
            //
        }
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void onWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Object attachment = key.attachment();
        if (attachment != null) {
            Response resp = (Response) key.attachment();
            ByteBuffer wrap = ByteBuffer.wrap(resp.toBytes());
            channel.write(wrap);
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void registerCmd() {

        Reflections reflections = new Reflections("org.holicc.cmd");

        Set<Class<? extends JedisCommand>> commands = reflections.getSubTypesOf(JedisCommand.class);
        // TODO use proxy class
        commands.stream().map(ReflectionUtils::getAllAnnotations)
                .flatMap(Set::stream)
                .filter(annotation -> annotation.annotationType().equals(Command.class))
                .map(annotation -> ((Command) annotation).name())


        Stream.of(
                JedisCommand.ALL_COMMAND
        ).forEach(cmd -> {
            for (String c : cmd.supportCommands()) {
                cmds.put(c, cmd);
            }
        });
    }
}
