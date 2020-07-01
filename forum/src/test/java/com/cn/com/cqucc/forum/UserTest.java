package com.cn.com.cqucc.forum;

import com.cn.com.cqucc.forum.entity.User;
import com.cn.com.cqucc.forum.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserTest {

    @Autowired
    private UserService userService;

    @Test
    public void testSelectById() {
        System.out.println(userService.selectById(1));
    }

    @Test
    public void testSelectByUserName() {
        System.out.println(userService.selectByUserName("liubei"));
    }

    @Test
    public void testSelectByEmail() {
        System.out.println(userService.selectByEmail("nowcoder21@sina.com"));
    }


    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUserName("nowcoder26");
        user.setPassword("66666");
        user.setSalt("4519");
        user.setType(1);
        user.setStatus(1);
        user.setHeaderUrl("https://www.baidu.coom");
        user.setEmail("2386258658@qq.com");
        userService.insertUser(user);
    }


    @Test
    public void  testUpdateStatus() {
        int rows = userService.updateUserStatus(150, 2);
        System.out.println(rows);
    }

    @Test
    public void testUpdatePassword() {
        int rows = userService.updateUserPassword(150, "7777");
        System.out.println(rows);
    }

    @Test
    public void testUpdateHeaderUrl() {
        int rows = userService.updateUserHeader(150, "http://www.sina.com");
        System.out.println(rows);
    }

}
