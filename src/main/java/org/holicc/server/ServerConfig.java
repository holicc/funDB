package org.holicc.server;

import org.holicc.db.PersistenceMode;

public class ServerConfig {

    private volatile PersistenceMode persistenceMode = PersistenceMode.RDB;

    public ServerConfig() {

    }

    public ServerConfig(String configFile) {

    }

    public boolean isExists(String name) {
        return false;
    }

    public PersistenceMode getPersistenceMode() {
        return persistenceMode;
    }

    public void setPersistenceMode(PersistenceMode persistenceMode) {
        this.persistenceMode = persistenceMode;
    }
}
