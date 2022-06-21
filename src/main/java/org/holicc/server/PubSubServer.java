package org.holicc.server;

import org.tinylog.Logger;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class PubSubServer {

    private final Map<String, LinkedList<SocketChannel>> channels = new HashMap<>();

    public boolean subscribe(String channel, SocketChannel socket) throws SocketException {
        socket.socket().setKeepAlive(true);
        if (channels.containsKey(channel)) {
            return channels.get(channel).add(socket);
        } else {
            LinkedList<SocketChannel> q = new LinkedList<>();
            q.add(socket);
            channels.put(channel, q);
        }
        return true;
    }

    public boolean publish(String channel, Response message) throws IOException {
        if (!channels.containsKey(channel)) return false;
        LinkedList<SocketChannel> channels = this.channels.get(channel);
        for (SocketChannel socketChannel : channels) {
            if (socketChannel.isOpen() && socketChannel.isConnected()) {
                socketChannel.write(ByteBuffer.wrap(message.toBytes()));
            } else {
                Logger.warn("publish message failed !");
                channels.remove(socketChannel);
            }
        }
        return true;
    }
}
