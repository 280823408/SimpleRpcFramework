package org.example.breaker;


import org.example.annotation.Breaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Mike
 * @Date 2025/4/1
 **/
public class CircuitBreakerManager {
    private static final Map<String, CircuitBreaker> BREAKER_MAP = new ConcurrentHashMap<>();

    public static CircuitBreaker get(String key, Breaker breaker) {
        return BREAKER_MAP.computeIfAbsent(key, __ -> new CircuitBreaker(
            breaker.failThreshold(),
            breaker.successRateInHalfOpen(),
            breaker.windowTime()
        ));
    }
}
