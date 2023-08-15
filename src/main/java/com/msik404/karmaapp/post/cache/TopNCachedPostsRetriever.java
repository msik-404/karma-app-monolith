package com.msik404.karmaapp.post.cache;

import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;

public class TopNCachedPostsRetriever extends BaseCachedPostsRetriever {

    private final String karmaScoreZSetKey;

    protected TopNCachedPostsRetriever(
            String postsHashKey,
            ObjectMapper objectMapper,
            StringRedisTemplate redisTemplate,
            String karmaScoreZSetKey) {

        super(postsHashKey, objectMapper, redisTemplate);
        this.karmaScoreZSetKey = karmaScoreZSetKey;
    }

    @Override
    Set<String> getPostIdKeySet(long size) {
        return super.redisTemplate.opsForZSet().reverseRange(karmaScoreZSetKey, 0, size - 1);
    }

}
