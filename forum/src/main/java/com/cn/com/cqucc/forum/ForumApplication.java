package com.cn.com.cqucc.forum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class ForumApplication {
    /**
     * @PostConstruct 被该注解修饰的方法在构造器执行完成之后就会执行该方法
     */
    @PostConstruct
    public void init() {
        // 解决netty启动冲突的问题
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args) {
        SpringApplication.run(ForumApplication.class, args);
    }

}
