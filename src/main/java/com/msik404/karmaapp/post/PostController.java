package com.msik404.karmaapp.post;

import java.util.List;

import com.msik404.karmaapp.post.dto.NewPostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // TODO: add image support
    @GetMapping("user/posts")
    public List<Post> findPaginated(
            @RequestParam(value = "post_id", required = false) Long postId,
            @RequestParam(value = "karma_score", required = false) Long karmaScore,
            @RequestParam(value = "size", defaultValue = "100") int size) {

        return postService.findManyBykeysetPagination(postId, karmaScore, size);
    }

    // TODO: add image support
    @PostMapping("user/posts")
    public void create(@RequestBody NewPostRequest request) {
        postService.create(request);
    }

    // TODO: Give positive or negative karma score
    // TODO: Mod and user can hide post
    // TODO: Admin and user can delete post
}
