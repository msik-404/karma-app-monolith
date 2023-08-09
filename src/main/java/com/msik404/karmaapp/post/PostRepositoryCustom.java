package com.msik404.karmaapp.post;

import java.util.List;
import java.util.Optional;

import com.msik404.karmaapp.post.dto.PostResponse;
import org.springframework.lang.NonNull;

public interface PostRepositoryCustom {

    List<PostResponse> findKeysetPaginated(Long karmaScore, int size);

    void addKarmaScoreToPost(long postId, long value) throws PostNotFoundException;

    void changeVisibilityById(long postId, @NonNull PostVisibility visibility) throws PostNotFoundException;

    Optional<byte[]> findImageByPostId(long postId);
}
