package com.msik404.karmaapp.post;

import java.util.List;

public interface PostRepositoryCustom {

    List<Post> findTopN(int size);

    List<Post> findTopNextN(Long postId, Long karmaScore, int size);

}
