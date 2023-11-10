package com.msik404.karmaappmonolith.post.comparator;

public record BasicComparablePost(long id, long karmaScore) implements ComparablePost {

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Long getKarmaScore() {
        return karmaScore;
    }

}
