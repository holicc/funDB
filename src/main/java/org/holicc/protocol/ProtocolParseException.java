package org.holicc.protocol;

public class ProtocolParseException extends Exception {

    public ProtocolParseException(String msg) {
        super(msg);
    }

    public static ProtocolParseException NullBulkString() {
        return new ProtocolParseException("null bulk string");
    }


    public static ProtocolParseException BadBulkStringSize(int len) {
        return new ProtocolParseException("bad bulk string minArgSize of " + len);
    }

    public static ProtocolParseException BadArraySize(int len) {
        return new ProtocolParseException("bad array minArgSize of " + len);
    }

    public static ProtocolParseException NotEnoughDataLength(int actual, int except) {
        return new ProtocolParseException(String.format("not enough data length,except %d actual %d", except, actual));
    }
}
