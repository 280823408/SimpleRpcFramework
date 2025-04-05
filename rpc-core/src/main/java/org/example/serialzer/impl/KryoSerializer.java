package org.example.serialzer.impl;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.RpcRequest;
import org.example.dto.RpcResponse;
import org.example.serialzer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @Author Mike
 * @Date 2025/4/1
 **/
@Slf4j
public class KryoSerializer implements Serializer {
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);

        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream oos = new ByteArrayOutputStream();
             Output output = new Output(oos)) {

            Kryo kryo = KRYO_THREAD_LOCAL.get();
            kryo.writeObject(output, obj);
            output.flush();

            log.info("========使用kryo做序列化==========");

            return oos.toByteArray();
        } catch (Exception e) {
            log.error("kryo序列化失败", e);
            throw new RuntimeException(e);
        } finally {
            KRYO_THREAD_LOCAL.remove();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes);
             Input input = new Input(is)) {

            Kryo kryo = KRYO_THREAD_LOCAL.get();
            log.info("========使用kryo做反序列化==========");
            return kryo.readObject(input, clazz);
        } catch (Exception e) {
            log.error("kryo反序列化失败", e);
            throw new RuntimeException(e);
        } finally {
            KRYO_THREAD_LOCAL.remove();
        }
    }
}
