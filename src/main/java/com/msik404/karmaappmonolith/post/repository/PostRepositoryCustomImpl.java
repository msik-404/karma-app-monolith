package com.msik404.karmaappmonolith.post.repository;

import java.util.List;

import com.msik404.karmaappmonolith.position.ScrollPosition;
import com.msik404.karmaappmonolith.post.Post;
import com.msik404.karmaappmonolith.post.Visibility;
import com.msik404.karmaappmonolith.post.dto.PostDto;
import com.msik404.karmaappmonolith.post.dto.PostRatingResponse;
import com.msik404.karmaappmonolith.post.exception.InternalServerErrorException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

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
            @NonNull List<Visibility> visibilities)
            throws InternalServerErrorException {

        var finder = new FindNPosts(entityManager, cb);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }

        return finder.execute(size);
    }

    @Override
    public List<PostDto> findNextNPosts(
            int size,
            @NonNull List<Visibility> visibilities,
            @NonNull ScrollPosition position
    ) throws InternalServerErrorException {

        var finder = new FindNPosts(entityManager, cb);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        finder.setPagination(position);

        return finder.execute(size);
    }

    @Override
    public List<PostDto> findTopNPostsWithUsername(
            int size,
            @NonNull List<Visibility> visibilities,
            @NonNull String username)
            throws InternalServerErrorException {

        var finder = new FindNPosts(entityManager, cb);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        finder.setUsernameEqual(username);

        return finder.execute(size);
    }

    @Override
    public List<PostDto> findNextNPostsWithUsername(
            int size,
            @NonNull List<Visibility> visibilities,
            @NonNull ScrollPosition position,
            @NonNull String username)
            throws InternalServerErrorException {

        var finder = new FindNPosts(entityManager, cb);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        finder.setPagination(position);
        finder.setUsernameEqual(username);

        return finder.execute(size);
    }

    @Override
    public List<PostDto> findTopNWithUserId(
            int size,
            @NonNull List<Visibility> visibilities,
            long userId
    ) throws InternalServerErrorException {

        var finder = new FindNPosts(entityManager, cb);
        finder.setVisibilitiesIn(visibilities);
        finder.setUserIdEqual(userId);

        return finder.execute(size);
    }

    @Override
    public List<PostDto> findNextNWithUserId(
            int size,
            @NonNull List<Visibility> visibilities,
            long userId,
            @NonNull ScrollPosition position
    ) throws InternalServerErrorException {

        var finder = new FindNPosts(entityManager, cb);
        finder.setVisibilitiesIn(visibilities);
        finder.setPagination(position);
        finder.setUserIdEqual(userId);

        return finder.execute(size);
    }

    @Override
    public List<PostRatingResponse> findTopNRatings(
            int size,
            @NonNull List<Visibility> visibilities,
            long userId)
            throws InternalServerErrorException {

        var finder = new FindNRatings(entityManager, cb, userId);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }

        return finder.execute(size);
    }

    @Override
    public List<PostRatingResponse> findNextNRatings(
            int size,
            @NonNull List<Visibility> visibilities,
            long userId,
            @NonNull ScrollPosition position)
            throws InternalServerErrorException {

        var finder = new FindNRatings(entityManager, cb, userId);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        finder.setPagination(position);

        return finder.execute(size);
    }

    @Override
    public List<PostRatingResponse> findTopNRatingsWithUsername(
            int size,
            @NonNull List<Visibility> visibilities,
            long userId,
            @NonNull String username)
            throws InternalServerErrorException {

        var finder = new FindNRatings(entityManager, cb, userId);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        finder.setUsernameEqual(username);

        return finder.execute(size);
    }

    @Override
    public List<PostRatingResponse> findNextNRatingsWithUsername(
            int size,
            @NonNull List<Visibility> visibilities,
            long userId,
            @NonNull ScrollPosition position,
            @NonNull String username)
            throws InternalServerErrorException {

        var finder = new FindNRatings(entityManager, cb, userId);
        if (!visibilities.isEmpty()) {
            finder.setVisibilitiesIn(visibilities);
        }
        finder.setPagination(position);
        finder.setUsernameEqual(username);

        return finder.execute(size);
    }

    @Override
    public int addKarmaScoreToPost(long postId, long value) throws InternalServerErrorException {

        var criteriaUpdate = cb.createCriteriaUpdate(Post.class);
        var root = criteriaUpdate.getRoot();

        Path<Long> karmaScorePath = root.get("karmaScore");

        criteriaUpdate.set(karmaScorePath, cb.sum(karmaScorePath, value));
        criteriaUpdate.where(cb.equal(root.get("id"), postId));

        try {
            return entityManager.createQuery(criteriaUpdate).executeUpdate();
        } catch (RuntimeException ex) {
            throw new InternalServerErrorException("Could not add karma score to post.");
        }
    }

    @Override
    public int changeVisibilityById(long postId, @NonNull Visibility visibility) throws InternalServerErrorException {

        var criteriaUpdate = cb.createCriteriaUpdate(Post.class);
        var root = criteriaUpdate.getRoot();

        criteriaUpdate.set(root.get("visibility"), visibility);
        criteriaUpdate.where(cb.equal(root.get("id"), postId));

        try {
            return entityManager.createQuery(criteriaUpdate).executeUpdate();
        } catch (RuntimeException ex) {
            throw new InternalServerErrorException("Could not change visibility of the post.");
        }
    }

}
