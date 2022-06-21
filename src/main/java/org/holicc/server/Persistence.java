package org.holicc.server;

import java.io.IOException;

public interface Persistence {

    void write(byte[] cmd) throws IOException;

    void fsync() throws IOException;

}
