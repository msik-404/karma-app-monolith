package com.msik404.karmaapp.post;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import javax.imageio.ImageIO;

import com.msik404.karmaapp.auth.exception.InsufficientRoleException;
import com.msik404.karmaapp.karma.KarmaKey;
import com.msik404.karmaapp.karma.KarmaScoreService;
import com.msik404.karmaapp.karma.exception.KarmaScoreNotFoundException;
import com.msik404.karmaapp.position.ScrollPosition;
import com.msik404.karmaapp.post.cache.PostRedisCache;
import com.msik404.karmaapp.post.cache.PostRedisCacheHandlerService;
import com.msik404.karmaapp.post.dto.*;
import com.msik404.karmaapp.post.exception.*;
import com.msik404.karmaapp.post.repository.PostRepository;
import com.msik404.karmaapp.user.Role;
import com.msik404.karmaapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
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
    @NonNull
    public List<PostDto> findPaginatedPosts(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable ScrollPosition position,
            @Nullable String creatorUsername)
            throws InternalServerErrorException {

        List<PostDto> results;

        if (creatorUsername == null) {
            if (position == null) {
                results = cacheHandler.findTopNHandler(size, visibilities);
            } else {
                results = cacheHandler.findNextNHandler(size, visibilities, position);
            }
        } else {
            if (position == null) {
                results = repository.findTopNPostsWithUsername(size, visibilities, creatorUsername);
            } else {
                results = repository.findNextNPostsWithUsername(size, visibilities, position, creatorUsername);
            }
        }

        return results;
    }

    @Transactional(readOnly = true)
    @NonNull
    public List<PostDto> findPaginatedOwnedPosts(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable ScrollPosition position)
            throws InternalServerErrorException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (long) authentication.getPrincipal();

        List<PostDto> results;

        if (position == null) {
            results = repository.findTopNWithUserId(size, visibilities, userId);
        } else {
            results = repository.findNextNWithUserId(size, visibilities, userId, position);
        }

        return results;
    }

    @Transactional(readOnly = true)
    @NonNull
    public List<PostRatingResponse> findPaginatedPostRatings(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable ScrollPosition position,
            @Nullable String creatorUsername)
            throws InternalServerErrorException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var clientId = (long) authentication.getPrincipal();

        List<PostRatingResponse> results;

        if (creatorUsername == null) {
            if (position == null) {
                results = repository.findTopNRatings(size, visibilities, clientId);
            } else {
                results = repository.findNextNRatings(size, visibilities, clientId, position);
            }
        } else {
            if (position == null) {
                results = repository.findTopNRatingsWithUsername(size, visibilities, clientId, creatorUsername);
            } else {
                results = repository.findNextNRatingsWithUsername(
                        size, visibilities, clientId, position, creatorUsername);
            }
        }

        return results;
    }

    @Transactional(readOnly = true)
    @NonNull
    public byte[] findImageByPostId(long postId) throws ImageNotFoundException {

        return cache.getCachedImage(postId).orElseGet(() -> {

            Optional<ImageOnlyDto> optionalPostImageDataProjection = repository.findImageById(postId);

            ImageOnlyDto postImageDataProjection = optionalPostImageDataProjection
                    .orElseThrow(ImageNotFoundException::new);

            byte[] imageData = postImageDataProjection.imageData();
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

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var clientId = (long) authentication.getPrincipal();

        var newPost = new Post(request.headline(), request.text(), userRepository.getReferenceById(clientId));

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
                newPost.setImageData(byteArrayOutputStream.toByteArray());
            }
        } catch (IOException ex) {
            throw new FileProcessingException();
        }

        Post persistedPost = repository.save(newPost);
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
            throws PostNotFoundException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (long) authentication.getPrincipal();

        long delta = isNewRatingPositive ? 1L : -1L;
        try {
            var karmaScore = karmaScoreService.findById(new KarmaKey(userId, postId));
            boolean isOldRatingPositive = karmaScore.isPositive();
            if (isOldRatingPositive == isNewRatingPositive) {
                return; // Requested visibility is already in place.
            }
            karmaScore.setPositive(isNewRatingPositive);
            delta = isOldRatingPositive ? -2L : 2L;
        } catch (KarmaScoreNotFoundException ex) {
            karmaScoreService.create(userId, postId, isNewRatingPositive);
        }

        int rowsAffected = repository.addKarmaScoreToPost(postId, delta);
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }

        OptionalDouble optionalNewKarmaScore = cache.updateKarmaScoreIfPresent(postId, (double) delta);
        if (optionalNewKarmaScore.isEmpty()) { // this means that this post is not cached
            cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
        }
    }

    /**
     * @param postId Long id of post
     * @throws KarmaScoreNotFoundException This exception is thrown when KarmaScore entity is not found
     */
    @Transactional
    @NonNull
    public void unrate(long postId) throws KarmaScoreNotFoundException, PostNotFoundException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (long) authentication.getPrincipal();

        var karmaKey = new KarmaKey(userId, postId);
        var karmaScore = karmaScoreService.findById(karmaKey);

        long delta = karmaScore.isPositive() ? -1L : 1L;

        int rowsAffected = repository.addKarmaScoreToPost(postId, delta);
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }

        karmaScoreService.deleteById(karmaKey);

        OptionalDouble optionalNewKarmaScore = cache.updateKarmaScoreIfPresent(postId, (double) delta);
        if (optionalNewKarmaScore.isEmpty()) { // this means that this post is not cached
            cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
        }
    }

    @Transactional(readOnly = true)
    @NonNull
    public Visibility findVisibility(long postId) throws PostNotFoundException {

        Optional<VisibilityOnlyDto> optionalVisibility = repository.findVisibilityById(postId);
        if (optionalVisibility.isEmpty()) {
            throw new PostNotFoundException();
        }
        return optionalVisibility.get().visibility();
    }

    @Transactional
    @NonNull
    public void changeVisibility(
            long postId,
            @NonNull Visibility visibility
    ) throws PostNotFoundException, InsufficientRoleException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> clientAuthorities = authentication.getAuthorities();
        boolean isAdmin = clientAuthorities.contains(new SimpleGrantedAuthority(Role.ADMIN.name()));

        if (!isAdmin) {
            Visibility persistedVisibility = findVisibility(postId);
            if (persistedVisibility.equals(Visibility.DELETED)) {
                throw new InsufficientRoleException(
                        "Access denied. You must be admin to change deleted post status to hidden status."
                );
            }
        }

        int rowsAffected = repository.changeVisibilityById(postId, visibility);
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }

        if (visibility.equals(Visibility.ACTIVE)) { // if this post was made active it might have high enough karma score
            cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
        } else {
            cache.deletePostFromCache(postId);
        }
    }

    @Transactional
    @NonNull
    public void changeOwnedPostVisibility(long postId, @NonNull Visibility visibility)
            throws PostNotFoundException, PostNotFoundOrClientIsNotOwnerException, InsufficientRoleException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var clientId = (long) authentication.getPrincipal();

        var optionalPostDtoWithImageData = repository.findPostDtoWithImageDataByIdAndUserId(postId, clientId);
        optionalPostDtoWithImageData.ifPresentOrElse(
                post -> {
                    boolean isVisibilityDeleted = post.postDto().getVisibility().equals(Visibility.DELETED);
                    boolean isUserAdmin = authentication.getAuthorities()
                            .contains(new SimpleGrantedAuthority(Role.ADMIN.name()));

                    if (isVisibilityDeleted && !isUserAdmin) {
                        throw new InsufficientRoleException(
                                "Access denied. You must be Admin to activate deleted post."
                        );
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
                    throw new PostNotFoundOrClientIsNotOwnerException();
                }
        );
    }
}
