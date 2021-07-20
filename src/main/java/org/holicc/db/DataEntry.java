package org.holicc.db;

public class DataEntry {

    private String key;

    private long ttl;

    private Object value;

    private DataPolicy policy;

    public DataEntry(String key, long ttl, Object value, DataPolicy policy) {
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

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }


    public DataPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(DataPolicy policy) {
        this.policy = policy;
    }
}
