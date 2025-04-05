package org.example.proxy;

import cn.hutool.core.util.IdUtil;
import com.github.rholder.retry.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.annotation.Breaker;
import org.example.annotation.Retry;
import org.example.breaker.CircuitBreaker;
import org.example.breaker.CircuitBreakerManager;
import org.example.config.RpcServiceConfig;
import org.example.dto.RpcRequest;
import org.example.dto.RpcResponse;
import org.example.enums.RpcRespStatus;
import org.example.exception.RpcException;
import org.example.transmission.RpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private final RpcClient rpcClient;
    private final RpcServiceConfig config;

    public RpcClientProxy(RpcClient rpcClient) {
        this(rpcClient, new RpcServiceConfig());
    }

    public RpcClientProxy(RpcClient rpcClient, RpcServiceConfig config) {
        this.rpcClient = rpcClient;
        this.config = config;
    }


    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                this
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcReq = RpcRequest.builder()
                .reqId(IdUtil.fastSimpleUUID())
                .interfaceName(method.getDeclaringClass().getCanonicalName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .version(config.getVersion())
                .group(config.getGroup())
                .build();

        Breaker breaker = method.getAnnotation(Breaker.class);
        if (Objects.isNull(breaker)) {
            return sendRequestWithRetry(rpcReq, method);
        }

        CircuitBreaker circuitBreaker = CircuitBreakerManager.
                get(rpcReq.rpcServiceName(), breaker);
        if (!circuitBreaker.canRequest()) {
            log.error("请求被熔断处理");
            throw new RpcException("已被熔断处理");
        }

        try {
            Object data = sendRequest(rpcReq);
            circuitBreaker.success();
            return data;
        } catch (Exception e) {
            circuitBreaker.fail();
            throw e;
        }
    }

    @SneakyThrows
    private Object sendRequestWithRetry(RpcRequest rpcReq, Method method) {
        Retry retry = method.getAnnotation(Retry.class);
        if (Objects.isNull(retry)) {
            return sendRequest(rpcReq);
        }

        Retryer<Object> retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(retry.value())
                .withStopStrategy(StopStrategies.stopAfterAttempt(retry.maxRetryTimes()))
                .withWaitStrategy(WaitStrategies.fixedWait(retry.delay(), TimeUnit.MILLISECONDS))
                .build();

        return retryer.call(() -> sendRequest(rpcReq));
    }

    @SneakyThrows
    private Object sendRequest(RpcRequest rpcReq) {
        Future<RpcResponse<?>> future = rpcClient.sendRequest(rpcReq);
        RpcResponse<?> rpcResp = future.get();
        check(rpcReq, rpcResp);

        return rpcResp.getData();
    }

    private void check(RpcRequest rpcReq, RpcResponse<?> rpcResp) {
        if (Objects.isNull(rpcResp)) {
            throw new RpcException("rpcResp为空");
        }

        if (!Objects.equals(rpcReq.getReqId(), rpcResp.getRequestId())) {
            throw new RpcException("请求和响应的id不一致");
        }

        if (RpcRespStatus.isFailed(rpcResp.getCode())) {
            throw new RpcException("响应值为失败: " + rpcResp.getMessage());
        }
    }
}
