package org.holicc.protocol;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public interface ProtocolParser {

    Set<String> COMMANDS = Set.of(
            "PING"
    );

    LinkedList<Object> parse(byte[] buffer) throws ProtocolParseException;

}
