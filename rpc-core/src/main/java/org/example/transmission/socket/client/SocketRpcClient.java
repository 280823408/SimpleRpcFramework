package org.example.transmission.socket.client;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.RpcRequest;
import org.example.dto.RpcResponse;
import org.example.factory.SingletonFactory;
import org.example.registry.ServiceDiscovery;
import org.example.registry.impl.ZkServiceDiscovery;
import org.example.transmission.RpcClient;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @Author Mike
 * @Date 2025/3/25
 */
@Slf4j
public class SocketRpcClient implements RpcClient {
    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient() {
        this(SingletonFactory.getInstance(ZkServiceDiscovery.class));
    }

    public SocketRpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public Future<RpcResponse<?>> sendRequest(RpcRequest rpcRequest) {
        InetSocketAddress address = serviceDiscovery.lookupService(rpcRequest);
        try (Socket socket = new Socket(address.getAddress(), address.getPort())) {
            ObjectOutputStream outputStream = new ObjectOutputStream(
                    socket.getOutputStream());
            outputStream.writeObject(rpcRequest);
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(
                    socket.getInputStream());
            Object response = inputStream.readObject();
            return CompletableFuture.completedFuture((RpcResponse<?>) response);
        } catch (Exception e) {
            log.error("发送rpc请求失败：", e);
            throw new RuntimeException(e);
        }
    }
}
