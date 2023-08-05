package com.msik404.karmaapp.post;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository repository;

    public List<Post> findManyBykeysetPagination(Long postId, Long karmaScore, int size) {

        List<Post> pageContents;
        if (postId != null && karmaScore != null) {
            pageContents = repository.findTopNextN(postId, karmaScore, size);
        } else {
            pageContents = repository.findTopN(size);
        }
        return pageContents;
    }

}
