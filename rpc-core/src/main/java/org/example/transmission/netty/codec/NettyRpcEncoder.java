package org.example.transmission.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.example.compress.Compress;
import org.example.compress.impl.GzipCompress;
import org.example.constant.RpcConstant;
import org.example.dto.RpcMessage;
import org.example.factory.SingletonFactory;
import org.example.serialzer.Serializer;
import org.example.serialzer.impl.KryoSerializer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Mr.Pan
 * @Date 2025/2/23
 **/
@Slf4j
public class NettyRpcEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ID_GEN = new AtomicInteger(0);


    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMsg, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(RpcConstant.RPC_MAGIC_CODE);
        byteBuf.writeByte(rpcMsg.getVersion().getCode());

        // 往右挪动4位, 给报文长度腾出空间
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);

        byteBuf.writeByte(rpcMsg.getMsgType().getCode());
        byteBuf.writeByte(rpcMsg.getSerializeType().getCode());
        byteBuf.writeByte(rpcMsg.getCompressType().getCode());
        byteBuf.writeInt(ID_GEN.getAndIncrement());

        int msgLen = RpcConstant.REQ_HEAD_LEN;
        if (!rpcMsg.getMsgType().isHeartbeat()
            && !Objects.isNull(rpcMsg.getData())) {
            byte[] data = data2Bytes(rpcMsg);
            byteBuf.writeBytes(data);
            msgLen += data.length;
        }

        int curIdx = byteBuf.writerIndex();
        byteBuf.writerIndex(curIdx - msgLen + RpcConstant.RPC_MAGIC_CODE.length + 1);
        byteBuf.writeInt(msgLen);
        byteBuf.writerIndex(curIdx);
    }

    private byte[] data2Bytes(RpcMessage rpcMsg) {
        String serializerTypeStr = rpcMsg.getSerializeType().getDesc();

        Serializer serializer = SingletonFactory.getInstance(KryoSerializer.class);
        byte[] data = serializer.serialize(rpcMsg.getData());

        Compress compress = SingletonFactory.getInstance(GzipCompress.class);
        return compress.compress(data);
    }
}
