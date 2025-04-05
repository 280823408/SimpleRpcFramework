package org.example.transmission.socket.server;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.RpcRequest;
import org.example.dto.RpcResponse;
import org.example.handler.RpcRequestHandler;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Slf4j
@AllArgsConstructor
public class SocketRequestHandler implements Runnable {
    private final Socket socket;
    private final RpcRequestHandler rpcRequestHandler;
    @SneakyThrows
    @Override
    public void run() {
        ObjectInputStream inputStream = new ObjectInputStream(
                socket.getInputStream());
        RpcRequest rpcRequest = (RpcRequest) inputStream.readObject();

        Object data = rpcRequestHandler.invoke(rpcRequest);

        ObjectOutputStream outputStream = new ObjectOutputStream(
                socket.getOutputStream());
        RpcResponse<?> rpcResponse = RpcResponse.success(rpcRequest.getReqId(), data);
        outputStream.writeObject(rpcResponse);
        outputStream.flush();
    }
}
