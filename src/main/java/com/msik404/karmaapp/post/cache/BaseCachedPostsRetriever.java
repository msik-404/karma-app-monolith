package com.msik404.karmaapp.post.cache;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msik404.karmaapp.post.dto.PostJoinedDto;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;

import static java.util.stream.Collectors.toList;

public abstract class BaseCachedPostsRetriever {

    private final String postsHashKey;

    private final ObjectMapper objectMapper;

    protected final StringRedisTemplate redisTemplate;

    protected BaseCachedPostsRetriever(
            String postsHashKey, ObjectMapper objectMapper, StringRedisTemplate redisTemplate) {

        this.postsHashKey = postsHashKey;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    abstract Set<String> getPostIdKeySet(long size);

    private PostJoinedDto deserialize(@NonNull String json) {

        try {
            return objectMapper.readValue(json, PostJoinedDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing JSON to PostJoinedDto", e);
        }
    }

    public Optional<List<PostJoinedDto>> getPostsFromCache(long size) {

        Set<String> postIdKeySet = getPostIdKeySet(size);

        List<String> postIdKeyList = null;
        if (postIdKeySet != null && postIdKeySet.size() == size) {
            postIdKeyList = postIdKeySet.stream()
                    .map(postId -> PostRedisCache.getPostsHashKey(postsHashKey, postId))
                    .toList();
        }

        List<PostJoinedDto> results = null;
        if (postIdKeyList != null) {
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            List<String> serializedResults = hashOps.multiGet(postsHashKey, postIdKeyList);
            results = serializedResults.stream().map(this::deserialize).collect(toList());
        }

        return Optional.ofNullable(results);
    }

}
