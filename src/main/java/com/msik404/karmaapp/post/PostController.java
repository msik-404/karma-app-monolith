package com.msik404.karmaapp.post;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.msik404.karmaapp.KarmaAppEndpointDocs;
import com.msik404.karmaapp.auth.exception.InsufficientRoleException;
import com.msik404.karmaapp.karma.exception.KarmaScoreNotFoundException;
import com.msik404.karmaapp.position.ScrollPosition;
import com.msik404.karmaapp.post.dto.PostCreationRequest;
import com.msik404.karmaapp.post.dto.PostRatingResponse;
import com.msik404.karmaapp.post.dto.PostResponse;
import com.msik404.karmaapp.post.exception.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostResponseModelAssembler assembler;

    @Operation(summary = KarmaAppEndpointDocs.FIND_PAGINATED_POSTS_DESC)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.PAGINATED_POSTS_RETURN_DESC,
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.PAGINATED_POSTS_INTERNAL_ERROR_DESC,
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @GetMapping("guest/posts")
    public List<EntityModel<PostResponse>> findPaginatedPosts(

            @Parameter(description = KarmaAppEndpointDocs.SIZE_DESC)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.POST_ID_DESC)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.KARMA_SCORE_DESC)
            @RequestParam(value = "karma_score", required = false) Long karmaScore,

            @Parameter(description = KarmaAppEndpointDocs.USERNAME_DESC)
            @RequestParam(value = "username", required = false) String username)

            throws InternalServerErrorException {

        ScrollPosition position = null;
        if (postId != null && karmaScore != null) {
            position = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPosts(size, List.of(Visibility.ACTIVE), position, username)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
    }

    @Operation(summary = KarmaAppEndpointDocs.FIND_PAGINATED_OWNED_POSTS_BASE_DESC)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.PAGINATED_POSTS_RETURN_DESC,
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.PAGINATED_POSTS_INTERNAL_ERROR_DESC,
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @GetMapping("user/posts")
    public List<EntityModel<PostResponse>> findPaginatedOwnedPosts(

            @Parameter(description = KarmaAppEndpointDocs.SIZE_DESC)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.ACTIVE_DESC)
            @RequestParam(value = "active", defaultValue = "false") boolean active,

            @Parameter(description = KarmaAppEndpointDocs.HIDDEN_DESC)
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,

            @Parameter(description = KarmaAppEndpointDocs.POST_ID_DESC)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.KARMA_SCORE_DESC)
            @RequestParam(value = "karma_score", required = false) Long karmaScore)
            throws InternalServerErrorException {

        List<Visibility> visibilities = createVisibilityList(active, hidden, false);

        ScrollPosition position = null;
        if (postId != null && karmaScore != null) {
            position = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedOwnedPosts(size, visibilities, position)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
    }

    @Operation(summary = KarmaAppEndpointDocs.FIND_PERSONAL_POST_RATINGS_BASE_DESC)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.PAGINATED_POSTS_RATINGS_RETURN_DESC,
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostRatingResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.PAGINATED_POSTS_RATINGS_INTERNAL_ERROR_DESC,
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @GetMapping("user/posts/ratings")
    public List<PostRatingResponse> findPersonalPostRatings(

            @Parameter(description = KarmaAppEndpointDocs.SIZE_DESC)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.POST_ID_DESC)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.KARMA_SCORE_DESC)
            @RequestParam(value = "karma_score", required = false) Long karmaScore,

            @Parameter(description = KarmaAppEndpointDocs.USERNAME_DESC)
            @RequestParam(value = "username", required = false) String username)
            throws InternalServerErrorException {

        ScrollPosition position = null;
        if (postId != null && karmaScore != null) {
            position = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPostRatings(size, List.of(Visibility.ACTIVE), position, username);
    }

    @NonNull
    private static List<Visibility> createVisibilityList(boolean active, boolean hidden, boolean deleted) {

        List<Visibility> visibilities = new ArrayList<>();

        if (active) {
            visibilities.add(Visibility.ACTIVE);
        }
        if (hidden) {
            visibilities.add(Visibility.HIDDEN);
        }
        if (deleted) {
            visibilities.add(Visibility.DELETED);
        }
        return visibilities;
    }

    @Operation(summary = KarmaAppEndpointDocs.FIND_PAGINATED_POSTS_FOR_MOD_DESC)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.PAGINATED_POSTS_RETURN_DESC,
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.PAGINATED_POSTS_INTERNAL_ERROR_DESC,
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @GetMapping("mod/posts")
    public List<EntityModel<PostResponse>> findPaginatedPostsForMod(

            @Parameter(description = KarmaAppEndpointDocs.SIZE_DESC)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.ACTIVE_DESC)
            @RequestParam(value = "active", defaultValue = "false") boolean active,

            @Parameter(description = KarmaAppEndpointDocs.HIDDEN_DESC)
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,

            @Parameter(description = KarmaAppEndpointDocs.POST_ID_DESC)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.KARMA_SCORE_DESC)
            @RequestParam(value = "karma_score", required = false) Long karmaScore,

            @Parameter(description = KarmaAppEndpointDocs.USERNAME_DESC)
            @RequestParam(value = "username", required = false) String username)
            throws InternalServerErrorException {

        List<Visibility> visibilities = createVisibilityList(active, hidden, false);

        ScrollPosition position = null;
        if (postId != null && karmaScore != null) {
            position = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPosts(size, visibilities, position, username)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
    }

    @Operation(summary = KarmaAppEndpointDocs.FIND_PERSONAL_POST_RATINGS_FOR_MOD_DESC)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.PAGINATED_POSTS_RATINGS_RETURN_DESC,
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostRatingResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.PAGINATED_POSTS_RATINGS_INTERNAL_ERROR_DESC,
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @GetMapping("mod/posts/ratings")
    public List<PostRatingResponse> findPersonalPostRatingsForMod(

            @Parameter(description = KarmaAppEndpointDocs.SIZE_DESC)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.ACTIVE_DESC)
            @RequestParam(value = "active", defaultValue = "false") boolean active,

            @Parameter(description = KarmaAppEndpointDocs.HIDDEN_DESC)
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,

            @Parameter(description = KarmaAppEndpointDocs.POST_ID_DESC)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.KARMA_SCORE_DESC)
            @RequestParam(value = "karma_score", required = false) Long karmaScore,

            @Parameter(description = KarmaAppEndpointDocs.USERNAME_DESC)
            @RequestParam(value = "username", required = false) String username)
            throws InternalServerErrorException {

        List<Visibility> visibilities = createVisibilityList(active, hidden, false);

        ScrollPosition position = null;
        if (postId != null && karmaScore != null) {
            position = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPostRatings(size, visibilities, position, username);
    }

    @Operation(summary = KarmaAppEndpointDocs.FIND_PAGINATED_POSTS_FOR_ADMIN_DESC)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returned paginated posts.",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not get posts ratings from the database for some reason.",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @GetMapping("admin/posts")
    public List<EntityModel<PostResponse>> findPaginatedPostsForAdmin(

            @Parameter(description = KarmaAppEndpointDocs.SIZE_DESC)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.ACTIVE_DESC)
            @RequestParam(value = "active", defaultValue = "false") boolean active,

            @Parameter(description = KarmaAppEndpointDocs.HIDDEN_DESC)
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,

            @Parameter(description = KarmaAppEndpointDocs.DELETED_DESC)
            @RequestParam(value = "deleted", defaultValue = "false") boolean deleted,

            @Parameter(description = KarmaAppEndpointDocs.POST_ID_DESC)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.KARMA_SCORE_DESC)
            @RequestParam(value = "karma_score", required = false) Long karmaScore,

            @Parameter(description = KarmaAppEndpointDocs.USERNAME_DESC)
            @RequestParam(value = "username", required = false) String username)
            throws InternalServerErrorException {

        List<Visibility> visibilities = createVisibilityList(active, hidden, deleted);

        ScrollPosition position = null;
        if (postId != null && karmaScore != null) {
            position = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPosts(size, visibilities, position, username)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
    }

    @Operation(summary = KarmaAppEndpointDocs.FIND_PERSONAL_POST_RATINGS_FOR_ADMIN_DESC)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.PAGINATED_POSTS_RATINGS_RETURN_DESC,
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostRatingResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.PAGINATED_POSTS_RATINGS_INTERNAL_ERROR_DESC,
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @GetMapping("admin/posts/ratings")
    public List<PostRatingResponse> findPersonalPostRatingsForAdmin(

            @Parameter(description = KarmaAppEndpointDocs.SIZE_DESC)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.ACTIVE_DESC)
            @RequestParam(value = "active", defaultValue = "false") boolean active,

            @Parameter(description = KarmaAppEndpointDocs.HIDDEN_DESC)
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,

            @Parameter(description = KarmaAppEndpointDocs.DELETED_DESC)
            @RequestParam(value = "deleted", defaultValue = "false") boolean deleted,

            @Parameter(description = KarmaAppEndpointDocs.POST_ID_DESC)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.KARMA_SCORE_DESC)
            @RequestParam(value = "karma_score", required = false) Long karmaScore,

            @Parameter(description = KarmaAppEndpointDocs.USERNAME_DESC)
            @RequestParam(value = "username", required = false) String username)
            throws InternalServerErrorException {

        List<Visibility> visibilities = createVisibilityList(active, hidden, deleted);

        ScrollPosition position = null;
        if (postId != null && karmaScore != null) {
            position = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPostRatings(size, visibilities, position, username);
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
            @Valid @RequestPart("json_data") PostCreationRequest jsonData,
            @RequestPart(value = "image", required = false) MultipartFile image) throws FileProcessingException {

        postService.create(jsonData, image);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/rate")
    public ResponseEntity<Void> rate(@PathVariable Long postId, @RequestParam("is_positive") boolean isPositive)
            throws InternalServerErrorException, PostNotFoundException {

        postService.rate(postId, isPositive);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/unrate")
    public ResponseEntity<Void> unrate(@PathVariable Long postId)
            throws InternalServerErrorException, KarmaScoreNotFoundException, PostNotFoundException {

        postService.unrate(postId);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/hide")
    public ResponseEntity<Void> hideByUser(
            @PathVariable Long postId
    ) throws InternalServerErrorException, InsufficientRoleException, PostNotFoundOrClientIsNotOwnerException,
            PostNotFoundException {

        postService.changeOwnedPostVisibility(postId, Visibility.HIDDEN);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/unhide")
    public ResponseEntity<Void> unhideByUser(
            @PathVariable Long postId
    ) throws InternalServerErrorException, InsufficientRoleException, PostNotFoundOrClientIsNotOwnerException,
            PostNotFoundException {

        postService.changeOwnedPostVisibility(postId, Visibility.ACTIVE);
        return ResponseEntity.ok(null);
    }

    @PostMapping("mod/posts/{postId}/hide")
    public ResponseEntity<Void> hideByMod(
            @PathVariable Long postId
    ) throws InternalServerErrorException, InsufficientRoleException, PostNotFoundException {

        postService.changeVisibility(postId, Visibility.HIDDEN);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/delete")
    public ResponseEntity<Void> deleteByUser(
            @PathVariable Long postId
    ) throws InternalServerErrorException, InsufficientRoleException, PostNotFoundOrClientIsNotOwnerException,
            PostNotFoundException {

        postService.changeOwnedPostVisibility(postId, Visibility.DELETED);
        return ResponseEntity.ok(null);
    }

    @PostMapping("admin/posts/{postId}/delete")
    public ResponseEntity<Void> deleteByAdmin(
            @PathVariable Long postId
    ) throws InternalServerErrorException, InsufficientRoleException, PostNotFoundException {

        postService.changeVisibility(postId, Visibility.DELETED);
        return ResponseEntity.ok(null);
    }

    @PostMapping("admin/posts/{postId}/activate")
    public ResponseEntity<Void> activateByAdmin(
            @PathVariable Long postId
    ) throws InternalServerErrorException, InsufficientRoleException, PostNotFoundException {

        postService.changeVisibility(postId, Visibility.ACTIVE);
        return ResponseEntity.ok(null);
    }
}
