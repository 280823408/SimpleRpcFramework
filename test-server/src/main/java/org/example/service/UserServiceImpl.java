package org.example.service;

import org.example.annotation.Breaker;
import org.example.annotation.Limit;
import org.example.annotation.Retry;
import org.example.api.User;
import org.example.api.UserService;

/**
 * @Author Mike
 * @Date 2025/3/27
 **/
public class UserServiceImpl implements UserService {
    @Override
    @Retry(delay = 5000)
    @Limit(permitsPerSecond = 5, timeout = 0)
    @Breaker(windowTime = 30000)
    public User getUser(Long id) {
        return User.builder()
            .id(++id)
            .name("张三")
            .build();
    }
}
