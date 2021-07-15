package org.holicc.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
            case '$' -> bulkStrValue(data, pos + 1);
            case '*' -> arrayValue();
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

    private RedisValueType<String> strValue(byte[] data, int pos) {
        Optional<Word> word = word(data, pos);
        if (word.isPresent()) {
            Word wd = word.get();
            byte[] s = Arrays.copyOfRange(data, wd.start(), wd.end() - 2);
            return new RedisValueType<>(wd, new String(s));
        }
        return null;
    }

    private RedisValueType<RedisError> error(byte[] data, int pos) {
        Optional<Word> word = word(data, pos);
        if (word.isPresent()) {
            Word wd = word.get();
            byte[] s = Arrays.copyOfRange(data, wd.start(), wd.end() - 2);
            RedisError error = new RedisError(new String(s));
            return new RedisValueType<>(wd, error);
        }
        return null;
    }

    private RedisValueType bulkStrValue(byte[] data, int pos) {
        RedisValueType<Integer> type = intValue(data, pos);
        if (Objects.nonNull(type)) {
            int len = Optional.ofNullable(type.getValue()).orElse(-1);
            if (len == -1) {
                //todo should throw a exception instead ?
                return new RedisValueType<>(null, RedisError.nullBulkString());
            }

            if (data.length<len+1){

            }else{

            }

        }

        return null;
    }

    private RedisValueType<Integer> intValue(byte[] data, int pos) {
        Optional<Word> word = word(data, pos);
        if (word.isPresent()) {
            Word wd = word.get();
            byte[] s = Arrays.copyOfRange(data, wd.start(), wd.end() - 2);
            int i = Integer.parseInt(new String(s));
            return new RedisValueType<>(wd, i);
        }
        return null;
    }

    private RedisValueType<List<RedisValueType>> arrayValue() {
        return null;
    }

}
