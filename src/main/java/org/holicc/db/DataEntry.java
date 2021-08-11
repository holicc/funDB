package org.holicc.db;

import java.time.LocalDateTime;

public class DataEntry {

    private String key;

    private LocalDateTime ttl;

    private Object value;

    private DataPolicy policy = DataPolicy.DEFAULT;

    public DataEntry(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public DataEntry(String key, Object value, LocalDateTime ttl) {
        this.key = key;
        this.ttl = ttl;
        this.value = value;
    }

    public DataEntry(String key, Object value, LocalDateTime ttl, DataPolicy policy) {
        this.key = key;
        this.ttl = ttl;
        this.value = value;
        this.policy = policy;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LocalDateTime getTtl() {
        return ttl;
    }

    public void setTtl(LocalDateTime ttl) {
        this.ttl = ttl;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public <T> T getValue(){
        return (T)value;
    }

    public DataPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(DataPolicy policy) {
        this.policy = policy;
    }
}
