package com.msik404.karmaapp.post.comparator;

import java.util.Comparator;

public class PostComparator implements Comparator<ComparablePost> {

    @Override
    public int compare(ComparablePost postOne, ComparablePost postTwo) {

        if (postOne.getKarmaScore().equals(postTwo.getKarmaScore())) {
            return -postOne.getId().compareTo(postTwo.getId());
        }
        return postOne.getKarmaScore().compareTo(postTwo.getKarmaScore());
    }

}
