package com.msik404.karmaapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfiguration {

    @Bean
    StringRedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        var template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    RedisTemplate<String, byte[]> byteRedisTemplate(RedisConnectionFactory redisConnectionFactory) {

        var template = new RedisTemplate<String, byte[]>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

}
