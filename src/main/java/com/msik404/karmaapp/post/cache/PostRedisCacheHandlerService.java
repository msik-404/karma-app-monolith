package com.msik404.karmaapp.post.cache;

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

    private List<PostDto> updateCache() {
        List<PostDto> newValuesForCache = repository.findTopNPosts(CACHED_POSTS_AMOUNT, List.of(Visibility.ACTIVE));
        cache.reinitializeCache(newValuesForCache);
        return newValuesForCache;
    }

    @Transactional(readOnly = true)
    public List<PostDto> findTopNHandler(int size, List<Visibility> visibilities) {

        List<PostDto> results;

        if (isOnlyActive(visibilities)) {
            if (cache.isEmpty()) {
                List<PostDto> newValuesForCache = updateCache();
                results = newValuesForCache.subList(0, size);
            } else {
                results = cache.findTopNCached(size);
                if (results.isEmpty()) {
                    results = repository.findTopNPosts(size, visibilities);
                }
            }
        } else {
            results = repository.findTopNPosts(size, visibilities);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<PostDto> findNextNHandler(int size, List<Visibility> visibilities, Pagination pagination) {

        List<PostDto> results;

        if (isOnlyActive(visibilities)) {
            if (cache.isEmpty()) {
                List<PostDto> newValuesForCache = updateCache();
                int firstSmallerElementIdx = 0;
                for (int i = 0; i < newValuesForCache.size(); i++) {
                    if (newValuesForCache.get(i).getKarmaScore() < pagination.karmaScore()) {
                        firstSmallerElementIdx = i;
                        break;
                    }
                }
                results = newValuesForCache.subList(firstSmallerElementIdx, size);
            } else {
                results = cache.findNextNCached(size, pagination.karmaScore());
                if (results.isEmpty()) {
                    results = repository.findNextNPosts(size, visibilities, pagination);
                }
            }

        } else {
            results = repository.findNextNPosts(size, visibilities, pagination);
        }

        return results;
    }

}