package org.holicc.protocol;


import java.util.LinkedList;

public interface ProtocolParser {

    LinkedList<Object> parse(byte[] buffer) throws ProtocolParseException;

}
