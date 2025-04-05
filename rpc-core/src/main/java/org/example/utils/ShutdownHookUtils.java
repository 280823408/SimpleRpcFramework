package org.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.factory.SingletonFactory;
import org.example.registry.ServiceRegistry;
import org.example.registry.impl.ZkServiceRegistry;

/**
 * @Author Mike
 * @Date 2025/3/28
 **/
@Slf4j
public class ShutdownHookUtils {
    public static void clearAll() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("系统结束运行, 清理资源");
            ServiceRegistry serviceRegistry = SingletonFactory.getInstance(ZkServiceRegistry.class);
            serviceRegistry.clearAll();
            ThreadPoolUtils.shutdownAll();
        }));
    }
}
