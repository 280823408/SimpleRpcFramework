package org.example.registry.zk;

import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.example.utils.IPUtils;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.constant.RpcConstant.ZK_HOST;
import static org.example.constant.RpcConstant.ZK_PORT;

@Slf4j
public class ZkClient {
    private CuratorFramework client;
    private static final Integer BASE_SLEEP_TIME = 1000;
    private static final Integer MAX_RETRIES = 3;
    // key为/rpc/rpcServiceName, value为childrenNode [ip:port]
    private static final Map<String, List<String>> SERVICE_ADDRESS_CACHE = new ConcurrentHashMap<>();
    // /rpc/rpcServiceName/ip:port
    private static final Set<String> SERVICE_ADDRESS_SET = ConcurrentHashMap.newKeySet();

    public ZkClient() {
        this(ZK_HOST, ZK_PORT);
    }

    public ZkClient (String hostname, int port) {
        log.info("开始连接zk, hostname: {}, port: {}", hostname, port);
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME,
                MAX_RETRIES);
        this.client = CuratorFrameworkFactory.builder()
                .connectString(hostname + StrUtil.COLON + port)
                .retryPolicy(retryPolicy)
                .build();

        log.info("开始连接zk...");

        this.client.start();

        log.info("连接zk成功");
    }

    @SneakyThrows
    public void createPersistentNode(String path) {
        if (StrUtil.isBlank(path)) {
            throw new IllegalArgumentException("path为空");
        }

        if (SERVICE_ADDRESS_SET.contains(path)) {
            log.info("该节点已存在: {}", path);
            return;
        }

        if (client.checkExists().forPath(path) != null) {
            SERVICE_ADDRESS_SET.add(path);
            log.info("该节点已存在: {}", path);
            return;
        }

        log.info("创建持久化节点: {}", path);
        client.create()
                .creatingParentsIfNeeded()
                .forPath(path);

        SERVICE_ADDRESS_SET.add(path);
    }

    @SneakyThrows
    public List<String> getChildrenNodes(String path) {
        if (StrUtil.isBlank(path)) {
            throw new IllegalArgumentException("rpcServiceName为空");
        }

        if (SERVICE_ADDRESS_CACHE.containsKey(path)) {
            return SERVICE_ADDRESS_CACHE.get(path);
        }

        List<String> children = client.getChildren().forPath(path);
        SERVICE_ADDRESS_CACHE.put(path, children);
        return client.getChildren().forPath(path);
   }

    public void clearAll(InetSocketAddress address) {
        if (Objects.isNull(address)) {
            throw new IllegalArgumentException("address为null");
        }

        // /rpc/rpcServiceName/ip:port
        SERVICE_ADDRESS_SET.forEach(path -> {
            if (path.endsWith(IPUtils.toIpPort(address))) {
                log.debug("zk删除节点, {}", path);
                try {
                    client.delete().deletingChildrenIfNeeded().forPath(path);
                } catch (Exception e) {
                    log.error("zk删除失败, {}", path, e);
                }
            }
        });
    }
}
