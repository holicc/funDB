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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.function.Function;

public class JedisServer {

    private String host;
    private int port;

    private DataBase db;
    private Map<String, Function<RedisValue, Response>> cmds;

    private final ProtocolParser parser = new DefaultProtocolParser();
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
        cmds = registerCmd();
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
                    Logger.error(e.getMessage());
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
            byte[] array = buffer.array();
            // TODO parse is slow
            RedisValue parse = parser.parse(array, 0);
            String command = parse.getCommand();
            Function<RedisValue, Response> f = cmds.get(command.toUpperCase(Locale.ROOT));
            Response response = Response.Error("unknown command " + command);
            if (f != null) {
                Optional.ofNullable(f.apply(parse)).ifPresent(key::attach);
            } else {
                key.attach(response);
            }
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

    private Map<String, Function<RedisValue, Response>> registerCmd() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections("org.holicc.cmd");
        Map<String, Function<RedisValue, Response>> cmds = new HashMap<>();
        Set<Class<? extends JedisCommand>> commands = reflections.getSubTypesOf(JedisCommand.class);
        for (Class<? extends JedisCommand> aClass : commands) {
            Constructor<? extends JedisCommand> constructor = aClass.getDeclaredConstructor();
            JedisCommand jedisCommand = constructor.newInstance();
            Set<Method> methods = ReflectionUtils.getMethods(aClass, ReflectionUtils.withAnnotation(Command.class));
            for (Method method : methods) {
                Command annotation = method.getAnnotation(Command.class);
                //
                if (!cmds.containsKey(annotation.name())) {
                    Function<RedisValue, Response> f = (redisValue) -> {
                        try {
                            Object v = redisValue.getValue();
                            List<RedisValue> args = (List<RedisValue>) v;
                            if (annotation.minimumArgs() > args.size() - 1)
                                return Response.Error("wrong number args of [" + annotation.name() + "], see " + annotation.description());
                            List<RedisValue> params = args.subList(1, args.size());
                            Class<?>[] parameterTypes = method.getParameterTypes();
                            List<Object> param = new ArrayList<>();
                            for (Class<?> parameterType : parameterTypes) {
                                if (parameterType.equals(DataBase.class)) {
                                    param.add(db);
                                } else if (parameterType.equals(String.class)) {
                                    param.add(params.isEmpty() ? "" : params.remove(0).getValueAsString());
                                } else if (parameterType.equals(String[].class)) {
                                    param.add(params.isEmpty() ? null : params.stream().map(RedisValue::getValueAsString).toArray(String[]::new));
                                } else {
                                    param.add(params.isEmpty() ? null : params.remove(0).getValueAsString());
                                }
                            }
                            Object invoke = method.invoke(jedisCommand, param.toArray());
                            if (invoke instanceof String) {
                                return Response.BulkStringReply((String) invoke);
                            } else if (invoke instanceof Collection) {
                                return Response.ArrayReply((Collection<?>) invoke);
                            } else if (invoke instanceof Long || invoke instanceof Integer) {
                                return Response.IntReply((int) invoke);
                            } else {
                                return Response.NullBulkResponse();
                            }
                        } catch (Exception e) {
                            return Response.Error(e.getMessage());
                        }
                    };
                    cmds.put(annotation.name(), f);
                } else {
                    Logger.warn("exists command [{}]", annotation.name());
                }
            }
        }
        return cmds;
    }

}
