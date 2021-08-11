package org.holicc.db;

import javax.xml.crypto.Data;
import java.util.Map;
import java.util.Set;

/**
 * https://redis.io/commands/set
 */
public interface DataBase {

    DataEntry persistInMemory(DataEntry entry);

    Set<String> keys(String pattern);

    DataEntry getEntry(String key);
}
