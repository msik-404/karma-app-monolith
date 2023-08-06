package com.msik404.karmaapp.post;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
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
    public List<Post> findTopN(int size) {

        CriteriaQuery<Post> criteriaQuery = cb.createQuery(Post.class);
        Root<Post> root = criteriaQuery.from(Post.class);

        criteriaQuery.select(root);

        criteriaQuery.orderBy(
                cb.asc(root.get("id")),
                cb.desc(root.get("karmaScore")));

        final int offset = 0;
        return entityManager.createQuery(criteriaQuery).setFirstResult(offset).setMaxResults(size).getResultList();
    }

    @Override
    public List<Post> findTopNextN(@NonNull Long postId, @NonNull Long karmaScore, int size) {

        CriteriaQuery<Post> criteriaQuery = cb.createQuery(Post.class);
        Root<Post> root = criteriaQuery.from(Post.class);

        criteriaQuery.select(root);

        criteriaQuery.where(cb.and(
                cb.greaterThan(root.get("id"), postId),
                cb.lessThan(root.get("karmaScore"), karmaScore)));

        criteriaQuery.orderBy(
                cb.asc(root.get("id")),
                cb.desc(root.get("karmaScore")));

        final int offset = 0;
        return entityManager.createQuery(criteriaQuery).setFirstResult(offset).setMaxResults(size).getResultList();
    }

    public void addKarmaScoreToPost(@NonNull Long postId, @NonNull Long value) throws PostNotFoundException {

        CriteriaUpdate<Post> criteriaUpdate = cb.createCriteriaUpdate(Post.class);
        Root<Post> root = criteriaUpdate.getRoot();

        Path<Long> karmaScorePath = root.get("karmaScore");

        criteriaUpdate.set(karmaScorePath, cb.sum(karmaScorePath, value));
        criteriaUpdate.where(cb.equal(root.get("id"), postId));

        int rowsAffected = entityManager.createQuery(criteriaUpdate).executeUpdate();
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }
    }

    @Override
    public void changeVisibilityById(@NonNull Long postId, @NonNull PostVisibility visibility)
            throws PostNotFoundException {

        CriteriaUpdate<Post> criteriaUpdate = cb.createCriteriaUpdate(Post.class);
        Root<Post> root = criteriaUpdate.getRoot();

        criteriaUpdate.set(root.get("visibility"), visibility);
        criteriaUpdate.where(cb.equal(root.get("id"), postId));

        int rowsAffected = entityManager.createQuery(criteriaUpdate).executeUpdate();
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }
    }

    // private void createKarmaScoreRelation(Long postId, Long userId, boolean isPositive) {

    //     String queryString = "INSERT INTO " + KarmaScore.class.getSimpleName() +
    //             " (post_id, user_id, is_positive) VALUES (:postId, :userId, :isPositive)";

    //     entityManager.createNativeQuery(queryString)
    //             .setParameter("postId", postId)
    //             .setParameter("userId", userId)
    //             .setParameter("isPositive", isPositive)
    //             .executeUpdate();
    // }

    // private void deleteKarmaScoreRelation(Long postId, Long userId) {

    //     String queryString = "DELETE FROM " + KarmaScore.class.getSimpleName()
    //             + " WHERE post_id = :postId AND user_id = :userId";

    //     entityManager.createNativeQuery(queryString)
    //             .setParameter("post_id", postId)
    //             .setParameter("user_id", userId)
    //             .executeUpdate();
    // }

}
