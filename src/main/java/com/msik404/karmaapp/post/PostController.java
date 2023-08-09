package com.msik404.karmaapp.post;

import java.util.List;

import com.msik404.karmaapp.karma.KarmaScoreAlreadyExistsException;
import com.msik404.karmaapp.karma.KarmaScoreNotFoundException;
import com.msik404.karmaapp.post.dto.PostCreationRequest;
import com.msik404.karmaapp.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("guest/posts")
    public List<PostResponse> findKeysetPaginated(
            @RequestParam(value = "karma_score", required = false) Long karmaScore,
            @RequestParam(value = "size", defaultValue = "100") int size) {

        return postService.findKeysetPaginated(karmaScore, size);
    }

    @GetMapping("guest/posts/{postId}/image")
    public ResponseEntity<byte[]> findImageByPostId(@PathVariable Long postId) throws ImageNotFoundException {

        byte[] imageData = postService.findImageByPostId(postId);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

    @PostMapping("user/posts")
    public void create(
            @RequestPart("json_data") PostCreationRequest jsonData,
            @RequestPart("image") MultipartFile image) throws FileProcessingException {

        postService.create(jsonData, image);
    }

    @PostMapping("user/posts/{postId}/rate")
    public void rate(@PathVariable Long postId, @RequestParam("is_positive") boolean isPositive)
            throws KarmaScoreAlreadyExistsException, PostNotFoundException {

        postService.rate(postId, isPositive);
    }

    @PostMapping("user/posts/{postId}/unrate")
    public void unrate(@PathVariable Long postId) throws KarmaScoreNotFoundException, PostNotFoundException {
        postService.unrate(postId);
    }

    @PostMapping("user/posts/{postId}/hide")
    public void hideByUser(@PathVariable Long postId) throws AccessDeniedException, PostNotFoundException {
        postService.changeVisibilityByUser(postId, PostVisibility.HIDDEN);
    }

    @PostMapping("mod/posts/{postId}/hide")
    public void hideByMod(@PathVariable Long postId) throws PostNotFoundException {
        postService.changeVisibility(postId, PostVisibility.HIDDEN);
    }

    @PostMapping("user/posts/{postId}/delete")
    public void deleteByUser(@PathVariable Long postId) throws AccessDeniedException, PostNotFoundException {
        postService.changeVisibilityByUser(postId, PostVisibility.DELETED);
    }

    @PostMapping("admin/posts/{postId}/delete")
    public void deleteByAdmin(@PathVariable Long postId) throws AccessDeniedException, PostNotFoundException {
        postService.changeVisibility(postId, PostVisibility.DELETED);
    }
}
