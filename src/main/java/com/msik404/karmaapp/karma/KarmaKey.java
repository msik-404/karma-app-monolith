package com.msik404.karmaapp.karma;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Embeddable
@RequiredArgsConstructor
@Getter
@Setter
public class KarmaKey implements Serializable {

    @Column(name = "user_id")
    private final Long userId;

    @Column(name = "post_id")
    private final Long postId;

    public KarmaKey() {

        this.userId = null;
        this.postId = null;
    }
}