package com.msik404.karmaapp.post;

import java.util.List;

import org.springframework.lang.NonNull;

public interface PostRepositoryCustom {

    List<Post> findTopN(int size);

    List<Post> findTopNextN(long postId, long karmaScore, int size);

    void addKarmaScoreToPost(long postId, long value) throws PostNotFoundException;

    void changeVisibilityById(long postId, @NonNull PostVisibility visibility) throws PostNotFoundException;

}
