package com.msik404.karmaapp.karma;

import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.user.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KarmaScoreService {

    private final KarmaScoreRepository repository;
    private final EntityManager entityManager;

    public KarmaScore findById(KarmaKey id) throws KarmaScoreNotFoundException {
        return repository.findById(id).orElseThrow(KarmaScoreNotFoundException::new);
    }

    // WARNING THERE MAY BE possible errors when entity with that KarmaKey exists
    public KarmaScore create(long userId, long postId, boolean isPositive) {

        var karmaScore = KarmaScore.builder()
                .id(new KarmaKey(userId, postId))
                .user(entityManager.getReference(User.class, userId))
                .post(entityManager.getReference(Post.class, postId))
                .isPositive(isPositive).build();
        return repository.save(karmaScore);
    }

    public void deleteById(KarmaKey karmaKey) {
        repository.deleteById(karmaKey);
    }
}
