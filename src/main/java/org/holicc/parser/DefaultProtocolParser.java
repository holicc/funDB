package org.holicc.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultProtocolParser implements ProtocolParser {


    /**
     * words represent data range
     * eg: $3foo\r\n  => Word(2,4)
     */
    record Word(int start, int end) {

        /**
         * @return next position of start
         */
        public int nextPosition() {
            return end + 3;
        }

    }

    @Override
    public RedisValueType parse(byte[] data, int pos) throws ProtocolParseException {
        return switch (data[pos]) {
            case '+' -> strValue(data, pos + 1);
            case ':' -> intValue(data, pos + 1);
            case '-' -> error(data, pos + 1);
            case '$' -> bulkStrValue(data, pos + 1);
            case '*' -> arrayValue(data, pos + 1);
            default -> throw new ProtocolParseException("Unknown protocol");
        };
    }

    private Word word(byte[] data, int pos) throws ProtocolParseException {
        int index = pos;
        while (data[index] != '\r') index++;
        if (index + 1 > data.length || data[index + 1] != '\n') {
            throw new ProtocolParseException("invalid data format");
        }
        return new Word(pos, index - 1);
    }

    private RedisValueType<String> strValue(byte[] data, int pos) throws ProtocolParseException {
        Word wd = word(data, pos);
        byte[] s = Arrays.copyOfRange(data, wd.start(), wd.end() + 1);
        return new RedisValueType<>(wd, new String(s));
    }

    private RedisValueType<RedisError> error(byte[] data, int pos) throws ProtocolParseException {
        Word wd = word(data, pos);
        byte[] s = Arrays.copyOfRange(data, wd.start(), wd.end() + 1);
        RedisError error = new RedisError(new String(s));
        return new RedisValueType<>(wd, error);
    }

    private RedisValueType<byte[]> bulkStrValue(byte[] data, int pos) throws ProtocolParseException {
        RedisValueType<Integer> intValue = intValue(data, pos);
        Word wd = intValue.getWord();
        int len = intValue.getValue();
        if (len < -1) {
            throw ProtocolParseException.BadBulkStringSize(len);
        } else if (len == -1) {
            return RedisValueType.nullValue();
        } else if (len == 0) {
            return new RedisValueType<>(null, new byte[0]);
        } else if (data.length - wd.end() - 2 < len + 2) {
            throw ProtocolParseException.NotEnoughDataLength(data.length, len + wd.end() + 2);
        } else {
            int start = wd.nextPosition();
            int end = start + len - 1;
            byte[] s = Arrays.copyOfRange(data, start, end + 1);
            return new RedisValueType<>(new Word(start, end), s);
        }
    }

    private RedisValueType<Integer> intValue(byte[] data, int pos) throws ProtocolParseException {
        Word wd = word(data, pos);
        byte[] s = Arrays.copyOfRange(data, wd.start(), wd.end() + 1);
        int i = Integer.parseInt(new String(s));
        return new RedisValueType<>(wd, i);
    }

    private RedisValueType<List<RedisValueType>> arrayValue(byte[] data, int pos) throws ProtocolParseException {
        RedisValueType<Integer> intValue = intValue(data, pos);
        int len = intValue.getValue();
        if (len > 0) {
            List<RedisValueType> ary = new ArrayList<>(len);
            // 1(value last pos) + 2 (\r\n) = 3
            int curPos = intValue.getWord().nextPosition();
            for (int i = 0; i < len; i++) {
                RedisValueType value = parse(data, curPos);
                ary.add(value);
                curPos = value.getWord().nextPosition();
            }
            return new RedisValueType<>(new Word(pos, curPos), ary);
        }
        throw ProtocolParseException.BadArraySize(len);
    }

}
