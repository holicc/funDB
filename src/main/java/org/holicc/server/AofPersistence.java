package org.holicc.server;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

public class AofPersistence implements Persistence {


    private String fileName;
    private String dir;
    private Path aofFile;
    private final ServerConfig config;
    private volatile FileDescriptor fd;

    public AofPersistence(ServerConfig config) {
        this.config = config;
        this.dir = config.getDir();
        this.fileName = config.getAppendFileName();
        this.aofFile = Path.of(dir + fileName);
    }

    @Override
    public void write(byte[] cmd) throws IOException {
        if (!config.getDir().equals(dir) || !config.getAppendFileName().equals(fileName)) {
            dir = config.getDir();
            fileName = config.getAppendFileName();
            aofFile = Path.of(dir + fileName);
        }
        if (Files.notExists(aofFile)) {
            Files.createFile(aofFile);
        }
        //
        try (FileOutputStream out = new FileOutputStream(aofFile.toFile())) {
            out.write(cmd);
            out.flush();
            fd = out.getFD();
        }
    }

    @Override
    public void fsync() throws IOException {
        if (fd != null && fd.valid()) {
            fd.sync();
        }
    }
}
