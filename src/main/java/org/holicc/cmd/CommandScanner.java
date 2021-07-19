package org.holicc.cmd;

import org.holicc.cmd.annotation.Command;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CommandScanner {

    private static final String COMMAND_IMPL_PACKAGE = "org.holicc.cmd.impl";


    public Map<String, JedisCommand> scan() throws Exception {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream(COMMAND_IMPL_PACKAGE.replaceAll("[.]", "/"));
        if (resourceAsStream == null) throw new Exception("scan package failed!");
        BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        Map<String, JedisCommand> r = new HashMap<>();
        reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(this::getClass)
                .filter(c -> Objects.nonNull(c)
                        && Arrays.asList(c.getInterfaces()).contains(JedisCommand.class)
                        && ((Class<? extends JedisCommand>) c).isAnnotationPresent(Command.class)
                )
                .forEach(c -> {
                    try {
                        Command command = (Command) c.getDeclaredAnnotation(Command.class);
                        JedisCommand cmd = (JedisCommand) c.getConstructor().newInstance();
                        r.put(command.name().toUpperCase(Locale.ROOT), cmd);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                });
        return r;
    }

    private Class getClass(String className) {
        try {
            return Class.forName(CommandScanner.COMMAND_IMPL_PACKAGE + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }
}
