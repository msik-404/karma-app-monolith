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
    void reinitializeCache() {

        final List<PostDto> posts = TEST_CACHED_POSTS.subList(0, 2);

        redisCache.reinitializeCache(posts);

        final List<PostDto> cachedPosts = redisCache.findTopNCached(posts.size());

        assertEquals(posts.size(), cachedPosts.size());

        for (int i = 0; i < posts.size(); i++) {
            assertEquals(posts.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void isNotEmpty() {
        assertFalse(redisCache.isEmpty());
    }

    @Test
    void isEmpty() {

        redisConnectionFactory.getConnection().serverCommands().flushAll();
        assertTrue(redisCache.isEmpty());
    }

    @Test
    void findTopALLCached() {

        final List<PostDto> cachedPosts = redisCache.findTopNCached(TEST_CACHED_POSTS.size());

        assertEquals(TEST_CACHED_POSTS.size(), cachedPosts.size());

        for (int i = 0; i < cachedPosts.size(); i++) {
            assertEquals(TEST_CACHED_POSTS.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void findNextTwoAfterTopTwoCached() {

        final int topSize = 2;

        final List<PostDto> topCachedPosts = redisCache.findTopNCached(topSize);

        assertEquals(topSize, topCachedPosts.size());

        for (int i = 0; i < topCachedPosts.size(); i++) {
            assertEquals(TEST_CACHED_POSTS.get(i), topCachedPosts.get(i));
        }

        final int nextSize = 2;
        final long lastPostScore = topCachedPosts.get(topSize-1).getKarmaScore();

        final List<PostDto> nextCachedPosts = redisCache.findNextNCached(nextSize, lastPostScore);

        assertEquals(nextSize, nextCachedPosts.size());

        final int endBound = Math.min(topSize + nextSize, TEST_CACHED_POSTS.size());
        List<PostDto> groundTruthNextPosts = TEST_CACHED_POSTS.subList(topSize, endBound);

        for (int i = 0; i < nextCachedPosts.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i), nextCachedPosts.get(i));
        }
    }

    @Test
    void cacheImageAndGetCachedImage() {

        final PostDto post = TEST_CACHED_POSTS.get(0);
        final byte[] dummyImageData = REDIS_CONTAINER.getDockerImageName().getBytes();

        redisCache.cacheImage(post.getId(), dummyImageData);

        final Optional<byte[]> cachedImageData = redisCache.getCachedImage(post.getId());

        assertTrue(cachedImageData.isPresent());
        assertArrayEquals(dummyImageData, cachedImageData.get());
    }

    @Test
    void getNonexistentCachedImage() {

        final PostDto post = TEST_CACHED_POSTS.get(0);

        final Optional<byte[]> cachedImageData = redisCache.getCachedImage(post.getId());

        assertFalse(cachedImageData.isPresent());
    }

    @Test
    void updateKarmaScoreIfPresent() {

        final PostDto post = TEST_CACHED_POSTS.get(0);

        final double delta = 1;

        OptionalDouble newScore = redisCache.updateKarmaScoreIfPresent(post.getId(), delta);

        assertTrue(newScore.isPresent());

        assertEquals(post.getKarmaScore() + delta, newScore.getAsDouble());

        final List<PostDto> cachedPosts = redisCache.findTopNCached(TEST_CACHED_POSTS.size());

        assertEquals(TEST_CACHED_POSTS.size(), cachedPosts.size());

        final PostDto newTopPost = PostDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .username(post.getUsername())
                .headline(post.getHeadline())
                .text(post.getText())
                .karmaScore((long) (post.getKarmaScore() + delta))
                .visibility(post.getVisibility())
                .build();

        assertEquals(newTopPost, cachedPosts.get(0));

        for (int i = 1; i < TEST_CACHED_POSTS.size(); i++) {
            assertEquals(TEST_CACHED_POSTS.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void updateKarmaScoreIfPresentNonExistent() {

        final int nonExistentUserId = 404;

        final double delta = 1;

        OptionalDouble newScore = redisCache.updateKarmaScoreIfPresent(nonExistentUserId, delta);

        assertTrue(newScore.isEmpty());
    }

    @Test
    void deletePostFromCache() {

        final PostDto post = TEST_CACHED_POSTS.get(0);

        boolean wasSuccessful = redisCache.deletePostFromCache(post.getId());

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