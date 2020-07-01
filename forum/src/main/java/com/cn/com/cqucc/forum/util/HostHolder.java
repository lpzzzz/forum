package com.cn.com.cqucc.forum.util;

import com.cn.com.cqucc.forum.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替session对象
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    /**
     * 用完之后对其进行清理
     */
    public void clear() {
        users.remove();
    }
}
