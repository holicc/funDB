package org.holicc.server;

import org.holicc.cmd.CommandWrapper;
import org.holicc.cmd.FunDBCommand;
import org.holicc.cmd.annotation.Command;
import org.holicc.db.DataBase;
import org.holicc.db.LocalDataBase;
import org.holicc.protocol.DefaultProtocolParser;
import org.holicc.protocol.ProtocolParseException;
import org.holicc.protocol.ProtocolParser;
import org.holicc.protocol.RedisValue;
import org.reflections.Reflections;
import org.tinylog.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class FunDBServer {


    private DataBase db;
    private AofPersistence aofPersistence;
    private RdbPersistence rdbPersistence;
    private Map<String, CommandWrapper> commands;

    private final ServerConfig config;
    private final ProtocolParser parser = new DefaultProtocolParser();
    private final PubSubServer pubSubServer = new PubSubServer();
    private final Arguments arguments = new Arguments();

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


    public FunDBServer(ServerConfig config) {
        this.config = config;
    }

    public void run() throws Exception {
        System.out.println(BANNER);
        //  init database
        db = initDatabase();
        // register commands
        commands = registerCmd();
        // init persistence
        initPersistence();
        //  load data
        reloadDatabase();
        // create tcp server
        Selector selector = Selector.open();
        //
        String bind = config.getBind();
        int port = config.getPort();
        InetSocketAddress address = new InetSocketAddress(bind, port);
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.register(selector, channel.validOps());
        channel.bind(address);
        //
        Logger.info("server start at: {}:{} ...", bind, port);
        while (selector.isOpen()) {
            selector.select(key -> {
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
                    if (key.channel().isOpen()) {
                        try {
                            key.channel().close();
                        } catch (IOException ex) {
                            Logger.error(ex.getMessage());
                        }
                    }
                    key.cancel();
                    Logger.error(e.getMessage());
                }
            }, 1000L);
            // TODO
            Optional.ofNullable(config.getAppendfsync())
                    .ifPresent(af -> {
                        if (af.equals(ServerConfig.EVERY_SECOND_APPEND_FSYNC)) {
                            try {
                                aofPersistence.fsync();
                            } catch (IOException e) {
                                Logger.error("do file sync failed {}", e.getMessage());
                            }
                        }
                    });
        }
    }

    public Response doCommand(RedisValue redisValue, byte[] array) throws ProtocolParseException, IOException {
        if (redisValue.isEmpty()) throw new ProtocolParseException("none command data");
        String commandName = redisValue.command().toUpperCase(Locale.ROOT);
        CommandWrapper command = commands.get(commandName);
        if (command != null) {
            if (command.persistence()) {
                persistence(array);
            }
            return command.execute(redisValue, arguments);
        }
        return Response.Error("unknown command [" + commandName + "]");
    }

    private void initPersistence() {
        if (config.isAppendOnly()) {
            aofPersistence = new AofPersistence(config);
        }
    }

    private DataBase initDatabase() {
        if (config.isClusterEnabled()) {
            // TODO cluster enable
            Logger.debug("init cluster database");
            return null;
        } else {
            Logger.debug("init local database");
            return new LocalDataBase();
        }
    }

    private void register(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
        SocketChannel client = serverSocketChannel.accept();
        client.configureBlocking(false);
        client.register(selector, client.validOps());
    }

    private void reloadDatabase() throws IOException, ProtocolParseException {
        if (config.isAppendOnly()) {
            String aofFile = config.getDir() + config.getAppendFileName();
            Path path = Path.of(aofFile);
            if (Files.notExists(path)) {
                Logger.warn("can not find aof file in: {}", aofFile);
                return;
            }
            byte[] bytes = Files.readAllBytes(path);
            int pos = 0;
            while (pos < bytes.length) {
                RedisValue parse = parser.parse(bytes);
                doCommand(parse, bytes);
            }
        }
    }

    private void onRead(SelectionKey key) throws IOException, ProtocolParseException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(64);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int size = 0;
            do {
                size = channel.read(buffer);
                if (size > 0) {
                    byte[] ary = buffer.array();
                    out.write(ary, 0, size);
                    buffer.clear();
                }
                if (size == -1) {
                    throw new SocketException("peer close socket");
                }
            } while (size != 0);
            byte[] bytes = out.toByteArray();
            RedisValue redisValue = parser.parse(bytes);
            // put socket as dynamic arg
            arguments.put(SocketChannel.class, channel);
            // do command and attach response
            key.attach(doCommand(redisValue, bytes));
            // make sure remove unused socket arg
            arguments.get(SocketChannel.class);
            key.interestOps(SelectionKey.OP_WRITE);
        }
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

    private void persistence(byte[] array) throws IOException {
        if (config.isAppendOnly()) {
            aofPersistence.write(array);
            if (config.getAppendfsync().equals(ServerConfig.ALWAYS_APPEND_FSYNC)) {
                aofPersistence.fsync();
            }
        }
    }

    private Map<String, CommandWrapper> registerCmd() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections("org.holicc.cmd");
        Map<String, CommandWrapper> cmds = new HashMap<>();
        Set<Class<? extends FunDBCommand>> commands = reflections.getSubTypesOf(FunDBCommand.class);
        for (Class<? extends FunDBCommand> aClass : commands) {
            FunDBCommand instance = newInstance(aClass);
            Set<Method> methods = Arrays.stream(aClass.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(Command.class))
                    .collect(Collectors.toSet());
            for (Method method : methods) {
                Command annotation = method.getAnnotation(Command.class);
                String commandName = annotation.subCommand().equals("") ?
                        annotation.name() : annotation.name() + "-" + annotation.subCommand();
                if (!cmds.containsKey(commandName)) {
                    cmds.put(commandName, new CommandWrapper(instance, annotation.persistence(), method));
                } else {
                    Logger.warn("command {} exists !", commandName);
                }
            }
        }
        return cmds;
    }

    private FunDBCommand newInstance(Class<? extends FunDBCommand> aClass) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends FunDBCommand> constructor = (Constructor<? extends FunDBCommand>) aClass.getDeclaredConstructors()[0];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        if (parameterTypes.length == 0) return constructor.newInstance();
        List<Object> params = new ArrayList<>();
        for (Class<?> parameterType : parameterTypes) {
            if (parameterType.equals(DataBase.class)) {
                params.add(db);
            } else if (parameterType.equals(ServerConfig.class)) {
                params.add(config);
            } else if (parameterType.equals(PubSubServer.class)) {
                params.add(pubSubServer);
            }
        }
        return constructor.newInstance(params.toArray());
    }
}
