package org.example.registry;

import java.net.InetSocketAddress;

public interface ServiceRegistry {
    void registerService(String rpcServiceName
            , InetSocketAddress serviceAddress);
    void clearAll();
}
