package com.msik404.karmaapp.post;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.msik404.karmaapp.karma.KarmaKey;
import com.msik404.karmaapp.karma.KarmaScoreService;
import com.msik404.karmaapp.karma.exception.KarmaScoreAlreadyExistsException;
import com.msik404.karmaapp.karma.exception.KarmaScoreNotFoundException;
import com.msik404.karmaapp.post.cache.PostRedisCache;
import com.msik404.karmaapp.post.dto.PostCreationRequest;
import com.msik404.karmaapp.post.dto.PostDto;
import com.msik404.karmaapp.post.dto.PostRatingResponse;
import com.msik404.karmaapp.post.exception.FileProcessingException;
import com.msik404.karmaapp.post.exception.ImageNotFoundException;
import com.msik404.karmaapp.post.exception.InternalServerErrorException;
import com.msik404.karmaapp.post.exception.PostNotFoundException;
import com.msik404.karmaapp.post.repository.PostRepository;
import com.msik404.karmaapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final int CACHED_POSTS_AMOUNT = 10_000;

    private final PostRepository repository;
    private final UserRepository userRepository;
    private final KarmaScoreService karmaScoreService;
    private final PostRedisCache cache;

    private static boolean isOnlyActive(@NonNull List<Visibility> visibilities) {
        return visibilities.size() == 1 && visibilities.contains(Visibility.ACTIVE);
    }

    private List<PostDto> updateCache() {
        List<PostDto> newValuesForCache = repository.findTopNPosts(CACHED_POSTS_AMOUNT, List.of(Visibility.ACTIVE));
        cache.reinitializeCache(newValuesForCache);
        return newValuesForCache;
    }

    private List<PostDto> findTopNHandler(int size, List<Visibility> visibilities) {

        List<PostDto> results;

        if (isOnlyActive(visibilities)) {
            if (cache.isEmpty()) {
                List<PostDto> newValuesForCache = updateCache();
                results = newValuesForCache.subList(0, size);
            } else {
                results = cache.findTopNCached(size).orElseGet(() -> repository.findTopNPosts(size, visibilities));
            }
        } else {
            results = repository.findTopNPosts(size, visibilities);
        }

        return results;
    }

    private List<PostDto> findNextNHandler(int size, List<Visibility> visibilities, long postId, long karmaScore) {

        List<PostDto> results;

        if (isOnlyActive(visibilities)) {
            if (cache.isEmpty()) {
                List<PostDto> newValuesForCache = updateCache();
                int firstSmallerElementIdx = 0;
                for (int i = 0; i < newValuesForCache.size(); i++) {
                    if (newValuesForCache.get(i).getKarmaScore() < karmaScore) {
                        firstSmallerElementIdx = i;
                        break;
                    }
                }
                results = newValuesForCache.subList(firstSmallerElementIdx, size);
            } else {
                results = cache.findNextNCached(size, karmaScore).orElseGet(
                        () -> repository.findNextNPosts(size, visibilities, postId, karmaScore));
            }

        } else {
            results = repository.findNextNPosts(size, visibilities, postId, karmaScore);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<PostDto> findPaginatedPosts(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable Long postId,
            @Nullable Long karmaScore,
            @Nullable String username)
            throws InternalServerErrorException {

        List<PostDto> results;

        if (karmaScore == null && username == null) {
            results = findTopNHandler(size, visibilities);
        } else if (karmaScore != null && username != null) {
            results = repository.findNextNPostsWithUsername(size, visibilities, karmaScore, username);
        } else if (karmaScore != null) { // username == null
            results = findNextNHandler(size, visibilities, postId, karmaScore);
        } else { // username != null and karmaScore == null
            results = repository.findTopNPostsWithUsername(size, visibilities, username);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<PostDto> findPaginatedOwnedPosts(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable Long karmaScore)
            throws InternalServerErrorException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();

        List<PostDto> results;

        if (karmaScore == null) {
            results = repository.findTopNWithUserId(size, visibilities, userId);
        } else {
            results = repository.findNextNWithUserId(size, visibilities, userId, karmaScore);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<PostRatingResponse> findPaginatedPostRatings(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable Long karmaScore,
            @Nullable String username)
            throws InternalServerErrorException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();

        List<PostRatingResponse> results;

        if (karmaScore == null && username == null) {
            results = repository.findTopNRatings(size, visibilities, userId);
        } else if (karmaScore != null && username != null) {
            results = repository.findNextNRatingsWithUsername(size, visibilities, userId, karmaScore, username);
        } else if (karmaScore != null) { // username == null
            results = repository.findNextNRatings(size, visibilities, userId, karmaScore);
        } else { // username != null and karmaScore == null
            results = repository.findTopNRatingsWithUsername(size, visibilities, userId, username);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public byte[] findImageByPostId(long postId) throws ImageNotFoundException {

        return cache.getCachedImage(postId).orElseGet(() -> {

            Optional<PostImageDataProjection> optionalPostImageDataProjection = repository.findImageById(postId);

            PostImageDataProjection postImageDataProjection = optionalPostImageDataProjection
                    .orElseThrow(ImageNotFoundException::new);

            byte[] imageData = postImageDataProjection.getImageData();
            if (imageData.length == 0) {
                throw new ImageNotFoundException();
            }
            cache.cacheImage(postId, imageData);
            return imageData;
        });
    }

    @Transactional
    public void create(@NonNull PostCreationRequest request, @NonNull MultipartFile image) throws FileProcessingException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();

        var newPost = Post.builder()
                .headline(request.getHeadline())
                .text(request.getText())
                .karmaScore(0L)
                .visibility(Visibility.ACTIVE)
                .user(userRepository.getReferenceById(userId));

        try {
            if (!image.isEmpty()) {
                // Load the image from the multipart file
                var bufferedImage = ImageIO.read(image.getInputStream());
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

        Post persistedPost = repository.save(newPost.build());
        cache.cacheImage(persistedPost.getId(), persistedPost.getImageData());
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
            boolean isOldRatingPositive = karmaScore.getIsPositive();
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

        int rowsAffected = repository.addKarmaScoreToPost(postId, delta);
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }

        cache.updateKarmaScoreIfPresent(postId, (double) delta);
    }

    /**
     * @param postId Long id of post
     * @throws KarmaScoreNotFoundException This exception is thrown when KarmaScore entity is not found
     */
    @Transactional
    public void unrate(long postId) throws KarmaScoreNotFoundException, PostNotFoundException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();

        var karmaKey = new KarmaKey(userId, postId);
        var karmaScore = karmaScoreService.findById(karmaKey);

        long delta = karmaScore.getIsPositive() ? -1L : 1L;

        int rowsAffected = repository.addKarmaScoreToPost(postId, delta);
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }

        karmaScoreService.deleteById(karmaKey);

        cache.updateKarmaScoreIfPresent(postId, (double) delta);
    }

    @Transactional
    public void changeOwnedPostVisibility(long postId, @NonNull Visibility visibility)
            throws AccessDeniedException, PostNotFoundException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();

        var optionalPost = repository.findById(postId);
        optionalPost.ifPresentOrElse(
                post -> {
                    // Id is lazy loaded. User can modify only his own visibility and can't change deleted state.
                    if (!post.getUser().getId().equals(userId) || post.getVisibility().equals(Visibility.DELETED)) {
                        throw new AccessDeniedException("Access denied");
                    }
                    post.setVisibility(visibility);
                    repository.save(post);
                    if (!visibility.equals(Visibility.ACTIVE)) {
                        cache.deleteFromCache(postId);
                    }
                },
                PostNotFoundException::new
        );
    }

    @Transactional
    public void changeVisibility(long postId, @NonNull Visibility visibility) throws PostNotFoundException {
        int rowsAffected = repository.changeVisibilityById(postId, visibility);
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }
        if (!visibility.equals(Visibility.ACTIVE)) {
            cache.deleteFromCache(postId);
        }
    }
}
