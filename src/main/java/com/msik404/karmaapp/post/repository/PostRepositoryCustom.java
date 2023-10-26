package com.msik404.karmaapp.post.repository;

import java.util.List;

import com.msik404.karmaapp.position.ScrollPosition;
import com.msik404.karmaapp.post.Visibility;
import com.msik404.karmaapp.post.dto.PostDto;
import com.msik404.karmaapp.post.dto.PostRatingResponse;
import com.msik404.karmaapp.post.exception.InternalServerErrorException;
import org.springframework.lang.NonNull;

public interface PostRepositoryCustom {

    List<PostDto> findTopNPosts(
            int size,
            @NonNull List<Visibility> visibilities
    ) throws InternalServerErrorException;

    List<PostDto> findNextNPosts(
            int size,
            @NonNull List<Visibility> visibilities,
            @NonNull ScrollPosition position) throws InternalServerErrorException;

    List<PostDto> findTopNPostsWithUsername(
            int size,
            @NonNull List<Visibility> visibilities,
            @NonNull String username
    ) throws InternalServerErrorException;

    List<PostDto> findNextNPostsWithUsername(
            int size,
            @NonNull List<Visibility> visibilities,
            @NonNull ScrollPosition position,
            @NonNull String username
    ) throws InternalServerErrorException;

    List<PostDto> findTopNWithUserId(int size, @NonNull List<Visibility> visibilities, long userId);

    List<PostDto> findNextNWithUserId(
            int size,
            @NonNull List<Visibility> visibilities,
            long userId,
            @NonNull ScrollPosition position);

    List<PostRatingResponse> findTopNRatings(
            int size,
            @NonNull List<Visibility> visibilities,
            long userId
    ) throws InternalServerErrorException;

    List<PostRatingResponse> findNextNRatings(
            int size,
            @NonNull List<Visibility> visibilities,
            long userId,
            @NonNull ScrollPosition position) throws InternalServerErrorException;

    List<PostRatingResponse> findTopNRatingsWithUsername(
            int size,
            @NonNull List<Visibility> visibilities,
            long userId,
            @NonNull String username
    ) throws InternalServerErrorException;

    List<PostRatingResponse> findNextNRatingsWithUsername(
            int size,
            @NonNull List<Visibility> visibilities,
            long userId,
            @NonNull ScrollPosition position,
            @NonNull String username
    ) throws InternalServerErrorException;

    int addKarmaScoreToPost(long postId, long value);

    int changeVisibilityById(long postId, @NonNull Visibility visibility);
}
