package org.example.loadbalance.impl;

import cn.hutool.core.util.RandomUtil;
import org.example.loadbalance.LoadBalance;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance {
    @Override
    public String select(List<String> list) {
        return RandomUtil.randomEle(list);
    }
}
