package com.msik404.karmaapp.post.repository;

import java.util.List;

import com.msik404.karmaapp.post.PostVisibility;
import com.msik404.karmaapp.post.dto.PostDto;
import com.msik404.karmaapp.post.dto.PostRatingResponse;
import com.msik404.karmaapp.post.exception.InternalServerErrorException;
import org.springframework.lang.NonNull;

public interface PostRepositoryCustom {

    List<PostDto> findTopNPosts(
            int size,
            @NonNull List<PostVisibility> visibilities
            ) throws InternalServerErrorException;

    List<PostDto> findNextNPosts(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long karmaScore
            ) throws InternalServerErrorException;

    List<PostDto> findTopNPostsWithUsername(
            int size,
            @NonNull List<PostVisibility> visibilities,
            @NonNull String username
    ) throws InternalServerErrorException;

    List<PostDto> findNextNPostsWithUsername(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long karmaScore,
            @NonNull String username
    ) throws InternalServerErrorException;

    List<PostRatingResponse> findTopNRatings(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId
    ) throws InternalServerErrorException;

    List<PostRatingResponse> findNextNRatings(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId,
            long karmaScore
    ) throws InternalServerErrorException;

    List<PostRatingResponse> findTopNRatingsWithUsername(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId,
            @NonNull String username
    ) throws InternalServerErrorException;

    List<PostRatingResponse> findNextNRatingsWithUsername(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId,
            long karmaScore,
            @NonNull String username
    ) throws InternalServerErrorException;

    int addKarmaScoreToPost(long postId, long value);

    int changeVisibilityById(long postId, @NonNull PostVisibility visibility);

    List<PostDto> findTopNWithUserId(int size, @NonNull List<PostVisibility> visibilities, long userId);

    List<PostDto> findNextNWithUserId(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId,
            long karmaScore);
}
