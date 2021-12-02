package org.holicc.protocol;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class DefaultProtocolParser implements ProtocolParser {

    private int cur;
    private int limit;
    private byte[] buffer;


    @Override
    public LinkedList<Object> parse(byte[] buffer) throws ProtocolParseException {
        this.buffer = buffer;
        this.cur = 0;
        this.limit = buffer.length;

        Object value = parse();
        if (value instanceof LinkedList linkedList) {
            return linkedList;
        }
        return new LinkedList<>(List.of(value));
    }

    private Object parse() throws ProtocolParseException {
        return switch (buffer[cur++]) {
            case '+' -> strValue();
            case ':' -> intValue();
            case '-' -> error();
            case '$' -> bulkStrValue();
            case '*' -> arrayValue();
            default -> throw new ProtocolParseException("Unknown protocol");
        };
    }

    private String strValue() {
        return readUntilCRLF().map(String::new).orElse("");
    }

    private String error() {
        return readUntilCRLF().map(String::new).orElse("");
    }

    private String bulkStrValue() {
        long size = intValue();
        byte[] bytes = new byte[(int) size];
        System.arraycopy(buffer, cur, bytes, 0, (int) size);
        cur += size + 2;
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private long intValue() {
        return readUntilCRLF().map(bytes -> {
            final boolean isNeg = bytes[0] == '-';
            long value = 0;
            for (int i = isNeg ? 1 : 0; i < bytes.length; i++) {
                value = value * 10 + bytes[i] - '0';
            }
            return isNeg ? -value : value;
        }).orElse(0L);
    }

    private LinkedList<?> arrayValue() throws ProtocolParseException {
        long len = intValue();
        LinkedList<Object> array = new LinkedList<>();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                array.add(parse());
            }
            return array;
        }
        throw ProtocolParseException.BadArraySize((int) len);
    }

    private Optional<byte[]> readUntilCRLF() {
        if (cur >= limit) {
            return Optional.empty();
        }
        int i = cur;
        for (; i < limit; i++) {
            if (buffer[i] == '\r') {
                break;
            }
        }
        byte[] bytes = new byte[i - cur];
        System.arraycopy(buffer, cur, bytes, 0, bytes.length);
        // cur at '\r' plus 2 to '\n'->next
        cur = i + 2;
        return Optional.of(bytes);
    }
}
