package com.msik404.karmaappmonolith.post.cache;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msik404.karmaappmonolith.RedisConfiguration;
import com.msik404.karmaappmonolith.TestingDataCreator;
import com.msik404.karmaappmonolith.TestingImageDataCreator;
import com.msik404.karmaappmonolith.position.ScrollPosition;
import com.msik404.karmaappmonolith.post.Visibility;
import com.msik404.karmaappmonolith.post.dto.PostDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(classes = {ObjectMapper.class, RedisConfiguration.class, PostRedisCache.class})
class PostRedisCacheTest {

    private final RedisConnectionFactory redisConnectionFactory;

    private final PostRedisCache redisCache;

    private static final List<PostDto> TEST_CACHED_POSTS = getPostsForTesting();

    public static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer(DockerImageName.parse("redis:alpine")).withExposedPorts(6379);

    static {
        REDIS_CONTAINER.start();
    }

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

    @Autowired
    PostRedisCacheTest(RedisConnectionFactory redisConnectionFactory, PostRedisCache redisCache) {

        this.redisConnectionFactory = redisConnectionFactory;
        this.redisCache = redisCache;
    }

    @Nullable
    private static String getPostKey(long postId) {
        return String.format("post:%d", postId);
    }

    @NonNull
    private static PostDto getPostDtoForTesting(long userId, long postId, long karmaScore) {

        final String sampleTextData = getPostKey(postId);

        return new PostDto(
                postId,
                userId,
                TestingDataCreator.getTestingUsername(userId),
                sampleTextData,
                sampleTextData,
                karmaScore,
                Visibility.ACTIVE
        );
    }

    /**
     * This comparator is made so to mimic redis ordered set reversed range retrieval order.
     */
    static class CachedPostComparator implements Comparator<PostDto> {

        @Override
        public int compare(@NonNull PostDto postOne, @NonNull PostDto postTwo) {

            if (postOne.getKarmaScore().equals(postTwo.getKarmaScore())) {
                String postKeyOne = getPostKey(postOne.getId());
                String postKeyTwo = getPostKey(postTwo.getId());
                return -postKeyOne.compareTo(postKeyTwo);
            }
            return -postOne.getKarmaScore().compareTo(postTwo.getKarmaScore());
        }

    }

    @NonNull
    private static List<PostDto> getPostsForTesting() {

        int postsAmount = 9;
        List<PostDto> posts = new ArrayList<>(postsAmount);

        long userOneId = 1;
        posts.add(getPostDtoForTesting(userOneId, 1, 4));
        posts.add(getPostDtoForTesting(userOneId, 2, -1));
        posts.add(getPostDtoForTesting(userOneId, 3, 5));
        posts.add(getPostDtoForTesting(userOneId, 4, 5));
        posts.add(getPostDtoForTesting(userOneId, 5, 6));

        long userTwoId = 2;
        posts.add(getPostDtoForTesting(userTwoId, 6, 3));
        posts.add(getPostDtoForTesting(userTwoId, 7, 2));
        posts.add(getPostDtoForTesting(userTwoId, 8, 4));
        posts.add(getPostDtoForTesting(userTwoId, 9, 0));

        posts.sort(new CachedPostComparator());

        return posts;
    }

    @BeforeEach
    void setUp() {
        redisCache.reinitializeCache(TEST_CACHED_POSTS);
    }

    @AfterEach
    void tearDown() {
        redisConnectionFactory.getConnection().serverCommands().flushAll();
    }

