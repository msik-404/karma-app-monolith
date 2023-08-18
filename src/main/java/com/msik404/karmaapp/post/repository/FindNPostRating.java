package com.msik404.karmaapp.post.repository;

import com.msik404.karmaapp.karma.KarmaScore;
import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.dto.PostRatingResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class FindNPostRating extends FindNPostPaginated<PostRatingResponse> {

    protected Join<Post, KarmaScore> karmaScoreJoin;

    public FindNPostRating(EntityManager entityManager, CriteriaBuilder cb, long userId) {
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
