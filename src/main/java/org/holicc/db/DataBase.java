package org.holicc.db;

/**
 * https://redis.io/commands/set
 */
public interface DataBase {

    void persistInMemory(DataEntry entry);

    DataEntry getEntry(String key);
}
