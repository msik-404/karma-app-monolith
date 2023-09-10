package com.msik404.karmaapp.post.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.msik404.karmaapp.TestingImageDataCreator;
import com.msik404.karmaapp.pagin.Pagination;
import com.msik404.karmaapp.post.Visibility;
import com.msik404.karmaapp.post.dto.PostDto;
import com.msik404.karmaapp.post.dto.PostDtoWithImageData;
import com.msik404.karmaapp.post.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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

    List<PostDto> getPosts(int size) {

        List<PostDto> posts = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            posts.add(PostDto.builder().id((long) i).build());
        }
        return posts;
    }

    @Test
    void findTopNHandler_SizeIsThreeAndVisibilityIsActiveAndCacheHasRequestedPosts_RepositoryShouldNotBeUsed() {

        // given
        final int size = 3;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        final boolean isCacheEmpty = false;
        when(cache.isEmpty()).thenReturn(isCacheEmpty);

        final int cacheSize = 3;
        final List<PostDto> cacheResults = getPosts(cacheSize);
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
        final int size = 3;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        final List<PostDto> repoResults = getPosts(size);
        when(repository.findTopNPosts(size, visibilities)).thenReturn(repoResults);

        final boolean isCacheEmpty = false;
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
        final int size = 7;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        final boolean isCacheEmpty = true;
        when(cache.isEmpty()).thenReturn(isCacheEmpty);

        final int updatedCacheSize = 5;
        final List<PostDto> updatedCachePosts = getPosts(updatedCacheSize);
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
        final int size = 3;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE, Visibility.HIDDEN);

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
        final int size = 3;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final var pagination = new Pagination(3, 10);

        final boolean isCacheEmpty = false;
        when(cache.isEmpty()).thenReturn(isCacheEmpty);

        when(cache.findNextNCached(size, pagination.karmaScore())).thenReturn(Optional.of(getPosts(size)));

        // when
        cacheHandler.findNextNHandler(size, visibilities, pagination);

        // then
        verify(cache).isEmpty();
        verify(cache).findNextNCached(size, pagination.karmaScore());
        verify(repository, never()).findTopNPosts(size, visibilities);
    }

    @Test
    void findNextNHandler_SizeIsThreeAndVisibilityIsActiveAndProperPaginationIsSetAndCacheDoesNotHaveRequestedPostsButIsNotEmpty_RepositoryShouldBeUsed() {

        // given
        final int size = 3;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final var pagination = new Pagination(3, 10);

        final boolean isCacheEmpty = false;
        when(cache.isEmpty()).thenReturn(isCacheEmpty);

        when(cache.findNextNCached(size, pagination.karmaScore())).thenReturn(Optional.empty());

        // when
        cacheHandler.findNextNHandler(size, visibilities, pagination);

        // then
        verify(cache).isEmpty();
        verify(cache).findNextNCached(size, pagination.karmaScore());
        verify(repository).findNextNPosts(size, visibilities, pagination);
    }

    // proper here means that pagination obj values are based on real values, post with id and karmaScore exists.
    @Test
    void findNextNHandler_SizeIsThreeAndVisibilityIsActiveAndProperPaginationIsSetAndCacheHasRequestedPosts_CacheShouldBeUpdated() {

        // given
        final int topSize = 3;
        final int size = 3;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        List<PostDto> groundTruthPosts = new ArrayList<>();
        groundTruthPosts.add(PostDto.builder().id(3L).karmaScore(10L).build());
        groundTruthPosts.add(PostDto.builder().id(1L).karmaScore(8L).build());
        groundTruthPosts.add(PostDto.builder().id(2L).karmaScore(8L).build());
        groundTruthPosts.add(PostDto.builder().id(5L).karmaScore(8L).build());
        groundTruthPosts.add(PostDto.builder().id(0L).karmaScore(6L).build());
        groundTruthPosts.add(PostDto.builder().id(4L).karmaScore(4L).build());
        groundTruthPosts.add(PostDto.builder().id(6L).karmaScore(2L).build());
        groundTruthPosts.add(PostDto.builder().id(7L).karmaScore(-5L).build());

        final PostDto lastPost = groundTruthPosts.get(topSize - 1);
        final var pagination = new Pagination(
                lastPost.getId(),
                lastPost.getKarmaScore()
        );

        final boolean isCacheEmpty = true;
        when(cache.isEmpty()).thenReturn(isCacheEmpty);
        when(cacheHandler.updateCache()).thenReturn(groundTruthPosts);

        // when
        List<PostDto> results = cacheHandler.findNextNHandler(size, visibilities, pagination);

        // then
        verify(cache).isEmpty();
        verify(cacheHandler).updateCache();
        verify(cache, never()).findNextNCached(size, pagination.karmaScore());

        assertEquals(size, results.size());

        final List<PostDto> groundTruthNextPosts = groundTruthPosts.subList(topSize, topSize + size);
        for (int i = 0; i < results.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i), results.get(i));
        }
    }

    // arbitrary here means that pagination obj values are based on non existing values,
    // post with id and karmaScore does not exist.
    @Test
    void findNextNHandler_SizeIsThreeAndVisibilityIsActiveAndArbitraryPaginationIsSetAndCacheHasRequestedPosts_CacheShouldBeUpdated() {

        // given
        final int size = 6;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        List<PostDto> groundTruthPosts = new ArrayList<>();
        groundTruthPosts.add(PostDto.builder().id(3L).karmaScore(10L).build());
        groundTruthPosts.add(PostDto.builder().id(1L).karmaScore(8L).build());
        groundTruthPosts.add(PostDto.builder().id(2L).karmaScore(8L).build());
        groundTruthPosts.add(PostDto.builder().id(5L).karmaScore(8L).build());
        groundTruthPosts.add(PostDto.builder().id(0L).karmaScore(6L).build());
        groundTruthPosts.add(PostDto.builder().id(4L).karmaScore(4L).build());
        groundTruthPosts.add(PostDto.builder().id(6L).karmaScore(2L).build());
        groundTruthPosts.add(PostDto.builder().id(7L).karmaScore(-5L).build());

        final var pagination = new Pagination(-1, 7);

        final boolean isCacheEmpty = true;
        when(cache.isEmpty()).thenReturn(isCacheEmpty);
        when(cacheHandler.updateCache()).thenReturn(groundTruthPosts);

        // when
        List<PostDto> results = cacheHandler.findNextNHandler(size, visibilities, pagination);

        // then
        verify(cache).isEmpty();
        verify(cacheHandler).updateCache();
        verify(cache, never()).findNextNCached(size, pagination.karmaScore());

        final int startIdx = 4;
        final List<PostDto> groundTruthNextPosts = groundTruthPosts.subList(startIdx, groundTruthPosts.size());

        assertEquals(groundTruthNextPosts.size(), results.size());

        for (int i = 0; i < results.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i), results.get(i));
        }
    }

    @Test
    void findNextNHandler_SizeIsThreeAndVisibilityIsDeletedAndProperPaginationIsSetAndCacheCannotBeUsed_RepositoryShouldBeUsed() {

        // given
        final int size = 3;
        final List<Visibility> visibilities = List.of(Visibility.DELETED);
        final var pagination = new Pagination(3, 10);

        when(repository.findNextNPosts(size, visibilities, pagination)).thenReturn(getPosts(size));

        // when
        cacheHandler.findNextNHandler(size, visibilities, pagination);

        // then
        verify(cache, never()).isEmpty();
        verify(repository).findNextNPosts(size, visibilities, pagination);
    }


    @Test
    void loadPostDataToCacheIfKarmaScoreIsHighEnough_DtoIsFoundAndContainsImageDataAndScoreIsHighEnoughForCaching_DtoWithImageShouldBeCached() {

        // given
        final long postId = 404;
        final long karmaScore = 400;
        final var post = PostDtoWithImageData.builder()
                .id(postId)
                .userId(405L)
                .username("username")
                .headline("headline")
                .text("text")
                .karmaScore(karmaScore)
                .visibility(Visibility.ACTIVE)
                .imageData(TestingImageDataCreator.getTestingImage())
                .build();

        final var postDto = PostDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .username(post.getUsername())
                .headline(post.getHeadline())
                .text(post.getText())
                .karmaScore(post.getKarmaScore())
                .visibility(post.getVisibility())
                .build();

        when(repository.findPostDtoWithImageDataById(postId)).thenReturn(Optional.of(post));
        when(cache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore)).thenReturn(Optional.of(true));
        when(cache.insertPost(postDto, post.getImageData())).thenReturn(true);

        // when
        assertTrue(cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId));

        // then
        verify(repository).findPostDtoWithImageDataById(postId);
        verify(cache).isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);
        verify(cache).insertPost(postDto, post.getImageData());
    }

    @Test
    void loadPostDataToCacheIfKarmaScoreIsHighEnough_DtoIsFoundAndDoesNotContainImageDataAndScoreIsHighEnoughForCaching_DtoWithImageShouldBeCached() {

        // given
        final long postId = 404;
        final long karmaScore = 400;
        final var post = PostDtoWithImageData.builder()
                .id(postId)
                .userId(405L)
                .username("username")
                .headline("headline")
                .text("text")
                .karmaScore(karmaScore)
                .visibility(Visibility.ACTIVE)
                .imageData(null)
                .build();

        final var postDto = PostDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .username(post.getUsername())
                .headline(post.getHeadline())
                .text(post.getText())
                .karmaScore(post.getKarmaScore())
                .visibility(post.getVisibility())
                .build();

        when(repository.findPostDtoWithImageDataById(postId)).thenReturn(Optional.of(post));
        when(cache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore)).thenReturn(Optional.of(true));
        when(cache.insertPost(postDto, post.getImageData())).thenReturn(true);

        // when
        assertTrue(cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId));

        // then
        verify(repository).findPostDtoWithImageDataById(postId);
        verify(cache).isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);
        verify(cache).insertPost(postDto, post.getImageData());
    }

    @Test
    void loadPostDataToCacheIfKarmaScoreIsHighEnough_DtoIsFoundAndContainsImageDataAndScoreIsNotHighEnoughForCaching_DtoWithImageShouldNotBeCached() {

        // given
        final long postId = 404;
        final long karmaScore = 400;
        final var post = PostDtoWithImageData.builder()
                .id(postId)
                .userId(405L)
                .username("username")
                .headline("headline")
                .text("text")
                .karmaScore(karmaScore)
                .visibility(Visibility.ACTIVE)
                .imageData(TestingImageDataCreator.getTestingImage())
                .build();

        final var postDto = PostDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .username(post.getUsername())
                .headline(post.getHeadline())
                .text(post.getText())
                .karmaScore(post.getKarmaScore())
                .visibility(post.getVisibility())
                .build();

        when(repository.findPostDtoWithImageDataById(postId)).thenReturn(Optional.of(post));
        when(cache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore)).thenReturn(Optional.of(false));

        // when
        assertFalse(cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId));

        // then
        verify(repository).findPostDtoWithImageDataById(postId);
        verify(cache).isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);
        verify(cache, never()).insertPost(postDto, post.getImageData());
    }

    @Test
    void loadPostDataToCacheIfKarmaScoreIsHighEnough_DtoIsFoundAndContainsImageDataAndCacheIsEmpty_DtoWithImageShouldNotBeCached() {

        // given
        final long postId = 404;
        final long karmaScore = 400;
        final var post = PostDtoWithImageData.builder()
                .id(postId)
                .userId(405L)
                .username("username")
                .headline("headline")
                .text("text")
                .karmaScore(karmaScore)
                .visibility(Visibility.ACTIVE)
                .imageData(TestingImageDataCreator.getTestingImage())
                .build();

        final var postDto = PostDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .username(post.getUsername())
                .headline(post.getHeadline())
                .text(post.getText())
                .karmaScore(post.getKarmaScore())
                .visibility(post.getVisibility())
                .build();

        when(repository.findPostDtoWithImageDataById(postId)).thenReturn(Optional.of(post));
        when(cache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore)).thenReturn(Optional.empty());

        // when
        assertFalse(cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId));

        // then
        verify(repository).findPostDtoWithImageDataById(postId);
        verify(cache).isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);
        verify(cache, never()).insertPost(postDto, post.getImageData());
    }

    @Test
    void loadPostDataToCacheIfKarmaScoreIsHighEnough_DtoIsNotFound_DtoWithImageShouldNotBeCached() {

        // given
        final long postId = 404L;
        when(repository.findPostDtoWithImageDataById(postId)).thenReturn(Optional.empty());

        // when
        assertFalse(cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId));

        // then
        verify(repository).findPostDtoWithImageDataById(postId);
        verify(cache, never()).isKarmaScoreGreaterThanLowestScoreInZSet(anyLong());
        verify(cache, never()).insertPost(any(PostDto.class), any(byte[].class));
    }
}