package org.holicc.protocol;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BenchmarkParser {
    public static void main(String[] args) throws IOException {
        Main.main(args);
    }

    @Benchmark
    @BenchmarkMode({ Mode.SampleTime, Mode.Throughput})
    public void benchmarkParse() throws ProtocolParseException {
        ProtocolParser parser = new DefaultProtocolParser();
        RedisValue parse = parser.parse("*3\r\n$3\r\nset\r\n$1\r\na\r\n$1\r\n1\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
