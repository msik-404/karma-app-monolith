package com.msik404.karmaapp.post;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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

}
