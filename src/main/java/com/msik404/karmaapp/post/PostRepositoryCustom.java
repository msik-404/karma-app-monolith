package com.msik404.karmaapp.post;

import java.util.List;

import com.msik404.karmaapp.post.dto.PostJoinedDto;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface PostRepositoryCustom {

    List<PostJoinedDto> findKeysetPaginated(
            @Nullable Long karmaScore,
            @Nullable Long authenticatedUserId,
            @Nullable String username,
            @NonNull List<PostVisibility> visibilities,
            int size)
            throws InternalServerErrorException;

    byte[] findImageById(long postId) throws InternalServerErrorException;

    int addKarmaScoreToPost(long postId, long value);

    int changeVisibilityById(long postId, @NonNull PostVisibility visibility);

}
