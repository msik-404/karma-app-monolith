package com.msik404.karmaapp.post;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.msik404.karmaapp.karma.KarmaScoreAlreadyExistsException;
import com.msik404.karmaapp.karma.KarmaScoreNotFoundException;
import com.msik404.karmaapp.post.dto.PostCreationRequest;
import com.msik404.karmaapp.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
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
    private final PostResponseModelAssembler assembler;

    @GetMapping("guest/posts")
    public List<EntityModel<PostResponse>> findKeysetPaginated(
            @RequestParam(value = "karma_score", required = false) Long karmaScore,
            @RequestParam(value = "requested_username", required = false) String requestedUsername,
            @RequestParam(value = "size", defaultValue = "100") int size)
            throws InternalServerErrorException {

        return postService.findKeysetPaginated(karmaScore, requestedUsername, List.of(PostVisibility.ACTIVE), size)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("mod/posts")
    public List<EntityModel<PostResponse>> findKeysetPaginatedHidden(
            @RequestParam(value = "karma_score", required = false) Long karmaScore,
            @RequestParam(value = "requested_username", required = false) String requestedUsername,
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden)
            throws InternalServerErrorException {

        List<PostVisibility> visibilities = new ArrayList<>();
        if (active) {
            visibilities.add(PostVisibility.ACTIVE);
        }
        if (hidden) {
            visibilities.add(PostVisibility.HIDDEN);
        }

        return postService.findKeysetPaginated(karmaScore, requestedUsername, visibilities, size)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("admin/posts")
    public List<EntityModel<PostResponse>> findKeysetPaginatedDeleted(
            @RequestParam(value = "karma_score", required = false) Long karmaScore,
            @RequestParam(value = "requested_username", required = false) String requestedUsername,
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,
            @RequestParam(value = "deleted", defaultValue = "false") boolean deleted)
            throws  InternalServerErrorException {

        List<PostVisibility> visibilities = new ArrayList<>();
        if (active) {
            visibilities.add(PostVisibility.ACTIVE);
        }
        if (hidden) {
            visibilities.add(PostVisibility.HIDDEN);
        }
        if (deleted) {
            visibilities.add(PostVisibility.DELETED);
        }

        return postService.findKeysetPaginated(karmaScore, requestedUsername, visibilities, size)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("guest/posts/{postId}/image")
    public ResponseEntity<byte[]> findImageById(@PathVariable Long postId) throws ImageNotFoundException {

        byte[] imageData = postService.findImageByPostId(postId);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

    @PostMapping("user/posts")
    public ResponseEntity<Void> create(
            @RequestPart("json_data") PostCreationRequest jsonData,
            @RequestPart("image") MultipartFile image) throws FileProcessingException {

        postService.create(jsonData, image);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/rate")
    public ResponseEntity<Void> rate(@PathVariable Long postId, @RequestParam("is_positive") boolean isPositive)
            throws KarmaScoreAlreadyExistsException, PostNotFoundException {

        postService.rate(postId, isPositive);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/unrate")
    public ResponseEntity<Void> unrate(@PathVariable Long postId) throws KarmaScoreNotFoundException, PostNotFoundException {

        postService.unrate(postId);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/hide")
    public ResponseEntity<Void> hideByUser(@PathVariable Long postId) throws AccessDeniedException, PostNotFoundException {

        postService.changeVisibilityByUser(postId, PostVisibility.HIDDEN);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/unhide")
    public ResponseEntity<Void> unhideByUser(@PathVariable Long postId) throws AccessDeniedException, PostNotFoundException {

        postService.changeVisibilityByUser(postId, PostVisibility.ACTIVE);
        return ResponseEntity.ok(null);
    }

    @PostMapping("mod/posts/{postId}/hide")
    public ResponseEntity<Void> hideByMod(@PathVariable Long postId) throws PostNotFoundException {

        postService.changeVisibility(postId, PostVisibility.HIDDEN);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/delete")
    public ResponseEntity<Void> deleteByUser(@PathVariable Long postId) throws AccessDeniedException, PostNotFoundException {

        postService.changeVisibilityByUser(postId, PostVisibility.DELETED);
        return ResponseEntity.ok(null);
    }

    @PostMapping("admin/posts/{postId}/delete")
    public ResponseEntity<Void> deleteByAdmin(@PathVariable Long postId) throws AccessDeniedException, PostNotFoundException {

        postService.changeVisibility(postId, PostVisibility.DELETED);
        return ResponseEntity.ok(null);
    }

    @PostMapping("admin/posts/{postId}/activate")
    public ResponseEntity<Void> activateByAdmin(@PathVariable Long postId) throws AccessDeniedException, PostNotFoundException {

        postService.changeVisibility(postId, PostVisibility.ACTIVE);
        return ResponseEntity.ok(null);
    }
}
