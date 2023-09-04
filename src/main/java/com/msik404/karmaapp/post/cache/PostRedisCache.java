package com.msik404.karmaapp.post.cache;

import java.time.Duration;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msik404.karmaapp.post.dto.PostDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.DefaultStringTuple;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostRedisCache {

    private static final String KARMA_SCORE_ZSET_KEY = "karma-score-zset";
    private static final String POST_HASH_KEY = "posts-hash";
    private static final String POST_PREFIX = "post";

    private static final Duration TIMEOUT = Duration.ofSeconds(3600);

    private static String getPostKey(long postId) {
        return String.format("%s:%d", POST_PREFIX, postId);
    }

    private static String getPostImageKey(long postId) {
        return getPostKey(postId) + ":image";
    }

    private final ObjectMapper objectMapper;

    private final StringRedisTemplate redisTemplate;

    private final RedisTemplate<String, byte[]> byteRedisTemplate;

    /**
     * Method caches posts in redis. It uses ZSet with key: KARMA_SCORE_ZSET_KEY for keeping the order of post
     * for data retrieval. Posts as stored in Hash with key: POST_HASH_KEY in a form of string key, value pairs,
     * values are serialized as json strings. Values are first computed to a format compatible with redis pipeline
     * API, and then pipelined for maximum performance.
     *
     * @param posts Collection of posts which should be placed in a cache.
     */
    public void reinitializeCache(@NonNull Collection<PostDto> posts) {

        Set<StringRedisConnection.StringTuple> tuplesToAdd = new HashSet<>(posts.size());
        for (PostDto post : posts) {
            var tuple = new DefaultStringTuple(getPostKey(post.getId()), post.getKarmaScore().doubleValue());
            tuplesToAdd.add(tuple);
        }

        Map<String, String> valuesMap = new HashMap<>(posts.size());
        for (PostDto post : posts) {
            valuesMap.put(getPostKey(post.getId()), serialize(post));
        }

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {

            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;

            stringRedisConn.del(KARMA_SCORE_ZSET_KEY);
            stringRedisConn.del(POST_HASH_KEY);

            stringRedisConn.zAdd(KARMA_SCORE_ZSET_KEY, tuplesToAdd);
            stringRedisConn.expire(KARMA_SCORE_ZSET_KEY, TIMEOUT.getSeconds());

            stringRedisConn.hMSet(POST_HASH_KEY, valuesMap);
            stringRedisConn.expire(POST_HASH_KEY, TIMEOUT.getSeconds());

            return null;
        });
    }

    /**
     * @return true if both zSet with post scores and hash with post contents are present in cache else false
     */
    public boolean isEmpty() {

        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;

            stringRedisConn.exists(KARMA_SCORE_ZSET_KEY);
            stringRedisConn.exists(POST_HASH_KEY);

            return null;
        });

        return results.size() == 2 && !(Boolean) results.get(0) && !(Boolean) results.get(1);
    }

    public void cacheImage(long postId, byte[] imageData) {
        byteRedisTemplate.opsForValue().setIfAbsent(getPostImageKey(postId), imageData, TIMEOUT);
    }

    public Optional<byte[]> getCachedImage(long postId) {
        return Optional.ofNullable(byteRedisTemplate.opsForValue().getAndExpire(getPostImageKey(postId), TIMEOUT));
    }

    private List<PostDto> findCachedByZSet(
            @NonNull Collection<ZSetOperations.TypedTuple<String>> postIdKeySetWithScores) {

        int size = postIdKeySetWithScores.size();

        List<String> postIdKeyList = new ArrayList<>(size);
        List<Double> postScoreList = new ArrayList<>(size);
        for (ZSetOperations.TypedTuple<String> tuple : postIdKeySetWithScores) {
            postIdKeyList.add(Objects.requireNonNull(tuple.getValue()));
            postScoreList.add(tuple.getScore());
        }

        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        List<String> serializedResults = hashOps.multiGet(POST_HASH_KEY, postIdKeyList);

        List<PostDto> results = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            PostDto postDto = deserialize(serializedResults.get(i));
            postDto.setKarmaScore(postScoreList.get(i).longValue());
            results.add(postDto);
        }

        return results;
    }

    public List<PostDto> findTopNCached(int size) {

        Set<ZSetOperations.TypedTuple<String>> postIdKeySetWithScores = redisTemplate.opsForZSet()
                .reverseRangeWithScores(KARMA_SCORE_ZSET_KEY, 0, size-1);

        if (postIdKeySetWithScores == null || postIdKeySetWithScores.size() != size) {
            return new ArrayList<>();
        }

        return findCachedByZSet(postIdKeySetWithScores);
    }

    public List<PostDto> findNextNCached(int size, long karmaScore) {

        // offset is one because we have to skip first element with karmaScore, otherwise we will have duplicates
        // in pagination
        Set<ZSetOperations.TypedTuple<String>> postIdKeySetWithScores = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(
                        KARMA_SCORE_ZSET_KEY, Double.NEGATIVE_INFINITY, karmaScore, 1, size);

        if (postIdKeySetWithScores == null || postIdKeySetWithScores.size() != size) {
            return new ArrayList<>();
        }

        return findCachedByZSet(postIdKeySetWithScores);
    }

    public OptionalDouble updateKarmaScoreIfPresent(long postId, double delta) {

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        final String postIdKey = getPostKey(postId);

        if (zSetOps.score(KARMA_SCORE_ZSET_KEY, postIdKey) == null) {
            return  OptionalDouble.empty();
        }

        Double newScore = zSetOps.incrementScore(KARMA_SCORE_ZSET_KEY, postIdKey, delta);

        if (newScore == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(newScore);
    }

    public boolean deletePostFromCache(long postId) {

        final String postIdKey = getPostKey(postId);

        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;

            stringRedisConn.zRem(KARMA_SCORE_ZSET_KEY, postIdKey);
            stringRedisConn.hDel(POST_HASH_KEY, postIdKey);
            stringRedisConn.del(getPostImageKey(postId));

            return null;
        });

        return results.size() == 3 && (Long) results.get(0) == 1 && (Long) results.get(1) == 1;
    }

    private String serialize(@NonNull PostDto post) {

        try {
            return objectMapper.writeValueAsString(post);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing PostJoinedDto to JSON", e);
        }
    }

    private PostDto deserialize(@NonNull String json) {

        try {
            return objectMapper.readValue(json, PostDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing JSON to PostJoinedDto", e);
        }
    }

}
