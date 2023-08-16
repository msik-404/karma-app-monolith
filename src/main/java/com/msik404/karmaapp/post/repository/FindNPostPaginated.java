package com.msik404.karmaapp.post.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.PostVisibility;
import com.msik404.karmaapp.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.lang.NonNull;

public abstract class FindNPostPaginated<T> extends FindTemplate<T>{

    protected Root<Post> postRoot;
    protected Join<Post, User> userJoin;
    protected Map<String, Predicate> wherePredicateMap;

    public FindNPostPaginated(EntityManager entityManager, CriteriaBuilder cb, Class<T> entityClass) {

        super(entityManager, cb, entityClass);

        this.postRoot = criteriaQuery.from(Post.class);
        this.userJoin = postRoot.join("user");
        this.wherePredicateMap = new HashMap<>();
    }

    public void setVisibilitiesIn(@NonNull List<PostVisibility> visibilities) {
        wherePredicateMap.put("visibilityPredicate", postRoot.get("visibility").in(visibilities));
    }

    public void setKarmaScoreLessThan(long karmaScore) {
        wherePredicateMap.put("karmaScorePredicate", cb.lessThan(postRoot.get("karmaScore"), karmaScore));
    }

    public void setUsernameEqual(@NonNull String username) {
        wherePredicateMap.put("usernamePredicate", cb.equal(userJoin.get("username"), username));
    }

    @Override
    void whereMethod(CriteriaBuilder cb, CriteriaQuery<T> criteriaQuery) {

        if (!wherePredicateMap.isEmpty()) {
            criteriaQuery.where(cb.and(wherePredicateMap.values().toArray(new Predicate[0])));
        }
    }

    @Override
    void orderMethod(CriteriaBuilder cb, CriteriaQuery<T> criteriaQuery) {

        criteriaQuery.orderBy(
                cb.desc(postRoot.get("karmaScore")),
                cb.asc(postRoot.get("id")));
    }

}