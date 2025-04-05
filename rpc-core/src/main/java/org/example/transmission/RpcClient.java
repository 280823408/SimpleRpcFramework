package org.example.transmission;


import org.example.dto.RpcRequest;
import org.example.dto.RpcResponse;

import java.util.concurrent.Future;

/**
 * @Author Mike
 * @Date 2025/3/27
 **/

public interface RpcClient {
    Future<RpcResponse<?>> sendRequest(RpcRequest rpcRequest);
}
