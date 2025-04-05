# 项目概述

本项目是一个基于Java的简单RPC（远程过程调用）框架，旨在提供高效、可靠的远程服务调用能力。框架支持多种序列化方式（如Kryo、Hessian、Protostuff）、负载均衡策略（如随机、轮询、一致性哈希）以及熔断、限流、重试等机制，确保系统在高并发场景下的稳定性和可靠性。

## 主要特性

- **多种序列化方式**：支持Kryo、Hessian、Protostuff等多种序列化方式，用户可以根据需求选择合适的序列化工具。
- **负载均衡**：提供随机、轮询、一致性哈希等多种负载均衡策略，确保请求均匀分布到各个服务节点。
- **熔断机制**：通过`@Breaker`注解实现熔断机制，防止因服务节点故障导致系统雪崩。
- **限流机制**：通过`@Limit`注解实现限流机制，控制服务的请求速率，防止系统过载。
- **重试机制**：通过`@Retry`注解实现重试机制，确保在服务调用失败时能够自动重试。
- **服务注册与发现**：基于Zookeeper实现服务的注册与发现，支持动态扩展服务节点。
- **Netty与Socket支持**：支持基于Netty和Socket的两种通信方式，用户可以根据需求选择合适的通信方式。

## 快速开始

1. **启动服务端**：
    - 在`Server.java`中启动服务端，服务端会自动注册到Zookeeper。
```java
public class Server {
    public static void main(String[] args) {
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig(new UserServiceImpl());
        RpcServer rpcServer = new NettyRpcServer();
        rpcServer.publishService(rpcServiceConfig);
        rpcServer.start();
    }
}
```
2. **启动客户端**：
    - 在`Client.java`中启动客户端，客户端会通过Zookeeper发现服务并调用远程方法。
```java
public class Client {
    public static void main(String[] args) {
        UserService userService = ProxyUtils.getProxyService(UserService.class);
        ExecutorService threadPool = ThreadPoolUtils.createIoIntensiveThreadPool("test");
        
        for (int i = 0; i < 100; i++) {
            threadPool.submit(() -> {
                User user = userService.getUser(1L);
                System.out.println("user = " + user);
            });
        }
    }
}
```
## 依赖

- **Zookeeper**：用于服务注册与发现。
- **Netty**：用于高性能的网络通信。
- **Kryo/Hessian/Protostuff**：用于序列化与反序列化。

## 配置

- **Zookeeper地址**：在`RpcConstant.java`中配置Zookeeper的地址和端口。
- **服务端口**：在`RpcConstant.java`中配置服务端的监听端口。

## 贡献

欢迎提交Issue和Pull Request，共同完善该项目。

## 许可证

本项目采用MIT许可证，详情请参见LICENSE文件。

