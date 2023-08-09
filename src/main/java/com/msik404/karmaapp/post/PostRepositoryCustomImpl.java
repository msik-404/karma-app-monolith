package com.msik404.karmaapp.post;

import java.util.ArrayList;
import java.util.List;

import com.msik404.karmaapp.post.dto.PostResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
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
    public List<PostResponse> findKeysetPaginated(Long karmaScore, int size) throws InternalServerErrorException {

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
        if (karmaScore != null) {
            predicates.add(cb.lessThan(postRoot.get("karmaScore"), karmaScore));
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
