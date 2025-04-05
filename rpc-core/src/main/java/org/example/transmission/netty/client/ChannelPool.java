package org.example.transmission.netty.client;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @Author Mike
 * @Date 2025/4/1
 **/
public class ChannelPool {
    private final Map<String, Channel> pool = new ConcurrentHashMap<>();

    public Channel get(InetSocketAddress addr, Supplier<Channel> supplier) {
        String addrString = addr.toString();

        Channel channel = pool.get(addrString);
        if (channel != null && channel.isActive()) {
            return channel;
        }

        Channel newChannel = supplier.get();
        pool.put(addrString, newChannel);
        return newChannel;
    }
}
