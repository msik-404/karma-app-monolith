package com.msik404.karmaapp.karma;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KarmaScoreRepository extends JpaRepository<KarmaScore, KarmaKey> {
}
