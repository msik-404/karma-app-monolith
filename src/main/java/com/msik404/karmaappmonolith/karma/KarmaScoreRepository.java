package com.msik404.karmaappmonolith.karma;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KarmaScoreRepository extends JpaRepository<KarmaScore, KarmaKey> {
}
