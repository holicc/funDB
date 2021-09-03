package org.holicc.server;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PubSubServer {

    private final Map<String, LinkedList<SocketChannel>> channels = new HashMap<>();


    public boolean subscribe(String channel, SocketChannel socket) {
        return true;
    }

    public boolean publish(String channel, String message) {
        return true;
    }
}
