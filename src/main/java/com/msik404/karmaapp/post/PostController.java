package com.msik404.karmaapp.post;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.msik404.karmaapp.auth.exception.InsufficientRoleException;
import com.msik404.karmaapp.docs.KarmaAppEndpointDocs;
import com.msik404.karmaapp.docs.SwaggerConfiguration;
import com.msik404.karmaapp.position.ScrollPosition;
import com.msik404.karmaapp.post.dto.PostCreationRequest;
import com.msik404.karmaapp.post.dto.PostRatingResponse;
import com.msik404.karmaapp.post.dto.PostResponse;
import com.msik404.karmaapp.post.exception.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_FIND_PAGINATED_POSTS,
            description = KarmaAppEndpointDocs.OP_DESC_FIND_PAGINATED_POSTS
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_DESC_PAGINATED_POSTS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_PAGINATED_POSTS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @GetMapping("guest/posts")
    public List<EntityModel<PostResponse>> findPaginatedPosts(

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_SIZE)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_POST_ID)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_KARMA_SCORE)
            @RequestParam(value = "karma_score", required = false) Long karmaScore,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_USERNAME)
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

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_FIND_PAGINATED_OWNED_POSTS,
            description = KarmaAppEndpointDocs.OP_DESC_FIND_PAGINATED_OWNED_POSTS
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_DESC_PAGINATED_POSTS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_PAGINATED_POSTS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @GetMapping("user/posts")
    public List<EntityModel<PostResponse>> findPaginatedOwnedPosts(

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_SIZE)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_ACTIVE)
            @RequestParam(value = "active", defaultValue = "false") boolean active,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_HIDDEN)
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_POST_ID)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_KARMA_SCORE)
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

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_FIND_PAGINATED_POSTS_RATINGS,
            description = KarmaAppEndpointDocs.OP_DESC_FIND_PERSONAL_POST_RATINGS
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_DESC_PAGINATED_POSTS_RATINGS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostRatingResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_PAGINATED_POSTS_RATINGS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @GetMapping("user/posts/ratings")
    public List<PostRatingResponse> findPersonalPostRatings(

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_SIZE)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_POST_ID)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_KARMA_SCORE)
            @RequestParam(value = "karma_score", required = false) Long karmaScore,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_USERNAME)
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

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_FIND_PAGINATED_POSTS_FOR_MOD,
            description = KarmaAppEndpointDocs.OP_FIND_PAGINATED_POSTS_FOR_MOD
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_DESC_PAGINATED_POSTS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_PAGINATED_POSTS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @GetMapping("mod/posts")
    public List<EntityModel<PostResponse>> findPaginatedPostsForMod(

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_SIZE)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_ACTIVE)
            @RequestParam(value = "active", defaultValue = "false") boolean active,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_HIDDEN)
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_POST_ID)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_KARMA_SCORE)
            @RequestParam(value = "karma_score", required = false) Long karmaScore,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_USERNAME)
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

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_FIND_PERSONAL_POSTS_RATINGS_FOR_MOD,
            description = KarmaAppEndpointDocs.OP_DESC_FIND_PERSONAL_POST_RATINGS_FOR_MOD
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_DESC_PAGINATED_POSTS_RATINGS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostRatingResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_PAGINATED_POSTS_RATINGS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @GetMapping("mod/posts/ratings")
    public List<PostRatingResponse> findPersonalPostRatingsForMod(

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_SIZE)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_ACTIVE)
            @RequestParam(value = "active", defaultValue = "false") boolean active,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_HIDDEN)
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_POST_ID)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_KARMA_SCORE)
            @RequestParam(value = "karma_score", required = false) Long karmaScore,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_USERNAME)
            @RequestParam(value = "username", required = false) String username)
            throws InternalServerErrorException {

        List<Visibility> visibilities = createVisibilityList(active, hidden, false);

        ScrollPosition position = null;
        if (postId != null && karmaScore != null) {
            position = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPostRatings(size, visibilities, position, username);
    }

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_FIND_PAGINATED_POSTS_FOR_ADMIN,
            description = KarmaAppEndpointDocs.OP_DESC_FIND_PAGINATED_POSTS_FOR_ADMIN
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_DESC_PAGINATED_POSTS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_PAGINATED_POSTS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @GetMapping("admin/posts")
    public List<EntityModel<PostResponse>> findPaginatedPostsForAdmin(

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_SIZE)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_ACTIVE)
            @RequestParam(value = "active", defaultValue = "false") boolean active,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_HIDDEN)
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_DELETED)
            @RequestParam(value = "deleted", defaultValue = "false") boolean deleted,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_POST_ID)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_KARMA_SCORE)
            @RequestParam(value = "karma_score", required = false) Long karmaScore,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_USERNAME)
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

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_FIND_PERSONAL_POST_RATINGS_FOR_ADMIN,
            description = KarmaAppEndpointDocs.OP_DESC_FIND_PERSONAL_POST_RATINGS_FOR_ADMIN
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_DESC_PAGINATED_POSTS_RATINGS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostRatingResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_PAGINATED_POSTS_RATINGS,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @GetMapping("admin/posts/ratings")
    public List<PostRatingResponse> findPersonalPostRatingsForAdmin(

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_SIZE)
            @RequestParam(value = "size", defaultValue = "100") int size,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_ACTIVE)
            @RequestParam(value = "active", defaultValue = "false") boolean active,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_HIDDEN)
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_DELETED)
            @RequestParam(value = "deleted", defaultValue = "false") boolean deleted,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_POST_ID)
            @RequestParam(value = "post_id", required = false) Long postId,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_KARMA_SCORE)
            @RequestParam(value = "karma_score", required = false) Long karmaScore,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_USERNAME)
            @RequestParam(value = "username", required = false) String username)
            throws InternalServerErrorException {

        List<Visibility> visibilities = createVisibilityList(active, hidden, deleted);

        ScrollPosition position = null;
        if (postId != null && karmaScore != null) {
            position = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPostRatings(size, visibilities, position, username);
    }

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_FIND_IMAGE_BY_ID,
            description = KarmaAppEndpointDocs.OP_DESC_FIND_IMAGE_BY_ID
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_DESC_FIND_IMAGE,
                    content = {@Content(
                            mediaType = MediaType.IMAGE_JPEG_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = ImageNotFoundException.ERROR_MESSAGE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @GetMapping("guest/posts/{postId}/image")
    public ResponseEntity<byte[]> findImageById(
            @Parameter(description = KarmaAppEndpointDocs.PATH_DESC_POST_ID)
            @PathVariable Long postId
    ) throws ImageNotFoundException {

        byte[] imageData = postService.findImageByPostId(postId);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_CREATE_POST,
            description = KarmaAppEndpointDocs.OP_DESC_CREATE_POST
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_CREATE_POST
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = FileProcessingException.ERROR_MESSAGE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @PostMapping(value = "user/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> create(

            @Parameter(
                    description = KarmaAppEndpointDocs.PARAM_DESC_POST_CREATION_REQUEST,
                    required = true,
                    content = @Content(
                            encoding = @Encoding(
                                    name = "postCreationRequest",
                                    contentType = MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            )
            @Valid @RequestPart(value = "postCreationRequest") PostCreationRequest jsonData,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_IMAGE_DATA)
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws FileProcessingException {

        postService.create(jsonData, image);
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_RATE_POST,
            description = KarmaAppEndpointDocs.OP_DESC_RATE_POST
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_RATE_POST
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = PostNotFoundException.ERROR_MESSAGE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_RATE_POST,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @PostMapping("user/posts/{postId}/rate")
    public ResponseEntity<Void> rate(

            @Parameter(description = KarmaAppEndpointDocs.PATH_DESC_POST_ID)
            @PathVariable Long postId,

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_IS_POSITIVE)
            @RequestParam("is_positive") boolean isPositive
    ) throws InternalServerErrorException, PostNotFoundException {

        postService.rate(postId, isPositive);
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_UNRATE_POST,
            description = KarmaAppEndpointDocs.OP_DESC_UNRATE_POST
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_UNRATE_POST
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = PostNotFoundException.ERROR_MESSAGE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_UNRATE_POST,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @PostMapping("user/posts/{postId}/unrate")
    public ResponseEntity<Void> unrate(

            @Parameter(description = KarmaAppEndpointDocs.PATH_DESC_POST_ID)
            @PathVariable Long postId
    ) throws InternalServerErrorException, PostNotFoundException {

        postService.unrate(postId);
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_HIDE_POST_BY_USER,
            description = KarmaAppEndpointDocs.OP_DESC_HIDE_POST_BY_USER
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_HIDE_POST
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = KarmaAppEndpointDocs.RESP_UNAUTH_DESC_CHANGE_POST_VISIBILITY,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = PostNotFoundException.ERROR_MESSAGE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_CHANGE_POST_VISIBILITY,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @PostMapping("user/posts/{postId}/hide")
    public ResponseEntity<Void> hideByUser(

            @Parameter(description = KarmaAppEndpointDocs.PATH_DESC_POST_ID)
            @PathVariable Long postId
    ) throws InternalServerErrorException, InsufficientRoleException, PostNotFoundOrClientIsNotOwnerException,
            PostNotFoundException {

        postService.changeOwnedPostVisibility(postId, Visibility.HIDDEN);
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_UNHIDE_POST_BY_USER,
            description = KarmaAppEndpointDocs.OP_DESC_UNHIDE_POST_BY_USER
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_ACTIVATE_POST
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = KarmaAppEndpointDocs.RESP_UNAUTH_DESC_CHANGE_POST_VISIBILITY,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = PostNotFoundException.ERROR_MESSAGE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_CHANGE_POST_VISIBILITY,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @PostMapping("user/posts/{postId}/unhide")
    public ResponseEntity<Void> unhideByUser(

            @Parameter(description = KarmaAppEndpointDocs.PATH_DESC_POST_ID)
            @PathVariable Long postId
    ) throws InternalServerErrorException, InsufficientRoleException, PostNotFoundOrClientIsNotOwnerException,
            PostNotFoundException {

        postService.changeOwnedPostVisibility(postId, Visibility.ACTIVE);
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_HIDE_POST_BY_MOD,
            description = KarmaAppEndpointDocs.OP_DESC_HIDE_POST_BY_MOD
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_HIDE_POST
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = KarmaAppEndpointDocs.RESP_UNAUTH_DESC_CHANGE_POST_VISIBILITY,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = PostNotFoundException.ERROR_MESSAGE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_CHANGE_POST_VISIBILITY,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @PostMapping("mod/posts/{postId}/hide")
    public ResponseEntity<Void> hideByMod(

            @Parameter(description = KarmaAppEndpointDocs.PATH_DESC_POST_ID)
            @PathVariable Long postId
    ) throws InternalServerErrorException, InsufficientRoleException, PostNotFoundException {

        postService.changeVisibility(postId, Visibility.HIDDEN);
        return ResponseEntity.ok(null);
    }


    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_DELETE_POST_BY_USER,
            description = KarmaAppEndpointDocs.OP_DESC_DELETE_POST_BY_USER
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_DELETE_POST
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = KarmaAppEndpointDocs.RESP_UNAUTH_DESC_CHANGE_POST_VISIBILITY,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = PostNotFoundException.ERROR_MESSAGE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_CHANGE_POST_VISIBILITY,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @PostMapping("user/posts/{postId}/delete")
    public ResponseEntity<Void> deleteByUser(

            @Parameter(description = KarmaAppEndpointDocs.PATH_DESC_POST_ID)
            @PathVariable Long postId
    ) throws InternalServerErrorException, InsufficientRoleException, PostNotFoundOrClientIsNotOwnerException,
            PostNotFoundException {

        postService.changeOwnedPostVisibility(postId, Visibility.DELETED);
        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_DELETE_POST_BY_ADMIN,
            description = KarmaAppEndpointDocs.OP_DESC_DELETE_POST_BY_ADMIN
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_DELETE_POST
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = KarmaAppEndpointDocs.RESP_UNAUTH_DESC_CHANGE_POST_VISIBILITY,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = PostNotFoundException.ERROR_MESSAGE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_CHANGE_POST_VISIBILITY,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @PostMapping("admin/posts/{postId}/delete")
    public ResponseEntity<Void> deleteByAdmin(

            @Parameter(description = KarmaAppEndpointDocs.PATH_DESC_POST_ID)
            @PathVariable Long postId
    ) throws InternalServerErrorException, InsufficientRoleException, PostNotFoundException {

        postService.changeVisibility(postId, Visibility.DELETED);
        return ResponseEntity.ok(null);
    }


    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_ACTIVATE_POST_BY_ADMIN,
            description = KarmaAppEndpointDocs.OP_DESC_ACTIVE_POST_BY_ADMIN
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_DELETE_POST
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = KarmaAppEndpointDocs.RESP_UNAUTH_DESC_CHANGE_POST_VISIBILITY,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = PostNotFoundException.ERROR_MESSAGE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = KarmaAppEndpointDocs.RESP_INTERNAL_DESC_CHANGE_POST_VISIBILITY,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
    })
    @SecurityRequirement(name = SwaggerConfiguration.AUTH)
    @PostMapping("admin/posts/{postId}/activate")
    public ResponseEntity<Void> activateByAdmin(

            @Parameter(description = KarmaAppEndpointDocs.PATH_DESC_POST_ID)
            @PathVariable Long postId
    ) throws InternalServerErrorException, InsufficientRoleException, PostNotFoundException {

        postService.changeVisibility(postId, Visibility.ACTIVE);
        return ResponseEntity.ok(null);
    }
}
