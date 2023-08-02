package com.msik404.karmaapp.post;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // TODO: get paginated posts with image and karma score
    // TODO: create new post with uploading image
    // TODO: Give positive or negative karma score
    // TODO: Mod can hide post
    // TODO: Admin can delete post
}
