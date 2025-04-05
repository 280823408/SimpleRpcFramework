package org.example.provider.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.config.RpcServiceConfig;
import org.example.provider.ServiceProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Mike
 * @Date 2025/3/27
 **/
@Slf4j
public class SimpleServiceProvider implements ServiceProvider {
    private final Map<String, Object> SERVICE_CACHE = new HashMap<>();

    @Override
    public void publishService(RpcServiceConfig config) {
        List<String> rpcServiceNames = config.rpcServiceNames();
        if (CollUtil.isEmpty(rpcServiceNames)) {
            throw new RuntimeException("该服务没有实现接口");
        }
        log.info("正在发布服务:{}", rpcServiceNames);
        rpcServiceNames.forEach(rpcServiceName -> {
            SERVICE_CACHE.put(rpcServiceName, config.getService());
        });
        log.info("服务发布完成:{}", rpcServiceNames);
    }

    @Override
    public Object getService(String rpcServiceName) {
        if (!SERVICE_CACHE.containsKey(rpcServiceName)) {
            throw new RuntimeException("找不到对应服务:" + rpcServiceName);
        }
        return SERVICE_CACHE.get(rpcServiceName);
    }
}
