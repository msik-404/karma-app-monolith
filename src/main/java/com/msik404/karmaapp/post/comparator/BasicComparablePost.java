package com.msik404.karmaapp.post.comparator;

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
