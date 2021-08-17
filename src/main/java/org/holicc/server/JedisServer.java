package org.holicc.server;

import org.holicc.cmd.JedisCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.db.PersistenceMode;
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

    private int port;
    private String host;
    private DataBase db;
    private ServerConfig config = new ServerConfig();

    private Map<String, Function<List<RedisValue>, Response>> cmds;

    private final ProtocolParser parser = new DefaultProtocolParser();


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
                             ░""".indent(3);


    private JedisServer() {
    }

    public static class Builder {

        private DataBase db;
        private String host;
        private String configFile;
        private int port;

        public Builder database(DataBase db) {
            this.db = db;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder config(String file) {
            this.configFile = file;
            return this;
        }

        public JedisServer build() {
            JedisServer server = new JedisServer();
            server.host = host == null || host.equals("") ? "localhost" : host;
            server.port = port == 0 ? 7891 : port;
            server.db = db;
            Optional.ofNullable(configFile).ifPresent(f -> server.config = new ServerConfig(configFile));
            return server;
        }
    }


    public void run() throws Exception {
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

    public ServerConfig getConfig() {
        return this.config;
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
            RedisValue parse = parser.parse(array, 0);
            List<RedisValue> args = (List<RedisValue>) parse.getValue();
            if (args.isEmpty()) throw new ProtocolParseException("none command data");
            String command = args.remove(0).getValueAsString();
            if (command.equals("CONFIG") && !args.isEmpty()) {
                command += "-" + args.remove(0).getValueAsString();
            }
            Function<List<RedisValue>, Response> f = cmds.get(command.toUpperCase(Locale.ROOT));
            Response response = Response.Error("unknown command " + command);
            if (f != null) {
                Optional.ofNullable(f.apply(args)).ifPresent(key::attach);
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

    private Map<String, Function<List<RedisValue>, Response>> registerCmd() throws NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections("org.holicc.cmd");
        Map<String, Function<List<RedisValue>, Response>> cmds = new HashMap<>();
        Set<Class<? extends JedisCommand>> commands = reflections.getSubTypesOf(JedisCommand.class);
        for (Class<? extends JedisCommand> aClass : commands) {
            Constructor<? extends JedisCommand> constructor = aClass.getDeclaredConstructor();
            JedisCommand jedisCommand = constructor.newInstance();
            Set<Method> methods = ReflectionUtils.getMethods(aClass, ReflectionUtils.withAnnotation(Command.class));
            for (Method method : methods) {
                Command annotation = method.getAnnotation(Command.class);
                String command = annotation.subCommand().equals("") ?
                        annotation.name() : String.join("-", annotation.name(), annotation.subCommand());
                if (!cmds.containsKey(annotation.name())) {
                    Function<List<RedisValue>, Response> f = (args) -> doCommand(jedisCommand, method, annotation, args);
                    cmds.put(command, f);
                } else {
                    Logger.warn("exists command [{}]", annotation.name());
                }
            }
        }
        return cmds;
    }

    private Response doCommand(JedisCommand jedisCommand, Method method, Command annotation, List<RedisValue> args) {
        try {
            if (annotation.minimumArgs() > args.size()) {
                return Response.Error("wrong number args of [" + annotation.name() + "], see " + annotation.description());
            }
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
                } else if (parameterType.equals(ServerConfig.class)) {
                    param.add(config);
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
    }

}
