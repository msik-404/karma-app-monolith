package com.msik404.karmaappmonolith.post.cache;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.msik404.karmaappmonolith.position.ScrollPosition;
import com.msik404.karmaappmonolith.post.Visibility;
import com.msik404.karmaappmonolith.post.comparator.BasicComparablePost;
import com.msik404.karmaappmonolith.post.comparator.PostComparator;
import com.msik404.karmaappmonolith.post.dto.PostDto;
import com.msik404.karmaappmonolith.post.dto.PostWithImageDataDto;
import com.msik404.karmaappmonolith.post.exception.PostNotFoundException;
import com.msik404.karmaappmonolith.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostRedisCacheHandlerService {

    private final PostRedisCache cache;
    private final PostRepository repository;

    private static boolean isOnlyActive(@NonNull List<Visibility> visibilities) {
        return visibilities.size() == 1 && visibilities.contains(Visibility.ACTIVE);
    }

    @NonNull
    public List<PostDto> updateCache() {

        final List<PostDto> newValuesForCache = repository.findTopNPosts(
                PostRedisCache.getMaxCachedPosts(),
                List.of(Visibility.ACTIVE)
        );

        if (!newValuesForCache.isEmpty()) {
            cache.reinitializeCache(newValuesForCache);
        }

        return newValuesForCache;
    }

    @Transactional(readOnly = true)
    @NonNull
    public List<PostDto> findTopNHandler(int size, @NonNull List<Visibility> visibilities) {

        List<PostDto> results;

        if (isOnlyActive(visibilities)) {
            if (cache.isEmpty()) {
                List<PostDto> newValuesForCache = updateCache();
                int endBound = Math.min(size, newValuesForCache.size());
                results = newValuesForCache.subList(0, endBound);
            } else {
                results = cache.findTopNCached(size)
                        .orElseGet(() -> repository.findTopNPosts(size, visibilities));
            }
        } else {
            results = repository.findTopNPosts(size, visibilities);
        }

        return results;
    }

    private int findNextSmallerThan(@NonNull List<PostDto> topPosts, @NonNull ScrollPosition position) {

        int value = Collections.binarySearch(
                topPosts,
                new BasicComparablePost(position.postId(), position.karmaScore()),
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
    @NonNull
    public List<PostDto> findNextNHandler(
            int size,
            @NonNull List<Visibility> visibilities,
            @NonNull ScrollPosition position) {

        List<PostDto> results;

        if (isOnlyActive(visibilities)) {
            if (cache.isEmpty()) {
                List<PostDto> newValuesForCache = updateCache();
                int firstSmallerElementIdx = findNextSmallerThan(newValuesForCache, position);

                int endBound = Math.min(firstSmallerElementIdx + size, newValuesForCache.size());
                results = newValuesForCache.subList(firstSmallerElementIdx, endBound);
            } else {
                results = cache.findNextNCached(size, position)
                        .orElseGet(() -> repository.findNextNPosts(size, visibilities, position));
            }
        } else {
            results = repository.findNextNPosts(size, visibilities, position);
        }

        return results;
    }

    /*
     * Post will be cached if less than CACHED_POSTS_AMOUNT posts are cached or input post score is higher than
     * the lowest cached post score. Because of this functionality there may be more cached posts than specified
     * amount allows, but I doubt that this would be problematic, because cache gets refreshed every PostRedisCache.TIMEOUT.
     *
     * @param post Input post with image data to be cached.
     * @return true if cached else false.
     */
    @Transactional(readOnly = true)
    public boolean loadToCacheIfPossible(@NonNull PostWithImageDataDto post) {

        long cacheSize = cache.getZSetSize();

        if (cacheSize >= PostRedisCache.getMaxCachedPosts()) {

            boolean isAccepted = cache.isKarmaScoreGreaterThanLowestScoreInZSet(
                    post.postDto().getKarmaScore());

            if (!isAccepted) {
                return false;
            }
        }

        return cache.insertPost(post.postDto(), post.imageData());
    }

    /**
     * @param postId Id of post with image data if found to be cached.
     * @return true if cached else false.
     * @throws PostNotFoundException thrown when post with requested postId is not found.
     */
    @Transactional(readOnly = true)
    public boolean loadPostDataToCacheIfPossible(long postId) {

        Optional<PostWithImageDataDto> optionalPost = repository.findPostDtoWithImageDataById(postId);
        if (optionalPost.isEmpty()) {
            return false;
        }
        return loadToCacheIfPossible(optionalPost.get());
    }

}