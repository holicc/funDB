package org.holicc.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Response {

    private String msg;

    public Response(String msg) {
        this.msg = msg;
    }

    public static Response NullBulkResponse() {
        return null;
    }

    public static Response Ok() {
        return new Response("+OK\r\n");
    }

    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
    }
}
