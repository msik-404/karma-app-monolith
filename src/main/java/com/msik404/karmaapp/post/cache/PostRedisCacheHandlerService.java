package com.msik404.karmaapp.post.cache;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.msik404.karmaapp.pagin.Pagination;
import com.msik404.karmaapp.post.PostComparator;
import com.msik404.karmaapp.post.Visibility;
import com.msik404.karmaapp.post.dto.PostDto;
import com.msik404.karmaapp.post.dto.PostDtoWithImageData;
import com.msik404.karmaapp.post.repository.PostRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostRedisCacheHandlerService {

    private static final int CACHED_POSTS_AMOUNT = 10_000;

    private final PostRedisCache cache;
    private final PostRepository repository;

    public PostRedisCacheHandlerService(PostRedisCache cache, PostRepository repository) {

        this.cache = cache;
        this.repository = repository;
    }

    private static boolean isOnlyActive(@NonNull List<Visibility> visibilities) {
        return visibilities.size() == 1 && visibilities.contains(Visibility.ACTIVE);
    }

    public List<PostDto> updateCache() {

        final List<PostDto> newValuesForCache = repository.findTopNPosts(
                CACHED_POSTS_AMOUNT, List.of(Visibility.ACTIVE));

        cache.reinitializeCache(newValuesForCache);

        return newValuesForCache;
    }

    @Transactional(readOnly = true)
    public List<PostDto> findTopNHandler(int size, @NonNull List<Visibility> visibilities) {

        List<PostDto> results;

        if (isOnlyActive(visibilities)) {
            if (cache.isEmpty()) {
                final List<PostDto> newValuesForCache = updateCache();
                final int endBound = Math.min(size, newValuesForCache.size());
                results = newValuesForCache.subList(0, endBound);
            } else {
                results = cache.findTopNCached(size).orElseGet(() -> repository.findTopNPosts(size, visibilities));
            }
        } else {
            results = repository.findTopNPosts(size, visibilities);
        }

        return results;
    }

    private int findNextSmallerThan(@NonNull List<PostDto> topPosts, @NonNull Pagination pagination) {

        int value = Collections.binarySearch(
                topPosts,
                PostDto.builder().id(pagination.postId()).karmaScore(pagination.karmaScore()).build(),
                new PostComparator().reversed()
        );

        // returns topPosts.size() if post with karmaScore is last or insertion point would be last
        if (value < 0) {
            // get insertion point
            return -value - 1;
        }
        return value + 1;
    }

    @Transactional(readOnly = true)
    public List<PostDto> findNextNHandler(
            int size,
            @NonNull List<Visibility> visibilities,
            @NonNull Pagination pagination) {

        List<PostDto> results;

        if (isOnlyActive(visibilities)) {
            if (cache.isEmpty()) {
                final List<PostDto> newValuesForCache = updateCache();
                final int firstSmallerElementIdx = findNextSmallerThan(newValuesForCache, pagination);

                final int endBound = Math.min(firstSmallerElementIdx + size, newValuesForCache.size());
                results = newValuesForCache.subList(firstSmallerElementIdx, endBound);
            } else {
                results = cache.findNextNCached(size, pagination.karmaScore())
                        .orElseGet(() -> repository.findNextNPosts(size, visibilities, pagination));
            }
        } else {
            results = repository.findNextNPosts(size, visibilities, pagination);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public boolean loadToCacheIfKarmaScoreIsHighEnough(@NonNull PostDtoWithImageData post) {

        final Optional<Boolean> optionalIsHighEnough = cache.isKarmaScoreGreaterThanLowestScoreInZSet(
                post.getKarmaScore());

        if (optionalIsHighEnough.isEmpty()) {
            return false;
        }

        final boolean isHighEnough = optionalIsHighEnough.get();
        if (!isHighEnough) {
            return false;
        }

        final var postDto = PostDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .username(post.getUsername())
                .headline(post.getHeadline())
                .text(post.getText())
                .karmaScore(post.getKarmaScore())
                .visibility(post.getVisibility())
                .build();

        return cache.insertPost(postDto, post.getImageData());
    }

    @Transactional(readOnly = true)
    public boolean loadPostDataToCacheIfKarmaScoreIsHighEnough(long postId) {

        Optional<PostDtoWithImageData> optionalPost = repository.findPostDtoWithImageDataById(postId);

        if (optionalPost.isEmpty()) {
            return false;
        }

        PostDtoWithImageData post = optionalPost.get();
        return loadToCacheIfKarmaScoreIsHighEnough(post);
    }

}