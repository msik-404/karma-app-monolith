package com.msik404.karmaapp.post;

import java.util.List;

import com.msik404.karmaapp.karma.KarmaKey;
import com.msik404.karmaapp.karma.KarmaScoreAlreadyExistsException;
import com.msik404.karmaapp.karma.KarmaScoreNotFoundException;
import com.msik404.karmaapp.karma.KarmaScoreService;
import com.msik404.karmaapp.post.dto.NewPostRequest;
import com.msik404.karmaapp.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository repository;
    private final UserRepository userRepository;
    private final KarmaScoreService karmaScoreService;

    public List<Post> findManyBykeysetPagination(Long postId, Long karmaScore, int size) {

        List<Post> pageContents;
        if (postId != null && karmaScore != null) {
            pageContents = repository.findTopNextN(postId, karmaScore, size);
        } else {
            pageContents = repository.findTopN(size);
        }
        return pageContents;
    }

    public void create(NewPostRequest request) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (long) authentication.getPrincipal();

        var newPost = Post.builder()
                .text(request.getText())
                .karmaScore(0L)
                .visibility(PostVisibility.ACTIVE)
                .user(userRepository.getReferenceById(userId))
                .build();

        repository.save(newPost);
    }

    /**
     * This method might perform one or three queries in transaction, possible results:
     * 1) create new KarmaScore and modify Post entity karma score value, three queries
     * 2) modify KarmaScore isPositive field and modify Post entity karma score value, three queries
     * 3) throw exception if required action result is already the current state, single query
     *
     * @param postId     Long id of post whose score will be changed
     * @param isPositive boolean value indicating whether to change to positive or negative
     */
    @Transactional
    public void rate(long postId, boolean isPositive) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (long) authentication.getPrincipal();

        long scoreDiff = isPositive ? 1L : -1L;
        try {
            var karmaScore = karmaScoreService.findById(new KarmaKey(userId, postId));
            boolean wasPositive = karmaScore.getIsPositive();
            if (wasPositive == isPositive) {
                var message = isPositive ? "positively" : "negatively";
                throw new KarmaScoreAlreadyExistsException("This post has been already rated " + message + "by you");
            }
            karmaScore.setIsPositive(isPositive);
            scoreDiff = wasPositive ? -2L : 2L;
        } catch (KarmaScoreNotFoundException ex) {
            karmaScoreService.create(userId, postId, isPositive);
        }
        repository.addKarmaScoreToPost(postId, scoreDiff);
    }

    /**
     * @param postId Long id of post
     * @throws KarmaScoreNotFoundException This exception is thrown when KarmaScore entity is not found
     */
    @Transactional
    public void unrate(long postId) throws KarmaScoreNotFoundException, PostNotFoundException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (long) authentication.getPrincipal();

        var karmaKey = new KarmaKey(userId, postId);
        var karmaScore = karmaScoreService.findById(karmaKey);
        repository.addKarmaScoreToPost(postId, karmaScore.getIsPositive() ? -1L : 1L);
        karmaScoreService.deleteById(karmaKey);
    }

    public void changeVisibilityByUser(long postId, PostVisibility visibility)
            throws AccessDeniedException, PostNotFoundException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (long) authentication.getPrincipal();

        var optionalPost = repository.findById(postId);
        optionalPost.ifPresentOrElse(
                post -> {
                    // Id is lazy loaded
                    if (!post.getUser().getId().equals(userId)) {
                        throw new AccessDeniedException("Access denied");
                    }
                    post.setVisibility(visibility);
                    repository.save(post);
                },
                PostNotFoundException::new
        );
    }

    public void changeVisibility(long postId, PostVisibility visibility) throws PostNotFoundException {
        repository.changeVisibilityById(postId, visibility);
    }

}
