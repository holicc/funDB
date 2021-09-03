package org.holicc.server;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

public record Response(String msg) {

    public static final String CRLF = "\r\n";

    public static Response NullBulkResponse() {
        return new Response("$-1" + CRLF);
    }

    public static Response BulkStringReply(String data) {
        return new Response("$" + data.length() + CRLF + data + CRLF);
    }

    public static Response Ok() {
        return new Response("+OK" + CRLF);
    }

    public static Response Error(String msg) {
        return new Response("-ERR " + msg + CRLF);
    }

    public static Response ArrayReply(Collection<?> collection) {
        if (collection.isEmpty()) return EmptyArrayReply();
        StringBuilder builder = new StringBuilder("*" + collection.size() + CRLF);
        for (Object item : collection) {
            if (item instanceof String) {
                builder.append(encodeStr((String) item));
            } else if (item instanceof Integer) {
                builder.append(encodeInt((int) item));
            }
        }
        return new Response(builder.toString());
    }

    public static Response EmptyArrayReply() {
        return new Response("*0" + CRLF);
    }

    public static Response IntReply(int val) {
        return new Response(encodeInt(val));
    }

    public byte[] toBytes() {
        return msg.getBytes(StandardCharsets.UTF_8);
    }

    private static String encodeStr(String data) {
        return "$" + data.length() + CRLF + data + CRLF;
    }

    private static String encodeInt(int i) {
        return ":" + i + CRLF;
    }
}
