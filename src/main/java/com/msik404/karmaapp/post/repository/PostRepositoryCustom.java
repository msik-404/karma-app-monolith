package com.msik404.karmaapp.post.repository;

import java.util.List;

import com.msik404.karmaapp.post.PostVisibility;
import com.msik404.karmaapp.post.dto.PostJoinedDto;
import com.msik404.karmaapp.post.exception.InternalServerErrorException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface PostRepositoryCustom {

    List<PostJoinedDto> findKeysetPaginated(
            int size,
            @Nullable Long karmaScore,
            @Nullable Long authenticatedUserId,
            @Nullable String username,
            @NonNull List<PostVisibility> visibilities)
            throws InternalServerErrorException;

    byte[] findImageById(long postId) throws InternalServerErrorException;

    int addKarmaScoreToPost(long postId, long value);

    int changeVisibilityById(long postId, @NonNull PostVisibility visibility);

}
