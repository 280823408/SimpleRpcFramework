package org.example.provider.impl;

import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import org.example.config.RpcServiceConfig;
import org.example.constant.RpcConstant;
import org.example.factory.SingletonFactory;
import org.example.provider.ServiceProvider;
import org.example.registry.ServiceRegistry;
import org.example.registry.impl.ZkServiceRegistry;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import static org.example.constant.RpcConstant.SERVER_PORT;

public class ZkServiceProvider implements ServiceProvider {
    private final Map<String, Object> SERVICE_CACHE = new HashMap<>();
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProvider() {
        this(SingletonFactory.getInstance(ZkServiceRegistry.class));
    }

    public ZkServiceProvider(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void publishService(RpcServiceConfig config) {
        config.rpcServiceNames().forEach(rpcServiceName -> {
            publishService(rpcServiceName, config.getService());
        });
    }

    @Override
    public Object getService(String rpcServiceName) {
        if (StrUtil.isBlank(rpcServiceName)) {
            throw new RuntimeException("rpcServiceName为空");
        }
        if (!SERVICE_CACHE.containsKey(rpcServiceName)) {
            throw new RuntimeException("找不到对应服务:" + rpcServiceName);
        }
        return SERVICE_CACHE.get(rpcServiceName);
    }

    @SneakyThrows
    private void publishService(String rpcServiceName, Object service) {
        SERVICE_CACHE.put(rpcServiceName, service);

        String host = InetAddress.getLocalHost().getHostAddress();
        int port = SERVER_PORT;

        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        serviceRegistry.registerService(rpcServiceName, inetSocketAddress);
    }
}
