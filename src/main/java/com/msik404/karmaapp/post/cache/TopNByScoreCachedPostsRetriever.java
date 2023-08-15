package com.msik404.karmaapp.post.cache;

import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;

public class TopNByScoreCachedPostsRetriever extends BaseCachedPostsRetriever {

    private final String karmaScoreZSetKey;
    private final Long karmaScore;

    protected TopNByScoreCachedPostsRetriever(
            String postsHashKey,
            ObjectMapper objectMapper,
            StringRedisTemplate redisTemplate,
            String karmaScoreZSetKey,
            Long karmaScore) {

        super(postsHashKey, objectMapper, redisTemplate);
        this.karmaScoreZSetKey = karmaScoreZSetKey;
        this.karmaScore = karmaScore;
    }

    @Override
    Set<String> getPostIdKeySet(long size) {
        return super.redisTemplate.opsForZSet().rangeByScore(
                karmaScoreZSetKey, Double.NEGATIVE_INFINITY, karmaScore, 0, size - 1);
    }
}
