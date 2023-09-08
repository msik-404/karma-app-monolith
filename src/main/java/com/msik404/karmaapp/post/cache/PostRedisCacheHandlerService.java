package com.msik404.karmaapp.post.cache;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.msik404.karmaapp.pagin.Pagination;
import com.msik404.karmaapp.post.Visibility;
import com.msik404.karmaapp.post.dto.PostDto;
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

    private int findNextSmallerThan(@NonNull List<PostDto> topPosts, long karmaScore) {

        int value = Collections.binarySearch(
                topPosts,
                PostDto.builder().karmaScore(karmaScore).build(),
                Comparator.comparing(PostDto::getKarmaScore, Comparator.reverseOrder()));

        // returns topPosts.size() if post with karmaScore is last or insertion point would be last
        if (value < 0) {
            // get insertion point
            return - value - 1;
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
                final int firstSmallerElementIdx = findNextSmallerThan(newValuesForCache, pagination.karmaScore());
                results = newValuesForCache.subList(firstSmallerElementIdx, newValuesForCache.size());
            } else {
                results = cache.findNextNCached(size, pagination.karmaScore())
                        .orElseGet(() -> repository.findNextNPosts(size, visibilities, pagination) );
            }
        } else {
            results = repository.findNextNPosts(size, visibilities, pagination);
        }

        return results;
    }

}