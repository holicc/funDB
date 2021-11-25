package org.holicc.cmd;

import org.holicc.protocol.RedisValue;

import java.util.List;

public interface From {

    From from(List<RedisValue> args);

}
