package com.cn.com.cqucc.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // 设置普通 key 的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // 设置普通 key 的序列化方式 返回的值比较复杂 返回json
        template.setValueSerializer(RedisSerializer.json());
        // 设置hash key 的 序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.json());
        template.afterPropertiesSet(); // 启动配置
        return template;
    }
}
