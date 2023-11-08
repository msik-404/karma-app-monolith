package com.msik404.karmaapp;

public class KarmaAppEndpointDocs {

    private static final String FIND_PAGINATED_POSTS_BASE_DESC = """
            Get key-set (karma_score, post_id) paginated posts. If two posts karma_scores are the same, post with
            greater post_id is first. If pagination is not set top posts are returned.
             """;

    public static final String FIND_PAGINATED_POSTS_DESC = FIND_PAGINATED_POSTS_BASE_DESC +
            "Every user can use this endpoint, even not logged-in.";

    public static final String FIND_PAGINATED_POSTS_FOR_MOD_DESC = FIND_PAGINATED_POSTS_BASE_DESC +
            "This endpoint requires at least MOD privilege, thanks to this, hidden posts can be returned.";

    public static final String FIND_PAGINATED_POSTS_FOR_ADMIN_DESC = FIND_PAGINATED_POSTS_BASE_DESC +
            "This endpoint requires at least ADMIN privilege, thanks to this, hidden and deleted posts can be returned.";

    public static final String FIND_PAGINATED_OWNED_POSTS_BASE_DESC = """
            Get key-set (karma_score, post_id) paginated posts of currently logged-in user. If two posts karma_scores
            are the same, post with greater post_id is first. If pagination is not set top posts are returned.
            """;

    public static final String FIND_PERSONAL_POST_RATINGS_BASE_DESC = """
            Get key-set (karma_score, post_id) paginated posts ratings of currently logged-in user. If two posts
            karma_scores are the same, post ratings with greater post_id is first. Post ratings can be returned in the
            same way as posts. This endpoint can be used with endpoints for returning paginated posts at the same time
            to show to the user which posts were rated by him and in what way (positive/negative).
            """;

    public static final String FIND_PERSONAL_POST_RATINGS_FOR_MOD_DESC = FIND_PERSONAL_POST_RATINGS_BASE_DESC +
            "This endpoint requires at least MOD privilege, thanks to this, hidden posts ratings can be returned.";


    public static final String FIND_PERSONAL_POST_RATINGS_FOR_ADMIN_DESC = FIND_PERSONAL_POST_RATINGS_BASE_DESC +
            "This endpoint requires at least ADMIN privilege, thanks to this, hidden and deleted posts ratings can be returned.";

    public static final String SIZE_DESC = """
            Amount of posts to be returned. If there are less posts than requested, as many posts as possible are
            returned.
            """;

    public static final String POST_ID_DESC = """
            Id of the last returned post. Used for pagination. Pagination to work requires both karma_score and
            post_id to be set, otherwise top posts are returned.
            """;

    public static final String KARMA_SCORE_DESC = """
            Score of the last returned post. Used for pagination. Pagination to work requires both karma_score
            and post_id to be set, otherwise top posts are returned.
            """;

    public static final String USERNAME_DESC = """
            Creator user's username, makes endpoint return only these posts whose creator has this username.
            Can be omitted, to not filter by username.
            """;

    public static final String ACTIVE_DESC = """
            Optional visibility, makes returned post have active visibility. At the same time other visibilities
            can also be selected.
            """;

    public static final String HIDDEN_DESC = """
            Optional visibility, makes returned post have hidden visibility. At the same time other visibilities
            can also be selected.
            """;

    public static final String DELETED_DESC = """
            Optional visibility, makes returned post have deleted visibility. At the same time other visibilities
            can also be selected.
            """;

    public static final String PAGINATED_POSTS_RETURN_DESC = "Returned paginated posts.";

    public static final String PAGINATED_POSTS_INTERNAL_ERROR_DESC =
            "Could not get posts from the database for some reason.";

    public static final String PAGINATED_POSTS_RATINGS_RETURN_DESC = "Returned paginated posts ratings.";

    public static final String PAGINATED_POSTS_RATINGS_INTERNAL_ERROR_DESC =
            "Could not get posts ratings from the database for some reason.";

}