package com.msik404.karmaapp.post.cache;

import java.time.Duration;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msik404.karmaapp.post.dto.PostJoined;
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
    private static final String POST_HASH_PREFIX = "karma-score-post-hash";

    private static final Duration TIMEOUT = Duration.ofSeconds(3600);

    private static String getPostsHashKey(long postId) {
        return String.format("%s:%d", POST_HASH_PREFIX, postId);
    }

    private static String getPostsHashKey(@NonNull String postId) {
        return String.format("%s:%s", POST_HASH_PREFIX, postId);
    }

    private final ObjectMapper objectMapper;

    private final StringRedisTemplate redisTemplate;

    private void clearKarmaScoreZSet() {
        redisTemplate.delete(KARMA_SCORE_ZSET_KEY);
    }

    private void populateKarmaScoreZSet(@NonNull Iterable<PostJoined> posts) {

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        for (PostJoined post : posts) {
            zSetOps.add(KARMA_SCORE_ZSET_KEY, post.getId().toString(), post.getKarmaScore());
        }
        redisTemplate.expire(KARMA_SCORE_ZSET_KEY, TIMEOUT);
    }

    public Boolean zSetIsEmpty() {
        return redisTemplate.hasKey(KARMA_SCORE_ZSET_KEY);
    }

    private Set<String> getAllPostHashKeys() {
        return redisTemplate.keys(POST_HASH_PREFIX + "*");
    }

    private void clearPostHashes(@NonNull Iterable<String> postHashKeys) {

        for (String postHashKey : postHashKeys) {
            redisTemplate.delete(postHashKey);
        }
    }

    private void populateCacheWithHashes(@NonNull Iterable<PostJoined> posts) {

        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        for (PostJoined post : posts) {
            Long postId = post.getId();
            String postHashKey = getPostsHashKey(postId);
            hashOps.put(postHashKey, postId.toString(), serialize(post));
            redisTemplate.expire(postHashKey, TIMEOUT);
        }
    }

    private Optional<List<PostJoined>> findCachedByZSet(
            Collection<ZSetOperations.TypedTuple<String>> postIdKeySetWithScores) {

        int size = postIdKeySetWithScores.size();

        List<String> postIdKeyList = new ArrayList<>(size);
        List<Double> postScoreList = new ArrayList<>(size);
        for (ZSetOperations.TypedTuple<String> tuple : postIdKeySetWithScores) {
            postIdKeyList.add(getPostsHashKey(Objects.requireNonNull(tuple.getValue())));
            postScoreList.add(tuple.getScore());
        }

        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        List<String> serializedResults = hashOps.multiGet(POST_HASH_PREFIX, postIdKeyList);
        if (serializedResults.size() != size) {
            return Optional.empty();
        }

        List<PostJoined> results = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            PostJoined postJoinedDto = deserialize(serializedResults.get(i));
            postJoinedDto.setKarmaScore(postScoreList.get(i).longValue());
        }

        return Optional.of(results);
    }

    public Optional<List<PostJoined>> findTopNCached(int size) {

        Set<ZSetOperations.TypedTuple<String>> postIdKeySetWithScores = redisTemplate.opsForZSet()
                .reverseRangeWithScores(KARMA_SCORE_ZSET_KEY, 0, size - 1);

        if (postIdKeySetWithScores == null || postIdKeySetWithScores.size() != size) {
            return Optional.empty();
        }

        return findCachedByZSet(postIdKeySetWithScores);
    }

    public Optional<List<PostJoined>> findNextNCached(int size, long karmaScore) {

        Set<ZSetOperations.TypedTuple<String>> postIdKeySetWithScores = redisTemplate.opsForZSet()
                .rangeByScoreWithScores(KARMA_SCORE_ZSET_KEY, Double.NEGATIVE_INFINITY, karmaScore, 0, size - 1);

        if (postIdKeySetWithScores == null || postIdKeySetWithScores.size() != size) {
            return Optional.empty();
        }

        return findCachedByZSet(postIdKeySetWithScores);
    }

    public boolean zSetContains(long postId) {
        return redisTemplate.opsForZSet().score(KARMA_SCORE_ZSET_KEY, postId) != null;
    }

    public OptionalDouble updateKarmaScore(long postId, boolean isPositive) {

        Double newScore = redisTemplate.opsForZSet()
                .incrementScore(KARMA_SCORE_ZSET_KEY, String.valueOf(postId), isPositive ? 1 : -1);

        if (newScore == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(newScore);
    }

    public boolean shouldKarmaScoreBeInZSet(long score) {

        Set<String> lowestScore = redisTemplate.opsForZSet().range(KARMA_SCORE_ZSET_KEY, 0, 0);

        if (lowestScore == null || lowestScore.isEmpty()) {
            return true;
        }

        var minScore = Double.parseDouble(lowestScore.iterator().next());
        return minScore < score;
    }

    public Optional<Boolean> deleteKeyFromZSet(long postId) {

        Long removedKeysCount = redisTemplate.opsForZSet().remove(KARMA_SCORE_ZSET_KEY, postId);

        if (removedKeysCount == null) {
            return Optional.empty();
        }
        return Optional.of(removedKeysCount == 1L);
    }

    public boolean deleteHash(long postId) {
        return Boolean.TRUE.equals(redisTemplate.delete(getPostsHashKey(postId)));
    }

    private String serialize(@NonNull PostJoined post) {

        try {
            return objectMapper.writeValueAsString(post);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing PostJoinedDto to JSON", e);
        }
    }

    private PostJoined deserialize(@NonNull String json) {

        try {
            return objectMapper.readValue(json, PostJoined.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing JSON to PostJoinedDto", e);
        }
    }

}
