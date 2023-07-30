package com.msik404.karmaapp.karma;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class KarmaKey implements Serializable {

    public KarmaKey() {

        postId = null;
        userId = null;
    }

    @Column(name = "user_id")
    private final Long userId;

    @Column(name = "post_id")
    private final Long postId;
}
