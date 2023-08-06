package com.msik404.karmaapp.karma;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KarmaScoreService {

    private final KarmaScoreRepository repository;

    public KarmaScore findById(KarmaKey id) throws KarmaScoreNotFoundException {
        return repository.findById(id).orElseThrow(KarmaScoreNotFoundException::new);
    }

    // TODO: possibly constraintException
    public KarmaScore create(Long userId, Long postId, boolean isPositive) throws KarmaScoreAlreadyExistsException {

        var karmaScore = KarmaScore.builder()
                .id(new KarmaKey(userId, postId))
                .isPositive(isPositive).build();
        return repository.save(karmaScore);
    }

    public void deleteById(KarmaKey karmaKey) {
        repository.deleteById(karmaKey);
    }
}
