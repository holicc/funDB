package org.holicc.db;

import java.util.Set;

/**
 * https://redis.io/commands/set
 */
public interface DataBase {

    DataEntry persistInMemory(DataEntry entry);

    Set<String> keys(String pattern);

    DataEntry getEntry(String key);

    void delEntry(String key);
}
