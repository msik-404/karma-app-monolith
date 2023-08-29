package com.msik404.karmaapp.post.repository;

import java.util.List;

import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.PostVisibility;
import com.msik404.karmaapp.post.dto.PostDto;
import com.msik404.karmaapp.post.dto.PostRatingResponse;
import com.msik404.karmaapp.post.exception.InternalServerErrorException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

// TODO: write tests using h2 in memory database
@Repository
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final EntityManager entityManager;
    private final CriteriaBuilder cb;

    public PostRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.cb = entityManager.getCriteriaBuilder();
    }

    @Override
    public List<PostDto> findTopNPosts(
            int size,
            @NonNull List<PostVisibility> visibilities)
            throws InternalServerErrorException {

        var finder = new FindNPostJoined(entityManager, cb);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        return finder.execute(size);
    }

    @Override
    public List<PostDto> findNextNPosts(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long karmaScore
            ) throws InternalServerErrorException {

        var finder = new FindNPostJoined(entityManager, cb);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        finder.setKarmaScoreLessThan(karmaScore);
        return finder.execute(size);
    }

    @Override
    public List<PostDto> findTopNPostsWithUsername(
            int size,
            @NonNull List<PostVisibility> visibilities,
            @NonNull String username)
            throws InternalServerErrorException {

        var finder = new FindNPostJoined(entityManager, cb);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        finder.setUsernameEqual(username);
        return finder.execute(size);
    }

    @Override
    public List<PostDto> findNextNPostsWithUsername(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long karmaScore,
            @NonNull String username)
            throws InternalServerErrorException {

        var finder = new FindNPostJoined(entityManager, cb);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        finder.setKarmaScoreLessThan(karmaScore);
        finder.setUsernameEqual(username);
        return finder.execute(size);
    }

    @Override
    public List<PostRatingResponse> findTopNRatings(
            int size,
            @NonNull List<PostVisibility> visibilities, long userId)
            throws InternalServerErrorException {

        var finder = new FindNPostRating(entityManager, cb, userId);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        return finder.execute(size);
    }

    @Override
    public List<PostRatingResponse> findNextNRatings(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId,
            long karmaScore)
            throws InternalServerErrorException {

        var finder = new FindNPostRating(entityManager, cb, userId);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        finder.setKarmaScoreLessThan(karmaScore);
        return finder.execute(size);
    }

    @Override
    public List<PostRatingResponse> findTopNRatingsWithUsername(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId,
            @NonNull String username)
            throws InternalServerErrorException {

        var finder = new FindNPostRating(entityManager, cb, userId);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        finder.setUsernameEqual(username);
        return finder.execute(size);
    }

    @Override
    public List<PostRatingResponse> findNextNRatingsWithUsername(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId,
            long karmaScore,
            @NonNull String username)
            throws InternalServerErrorException {

        var finder = new FindNPostRating(entityManager, cb, userId);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        finder.setKarmaScoreLessThan(karmaScore);
        finder.setUsernameEqual(username);
        return finder.execute(size);
    }

    @Override
    public int addKarmaScoreToPost(long postId, long value) {

        var criteriaUpdate = cb.createCriteriaUpdate(Post.class);
        var root = criteriaUpdate.getRoot();

        Path<Long> karmaScorePath = root.get("karmaScore");

        criteriaUpdate.set(karmaScorePath, cb.sum(karmaScorePath, value));
        criteriaUpdate.where(cb.equal(root.get("id"), postId));

        return entityManager.createQuery(criteriaUpdate).executeUpdate();
    }

    @Override
    public int changeVisibilityById(long postId, @NonNull PostVisibility visibility) {

        var criteriaUpdate = cb.createCriteriaUpdate(Post.class);
        var root = criteriaUpdate.getRoot();

        criteriaUpdate.set(root.get("visibility"), visibility);
        criteriaUpdate.where(cb.equal(root.get("id"), postId));

        return entityManager.createQuery(criteriaUpdate).executeUpdate();
    }

    @Override
    public List<PostDto> findTopNWithUserId(int size, @NonNull List<PostVisibility> visibilities, long userId) {

        var finder = new FindNPostJoined(entityManager, cb);
        finder.setVisibilitiesIn(visibilities);
        finder.setUserIdEqual(userId);

        return finder.execute(size);
    }

    @Override
    public List<PostDto> findNextNWithUserId(
            int size,
            @NonNull List<PostVisibility> visibilities,
            long userId,
            long karmaScore) {

        var finder = new FindNPostJoined(entityManager, cb);
        finder.setVisibilitiesIn(visibilities);
        finder.setUserIdEqual(userId);
        finder.setKarmaScoreLessThan(karmaScore);

        return finder.execute(size);
    }

}
