package org.example.client;

import org.example.api.User;
import org.example.api.UserService;
import org.example.client.utils.ProxyUtils;
import org.example.utils.ThreadPoolUtils;

import java.util.concurrent.ExecutorService;

public class Client {
    public static void main(String[] args) {
        UserService userService = ProxyUtils.getProxyService(UserService.class);

        ExecutorService threadPool = ThreadPoolUtils.createIoIntensiveThreadPool
                ("test");
        for (int i = 0; i < 100; i++) {
            threadPool.submit(() -> {
                User user = userService.getUser(1L);
                System.out.println("user = " + user);
            });
        }
    }
}
