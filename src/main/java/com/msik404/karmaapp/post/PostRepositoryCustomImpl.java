package com.msik404.karmaapp.post;

import java.util.ArrayList;
import java.util.List;

import com.msik404.karmaapp.post.dto.PostResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
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
    public List<PostResponse> findKeysetPaginated(Long postId, Long karmaScore, int size) {

        var criteriaQuery = cb.createQuery(PostResponse.class);
        var postRoot = criteriaQuery.from(Post.class);
        var userJoin = postRoot.join("user");

        criteriaQuery.select(
                cb.construct(
                        PostResponse.class,
                        postRoot.get("id"),
                        postRoot.get("text"),
                        postRoot.get("karmaScore"),
                        userJoin.get("username")
                )
        );

        var predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(postRoot.get("visibility"), PostVisibility.ACTIVE));
        if (postId != null && karmaScore != null) {
           predicates.add(cb.greaterThan(postRoot.get("id"), postId));
           predicates.add(cb.lessThan(postRoot.get("karmaScore"), karmaScore));
        }
        criteriaQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        criteriaQuery.orderBy(
                cb.desc(postRoot.get("karmaScore")),
                cb.asc(postRoot.get("id")));

        final int offset = 0;
        return entityManager.createQuery(criteriaQuery)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
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
