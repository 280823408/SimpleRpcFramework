# Project Overview

This project is a simple RPC (Remote Procedure Call) framework based on Java, designed to provide efficient and reliable remote service invocation capabilities. The framework supports multiple serialization methods (such as Kryo, Hessian, Protostuff), load balancing strategies (such as random, round-robin, consistent hashing), and mechanisms like circuit breaking, rate limiting, and retries to ensure system stability and reliability in high-concurrency scenarios.

## Key Features

- **Multiple Serialization Methods**: Supports Kryo, Hessian, Protostuff, and other serialization methods, allowing users to choose the appropriate tool based on their needs.
- **Load Balancing**: Provides various load balancing strategies like random, round-robin, and consistent hashing to ensure even distribution of requests across service nodes.
- **Circuit Breaking**: Implements circuit breaking through the `@Breaker` annotation to prevent system avalanches caused by service node failures.
- **Rate Limiting**: Implements rate limiting through the `@Limit` annotation to control the request rate and prevent system overload.
- **Retry Mechanism**: Implements a retry mechanism through the `@Retry` annotation to ensure automatic retries in case of service invocation failures.
- **Service Registration and Discovery**: Implements service registration and discovery based on Zookeeper, supporting dynamic scaling of service nodes.
- **Netty and Socket Support**: Supports both Netty and Socket communication methods, allowing users to choose the appropriate communication method based on their needs.

## Quick Start

1. **Start the Server**:
    - Start the server in `Server.java`, and the server will automatically register with Zookeeper.
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
2. **Start the Client**:
    - Start the client in `Client.java`, and the client will discover the service through Zookeeper and invoke the remote method.
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
## Dependencies

- **Zookeeper**: Used for service registration and discovery.
- **Netty**: Used for high-performance network communication.
- **Kryo/Hessian/Protostuff**: Used for serialization and deserialization.

## Configuration

- **Zookeeper Address**: Configure the Zookeeper address and port in `RpcConstant.java`.
- **Service Port**: Configure the server listening port in `RpcConstant.java`.

## Contribution

Welcome to submit issues and pull requests to help improve this project.

## License

This project is licensed under the MIT License. See the LICENSE file for details.

