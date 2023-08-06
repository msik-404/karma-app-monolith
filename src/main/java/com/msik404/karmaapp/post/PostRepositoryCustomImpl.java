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

        var criteriaQuery = cb.createQuery(Post.class);
        var root = criteriaQuery.from(Post.class);

        criteriaQuery.select(root);

        criteriaQuery.orderBy(
                cb.asc(root.get("id")),
                cb.desc(root.get("karmaScore")));

        final int offset = 0;
        return entityManager.createQuery(criteriaQuery).setFirstResult(offset).setMaxResults(size).getResultList();
    }

    @Override
    public List<Post> findTopNextN(long postId, long karmaScore, int size) {

        var criteriaQuery = cb.createQuery(Post.class);
        var root = criteriaQuery.from(Post.class);

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

    public void addKarmaScoreToPost(long postId, long value) throws PostNotFoundException {

        var criteriaUpdate = cb.createCriteriaUpdate(Post.class);
        var root = criteriaUpdate.getRoot();

        Path<Long> karmaScorePath = root.get("karmaScore");

        criteriaUpdate.set(karmaScorePath, cb.sum(karmaScorePath, value));
        criteriaUpdate.where(cb.equal(root.get("id"), postId));

        int rowsAffected = entityManager.createQuery(criteriaUpdate).executeUpdate();
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }
    }

    @Override
    public void changeVisibilityById(long postId, @NonNull PostVisibility visibility)
            throws PostNotFoundException {

        var criteriaUpdate = cb.createCriteriaUpdate(Post.class);
        var root = criteriaUpdate.getRoot();

        criteriaUpdate.set(root.get("visibility"), visibility);
        criteriaUpdate.where(cb.equal(root.get("id"), postId));

        int rowsAffected = entityManager.createQuery(criteriaUpdate).executeUpdate();
        if (rowsAffected == 0) {
            throw new PostNotFoundException();
        }
    }

}
