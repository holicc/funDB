package org.holicc.server;

import org.reflections.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerConfig {

    private String bind;
    private int port;
    private boolean clusterEnabled;
    private boolean appendOnly;
    private String appendFileName;
    private String dir;

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
                    Field field = fieldMap.get(key);
                    Class<?> type = field.getType();
                    field.setAccessible(true);
                    if (type.equals(String.class)) {
                        field.set(config, raw.toString().replaceAll("[\"]", ""));
                    } else if (type.equals(int.class)) {
                        field.set(config, Integer.parseInt(raw.toString()));
                    } else if (type.equals(boolean.class)) {
                        field.set(config, raw.toString().equalsIgnoreCase("yes"));
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
}
