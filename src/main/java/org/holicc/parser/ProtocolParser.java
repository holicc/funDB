package org.holicc.parser;

import org.holicc.server.Command;

public interface ProtocolParser {

    Command parse(byte[] data, int pos) throws ProtocolParseException;

}
