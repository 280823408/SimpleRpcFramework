package org.example.serialzer.impl;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;
import org.example.serialzer.Serializer;

/**
 * @Author Mike
 * @Date 2025/4/1
 **/
@Slf4j
public class ProtostuffSerializer implements Serializer {
    private static final LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    @Override
    public byte[] serialize(Object obj) {
        Class<?> aClass = obj.getClass();

        Schema schema = RuntimeSchema.getSchema(aClass);

        try {
            log.info("========使用Protostuff做序列化==========");
            return ProtobufIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);

        T t = schema.newMessage();
        ProtobufIOUtil.mergeFrom(bytes, t, schema);
        log.info("========使用Protostuff做反序列化==========");

        return t;
    }
}
