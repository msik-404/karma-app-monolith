package com.msik404.karmaapp.post.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.msik404.karmaapp.TestingImageDataCreator;
import com.msik404.karmaapp.position.ScrollPosition;
import com.msik404.karmaapp.post.Visibility;
import com.msik404.karmaapp.post.dto.PostDto;
import com.msik404.karmaapp.post.dto.PostWithImageDataDto;
import com.msik404.karmaapp.post.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostRedisCacheHandlerServiceTest {

    @Mock
    private PostRepository repository;

    @Mock
    private PostRedisCache cache;

    @Spy
    @InjectMocks
    private PostRedisCacheHandlerService cacheHandler;

    @NonNull
    PostDto getPost(long id, long karmaScore) {
        return new PostDto(id, null, null, null, null, karmaScore, null);
    }

    @NonNull
    List<PostDto> getPosts(int size) {

        List<PostDto> posts = new ArrayList<>(size);
        for (long i = 0; i < size; i++) {
            posts.add(new PostDto(i, null, null, null, null, null, null));
        }
        return posts;
    }

    @Test
    void findTopNHandler_SizeIsThreeAndVisibilityIsActiveAndCacheHasRequestedPosts_RepositoryShouldNotBeUsed() {

        // given
        int size = 3;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        boolean isCacheEmpty = false;
        when(cache.isEmpty()).thenReturn(isCacheEmpty);

        int cacheSize = 3;
        List<PostDto> cacheResults = getPosts(cacheSize);
        when(cache.findTopNCached(size)).thenReturn(Optional.of(cacheResults));

        // when
        cacheHandler.findTopNHandler(size, visibilities);

        // then
        verify(cache).isEmpty();
        verify(cache).findTopNCached(size);
        verify(repository, never()).findTopNPosts(size, visibilities);
    }

    @Test
    void findTopNHandler_SizeIsThreeAndVisibilityIsActiveAndCacheHasNotEnoughPostsButIsNotEmpty_RepositoryShouldBeUsed() {

        // given
        int size = 3;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        List<PostDto> repoResults = getPosts(size);
        when(repository.findTopNPosts(size, visibilities)).thenReturn(repoResults);

        boolean isCacheEmpty = false;
        when(cache.isEmpty()).thenReturn(isCacheEmpty);

        when(cache.findTopNCached(size)).thenReturn(Optional.empty());

        // when
        cacheHandler.findTopNHandler(size, visibilities);

        // then
        verify(cache).isEmpty();
        verify(cache).findTopNCached(size);
        verify(repository).findTopNPosts(size, visibilities);
    }

    @Test
    void findTopNHandler_SizeIsSevenAndVisibilityIsActiveAndCacheIsEmpty_CacheShouldBeUpdated() {

        // given
        int size = 7;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        boolean isCacheEmpty = true;
        when(cache.isEmpty()).thenReturn(isCacheEmpty);

        int updatedCacheSize = 5;
        List<PostDto> updatedCachePosts = getPosts(updatedCacheSize);
        when(cacheHandler.updateCache()).thenReturn(updatedCachePosts);

        // when
        cacheHandler.findTopNHandler(size, visibilities);

        // then
        verify(cache).isEmpty();
        verify(cacheHandler).updateCache();
        verify(cache, never()).findTopNCached(size);
    }

    @Test
    void findTopNHandler_SizeIsThreeAndVisibilityIsActiveOrIsHiddenAndCacheHasRequestedPosts_RepositoryShouldBeUsed() {

        // given
        int size = 3;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE, Visibility.HIDDEN);

        when(repository.findTopNPosts(size, visibilities)).thenReturn(getPosts(size));

        // when
        cacheHandler.findTopNHandler(size, visibilities);

        // then
        verify(cache, never()).isEmpty();
        verify(cache, never()).findTopNCached(size);
        verify(repository).findTopNPosts(size, visibilities);
    }

    @Test
    void findNextNHandler_SizeIsThreeAndVisibilityIsActiveAndProperPaginationIsSetAndCacheHasRequestedPosts_RepositoryShouldNotBeUsed() {

        // given
        int size = 3;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        var pagination = new ScrollPosition(3, 10);

        boolean isCacheEmpty = false;
        when(cache.isEmpty()).thenReturn(isCacheEmpty);

        when(cache.findNextNCached(size, pagination)).thenReturn(Optional.of(getPosts(size)));

        // when
        cacheHandler.findNextNHandler(size, visibilities, pagination);

        // then
        verify(cache).isEmpty();
        verify(cache).findNextNCached(size, pagination);
        verify(repository, never()).findTopNPosts(size, visibilities);
    }

    @Test
    void findNextNHandler_SizeIsThreeAndVisibilityIsActiveAndProperPaginationIsSetAndCacheDoesNotHaveRequestedPostsButIsNotEmpty_RepositoryShouldBeUsed() {

        // given
        int size = 3;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        var pagination = new ScrollPosition(3, 10);

        boolean isCacheEmpty = false;
        when(cache.isEmpty()).thenReturn(isCacheEmpty);

        when(cache.findNextNCached(size, pagination)).thenReturn(Optional.empty());

        // when
        cacheHandler.findNextNHandler(size, visibilities, pagination);

        // then
        verify(cache).isEmpty();
        verify(cache).findNextNCached(size, pagination);
        verify(repository).findNextNPosts(size, visibilities, pagination);
    }

    // proper here means that pagination obj values are based on real values, post with id and karmaScore exists.
    @Test
    void findNextNHandler_SizeIsThreeAndVisibilityIsActiveAndProperPaginationIsSetAndCacheHasRequestedPosts_CacheShouldBeUpdated() {

        // given
        int topSize = 3;
        int size = 3;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        List<PostDto> groundTruthPosts = new ArrayList<>();
        groundTruthPosts.add(getPost(3, 10));
        groundTruthPosts.add(getPost(1, 8));
        groundTruthPosts.add(getPost(2, 8));
        groundTruthPosts.add(getPost(5, 8));
        groundTruthPosts.add(getPost(0, 6));
        groundTruthPosts.add(getPost(4, 4));
        groundTruthPosts.add(getPost(6, 2));
        groundTruthPosts.add(getPost(7, -5));

        PostDto lastPost = groundTruthPosts.get(topSize - 1);
        var pagination = new ScrollPosition(
                lastPost.getId(),
                lastPost.getKarmaScore()
        );

        boolean isCacheEmpty = true;
        when(cache.isEmpty()).thenReturn(isCacheEmpty);
        when(cacheHandler.updateCache()).thenReturn(groundTruthPosts);

        // when
        List<PostDto> results = cacheHandler.findNextNHandler(size, visibilities, pagination);

        // then
        verify(cache).isEmpty();
        verify(cacheHandler).updateCache();
        verify(cache, never()).findNextNCached(size, pagination);

        assertEquals(size, results.size());

        List<PostDto> groundTruthNextPosts = groundTruthPosts.subList(topSize, topSize + size);
        for (int i = 0; i < results.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i), results.get(i));
        }
    }

    // arbitrary here means that pagination obj values are based on non existing values,
    // post with id and karmaScore does not exist.
    @Test
    void findNextNHandler_SizeIsThreeAndVisibilityIsActiveAndArbitraryPaginationIsSetAndCacheHasRequestedPosts_CacheShouldBeUpdated() {

        // given
        int size = 6;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        List<PostDto> groundTruthPosts = new ArrayList<>();
        groundTruthPosts.add(getPost(3, 10));
        groundTruthPosts.add(getPost(1, 8));
        groundTruthPosts.add(getPost(2, 8));
        groundTruthPosts.add(getPost(5, 8));
        groundTruthPosts.add(getPost(0, 6));
        groundTruthPosts.add(getPost(4, 4));
        groundTruthPosts.add(getPost(6, 2));
        groundTruthPosts.add(getPost(7, -5));

        var pagination = new ScrollPosition(-1, 7);

        boolean isCacheEmpty = true;
        when(cache.isEmpty()).thenReturn(isCacheEmpty);
        when(cacheHandler.updateCache()).thenReturn(groundTruthPosts);

        // when
        List<PostDto> results = cacheHandler.findNextNHandler(size, visibilities, pagination);

        // then
        verify(cache).isEmpty();
        verify(cacheHandler).updateCache();
        verify(cache, never()).findNextNCached(size, pagination);

        int startIdx = 4;
        List<PostDto> groundTruthNextPosts = groundTruthPosts.subList(startIdx, groundTruthPosts.size());

        assertEquals(groundTruthNextPosts.size(), results.size());

        for (int i = 0; i < results.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i), results.get(i));
        }
    }

    @Test
    void findNextNHandler_SizeIsThreeAndVisibilityIsDeletedAndProperPaginationIsSetAndCacheCannotBeUsed_RepositoryShouldBeUsed() {

        // given
        int size = 3;
        List<Visibility> visibilities = List.of(Visibility.DELETED);
        var pagination = new ScrollPosition(3, 10);

        when(repository.findNextNPosts(size, visibilities, pagination)).thenReturn(getPosts(size));

        // when
        cacheHandler.findNextNHandler(size, visibilities, pagination);

        // then
        verify(cache, never()).isEmpty();
        verify(repository).findNextNPosts(size, visibilities, pagination);
    }

    @Test
    void loadPostDataToCacheIfKarmaScoreIsHighEnough_DtoIsFoundAndContainsImageDataAndScoreIsHighEnoughForCachingAndCacheIsNotEmpty_DtoWithImageShouldBeCached() {

        // given
        long postId = 404;
        long karmaScore = 400;
        var postDto = new PostDto(
                postId,
                405L,
                "username",
                "headline",
                "text",
                karmaScore,
                Visibility.ACTIVE
        );

        var post = new PostWithImageDataDto(
                postDto,
                TestingImageDataCreator.getTestingImage()
        );

        when(repository.findPostDtoWithImageDataById(postId)).thenReturn(Optional.of(post));
        when(cache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore)).thenReturn(true);
        when(cache.insertPost(postDto, post.imageData())).thenReturn(true);
        when(cache.getZSetSize()).thenReturn((long) PostRedisCache.getMaxCachedPosts());

        // when
        assertTrue(cacheHandler.loadPostDataToCacheIfPossible(postId));

        // then
        verify(repository).findPostDtoWithImageDataById(postId);
        verify(cache).isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);
        verify(cache).insertPost(postDto, post.imageData());
    }

    @Test
    void loadPostDataToCacheIfKarmaScoreIsHighEnough_DtoIsFoundAndDoesNotContainImageDataAndScoreIsHighEnoughForCachingAndCacheIsNotEmpty_DtoWithImageShouldBeCached() {

        // given
        long postId = 404;
        long karmaScore = 400;
        var postDto = new PostDto(
                postId,
                405L,
                "username",
                "headline",
                "text",
                karmaScore,
                Visibility.ACTIVE
        );

        var post = new PostWithImageDataDto(
                postDto,
                TestingImageDataCreator.getTestingImage()
        );

        when(repository.findPostDtoWithImageDataById(postId)).thenReturn(Optional.of(post));
        when(cache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore)).thenReturn(true);
        when(cache.insertPost(postDto, post.imageData())).thenReturn(true);
        when(cache.getZSetSize()).thenReturn((long) PostRedisCache.getMaxCachedPosts());

        // when
        assertTrue(cacheHandler.loadPostDataToCacheIfPossible(postId));

        // then
        verify(repository).findPostDtoWithImageDataById(postId);
        verify(cache).isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);
        verify(cache).insertPost(postDto, post.imageData());
    }

    @Test
    void loadPostDataToCacheIfKarmaScoreIsHighEnough_DtoIsFoundAndContainsImageDataAndScoreIsNotHighEnoughForCachingAndCacheIsNotEmpty_DtoWithImageShouldNotBeCached() {

        // given
        long postId = 404;
        long karmaScore = 400;
        var postDto = new PostDto(
                postId,
                405L,
                "username",
                "headline",
                "text",
                karmaScore,
                Visibility.ACTIVE
        );

        var post = new PostWithImageDataDto(
                postDto,
                TestingImageDataCreator.getTestingImage()
        );

        when(repository.findPostDtoWithImageDataById(postId)).thenReturn(Optional.of(post));
        when(cache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore)).thenReturn(false);
        when(cache.getZSetSize()).thenReturn((long) PostRedisCache.getMaxCachedPosts());

        // when
        assertFalse(cacheHandler.loadPostDataToCacheIfPossible(postId));

        // then
        verify(repository).findPostDtoWithImageDataById(postId);
        verify(cache).isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);
        verify(cache, never()).insertPost(postDto, post.imageData());
    }

    @Test
    void loadPostDataToCacheIfKarmaScoreIsHighEnough_DtoIsFoundAndContainsImageDataAndCacheIsEmpty_DtoWithImageShouldBeCached() {

        // given
        long postId = 404;
        long karmaScore = 400;
        var postDto = new PostDto(
                postId,
                405L,
                "username",
                "headline",
                "text",
                karmaScore,
                Visibility.ACTIVE
        );

        var post = new PostWithImageDataDto(
                postDto,
                TestingImageDataCreator.getTestingImage()
        );

        when(repository.findPostDtoWithImageDataById(postId)).thenReturn(Optional.of(post));
        when(cache.getZSetSize()).thenReturn(0L);

        // when
        assertFalse(cacheHandler.loadPostDataToCacheIfPossible(postId));

        // then
        verify(repository).findPostDtoWithImageDataById(postId);
        verify(cache, never()).isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);
        verify(cache).insertPost(postDto, post.imageData());
    }

    @Test
    void loadPostDataToCacheIfKarmaScoreIsHighEnough_DtoIsNotFound_DtoWithImageShouldNotBeCached() {

        // given
        long postId = 404L;
        when(repository.findPostDtoWithImageDataById(postId)).thenReturn(Optional.empty());

        // when
        assertFalse(cacheHandler.loadPostDataToCacheIfPossible(postId));

        // then
        verify(repository).findPostDtoWithImageDataById(postId);
        verify(cache, never()).isKarmaScoreGreaterThanLowestScoreInZSet(anyLong());
        verify(cache, never()).insertPost(any(PostDto.class), any(byte[].class));
    }
}