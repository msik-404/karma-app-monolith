package com.msik404.karmaappmonolith.karma;

import com.msik404.karmaappmonolith.karma.exception.KarmaScoreNotFoundException;
import com.msik404.karmaappmonolith.post.Post;
import com.msik404.karmaappmonolith.user.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KarmaScoreService {

    private final KarmaScoreRepository repository;
    private final EntityManager entityManager;

    public KarmaScore findById(@NonNull KarmaKey id) throws KarmaScoreNotFoundException {
        return repository.findById(id).orElseThrow(KarmaScoreNotFoundException::new);
    }

    // WARNING THERE MAY BE possible errors when entity with that KarmaKey exists
    public KarmaScore create(long userId, long postId, boolean isPositive) {

        var karmaScore = new KarmaScore(
                entityManager.getReference(User.class, userId),
                entityManager.getReference(Post.class, postId),
                isPositive
        );
        return repository.save(karmaScore);
    }

    public void deleteById(@NonNull KarmaKey karmaKey) {
        repository.deleteById(karmaKey);
    }
}
