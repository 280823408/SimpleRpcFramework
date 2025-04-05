package org.example.transmission.netty.client;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.RpcConstant;
import org.example.dto.RpcMessage;
import org.example.dto.RpcResponse;
import org.example.enums.CompressType;
import org.example.enums.MessageType;
import org.example.enums.SerializeType;
import org.example.enums.VersionType;

@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage rpcMessage) throws Exception {
        if (rpcMessage.getMsgType().isHeartbeat()) {
            log.debug("收到服务端心跳响应：{}", rpcMessage);
            return;
        }

        log.info("收到服务端数据：{}", rpcMessage);
        RpcResponse<?> response = (RpcResponse<?>) rpcMessage.getData();

        UnprocessedRpcReq.complete(response);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        boolean isNeedHeartBeat = evt instanceof IdleStateEvent
                && ((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE;

        if (!isNeedHeartBeat) {
            super.userEventTriggered(ctx, evt);
            return;
        }

        RpcMessage rpcMessage = RpcMessage.builder()
                .version(VersionType.VERSION1)
                .serializeType(SerializeType.KRYO)
                .compressType(CompressType.GZIP)
                .msgType(MessageType.HEARTBEAT_REQ)
                .build();

        log.debug("客户端发送心跳请求：{}", rpcMessage);
        ctx.writeAndFlush(rpcMessage)
                .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端捕获到异常：{}", cause.getMessage());
        ctx.close();
    }
}
