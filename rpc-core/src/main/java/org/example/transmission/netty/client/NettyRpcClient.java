package org.example.transmission.netty.client;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.RpcConstant;
import org.example.dto.RpcMessage;
import org.example.dto.RpcRequest;
import org.example.dto.RpcResponse;
import org.example.enums.CompressType;
import org.example.enums.MessageType;
import org.example.enums.SerializeType;
import org.example.enums.VersionType;
import org.example.factory.SingletonFactory;
import org.example.registry.ServiceDiscovery;
import org.example.registry.impl.ZkServiceDiscovery;
import org.example.transmission.RpcClient;
import org.example.transmission.netty.codec.NettyRpcDecoder;
import org.example.transmission.netty.codec.NettyRpcEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyRpcClient implements RpcClient {
    private static final Bootstrap bootstrap;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private final ServiceDiscovery serviceDiscovery;
    private final ChannelPool channelPool;

    public NettyRpcClient() {
        this(SingletonFactory.getInstance(ZkServiceDiscovery.class));
    }

    public NettyRpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        this.channelPool = SingletonFactory.getInstance(ChannelPool.class);
    }

    static {
        bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECT_TIMEOUT)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        channel.pipeline().addLast(new IdleStateHandler(
                                0,5,0, TimeUnit.SECONDS
                        ));
                        channel.pipeline().addLast(new NettyRpcDecoder());
                        channel.pipeline().addLast(new NettyRpcEncoder());
                        channel.pipeline().addLast(new NettyClientHandler());
                    }
                });

    }

    @SneakyThrows
    @Override
    public Future<RpcResponse<?>> sendRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<?>> cf = new CompletableFuture<>();
        UnprocessedRpcReq.put(rpcRequest.getReqId(), cf);

        InetSocketAddress address = serviceDiscovery.lookupService(rpcRequest);

        Channel channel = channelPool.get(address, () -> connect(address));

        log.info("客户端连接到:{}", address);

        RpcMessage rpcMessage = RpcMessage.builder()
                .version(VersionType.VERSION1)
                .serializeType(SerializeType.KRYO)
                .compressType(CompressType.GZIP)
                .msgType(MessageType.RPC_REQ)
                .data(rpcRequest)
                .build();

        channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) listener -> {
            if (!listener.isSuccess()) {
                listener.channel().close();
                cf.completeExceptionally(listener.cause());
            }
        });
        return cf;
    }

    private Channel connect(InetSocketAddress address) {
        try {
            return bootstrap.connect(address)
                    .sync()
                    .channel();
        } catch (InterruptedException e) {
            log.error("连接到远程服务器失败:{}", address);
            throw new RuntimeException(e);
        }
    }
}
