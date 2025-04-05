package org.example.transmission.socket.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.config.RpcServiceConfig;
import org.example.dto.RpcRequest;
import org.example.dto.RpcResponse;
import org.example.factory.SingletonFactory;
import org.example.handler.RpcRequestHandler;
import org.example.provider.ServiceProvider;
import org.example.provider.impl.SimpleServiceProvider;
import org.example.provider.impl.ZkServiceProvider;
import org.example.transmission.RpcServer;
import org.example.utils.ThreadPoolUtils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static org.example.constant.RpcConstant.SERVER_PORT;

/**
 * @Author Mike
 * @Date 2025/3/25
 */

@Slf4j
public class SocketRpcServer implements RpcServer {
    private final int port;
    private final RpcRequestHandler rpcRequestHandler;
    private final ServiceProvider serviceProvider;
    private final ExecutorService executor;

    public SocketRpcServer() {
        this(SERVER_PORT);
    }

    public SocketRpcServer(int port) {
        this(port, SingletonFactory.getInstance(ZkServiceProvider.class));
    }

    public SocketRpcServer(int port, ServiceProvider serviceProvider) {
        this.port = port;
        this.serviceProvider = serviceProvider;
        this.rpcRequestHandler = new RpcRequestHandler(serviceProvider);
        this.executor = ThreadPoolUtils.createIoIntensiveThreadPool
                ("socket-rpc-server-");
    }

    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)){
            log.info("服务启动, 端口: {}", port);
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                executor.submit(new SocketRequestHandler(socket, rpcRequestHandler));
            }
        } catch (Exception e) {
            log.error("服务端异常", e);
        }
    }

    @Override
    public void publishService(RpcServiceConfig config) {
        serviceProvider.publishService(config);
    }
}
