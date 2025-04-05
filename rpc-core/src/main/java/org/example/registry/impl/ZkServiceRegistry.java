package org.example.registry.impl;

import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.RpcConstant;
import org.example.factory.SingletonFactory;
import org.example.registry.ServiceRegistry;
import org.example.registry.zk.ZkClient;
import org.example.utils.IPUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static org.example.constant.RpcConstant.ZK_RPC_ROOT_PATH;

@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {
    private final ZkClient zkClient;

    public ZkServiceRegistry() {
        this(SingletonFactory.getInstance(ZkClient.class));
    }

    public ZkServiceRegistry(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public void registerService(String rpcServiceName, InetSocketAddress address) {
        log.info("注册服务: {}", rpcServiceName);

        String path = ZK_RPC_ROOT_PATH
                + StrUtil.SLASH
                + rpcServiceName
                + StrUtil.SLASH
                + IPUtils.toIpPort(address);

        log.info("创建持久化节点: {}", path);

        zkClient.createPersistentNode(path);
    }

    @SneakyThrows
    @Override
    public void clearAll() {
        String host = InetAddress.getLocalHost().getHostAddress();
        int port = RpcConstant.SERVER_PORT;
        zkClient.clearAll(new InetSocketAddress(host, port));
    }
}
