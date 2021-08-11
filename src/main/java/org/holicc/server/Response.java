package org.holicc.server;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

public record Response(String msg) {

    public static Response NullBulkResponse() {
        return new Response("$-1\r\n");
    }

    public static Response BulkStringReply(String data) {
        return new Response("$" + data.length() + "\r\n" + data + "\r\n");
    }

    public static Response Ok() {
        return new Response("+OK\r\n");
    }

    public static Response Error(String msg) {
        return new Response("-ERR " + msg + "\r\n");
    }

    public static Response ArrayReply(Collection<?> collection) {
        if (collection.isEmpty()) return EmptyArrayReply();
        StringBuilder builder = new StringBuilder("*" + collection.size() + "\r\n");
        for (Object item : collection) {
            if (item instanceof String) {
                builder.append(encodeStr((String) item));
            }
        }
        return new Response(builder.toString());
    }

    public static Response EmptyArrayReply() {
        return new Response("*0\r\n");
    }

    public static Response IntReply(int val) {
        return new Response(":" + val + "\r\n");
    }

    public byte[] toBytes() {
        return msg.getBytes(StandardCharsets.UTF_8);
    }

    public String getMsg() {
        return msg;
    }

    private static String encodeStr(String data) {
        return "$" + data.length() + "\r\n" + data + "\r\n";
    }
}
