package org.holicc.server;

import org.holicc.db.PersistenceMode;
import org.reflections.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerConfig {

    private Map<String, String> properties;

    private String bind;
    private int port;

    private boolean appendOnly;
    private String appendFileName;

    public ServerConfig() {

    }

    public static ServerConfig parse(String configFile) throws IOException, IllegalAccessException {
        ServerConfig config = new ServerConfig();
        Class<? extends ServerConfig> configClass = config.getClass();
        //
        Map<String, Field> fieldMap = ReflectionUtils.getAllFields(configClass).stream()
                .collect(Collectors.toMap(Field::getName, field -> field));
        //
        for (String line : Files.readAllLines(Path.of(configFile))) {
            if (line.startsWith("#")) continue;
            String[] kv = line.split(" ");
            if (kv.length != 0) {
                StringBuilder raw = new StringBuilder();
                for (int i = 1; i < kv.length; i++) {
                    raw.append(kv[i]);
                }
                if (fieldMap.containsKey(kv[0])) {
                    Field field = fieldMap.get(kv[0]);
                    Class<?> type = field.getType();
                    field.setAccessible(true);
                    if (type.equals(String.class)) {
                        field.set(config, raw.toString());
                    } else if (type.equals(int.class)) {
                        field.set(config, Integer.parseInt(raw.toString()));
                    }
                }
            }
        }
    }

    public PersistenceMode getPersistenceMode() {
        return persistenceMode;
    }

    public void setPersistenceMode(PersistenceMode persistenceMode) {
        this.persistenceMode = persistenceMode;
    }
}
