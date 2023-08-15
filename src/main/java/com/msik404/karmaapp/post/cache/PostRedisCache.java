package com.msik404.karmaapp.post.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msik404.karmaapp.post.dto.PostJoinedDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostRedisCache {

    private static final String KARMA_SCORE_ZSET_KEY = "karma-score-zset";
    private static final String POSTS_HASH_KEY = "karma-score-hash";

    private static String getPostsHashKey(long postId) {
        return String.format("%s:%d", POSTS_HASH_KEY, postId);
    }

    private static String getPostsHashKey(@NonNull String postId) {
        return String.format("%s:%s", POSTS_HASH_KEY, postId);
    }

    public static String getPostsHashKey(@NonNull String postsHashKey, @NonNull String postId) {
        return String.format("%s:%s", postsHashKey, postId);
    }

    private final ObjectMapper objectMapper;

    private final StringRedisTemplate redisTemplate;

    private List<Long> clearKarmaScoreZSet(@NonNull ZSetOperations<String, String> zSetOps) {

        Long size = zSetOps.zCard(KARMA_SCORE_ZSET_KEY);
        List<String> removedPostIdKeys = new ArrayList<>();

        if (size != null && size > 0) {
            Set<String> keysToRemove = zSetOps.range(KARMA_SCORE_ZSET_KEY, 0, size - 1);
            if (keysToRemove != null && !keysToRemove.isEmpty()) {
                zSetOps.remove(KARMA_SCORE_ZSET_KEY, keysToRemove.toArray(new Object[0]));
                removedPostIdKeys.addAll(keysToRemove);
            }
        }
        return removedPostIdKeys.stream().map(Long::valueOf).collect(Collectors.toList());
    }

    private List<Long> initKarmaScoreZSet(@NonNull List<PostJoinedDto> posts) {

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        List<Long> removedPostIdKeys = clearKarmaScoreZSet(zSetOps);
        for (PostJoinedDto post : posts) {
            zSetOps.add(KARMA_SCORE_ZSET_KEY, post.getId().toString(), post.getKarmaScore());
        }
        return removedPostIdKeys;
    }

    private void clearPostHashes(@NonNull HashOperations<String, String, String> hashOps, @NonNull List<Long> postIds) {

        for (Long postId : postIds) {
            hashOps.delete(getPostsHashKey(postId), postId.toString());
        }
    }

    private void initPostsAsHashes(@NonNull List<PostJoinedDto> posts, @NonNull List<Long> removedPostIds) {

        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        clearPostHashes(hashOps, removedPostIds);
        for (PostJoinedDto post : posts) {
            Long postId = post.getId();
            hashOps.put(getPostsHashKey(postId), postId.toString(), serialize(post));
        }
    }

    public void initCache(@NonNull List<PostJoinedDto> posts) {

        List<Long> removedPostIdKeys = initKarmaScoreZSet(posts);
        initPostsAsHashes(posts, removedPostIdKeys);
    }

    public Optional<List<PostJoinedDto>> getTopNCachedPosts(long size) {

        var postsRetriever = new TopNCachedPostsRetriever(
                POSTS_HASH_KEY, objectMapper, redisTemplate, KARMA_SCORE_ZSET_KEY);
        return postsRetriever.getPostsFromCache(size);
    }

    public Optional<List<PostJoinedDto>> getTopNByScoreCachedPosts(long karmaScore, long size) {

        var postsRetriever = new TopNByScoreCachedPostsRetriever(
                POSTS_HASH_KEY, objectMapper, redisTemplate, KARMA_SCORE_ZSET_KEY, karmaScore);
        return postsRetriever.getPostsFromCache(size);
    }

    public boolean zSetContains(long postId) {
        return redisTemplate.opsForZSet().score(KARMA_SCORE_ZSET_KEY, postId) != null;
    }

    public void updateKarmaScore(long postId, boolean isPositive) {

        var zSetOps = redisTemplate.opsForZSet();
        if (zSetContains(postId)) {
            zSetOps.incrementScore(KARMA_SCORE_ZSET_KEY, String.valueOf(postId), isPositive ? 1 : -1);
        }
    }

    public boolean shouldKarmaScoreBeInZSet(long score) {

        var zSetOps = redisTemplate.opsForZSet();
        Set<String> lowestScore = zSetOps.range(KARMA_SCORE_ZSET_KEY, 0, 0);
        if (lowestScore == null || lowestScore.isEmpty()) {
            return true;
        }
        var minScore = Double.parseDouble(lowestScore.iterator().next());
        return minScore < score;
    }

    private String serialize(@NonNull PostJoinedDto post) {

        try {
            return objectMapper.writeValueAsString(post);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing PostJoinedDto to JSON", e);
        }
    }

}
