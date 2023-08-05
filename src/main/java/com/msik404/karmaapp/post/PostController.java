package com.msik404.karmaapp.post;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // TODO: get paginated posts with image and karma score
    @GetMapping("user/posts")
    public List<Post> findPaginated(
            @RequestParam(value = "post_id", required = false) Long postId,
            @RequestParam(value = "karma_score", required = false) Long karmaScore,
            @RequestParam(value = "size", defaultValue = "100") int size) {

        return postService.findManyBykeysetPagination(postId, karmaScore, size);
    }

    // TODO: create new post with uploading image
    // TODO: Give positive or negative karma score
    // TODO: Mod can hide post
    // TODO: Admin can delete post
}
