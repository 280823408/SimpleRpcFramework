package org.example.registry;

import org.example.dto.RpcRequest;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