    @Test
    void reinitializeCache_TwoPosts_CacheHasOnlyTheseTwoPosts() {

        // given
        int postsInCacheAmount = 2;
        List<PostDto> posts = TEST_CACHED_POSTS.subList(0, postsInCacheAmount);

        // when
        redisCache.reinitializeCache(posts);

        // then
        Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(posts.size());

        assertTrue(optionalCachedPosts.isPresent());

        List<PostDto> cachedPosts = optionalCachedPosts.get();

        assertEquals(postsInCacheAmount, cachedPosts.size());

        assertEquals(2, cachedPosts.size());

        for (int i = 0; i < posts.size(); i++) {
            assertEquals(posts.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void isEmpty_CacheIsNotEmpty_False() {
        assertFalse(redisCache.isEmpty());
    }

    @Test
    void isEmpty_CacheIsEmpty_True() {

        // given
        redisConnectionFactory.getConnection().serverCommands().flushAll();

        // when
        boolean isCacheEmpty = redisCache.isEmpty();

        // then
        assertTrue(isCacheEmpty);
    }

    @Test
    void findTopNCached_AllCachedPosts_AllCachedPostsFound() {

        // given
        int size = TEST_CACHED_POSTS.size();

        // when
        Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(size);

        // then
        assertTrue(optionalCachedPosts.isPresent());

        List<PostDto> cachedPosts = optionalCachedPosts.get();

        assertEquals(size, cachedPosts.size());

        for (int i = 0; i < cachedPosts.size(); i++) {
            assertEquals(TEST_CACHED_POSTS.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void findNextNCached_NextSizeIsTwoAndTopSizeIsTwo_TwoAfterTopTwoFound() {

        // given
        int nextSize = 2;
        int topSize = 2;

        PostDto lastPost = TEST_CACHED_POSTS.get(topSize - 1);
        ScrollPosition position = new ScrollPosition(lastPost.getId(), lastPost.getKarmaScore());

        // when
        Optional<List<PostDto>> optionalNextCachedPosts = redisCache.findNextNCached(nextSize, position);

        // then
        assertTrue(optionalNextCachedPosts.isPresent());

        List<PostDto> nextCachedPosts = optionalNextCachedPosts.get();

        assertEquals(nextSize, nextCachedPosts.size());

        int endBound = Math.min(topSize + nextSize, TEST_CACHED_POSTS.size());
        List<PostDto> groundTruthNextPosts = TEST_CACHED_POSTS.subList(topSize, endBound);

        for (int i = 0; i < nextCachedPosts.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i), nextCachedPosts.get(i));
        }
    }

    @Test
    void cacheImage_PostIdIsTopAndDataIsTextAsBytes_GetCachedImage() {

        // given
        PostDto post = TEST_CACHED_POSTS.get(0);
        byte[] dummyImageData = TestingImageDataCreator.getTestingImage();

        // when
        assertTrue(redisCache.cacheImage(post.getId(), dummyImageData));

        Optional<byte[]> cachedImageData = redisCache.getCachedImage(post.getId());

        // then
        assertTrue(cachedImageData.isPresent());
        assertArrayEquals(dummyImageData, cachedImageData.get());
    }

    @Test
    void getCachedImage_PostIdIsTopAndDataIsNonExisting_EmptyOptional() {

        // given
        PostDto post = TEST_CACHED_POSTS.get(0);

        // when
        Optional<byte[]> cachedImageData = redisCache.getCachedImage(post.getId());

        // then
        assertFalse(cachedImageData.isPresent());
    }

    @Test
    void updateKarmaScoreIfPresent_PostIdIsTopAndDeltaIsMinusThree_ScoreIsIncreasedAndNewOrderIsInPlace() {

        // given
        PostDto post = TEST_CACHED_POSTS.get(0);

        double delta = -3;

        // when
        OptionalDouble newScore = redisCache.updateKarmaScoreIfPresent(post.getId(), delta);

        // then
        assertTrue(newScore.isPresent());

        assertEquals(post.getKarmaScore() + delta, newScore.getAsDouble());

        Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(TEST_CACHED_POSTS.size());

        assertTrue(optionalCachedPosts.isPresent());

        List<PostDto> cachedPosts = optionalCachedPosts.get();

        assertEquals(TEST_CACHED_POSTS.size(), cachedPosts.size());

        var updatedPost = new PostDto(
                post.getId(),
                post.getUserId(),
                post.getUsername(),
                post.getHeadline(),
                post.getText(),
                post.getKarmaScore() + (long) delta,
                post.getVisibility()
        );

        // shallow copy
        List<PostDto> updatedGroundTruthPosts = new ArrayList<>(TEST_CACHED_POSTS.stream().toList());
        // make the first element reference to updatedPost
        updatedGroundTruthPosts.set(0, updatedPost);
        // sort references
        updatedGroundTruthPosts.sort(new CachedPostComparator());

        for (int i = 0; i < TEST_CACHED_POSTS.size(); i++) {
            assertEquals(updatedGroundTruthPosts.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void updateKarmaScoreIfPresent_PostIdIsNonExistingAndDeltaIsOne_EmptyOptional() {

        // given
        int nonExistentUserId = 404;

        double delta = 1;

        // when
        OptionalDouble newScore = redisCache.updateKarmaScoreIfPresent(nonExistentUserId, delta);

        // then
        assertTrue(newScore.isEmpty());
    }

    @Test
    void deletePostFromCache_PostIdIsTop_PostGotDeletedAndNewOrderIsInPlace() {

        // given
        PostDto post = TEST_CACHED_POSTS.get(0);

        // when
        boolean wasSuccessful = redisCache.deletePostFromCache(post.getId());

        // then
        assertTrue(wasSuccessful);

        int newSize = TEST_CACHED_POSTS.size() - 1;

        Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(newSize);

        assertTrue(optionalCachedPosts.isPresent());

        List<PostDto> cachedPosts = optionalCachedPosts.get();

        assertEquals(newSize, cachedPosts.size());

        List<PostDto> groundTruthTopPosts = TEST_CACHED_POSTS.subList(1, TEST_CACHED_POSTS.size());

        for (int i = 0; i < cachedPosts.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void isKarmaScoreGreaterThanLowestScoreInZSet_KarmaScoreIsGreater_True() {

        // given
        long karmaScore = 5;

        // when
        boolean result = redisCache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);

        // then
        assertTrue(result);
    }

    @Test
    void isKarmaScoreGreaterThanLowestScoreInZSet_KarmaScoreIsNotGreater_False() {

        // given
        long karmaScore = -100;

        // when
        boolean result = redisCache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);

        // then
        assertFalse(result);
    }

    @Test
    void isKarmaScoreGreaterThanLowestScoreInZSet_CacheIsEmpty_True() {

        // given
        redisConnectionFactory.getConnection().serverCommands().flushAll();
        long karmaScore = 404;

        // when
        boolean result = redisCache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);

        // then
        assertTrue(result);
    }

    @Test
    void insertPost_ImageDataIsNonNullAndThisPostIsNotPresentInCacheNorIsImage_ZSetAndHashShouldBeUpdatedAndImageDataShouldBeCached() {

        // given
        long userId = 404;
        long postId = userId;
        long karmaScore = 5;

        PostDto postToBeInserted = getPostDtoForTesting(userId, postId, karmaScore);

        byte[] imageData = TestingImageDataCreator.getTestingImage();

        List<PostDto> groundTruthPosts = getPostsForTesting();
        groundTruthPosts.add(postToBeInserted);
        groundTruthPosts.sort(new CachedPostComparator());

        // when
        assertTrue(redisCache.insertPost(postToBeInserted, imageData));

        // then

        // ZSet and hash are updated
        Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(groundTruthPosts.size());
        assertTrue(optionalCachedPosts.isPresent());
        List<PostDto> cachedPosts = optionalCachedPosts.get();
        assertEquals(groundTruthPosts.size(), cachedPosts.size());

        for (int i = 0; i < groundTruthPosts.size(); i++) {
            assertEquals(groundTruthPosts.get(i), cachedPosts.get(i));
        }

        // image is present in cache
        Optional<byte[]> optionalCachedImageData = redisCache.getCachedImage(postId);
        assertTrue(optionalCachedImageData.isPresent());
        byte[] cachedImageData = optionalCachedImageData.get();
        assertArrayEquals(imageData, cachedImageData);
    }

    @Test
    void insertPost_ImageDataIsNullAndThisPostIsNotPresentInCacheNorIsImage_ZSetAndHashShouldBeUpdatedAndImageDataShouldNotBeCached() {

        // given
        long userId = 404;
        long postId = userId;
        long karmaScore = 5;

        PostDto postToBeInserted = getPostDtoForTesting(userId, postId, karmaScore);

        byte[] imageData = null;

        List<PostDto> groundTruthPosts = getPostsForTesting();
        groundTruthPosts.add(postToBeInserted);
        groundTruthPosts.sort(new CachedPostComparator());

        // when
        assertTrue(redisCache.insertPost(postToBeInserted, imageData));

        // then

        // ZSet and hash are updated
        Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(groundTruthPosts.size());
        assertTrue(optionalCachedPosts.isPresent());
        List<PostDto> cachedPosts = optionalCachedPosts.get();
        assertEquals(groundTruthPosts.size(), cachedPosts.size());

        for (int i = 0; i < groundTruthPosts.size(); i++) {
            assertEquals(groundTruthPosts.get(i), cachedPosts.get(i));
        }

        // image is not present in cache
        final Optional<byte[]> optionalCachedImageData = redisCache.getCachedImage(postId);
        assertFalse(optionalCachedImageData.isPresent());
    }
}