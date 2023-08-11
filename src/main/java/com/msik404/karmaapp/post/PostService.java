package com.msik404.karmaapp.post;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.msik404.karmaapp.karma.KarmaKey;
import com.msik404.karmaapp.karma.KarmaScoreAlreadyExistsException;
import com.msik404.karmaapp.karma.KarmaScoreNotFoundException;
import com.msik404.karmaapp.karma.KarmaScoreService;
import com.msik404.karmaapp.post.dto.PostCreationRequest;
import com.msik404.karmaapp.post.dto.PostJoinedDto;
import com.msik404.karmaapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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

    @Transactional(readOnly = true)
    public List<PostJoinedDto> findKeysetPaginated(
            Long karmaScore,
            String requestedUsername,
            List<PostVisibility> visibilities,
            int size)
            throws InternalServerErrorException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (authentication != null) {
            userId = (Long) authentication.getPrincipal();
        }
        return repository.findKeysetPaginated(karmaScore, userId, requestedUsername, visibilities, size);
    }

    @Transactional(readOnly = true)
    public byte[] findImageByPostId(Long postId) throws ImageNotFoundException {

        byte[] imageData = repository.findImageById(postId);
        if (imageData.length == 0) {
            throw new ImageNotFoundException();
        }
        return imageData;
    }

    @Transactional
    public void create(PostCreationRequest request, MultipartFile image) throws FileProcessingException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();

        var newPost = Post.builder()
                .headline(request.getHeadline())
                .text(request.getText())
                .karmaScore(0L)
                .visibility(PostVisibility.ACTIVE)
                .user(userRepository.getReferenceById(userId));

        try {
            if (image != null && !image.isEmpty()) {
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

        repository.save(newPost.build());
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

        long scoreDiff = isNewRatingPositive ? 1L : -1L;
        try {
            var karmaScore = karmaScoreService.findById(new KarmaKey(userId, postId));
            boolean isOldRatingPositive = karmaScore.getIsPositive();
            if (isOldRatingPositive == isNewRatingPositive) {
                throw new KarmaScoreAlreadyExistsException(String.format(
                        "This post has been already rated %s by you",
                        isNewRatingPositive ? "positively" : "negatively"));
            }
            karmaScore.setIsPositive(isNewRatingPositive);
            scoreDiff = isOldRatingPositive ? -2L : 2L;
        } catch (KarmaScoreNotFoundException ex) {
            karmaScoreService.create(userId, postId, isNewRatingPositive);
        }
        int rowsAffected = repository.addKarmaScoreToPost(postId, scoreDiff);
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
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

        var karmaKey = new KarmaKey(userId, postId);
        var karmaScore = karmaScoreService.findById(karmaKey);
        int rowsAffected = repository.addKarmaScoreToPost(postId, karmaScore.getIsPositive() ? -1L : 1L);
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }
        karmaScoreService.deleteById(karmaKey);
    }

    @Transactional
    public void changeVisibilityByUser(long postId, PostVisibility visibility)
            throws AccessDeniedException, PostNotFoundException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var userId = (long) authentication.getPrincipal();

        var optionalPost = repository.findById(postId);
        optionalPost.ifPresentOrElse(
                post -> {
                    // Id is lazy loaded. User can modify only his own visibility and can't change deleted state.
                    if (!post.getUser().getId().equals(userId) || post.getVisibility().equals(PostVisibility.DELETED)) {
                        throw new AccessDeniedException("Access denied");
                    }
                    post.setVisibility(visibility);
                    repository.save(post);
                },
                PostNotFoundException::new
        );
    }

    @Transactional
    public void changeVisibility(long postId, PostVisibility visibility) throws PostNotFoundException {
        int rowsAffected = repository.changeVisibilityById(postId, visibility);
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }
    }
}
