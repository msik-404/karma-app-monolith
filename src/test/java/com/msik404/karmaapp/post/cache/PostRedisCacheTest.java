package com.msik404.karmaapp.post.cache;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msik404.karmaapp.RedisConfiguration;
import com.msik404.karmaapp.TestingDataCreator;
import com.msik404.karmaapp.post.Visibility;
import com.msik404.karmaapp.post.dto.PostDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.lang.NonNull;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(classes = {ObjectMapper.class, RedisConfiguration.class, PostRedisCache.class})
class PostRedisCacheTest {

    private final RedisConnectionFactory redisConnectionFactory;

    private final PostRedisCache redisCache;

    private static final List<PostDto> TEST_CACHED_POSTS = getPostsForTesting();

    @Container
    public static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer(DockerImageName.parse("redis:alpine")).withExposedPorts(6379);

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

    private static String getPostKey(long postId) {
        return String.format("post:%d", postId);
    }

    private static PostDto getPostDtoForTesting(long userId, long postId, long karmaScore) {

        final String sampleTextData = getPostKey(postId);

        return PostDto.builder()
                .id(postId)
                .userId(userId)
                .username(TestingDataCreator.getTestingUsername(userId))
                .headline(sampleTextData)
                .text(sampleTextData)
                .karmaScore(karmaScore)
                .visibility(Visibility.ACTIVE)
                .build();
    }

    /**
     * This comparator is made so to mimic redis ordered set reversed range retrieval order.
     */
    static class CachedPostComparator implements Comparator<PostDto> {

        @Override
        public int compare(@NonNull PostDto postOne, @NonNull PostDto postTwo) {

            if (postOne.getKarmaScore().equals(postTwo.getKarmaScore())) {
                final String postKeyOne = getPostKey(postOne.getId());
                final String postKeyTwo = getPostKey(postTwo.getId());
                return -postKeyOne.compareTo(postKeyTwo);
            }
            return -postOne.getKarmaScore().compareTo(postTwo.getKarmaScore());
        }

    }

    private static List<PostDto> getPostsForTesting() {

        final int postsAmount = 9;
        List<PostDto> posts = new ArrayList<>(postsAmount);

        final long userOneId = 1;
        posts.add(getPostDtoForTesting(userOneId, 1, 4));
        posts.add(getPostDtoForTesting(userOneId, 2, -1));
        posts.add(getPostDtoForTesting(userOneId, 3, 5));
        posts.add(getPostDtoForTesting(userOneId, 4, 5));
        posts.add(getPostDtoForTesting(userOneId, 5, 6));

        final long userTwoId = 2;
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
        final List<PostDto> posts = TEST_CACHED_POSTS.subList(0, 2);

        // when
        redisCache.reinitializeCache(posts);

        // then
        final List<PostDto> cachedPosts = redisCache.findTopNCached(posts.size());

        assertEquals(posts.size(), cachedPosts.size());

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
        final int size = TEST_CACHED_POSTS.size();

        // when
        final List<PostDto> cachedPosts = redisCache.findTopNCached(size);

        // then
        assertEquals(TEST_CACHED_POSTS.size(), cachedPosts.size());

        for (int i = 0; i < cachedPosts.size(); i++) {
            assertEquals(TEST_CACHED_POSTS.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void findNextNCached_NextSizeIsTwoAndTopSizeIsTwo_TwoAfterTopTwoFound() {

        // given
        final int nextSize = 2;
        final int topSize = 2;

        final long lastPostScore = TEST_CACHED_POSTS.get(topSize-1).getKarmaScore();

        // when
        final List<PostDto> nextCachedPosts = redisCache.findNextNCached(nextSize, lastPostScore);

        // then
        assertEquals(nextSize, nextCachedPosts.size());

        final int endBound = Math.min(topSize + nextSize, TEST_CACHED_POSTS.size());
        List<PostDto> groundTruthNextPosts = TEST_CACHED_POSTS.subList(topSize, endBound);

        for (int i = 0; i < nextCachedPosts.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i), nextCachedPosts.get(i));
        }
    }

    @Test
    void cacheImage_PostIdIsTopAndDataIsTextAsBytes_GetCachedImage() {

        // given
        final PostDto post = TEST_CACHED_POSTS.get(0);
        final byte[] dummyImageData = REDIS_CONTAINER.getDockerImageName().getBytes();

        // when
        redisCache.cacheImage(post.getId(), dummyImageData);

        final Optional<byte[]> cachedImageData = redisCache.getCachedImage(post.getId());

        // then
        assertTrue(cachedImageData.isPresent());
        assertArrayEquals(dummyImageData, cachedImageData.get());
    }

    @Test
    void getCachedImage_PostIdIsTopAndDataIsNonExisting_EmptyOptional() {

        // given
        final PostDto post = TEST_CACHED_POSTS.get(0);

        // when
        final Optional<byte[]> cachedImageData = redisCache.getCachedImage(post.getId());

        // then
        assertFalse(cachedImageData.isPresent());
    }

    @Test
    void updateKarmaScoreIfPresent_PostIdIsTopAndDeltaIsMinusThree_ScoreIsIncreasedAndNewOrderIsInPlace() {

        // given
        final PostDto post = TEST_CACHED_POSTS.get(0);

        final double delta = -3;

        // when
        OptionalDouble newScore = redisCache.updateKarmaScoreIfPresent(post.getId(), delta);

        // then
        assertTrue(newScore.isPresent());

        assertEquals(post.getKarmaScore() + delta, newScore.getAsDouble());

        final List<PostDto> cachedPosts = redisCache.findTopNCached(TEST_CACHED_POSTS.size());

        assertEquals(TEST_CACHED_POSTS.size(), cachedPosts.size());

        final PostDto updatedPost = PostDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .username(post.getUsername())
                .headline(post.getHeadline())
                .text(post.getText())
                .karmaScore((long) (post.getKarmaScore() + delta))
                .visibility(post.getVisibility())
                .build();

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
        final int nonExistentUserId = 404;

        final double delta = 1;

        // when
        OptionalDouble newScore = redisCache.updateKarmaScoreIfPresent(nonExistentUserId, delta);

        // then
        assertTrue(newScore.isEmpty());
    }

    @Test
    void deletePostFromCache_PostIdIsTop_PostGotDeletedAndNewOrderIsInPlace() {

        // given
        final PostDto post = TEST_CACHED_POSTS.get(0);

        // when
        boolean wasSuccessful = redisCache.deletePostFromCache(post.getId());

        // then
        assertTrue(wasSuccessful);

        final int newSize = TEST_CACHED_POSTS.size()-1;

        final List<PostDto> cachedPosts = redisCache.findTopNCached(newSize);

        assertEquals(newSize, cachedPosts.size());

        final List<PostDto> groundTruthTopPosts = TEST_CACHED_POSTS.subList(1, TEST_CACHED_POSTS.size());

        for (int i = 0; i < cachedPosts.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i), cachedPosts.get(i));
        }
    }
}