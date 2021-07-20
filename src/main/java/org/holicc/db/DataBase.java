package org.holicc.db;

import java.util.Map;
import java.util.Set;

/**
 * https://redis.io/commands/set
 */
public interface DataBase {

    void persistInMemory(DataEntry entry);

}
