package org.example.transmission;

import org.example.config.RpcServiceConfig;

/**
 * @Author Mike
 * @Date 2025/3/27
 **/

public interface RpcServer {
    void start();
    void publishService(RpcServiceConfig config);
}
