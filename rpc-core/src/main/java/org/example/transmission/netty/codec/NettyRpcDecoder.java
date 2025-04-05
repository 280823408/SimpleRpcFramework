package org.example.transmission.netty.codec;

import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.example.compress.Compress;
import org.example.compress.impl.GzipCompress;
import org.example.constant.RpcConstant;
import org.example.dto.RpcMessage;
import org.example.dto.RpcRequest;
import org.example.dto.RpcResponse;
import org.example.enums.CompressType;
import org.example.enums.MessageType;
import org.example.enums.SerializeType;
import org.example.enums.VersionType;
import org.example.exception.RpcException;
import org.example.factory.SingletonFactory;
import org.example.serialzer.Serializer;
import org.example.serialzer.impl.KryoSerializer;

/**
 * @Author Mr.Pan
 * @Date 2025/2/23
 **/
public class NettyRpcDecoder extends LengthFieldBasedFrameDecoder {

    public NettyRpcDecoder() {
        super(RpcConstant.REQ_MAX_LEN, 5, 4, -9, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);

        return decodeFrame(frame);
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        readAndCheckMagicCode(byteBuf);

        byte versionCode = byteBuf.readByte();
        VersionType version = VersionType.from(versionCode);

        int msgLen = byteBuf.readInt();

        byte msgTypeCode = byteBuf.readByte();
        MessageType msgType = MessageType.from(msgTypeCode);

        byte serializerTypeCode = byteBuf.readByte();
        SerializeType serializeType = SerializeType.from(serializerTypeCode);

        byte compressTypeCode = byteBuf.readByte();
        CompressType compressType = CompressType.from(compressTypeCode);

        int reqId = byteBuf.readInt();

        Object data = readData(byteBuf, msgLen - RpcConstant.REQ_HEAD_LEN, msgType, serializeType);

        return RpcMessage.builder()
            .reqId(reqId)
            .msgType(msgType)
            .version(version)
            .compressType(compressType)
            .serializeType(serializeType)
            .data(data)
            .build();
    }

    private void readAndCheckMagicCode(ByteBuf byteBuf) {
        byte[] magicBytes = new byte[RpcConstant.RPC_MAGIC_CODE.length];
        byteBuf.readBytes(magicBytes);

        if (!ArrayUtil.equals(magicBytes, RpcConstant.RPC_MAGIC_CODE)) {
            throw new RpcException("魔法值异常:" + new String(magicBytes));
        }
    }

    private Object readData(ByteBuf byteBuf, int dataLen, MessageType msgType, SerializeType serializeType) {
        if (msgType.isReq()) {
            return readData(byteBuf, dataLen, RpcRequest.class, serializeType);
        }

        return readData(byteBuf, dataLen, RpcResponse.class, serializeType);
    }

    private <T> T readData(ByteBuf byteBuf, int dataLen, Class<T> clazz, SerializeType serializeType) {
        if (dataLen <= 0) {
            return null;
        }

        byte[] data = new byte[dataLen];
        byteBuf.readBytes(data);

        Compress compress = SingletonFactory.getInstance(GzipCompress.class);
        data = compress.decompress(data);

        String serializerTypeStr = serializeType.getDesc();

        Serializer serializer = SingletonFactory.getInstance(KryoSerializer.class);

        return serializer.deserialize(data, clazz);
    }
}
