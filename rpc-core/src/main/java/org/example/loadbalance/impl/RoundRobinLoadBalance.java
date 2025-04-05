package org.example.loadbalance.impl;

import org.example.loadbalance.LoadBalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalance implements LoadBalance {
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public String select(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int currentIndex = index.getAndIncrement() % list.size();
        return list.get(currentIndex);
    }
}
