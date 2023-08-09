package com.msik404.karmaapp.post;

import java.util.List;

import com.msik404.karmaapp.post.dto.PostResponse;
import org.springframework.lang.NonNull;

public interface PostRepositoryCustom {

    List<PostResponse> findKeysetPaginated(Long karmaScore, int size) throws InternalServerErrorException;

    byte[] findImageById(long postId) throws InternalServerErrorException;

    int addKarmaScoreToPost(long postId, long value);

    int changeVisibilityById(long postId, @NonNull PostVisibility visibility);

}
