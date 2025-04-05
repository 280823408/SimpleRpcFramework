package org.example.handler;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.util.concurrent.RateLimiter;
import org.example.annotation.Limit;
import org.example.dto.RpcRequest;
import org.example.provider.ServiceProvider;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;
    private static final Map<String, RateLimiter> RATE_LIMITER_MAP =
            new ConcurrentHashMap<>();
    public RpcRequestHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }
    @SneakyThrows
    public Object invoke(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.rpcServiceName();
        Object service = serviceProvider.getService(rpcServiceName);

        log.info("获取到对应服务: {}", service.getClass().getCanonicalName());
        Method method = service.getClass()
                .getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());

        Limit limit = method.getAnnotation(Limit.class);
        if (Objects.isNull(limit)) {
            return method.invoke(service, rpcRequest.getParams());
        }

        RateLimiter rateLimiter = RATE_LIMITER_MAP.computeIfAbsent(rpcServiceName, __ ->
                RateLimiter.create(limit.permitsPerSecond()));

        if (!rateLimiter.tryAcquire(limit.timeout(), TimeUnit.MILLISECONDS)) {
            log.error("限流: {}", rpcServiceName);
            throw new RuntimeException("系统繁忙, 请稍后再试");
        }

        return method.invoke(service, rpcRequest.getParams());
    }

}
