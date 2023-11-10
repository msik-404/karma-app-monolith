package com.msik404.karmaappmonolith.post.cache;

import java.time.Duration;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msik404.karmaappmonolith.position.ScrollPosition;
import com.msik404.karmaappmonolith.post.dto.PostDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.DefaultStringTuple;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostRedisCache {

    private static final String KARMA_SCORE_ZSET_KEY = "karma-score-zset";
    private static final String POST_HASH_KEY = "posts-hash";
    private static final String POST_PREFIX = "post";

    private static final Duration TIMEOUT = Duration.ofSeconds(3600);

    // Maximum amount of initial posts that can have same karmaScore.
    private static final int MAX_INITIAL_SCORE_DUPLICATES = 100;

    private static final int MAX_CACHED_POSTS = 10_000;

    public static int getMaxCachedPosts() {
        return MAX_CACHED_POSTS;
    }

    @NonNull
    private static String getPostKey(long postId) {
        return String.format("%s:%d", POST_PREFIX, postId);
    }

    @NonNull
    private static String getPostImageKey(long postId) {
        return getPostKey(postId) + ":image";
    }

    private final ObjectMapper objectMapper;

    private final StringRedisTemplate redisTemplate;

    /**
     * Method caches posts in redis. It uses ZSet with key: KARMA_SCORE_ZSET_KEY for keeping the order of post
     * for data retrieval. Posts as stored in Hash with key: POST_HASH_KEY in a form of string key, value pairs,
     * values are serialized as json strings. Values are first computed to a format compatible with redis pipeline
     * API, and then pipelined for maximum performance.
     *
     * @param posts Collection of posts which should be placed in a cache.
     */
    public void reinitializeCache(@NonNull Collection<PostDto> posts) {

        assert !posts.isEmpty();

        Set<StringRedisConnection.StringTuple> tuplesToAdd = new HashSet<>(posts.size());
        for (PostDto post : posts) {
            var tuple = new DefaultStringTuple(getPostKey(post.getId()), (double) post.getKarmaScore());
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

    public boolean cacheImage(long postId, @NonNull byte[] imageData) {

        Object results = redisTemplate.execute((RedisCallback<Object>) connection ->
                connection.stringCommands().set(
                        getPostImageKey(postId).getBytes(),
                        imageData,
                        Expiration.from(TIMEOUT),
                        RedisStringCommands.SetOption.ifAbsent())
        );

        return Boolean.TRUE.equals(results);
    }

    /**
     * @param postId Id of post whose image is requested.
     * @return Optional of byte array with image data, Optional empty if image is not found.
     */
    @NonNull
    public Optional<byte[]> getCachedImage(long postId) {

        Object results = redisTemplate.execute((RedisCallback<Object>) connection ->
                connection.stringCommands().getEx(
                        getPostImageKey(postId).getBytes(),
                        Expiration.from(TIMEOUT))
        );

        return Optional.ofNullable((byte[]) results);
    }

    @NonNull
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

    /**
     * @param size Requested amount of posts.
     * @return Optional of List of PostDto objects, this list has exactly size elements, Optional empty if not enough
     * posts are in cache. If two scores of posts are the same, the one with higher lexicographical order is first.
     */
    @NonNull
    public Optional<List<PostDto>> findTopNCached(int size) {

        // this set is LinkedHashSet so iteration order is preserved.
        Set<ZSetOperations.TypedTuple<String>> postIdKeySetWithScores = redisTemplate.opsForZSet()
                .reverseRangeWithScores(KARMA_SCORE_ZSET_KEY, 0, size - 1);

        // opsForZSet().reverseRangeWithScores() cannot be null because it only if used can in transaction|pipeline.
        if (postIdKeySetWithScores.size() != size) {
            return Optional.empty();
        }
        return Optional.of(findCachedByZSet(postIdKeySetWithScores));
    }

    /**
     * @param size     Requested amount of posts.
     * @param position ScrollPosition of lastly returned post.
     * @return Optional of List of PostDto objects, this list has exactly size elements, Optional empty if not enough
     * posts are in cache. If two scores of posts are the same, the one with higher lexicographical order is first.
     */
    @NonNull
    public Optional<List<PostDto>> findNextNCached(int size, @NonNull ScrollPosition position) {

        int count = size + MAX_INITIAL_SCORE_DUPLICATES;

        // this set is LinkedHashSet so iteration order is preserved.
        Set<ZSetOperations.TypedTuple<String>> postIdKeySetWithScores = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(
                        KARMA_SCORE_ZSET_KEY,
                        Double.NEGATIVE_INFINITY,
                        position.karmaScore(),
                        0,
                        count
                );

        // size + 1 is the most optimistic case. That is there is no initial duplicate and size tuples are left to
        // be collected. If size is less than size + 1, certainly all requested posts won't be able to be collected.
        if (postIdKeySetWithScores.size() < size + 1) {
            return Optional.empty();
        }

        // In this code we want to mimic key set pagination on (karmaScore, postId), that means that posts with higher
        // karmaScore are first and if two posts have the same karmaScore, post with higher lexicographical postId will
        // be first (postId are unique). Sadly reverseRangeByScoreWithScores does not guarantee that, because it fetches
        // count amount of tuples lower or equal to given input score. To solve this issue we have to specify count
        // variable high enough so that we can iterate over all initial tuples with score equal to given input score,
        // until we find tuple with required postId. After finding this tuple we should be able to collect the
        // remaining size amount of tuples. To achieve this goal, I define constant called MAX_INITIAL_SCORE_DUPLICATES.
        // If not enough tuples are collected, Optional empty is returned which means that cache could not be used.

        List<ZSetOperations.TypedTuple<String>> results = new ArrayList<>(size);
        String targetKey = getPostKey(position.postId());
        int collected = 0;
        int skipped = 0;
        boolean found = false;

        // opsForZSet().reverseRangeByScoreWithScores() cannot be null because it only if used can in transaction|pipeline.
        for (ZSetOperations.TypedTuple<String> tuple : postIdKeySetWithScores) {
            if (!found) {
                if (tuple.getValue().equals(targetKey)) {
                    found = true;
                }
                skipped++;
                if (count - skipped < size) { // this means that not enough posts left to be collected.
                    return Optional.empty();
                }
            } else {
                results.add(tuple);
                collected++;
                if (collected == size) {
                    break;
                }
            }
        }

        if (results.size() != size) {
            return Optional.empty();
        }
        return Optional.of(findCachedByZSet(results));
    }

    /**
     * @param postId Id of post whose score is being updated.
     * @param delta  score delta to be added to cached value.
     * @return Optional of new score if post was cached, Optional empty if post wasn't cached.
     */
    @NonNull
    public OptionalDouble updateKarmaScoreIfPresent(long postId, double delta) {

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        String postIdKey = getPostKey(postId);

        // If postIdKey does not exist return Optional empty.
        if (zSetOps.score(KARMA_SCORE_ZSET_KEY, postIdKey) == null) {
            return OptionalDouble.empty();
        }

        // opsForZset().incrementScore() cannot be null because it only can if used in transaction|pipeline.
        Double newScore = zSetOps.incrementScore(KARMA_SCORE_ZSET_KEY, postIdKey, delta);
        return OptionalDouble.of(newScore);
    }

    /**
     * @param postId Id of post which will be deleted.
     * @return true if post was deleted, false if not.
     */
    public boolean deletePostFromCache(long postId) {

        String postIdKey = getPostKey(postId);

        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;

            stringRedisConn.zRem(KARMA_SCORE_ZSET_KEY, postIdKey);
            stringRedisConn.hDel(POST_HASH_KEY, postIdKey);
            stringRedisConn.del(getPostImageKey(postId));

            return null;
        });

        return results.size() == 3 && (Long) results.get(0) == 1 && (Long) results.get(1) == 1;
    }

    /**
     * @return Amount of keys in ZSet which should be the same as amount of posts cached.
     */
    @NonNull
    public long getZSetSize() {

        // opsForZset().size() cannot be null because it only can if used in transaction|pipeline.
        return redisTemplate.opsForZSet().size(KARMA_SCORE_ZSET_KEY);
    }

    /**
     * @param karmaScore score of post which is being tested.
     * @return true if ZSet is empty or score is high enough, false if it's too small.
     */
    @NonNull
    public boolean isKarmaScoreGreaterThanLowestScoreInZSet(long karmaScore) {

        Set<ZSetOperations.TypedTuple<String>> lowestScorePostIdWithScore = redisTemplate.opsForZSet()
                .rangeWithScores(KARMA_SCORE_ZSET_KEY, 0, 0);

        // opsForZSet().rangeWithScores() cannot be null because it only can if used in transaction|pipeline.
        if (lowestScorePostIdWithScore.size() != 1) {
            return true; // when ZSet is empty
        }

        // lowestScore cannot be null because I check for size() being equal to one.
        Double lowestScore = lowestScorePostIdWithScore.iterator().next().getScore();
        return lowestScore < karmaScore;
    }

    /**
     * @param post      post contents to be cached.
     * @param imageData optional image data to be cached.
     * @return true if post was cached.
     */
    public boolean insertPost(@NonNull PostDto post, @Nullable byte[] imageData) {

        String serializedPost = serialize(post);

        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {

            byte[] postKeyBytes = getPostKey(post.getId()).getBytes();

            connection.zSetCommands().zAdd(
                    KARMA_SCORE_ZSET_KEY.getBytes(),
                    (double) post.getKarmaScore(),
                    postKeyBytes,
                    RedisZSetCommands.ZAddArgs.empty()
            );

            connection.hashCommands().hSet(
                    POST_HASH_KEY.getBytes(),
                    postKeyBytes,
                    serializedPost.getBytes()
            );

            if (imageData != null) {
                connection.stringCommands().set(
                        getPostImageKey(post.getId()).getBytes(),
                        imageData,
                        Expiration.from(TIMEOUT),
                        RedisStringCommands.SetOption.ifAbsent()
                );
            }

            return null;
        });

        if (imageData == null) {
            return results.size() == 2 && (Boolean) results.get(0) && (Boolean) results.get(1);
        }
        return results.size() == 3 && (Boolean) results.get(0) && (Boolean) results.get(1) && (Boolean) results.get(2);
    }

    @NonNull
    private String serialize(@NonNull PostDto post) {

        try {
            return objectMapper.writeValueAsString(post);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing PostJoinedDto to JSON", e);
        }
    }

    @NonNull
    private PostDto deserialize(@NonNull String json) {

        try {
            return objectMapper.readValue(json, PostDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing JSON to PostJoinedDto", e);
        }
    }

}
