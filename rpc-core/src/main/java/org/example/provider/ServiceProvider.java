package org.example.provider;

import org.example.config.RpcServiceConfig;

/**
 * @Author Mike
 * @Date 2025/3/27
 **/
public interface ServiceProvider {
    void publishService(RpcServiceConfig config);
    Object getService(String rpcServiceName);
}
