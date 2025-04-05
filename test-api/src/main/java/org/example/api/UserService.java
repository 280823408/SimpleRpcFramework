package org.example.api;

import org.example.annotation.Limit;
import org.example.annotation.Retry;

/**
 * @Author Mike
 * @Date 2025/3/27
 **/

public interface UserService {
    User getUser(Long id);
}
