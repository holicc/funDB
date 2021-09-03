package org.holicc.server;

import org.reflections.ReflectionUtils;
import org.tinylog.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServerConfig {

    public static final String ALWAYS_APPEND_FSYNC = "always";
    public static final String EVERY_SECOND_APPEND_FSYNC = "everysec";
    public static final String NO_APPEND_FSYNC = "no";

    private String bind;
    private int port;
    private boolean clusterEnabled;

    private String appendfsync;
    private boolean appendOnly;
    private String appendFileName;

    private String dir;

    private final Map<String, String> properties = new HashMap<>();

    public ServerConfig() {
        this.bind = "localhost";
        this.port = 7891;
    }

    public static ServerConfig parse(String configFile) throws IOException, IllegalAccessException {
        ServerConfig config = new ServerConfig();
        Class<? extends ServerConfig> configClass = config.getClass();
        //
        Map<String, Field> fieldMap = ReflectionUtils.getAllFields(configClass).stream()
                .collect(Collectors.toMap(field -> field.getName().toLowerCase(Locale.ROOT), field -> field));
        //
        for (String line : Files.readAllLines(Path.of(configFile))) {
            if (line.startsWith("#")) continue;
            String[] kv = line.split(" ");
            if (kv.length != 0) {
                StringBuilder raw = new StringBuilder();
                for (int i = 1; i < kv.length; i++) {
                    raw.append(kv[i]);
                }
                String key = kv[0].replaceAll("-", "").toLowerCase(Locale.ROOT);
                if (fieldMap.containsKey(key)) {
                    String rawValue = raw.toString();
                    config.properties.put(key, rawValue);
                    Field field = fieldMap.get(key);
                    Class<?> type = field.getType();
                    field.setAccessible(true);
                    if (type.equals(String.class)) {
                        field.set(config, rawValue.replaceAll("[\"]", ""));
                    } else if (type.equals(int.class)) {
                        field.set(config, Integer.parseInt(rawValue));
                    } else if (type.equals(boolean.class)) {
                        field.set(config, rawValue.equalsIgnoreCase("yes"));
                    }
                }
            }
        }
        return config;
    }

    public String getBind() {
        return bind;
    }

    public void setBind(String bind) {
        this.bind = bind;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isClusterEnabled() {
        return clusterEnabled;
    }

    public void setClusterEnabled(boolean clusterEnabled) {
        this.clusterEnabled = clusterEnabled;
    }

    public boolean isAppendOnly() {
        return appendOnly;
    }

    public void setAppendOnly(boolean appendOnly) {
        this.appendOnly = appendOnly;
    }

    public String getAppendFileName() {
        return appendFileName;
    }

    public void setAppendFileName(String appendFileName) {
        this.appendFileName = appendFileName;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getAppendfsync() {
        return appendfsync;
    }

    public void setAppendfsync(String appendfsync) {
        this.appendfsync = appendfsync;
    }

    public String getProperty(String name) {
        if (properties.isEmpty()) {
            ReflectionUtils.getAllFields(ServerConfig.class).forEach(field -> {
                try {
                    System.out.println(field.getName());
                    if (!Modifier.isFinal(field.getModifiers())) {
                        Optional.ofNullable(field.get(this))
                                .ifPresent(v -> properties.put(field.getName(), v.toString()));
                    }
                } catch (IllegalAccessException e) {
                    Logger.warn("get field value failed {}", e.getMessage());
                }
            });
        }
        return properties.get(name);
    }
}
