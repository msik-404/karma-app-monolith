package com.msik404.karmaapp.post.repository;

import java.util.ArrayList;
import java.util.List;

import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.PostVisibility;
import com.msik404.karmaapp.post.dto.PostJoinedDto;
import com.msik404.karmaapp.post.exception.InternalServerErrorException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final EntityManager entityManager;
    private final CriteriaBuilder cb;

    public PostRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.cb = entityManager.getCriteriaBuilder();
    }

    /**
     * @param size                Amount of posts that should be returned from db.
     * @param karmaScore          If not null, acts as pagination criterion,
     *                            results contain 'size' amount of posts with karma score lower than karmaScore.
     *                            If null, results contain top 'size' amount of posts based on karma score.
     * @param authenticatedUserId If not null, result contains information about which posts has been rated by authenticated user.
     *                            if null, field for this information is null.
     * @param username            if not null, results will contain only posts by user with this username.
     *                            if null, posts by all users are in results.
     * @param visibilities        if not empty, results will contain posts with visibilities present in visibilities.
     *                            if empty, posts with all visibilities will be in results.
     * @return list of paginated posts
     * @throws InternalServerErrorException is thrown when query could not be performed
     */
    public List<PostJoinedDto> findKeysetPaginated(
            int size,
            @Nullable Long karmaScore,
            @Nullable Long authenticatedUserId,
            @Nullable String username,
            @NonNull List<PostVisibility> visibilities)
            throws InternalServerErrorException {

        var criteriaQuery = cb.createQuery(PostJoinedDto.class);
        var postRoot = criteriaQuery.from(Post.class);
        var userJoin = postRoot.join("user");

        Expression<Boolean> wasRatedByAuthenticatedUserPositively = cb.nullLiteral(Boolean.class);
        if (authenticatedUserId != null) {
            var karmaScoreJoin = postRoot.join("karmaScores", JoinType.LEFT);
            karmaScoreJoin.on(
                    cb.equal(karmaScoreJoin.get("user").get("id"), authenticatedUserId)
            );
            wasRatedByAuthenticatedUserPositively = karmaScoreJoin.get("isPositive");
        }

        criteriaQuery.select(
                cb.construct(
                        PostJoinedDto.class,
                        postRoot.get("id"),
                        postRoot.get("user").get("id"),
                        userJoin.get("username"),
                        postRoot.get("headline"),
                        postRoot.get("text"),
                        postRoot.get("karmaScore"),
                        postRoot.get("visibility"),
                        wasRatedByAuthenticatedUserPositively
                )
        );

        var predicates = new ArrayList<Predicate>();

        if (!visibilities.isEmpty()) {
            predicates.add(postRoot.get("visibility").in(visibilities));
        }
        if (karmaScore != null) {
            predicates.add(cb.lessThan(postRoot.get("karmaScore"), karmaScore));
        }
        if (username != null) {
            predicates.add(cb.equal(userJoin.get("username"), username));
        }
        criteriaQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        criteriaQuery.orderBy(
                cb.desc(postRoot.get("karmaScore")),
                cb.asc(postRoot.get("id")));

        final int offset = 0;
        try {
            return entityManager.createQuery(criteriaQuery)
                    .setFirstResult(offset)
                    .setMaxResults(size)
                    .getResultList();
        } catch (RuntimeException ex) {
            throw new InternalServerErrorException("Could not get posts from database for some reason.");
        }
    }

    @Override
    public byte[] findImageById(long postId) throws InternalServerErrorException {
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        var query = criteriaBuilder.createQuery(byte[].class);
        var root = query.from(Post.class);

        query.select(root.get("imageData"))
                .where(criteriaBuilder.equal(root.get("id"), postId));

        byte[] result;
        try {
            result = entityManager.createQuery(query).getSingleResult();
        } catch (NoResultException ex) {
            result = new byte[0];
        } catch (RuntimeException ex) {
            throw new InternalServerErrorException("Could not get requested image from database for some reason.");
        }
        return result;
    }

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

}
