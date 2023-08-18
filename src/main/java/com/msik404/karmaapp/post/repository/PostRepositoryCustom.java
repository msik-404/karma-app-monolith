package com.msik404.karmaapp.post.repository;

import java.util.List;

import com.msik404.karmaapp.post.PostVisibility;
import com.msik404.karmaapp.post.dto.PostJoined;
import com.msik404.karmaapp.post.dto.PostRatingResponse;
import com.msik404.karmaapp.post.exception.InternalServerErrorException;
import org.springframework.lang.NonNull;

public interface PostRepositoryCustom {

    List<PostJoined> findTopN(
            int size,
            @NonNull List<PostVisibility> visibilities
            ) throws InternalServerErrorException;

    List<PostJoined> findNextN(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long karmaScore
            ) throws InternalServerErrorException;

    List<PostJoined> findTopNWithUsername(
            int size,
            @NonNull List<PostVisibility> visibilities,
            @NonNull String username
    ) throws InternalServerErrorException;

    List<PostJoined> findNextNWithUsername(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long karmaScore,
            @NonNull String username
    ) throws InternalServerErrorException;

    List<PostRatingResponse> findTopN(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId
    ) throws InternalServerErrorException;

    List<PostRatingResponse> findNextN(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId,
            long karmaScore
    ) throws InternalServerErrorException;

    List<PostRatingResponse> findTopNWithUsername(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId,
            @NonNull String username
    ) throws InternalServerErrorException;

    List<PostRatingResponse> findNextNWithUsername(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId,
            long karmaScore,
            @NonNull String username
    ) throws InternalServerErrorException;

    byte[] findImageById(long postId) throws InternalServerErrorException;

    int addKarmaScoreToPost(long postId, long value);

    int changeVisibilityById(long postId, @NonNull PostVisibility visibility);

    List<PostJoined> findTopNWithUserId(int size, @NonNull List<PostVisibility> visibilities, long userId);

    List<PostJoined> findNextNWithUserId(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId,
            long karmaScore);
}
