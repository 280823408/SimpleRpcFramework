package org.example;

import org.example.config.RpcServiceConfig;
import org.example.service.UserServiceImpl;
import org.example.transmission.RpcServer;
import org.example.transmission.netty.server.NettyRpcServer;

/**
 * @Author Mike
 * @Date 2025/3/27
 **/
public class Server {
    public static void main(String[] args) {
//        RpcServiceConfig config = new RpcServiceConfig(new UserServiceImpl());
//
//        RpcServer rpcServer = new SocketRpcServer();
//        rpcServer.publishService(config);
//
//        rpcServer.start();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig(new UserServiceImpl());
        RpcServer rpcServer = new NettyRpcServer();
        rpcServer.publishService(rpcServiceConfig);
        rpcServer.start();
    }
}
