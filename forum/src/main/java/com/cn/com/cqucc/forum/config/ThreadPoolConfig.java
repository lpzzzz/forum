package com.cn.com.cqucc.forum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring 线程池配置类 需要配置上改类之后执行才不会报错
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
