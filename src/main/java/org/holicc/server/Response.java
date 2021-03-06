package org.holicc.server;

import org.holicc.datastruct.SortNode;
import org.reflections.ReflectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;

public record Response(String msg) {

    public static final String CRLF = "\r\n";

    public static Response NullBulkResponse() {
        return new Response("$-1" + CRLF);
    }

    public static Response BulkStringReply(String data) {
        if (Objects.isNull(data)) return EmptyArrayReply();
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
        StringBuilder builder = new StringBuilder();
        int size = collection.size();
        for (Object item : collection) {
            if (item instanceof Integer) {
                builder.append(encodeInt((int) item));
            } else if (item instanceof SortNode node) {
                builder.append(encodeStr(node.value()));
                builder.append(encodeStr(node.score() + ""));
                size += 1;
            } else {
                builder.append(encodeStr(item.toString()));
            }
        }
        builder.insert(0, "*" + size + CRLF);
        return new Response(builder.toString());
    }

    public static Response EmptyArrayReply() {
        return new Response("*0" + CRLF);
    }

    public static Response IntReply(long val) {
        return new Response(encodeInt(val));
    }

    public byte[] toBytes() {
        return msg.getBytes(StandardCharsets.UTF_8);
    }

    private static String encodeStr(String data) {
        return "$" + data.length() + CRLF + data + CRLF;
    }

    private static String encodeInt(long i) {
        return ":" + i + CRLF;
    }
}
