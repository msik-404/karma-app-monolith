package com.msik404.karmaapp.post;

import java.util.List;

import com.msik404.karmaapp.post.dto.NewPostRequest;
import com.msik404.karmaapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository repository;
    private final UserRepository userRepository;

    public List<Post> findManyBykeysetPagination(Long postId, Long karmaScore, int size) {

        List<Post> pageContents;
        if (postId != null && karmaScore != null) {
            pageContents = repository.findTopNextN(postId, karmaScore, size);
        } else {
            pageContents = repository.findTopN(size);
        }
        return pageContents;
    }

    public void create(NewPostRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();

        Post newPost = Post.builder()
                .text(request.getText())
                .karmaScore(0L)
                .visibility(PostVisibility.ACTIVE)
                .user(userRepository.getReferenceById(userId))
                .build();

        repository.save(newPost);
    }
}
