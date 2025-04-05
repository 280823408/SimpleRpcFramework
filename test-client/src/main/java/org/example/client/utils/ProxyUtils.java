package org.example.client.utils;

import org.example.factory.SingletonFactory;
import org.example.proxy.RpcClientProxy;
import org.example.transmission.RpcClient;
import org.example.transmission.netty.client.NettyRpcClient;
import org.example.transmission.socket.client.SocketRpcClient;

public class ProxyUtils {
    private static final RpcClient rpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    private static final RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
    public static <T> T getProxyService(Class<T> clazz) {
        return rpcClientProxy.getProxy(clazz);
    }
}
