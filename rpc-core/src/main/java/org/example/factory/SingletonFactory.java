package org.example.factory;

import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactory {
    private static volatile Map<Class<?>, Object> INSTANCE_CACHE = new ConcurrentHashMap<Class<?>, Object>();
    private SingletonFactory() {
    }

    @SneakyThrows
    public static <T> T getInstance(Class<T> clazz) {
        if (Objects.isNull(clazz)) {
            throw new IllegalArgumentException("clazz不能为空");
        }
        if (INSTANCE_CACHE.containsKey(clazz)) {
            return clazz.cast(INSTANCE_CACHE.get(clazz));
        }
        synchronized (INSTANCE_CACHE) {
            if (INSTANCE_CACHE.containsKey(clazz)) {
                return clazz.cast(INSTANCE_CACHE.get(clazz));
            }
            T t = clazz.getConstructor().newInstance();
            INSTANCE_CACHE.put(clazz, t);
            return t;
        }
    }
}
