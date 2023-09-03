package com.msik404.karmaapp.post.cache;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msik404.karmaapp.RedisConfiguration;
import com.msik404.karmaapp.post.dto.PostDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(classes = {ObjectMapper.class, RedisConfiguration.class, PostRedisCache.class})
class PostRedisCacheTest {

    private final PostRedisCache redisCache;

    @Container
    public static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer(DockerImageName.parse("redis:alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

    @Autowired
    PostRedisCacheTest(PostRedisCache redisCache) {
        this.redisCache = redisCache;
    }

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void cacheShouldBeEmpty() {

        assertTrue(redisCache.isEmpty());
    }

    @Test
    void cacheShouldNotBeEmpty() {

        var newPost = PostDto.builder()
                .id(1L)
                .karmaScore(1L)
                .build();

        redisCache.reinitializeCache(List.of(newPost));

        assertFalse(redisCache.isEmpty());
    }
}