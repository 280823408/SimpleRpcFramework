package org.example.transmission.netty.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.RpcMessage;
import org.example.dto.RpcRequest;
import org.example.dto.RpcResponse;
import org.example.enums.CompressType;
import org.example.enums.MessageType;
import org.example.enums.SerializeType;
import org.example.enums.VersionType;
import org.example.handler.RpcRequestHandler;
import org.example.provider.ServiceProvider;

@Slf4j
public class NettyRpcServerHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler(ServiceProvider serviceProvider) {
        this.rpcRequestHandler = new RpcRequestHandler(serviceProvider);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        boolean isNeedHeartBeat = evt instanceof IdleStateEvent
                && ((IdleStateEvent) evt).state() == IdleState.READER_IDLE;

        if (!isNeedHeartBeat) {
            super.userEventTriggered(ctx, evt);
            return;
        }

        log.debug("服务端长时间未收到客户端心跳，关闭channel，addr{}"
                , ctx.channel().remoteAddress());
        ctx.channel().close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage rpcMessage) throws Exception {
        log.debug("收到客户端请求：{}", rpcMessage);
        MessageType messageType = rpcMessage.getMsgType();

        RpcRequest request = (RpcRequest) rpcMessage.getData();
        RpcResponse<?> response;

        if (messageType.isHeartbeat()) {
            log.debug("服务端收到心跳请求：{}", rpcMessage);
            messageType = MessageType.HEARTBEAT_RESP;
            response = null;
        } else {
            messageType = MessageType.RPC_RESP;
            response = handleRpcReq(request);
        }

        RpcMessage rpcResponse = RpcMessage.builder()
            .reqId(rpcMessage.getReqId())
            .version(VersionType.VERSION1)
            .msgType(messageType)
            .compressType(CompressType.GZIP)
            .serializeType(SerializeType.KRYO)
            .data(response)
            .build();

        ctx.channel()
            .writeAndFlush(rpcResponse)
            .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务端捕获到异常：{}", cause.getMessage());
        ctx.close();
    }

    private RpcResponse<?> handleRpcReq(RpcRequest rpcReq) {
        try {
            Object object = rpcRequestHandler.invoke(rpcReq);
            return RpcResponse.success(rpcReq.getReqId(), object);
        } catch (Exception e) {
            log.info("调用失败, ", e);
            return RpcResponse.fail(rpcReq.getReqId(), e.getMessage());
        }
    }
}
