package org.example.breaker;

import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {
    private State state = State.CLOSED;
    private final AtomicInteger failCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger totalCount = new AtomicInteger(0);
    // 失败阈值
    private final int failThreshold;
    // 半开窗口内成功阈值
    private final double successRateInHalfOpen;
    // 半开窗口时间
    private final long windowTime;
    // 上次失败时间
    private long lastFailTime;

    public CircuitBreaker(int failThreshold, double successRateInHalfOpen, long windowTime) {
        this.failThreshold = failThreshold;
        this.successRateInHalfOpen = successRateInHalfOpen;
        this.windowTime = windowTime;
    }

    public synchronized boolean canRequest() {
        switch (state) {
            case CLOSED: return true;
            case OPEN:
                if (System.currentTimeMillis() - lastFailTime <= windowTime) {
                    return false;
                }

                state = State.HALF_OPEN;
                resetCount();
                return true;
            case HALF_OPEN:
                totalCount.incrementAndGet();
                return true;
            default:
                throw new IllegalStateException("熔断器状态异常");
        }
    }

    public synchronized void success() {
        if (state != State.HALF_OPEN) {
            resetCount();
        }

        successCount.incrementAndGet();
        if (successCount.get() >= successRateInHalfOpen * totalCount.get()) {
            state = State.CLOSED;
            resetCount();
        }
    }

    public synchronized void fail() {
        lastFailTime = System.currentTimeMillis();
        failCount.incrementAndGet();

        if (state == State.HALF_OPEN) {
            state = State.OPEN;
            return;
        }

        if (failCount.get() >= failThreshold) {
            state = State.OPEN;
        }
    }

    private void resetCount() {
        failCount.set(0);
        successCount.set(0);
        totalCount.set(0);
    }

    enum State {
        OPEN,
        HALF_OPEN,
        CLOSED
    }
}
