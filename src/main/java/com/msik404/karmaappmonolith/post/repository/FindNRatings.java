package com.msik404.karmaappmonolith.post.repository;

import com.msik404.karmaappmonolith.karma.KarmaScore;
import com.msik404.karmaappmonolith.post.Post;
import com.msik404.karmaappmonolith.post.dto.PostRatingResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class FindNRatings extends FindNPostPaginated<PostRatingResponse> {

    protected Join<Post, KarmaScore> karmaScoreJoin;

    public FindNRatings(EntityManager entityManager, CriteriaBuilder cb, long userId) {
        super(entityManager, cb, PostRatingResponse.class);

        this.karmaScoreJoin = postRoot
                .join("karmaScores", JoinType.LEFT);
        karmaScoreJoin.on(
                cb.equal(karmaScoreJoin.get("user").get("id"), userId)
        );
    }

    @Override
    void selectMethod(CriteriaBuilder cb) {

        criteriaQuery.select(
                cb.construct(
                        PostRatingResponse.class,
                        postRoot.get("id"),
                        karmaScoreJoin.get("isPositive")
                )
        );
    }

}
