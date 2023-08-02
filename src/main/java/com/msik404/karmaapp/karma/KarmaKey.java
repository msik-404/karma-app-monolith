package com.msik404.karmaapp.karma;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

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
