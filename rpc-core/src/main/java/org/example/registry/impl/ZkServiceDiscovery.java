package org.example.registry.impl;

import cn.hutool.core.util.StrUtil;
import org.example.dto.RpcRequest;
import org.example.factory.SingletonFactory;
import org.example.loadbalance.LoadBalance;
import org.example.loadbalance.impl.HashConsistentLoadBalance;
import org.example.registry.ServiceDiscovery;
import org.example.registry.zk.ZkClient;
import org.example.utils.IPUtils;

import java.net.InetSocketAddress;
import java.util.List;

import static org.example.constant.RpcConstant.ZK_RPC_ROOT_PATH;

public class ZkServiceDiscovery implements ServiceDiscovery {
    private final ZkClient zkClient;
    private final LoadBalance loadBalance;

    public ZkServiceDiscovery() {
        this(
                SingletonFactory.getInstance(ZkClient.class),
                SingletonFactory.getInstance(HashConsistentLoadBalance.class)
        );
    }

    public ZkServiceDiscovery(ZkClient zkClient, LoadBalance loadBalance) {
        this.zkClient = zkClient;
        this.loadBalance = loadBalance;
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String path = ZK_RPC_ROOT_PATH
                + StrUtil.SLASH
                + rpcRequest.rpcServiceName();
        List<String> childrenNodes = zkClient.getChildrenNodes(path);
        String address = loadBalance.select(childrenNodes);
        return IPUtils.toInetSocketAddress(address);
    }
}
