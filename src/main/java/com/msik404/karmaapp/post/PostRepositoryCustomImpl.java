package com.msik404.karmaapp.post;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.msik404.karmaapp.post.dto.PostResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
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
    public List<PostResponse> findKeysetPaginated(Long karmaScore, int size) {

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

    @Override
    @Transactional
    public Optional<byte[]> findImageByPostId(long postId) {
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        var query = criteriaBuilder.createQuery(byte[].class);
        var root = query.from(Post.class);

        query.select(root.get("imageData"))
                .where(criteriaBuilder.equal(root.get("id"), postId));

        byte[] result;
        try {
            result = entityManager.createQuery(query).getSingleResult();
        } catch (NoResultException ex) {
            result = null;
        } catch (RuntimeException ex) {
            throw new InternalServerErrorException("Could not get requested image from database for some reason.");
        }
        return Optional.ofNullable(result);
    }

}
