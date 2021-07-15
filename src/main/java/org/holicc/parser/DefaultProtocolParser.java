package org.holicc.parser;

import java.util.Arrays;
import java.util.Optional;

public class DefaultProtocolParser implements ProtocolParser {

    record Word(int start, int end) {
    }

    @Override
    public RedisValueType parse(byte[] data) throws ProtocolParseException {
        int pos = 0;
        return switch (data[pos]) {
            case '+' -> strValue(data, pos + 1);
            case ':' -> intValue(data, pos + 1);
            case '-' -> error(data, pos + 1);
            case '$' -> bulkStrValue();
            case '*' -> ;
            default -> throw new ProtocolParseException("Unknown protocol");
        };
    }

    private Optional<Word> word(byte[] data, int pos) {
        int index = 0;
        while (data[index] != '\r') index++;
        if (index + 1 > data.length || data[index + 1] != '\n') {
            return Optional.empty();
        }
        return Optional.of(new Word(pos, index + 1));
    }

    private RedisValueType strValue(byte[] data, int pos) {
        Optional<Word> word = word(data, pos);
        if (word.isPresent()) {
            Word wd = word.get();
            byte[] s = Arrays.copyOfRange(data, wd.start(), wd.end() - 2);
            return new RedisValueType(wd, new String(s));
        }
        return null;
    }

    private RedisValueType error(byte[] data, int pos) {
        Optional<Word> word = word(data, pos);
        if (word.isPresent()) {
            Word wd = word.get();
            byte[] s = Arrays.copyOfRange(data, wd.start(), wd.end() - 2);
            return new RedisValueType(wd, new String(s));
        }
        return null;
    }

    private RedisValueType bulkStrValue(byte[] data, int pos) {
        RedisValueType type = intValue(data, pos);

    }

    private RedisValueType intValue(byte[] data, int pos) {
        Optional<Word> word = word(data, pos);
        if (word.isPresent()) {
            Word wd = word.get();
            byte[] s = Arrays.copyOfRange(data, wd.start(), wd.end() - 2);
            int i = Integer.parseInt(new String(s));
            return new RedisValueType(wd, i);
        }
        return null;
    }

    private RedisValueType arrayValue() {

    }

}
