package com.msik404.karmaapp.post;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import javax.imageio.ImageIO;

import com.msik404.karmaapp.karma.KarmaKey;
import com.msik404.karmaapp.karma.KarmaScoreService;
import com.msik404.karmaapp.karma.exception.KarmaScoreAlreadyExistsException;
import com.msik404.karmaapp.karma.exception.KarmaScoreNotFoundException;
import com.msik404.karmaapp.position.ScrollPosition;
import com.msik404.karmaapp.post.cache.PostRedisCache;
import com.msik404.karmaapp.post.cache.PostRedisCacheHandlerService;
import com.msik404.karmaapp.post.dto.PostCreationRequest;
import com.msik404.karmaapp.post.dto.PostDto;
import com.msik404.karmaapp.post.dto.PostRatingResponse;
import com.msik404.karmaapp.post.exception.FileProcessingException;
import com.msik404.karmaapp.post.exception.ImageNotFoundException;
import com.msik404.karmaapp.post.exception.InternalServerErrorException;
import com.msik404.karmaapp.post.exception.PostNotFoundException;
import com.msik404.karmaapp.post.repository.PostRepository;
import com.msik404.karmaapp.user.Role;
import com.msik404.karmaapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository repository;
    private final UserRepository userRepository;
    private final KarmaScoreService karmaScoreService;
    private final PostRedisCache cache;
    private final PostRedisCacheHandlerService cacheHandler;

    @Transactional(readOnly = true)
    public List<PostDto> findPaginatedPosts(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable ScrollPosition position,
            @Nullable String username)
            throws InternalServerErrorException {

        List<PostDto> results;

        if (position == null && username == null) {
            results = cacheHandler.findTopNHandler(size, visibilities);
        } else if (position != null && username != null) {
            results = repository.findNextNPostsWithUsername(size, visibilities, position, username);
        } else if (position != null) { // username == null
            results = cacheHandler.findNextNHandler(size, visibilities, position);
        } else { // username != null and pagination == null
            results = repository.findTopNPostsWithUsername(size, visibilities, username);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<PostDto> findPaginatedOwnedPosts(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable ScrollPosition position)
            throws InternalServerErrorException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();

        List<PostDto> results;

        if (position == null) {
            results = repository.findTopNWithUserId(size, visibilities, userId);
        } else {
            results = repository.findNextNWithUserId(size, visibilities, userId, position);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<PostRatingResponse> findPaginatedPostRatings(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable ScrollPosition position,
            @Nullable String username)
            throws InternalServerErrorException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();

        List<PostRatingResponse> results;

        if (position == null && username == null) {
            results = repository.findTopNRatings(size, visibilities, userId);
        } else if (position != null && username != null) {
            results = repository.findNextNRatingsWithUsername(size, visibilities, userId, position, username);
        } else if (position != null) { // username == null
            results = repository.findNextNRatings(size, visibilities, userId, position);
        } else { // username != null and pagination == null
            results = repository.findTopNRatingsWithUsername(size, visibilities, userId, username);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public byte[] findImageByPostId(long postId) throws ImageNotFoundException {

        return cache.getCachedImage(postId).orElseGet(() -> {

            final Optional<PostImageDataProjection> optionalPostImageDataProjection = repository.findImageById(postId);

            final PostImageDataProjection postImageDataProjection = optionalPostImageDataProjection
                    .orElseThrow(ImageNotFoundException::new);

            final byte[] imageData = postImageDataProjection.getImageData();
            if (imageData.length == 0) {
                throw new ImageNotFoundException();
            }
            cache.cacheImage(postId, imageData);
            return imageData;
        });
    }

    @Transactional
    public void create(
            @NonNull PostCreationRequest request,
            @NonNull MultipartFile image) throws FileProcessingException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();

        var newPost = Post.builder()
                .headline(request.headline())
                .text(request.text())
                .karmaScore(0L)
                .visibility(Visibility.ACTIVE)
                .user(userRepository.getReferenceById(userId));

        try {
            if (!image.isEmpty()) {
                // Load the image from the multipart file
                var bufferedImage = ImageIO.read(image.getInputStream());
                if (bufferedImage == null) {
                    throw new FileProcessingException();
                }
                // Create a ByteArrayOutputStream to hold the compressed image data
                var byteArrayOutputStream = new ByteArrayOutputStream();
                // Write the compressed image data to the ByteArrayOutputStream
                ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream);
                // Set the compressed image data to the newPost
                newPost.imageData(byteArrayOutputStream.toByteArray());
            }
        } catch (IOException ex) {
            throw new FileProcessingException();
        }

        final Post persistedPost = repository.save(newPost.build());
        if (persistedPost.getImageData() != null) {
            cache.cacheImage(persistedPost.getId(), persistedPost.getImageData());
        }
    }

    /**
     * This method might perform one or three queries in transaction, possible results:
     * 1) create new KarmaScore and modify Post entity karma score value, three queries
     * 2) modify KarmaScore isPositive field and modify Post entity karma score value, three queries
     * 3) throw exception if required action result is already the current state, single query
     *
     * @param postId              Long id of post whose score will be changed
     * @param isNewRatingPositive boolean value indicating whether to change to positive or negative
     */
    @Transactional
    public void rate(long postId, boolean isNewRatingPositive)
            throws KarmaScoreAlreadyExistsException, PostNotFoundException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();

        long delta = isNewRatingPositive ? 1L : -1L;
        try {
            var karmaScore = karmaScoreService.findById(new KarmaKey(userId, postId));
            final boolean isOldRatingPositive = karmaScore.getIsPositive();
            if (isOldRatingPositive == isNewRatingPositive) {
                throw new KarmaScoreAlreadyExistsException(String.format(
                        "This post has been already rated %s by you",
                        isNewRatingPositive ? "positively" : "negatively"));
            }
            karmaScore.setIsPositive(isNewRatingPositive);
            delta = isOldRatingPositive ? -2L : 2L;
        } catch (KarmaScoreNotFoundException ex) {
            karmaScoreService.create(userId, postId, isNewRatingPositive);
        }

        final int rowsAffected = repository.addKarmaScoreToPost(postId, delta);
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }

        final OptionalDouble optionalNewKarmaScore = cache.updateKarmaScoreIfPresent(postId, (double) delta);
        if (optionalNewKarmaScore.isEmpty()) { // this means that this post is not cached
            cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
        }
    }

    /**
     * @param postId Long id of post
     * @throws KarmaScoreNotFoundException This exception is thrown when KarmaScore entity is not found
     */
    @Transactional
    public void unrate(long postId) throws KarmaScoreNotFoundException, PostNotFoundException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();

        final var karmaKey = new KarmaKey(userId, postId);
        final var karmaScore = karmaScoreService.findById(karmaKey);

        final long delta = karmaScore.getIsPositive() ? -1L : 1L;

        final int rowsAffected = repository.addKarmaScoreToPost(postId, delta);
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }

        karmaScoreService.deleteById(karmaKey);

        OptionalDouble optionalNewKarmaScore = cache.updateKarmaScoreIfPresent(postId, (double) delta);
        if (optionalNewKarmaScore.isEmpty()) { // this means that this post is not cached
            cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
        }
    }

    @Transactional
    public void changeVisibility(long postId, @NonNull Visibility visibility) throws PostNotFoundException {
        final int rowsAffected = repository.changeVisibilityById(postId, visibility);
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }
        if (visibility.equals(Visibility.ACTIVE)) { // if this post was made active it might high enough karma score
            cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
        } else {
            cache.deletePostFromCache(postId);
        }
    }

    @Transactional
    public void changeOwnedPostVisibility(long postId, @NonNull Visibility visibility)
            throws AccessDeniedException, PostNotFoundException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();

        var optionalPostDtoWithImageData = repository.findPostDtoWithImageDataByIdAndUserId(postId, userId);
        optionalPostDtoWithImageData.ifPresentOrElse(
                post -> {
                    final boolean isVisibilityDeleted = post.getVisibility().equals(Visibility.DELETED);
                    final boolean isUserAdmin = authentication.getAuthorities().contains(
                            new SimpleGrantedAuthority(Role.ADMIN.name()));

                    if (isVisibilityDeleted && !isUserAdmin) {
                        throw new AccessDeniedException("Access denied");
                    }

                    repository.changeVisibilityById(postId, visibility);

                    // if this post was made active it might have high enough karma score
                    if (visibility.equals(Visibility.ACTIVE)) {
                        cacheHandler.loadToCacheIfKarmaScoreIsHighEnough(post);
                    } else {
                        cache.deletePostFromCache(postId);
                    }
                },
                () -> {
                    throw new PostNotFoundException();
                }
        );
    }
}
