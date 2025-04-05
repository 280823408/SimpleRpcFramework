package org.example.transmission.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.config.RpcServiceConfig;
import org.example.constant.RpcConstant;
import org.example.factory.SingletonFactory;
import org.example.provider.ServiceProvider;
import org.example.provider.impl.ZkServiceProvider;
import org.example.transmission.RpcServer;
import org.example.transmission.netty.codec.NettyRpcDecoder;
import org.example.transmission.netty.codec.NettyRpcEncoder;
import org.example.utils.ShutdownHookUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyRpcServer implements RpcServer {
    private final ServiceProvider serviceProvider;
    private final int port;

    public NettyRpcServer() {
        this(SingletonFactory.getInstance(ZkServiceProvider.class),RpcConstant.SERVER_PORT);
    }

    public NettyRpcServer(int port) {
        this(SingletonFactory.getInstance(ZkServiceProvider.class),port);
    }

    public NettyRpcServer(ServiceProvider serviceProvider, int port) {
        this.serviceProvider = serviceProvider;
        this.port = port;
    }

    @Override
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new IdleStateHandler(
                                    30,0,0, TimeUnit.SECONDS
                            ));
                            channel.pipeline().addLast(new NettyRpcDecoder());
                            channel.pipeline().addLast(new NettyRpcEncoder());
                            channel.pipeline().addLast(new NettyRpcServerHandler(serviceProvider));
                        }
                    });
            ShutdownHookUtils.clearAll();
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            log.info("服务器启动成功,端口{}", port);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("启动服务器时有错误发生", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    @Override
    public void publishService(RpcServiceConfig config) {
        serviceProvider.publishService(config);
    }
}
