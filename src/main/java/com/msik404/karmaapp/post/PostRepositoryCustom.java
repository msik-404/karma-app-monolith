package com.msik404.karmaapp.post;

import java.util.List;

import org.springframework.lang.NonNull;

public interface PostRepositoryCustom {

    List<Post> findTopN(int size);

    List<Post> findTopNextN(@NonNull Long postId, @NonNull Long karmaScore, int size);

    void addKarmaScoreToPost(@NonNull Long postId, @NonNull Long value) throws PostNotFoundException;

}
