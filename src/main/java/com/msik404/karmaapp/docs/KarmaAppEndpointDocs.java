package com.msik404.karmaapp.docs;

public class KarmaAppEndpointDocs {

    // OPERATIONS
    private static final String OP_BASE_DESC_FIND_PAGINATED_POSTS = """
            Get key-set (karma_score, post_id) paginated posts. If two posts karma_scores are the same, post with
            greater post_id is first. If pagination is not set top posts are returned.
             """;

    public static final String OP_DESC_FIND_PAGINATED_POSTS = OP_BASE_DESC_FIND_PAGINATED_POSTS +
            "User does not need to be logged-in to use this endpoint.";

    public static final String OP_SUM_FIND_PAGINATED_POSTS = "Get key-set (karma_score, post_id) paginated posts.";

    public static final String OP_BASE_FIND_PAGINATED_POSTS_FOR_MOD = OP_BASE_DESC_FIND_PAGINATED_POSTS +
            "This endpoint requires at least MOD privilege, thanks to this, hidden posts can be returned.";

    public static final String OP_SUM_FIND_PAGINATED_POSTS_FOR_MOD =
            "Get key-set (karma_score, post_id) paginated posts with mod privilege.";

    public static final String OP_DESC_FIND_PAGINATED_POSTS_FOR_ADMIN = OP_BASE_DESC_FIND_PAGINATED_POSTS +
            "This endpoint requires at least ADMIN privilege, thanks to this, hidden and deleted posts can be returned.";

    public static final String OP_SUM_FIND_PAGINATED_POSTS_FOR_ADMIN =
            "Get key-set (karma_score, post_id) paginated posts with admin privilege.";

    public static final String OP_BASE_DESC_FIND_PAGINATED_OWNED_POSTS = """
            Get key-set (karma_score, post_id) paginated posts of currently logged-in user. If two posts karma_scores
            are the same, post with greater post_id is first. If pagination is not set top posts are returned.
            """;

    public static final String OP_SUM_FIND_PAGINATED_OWNED_POSTS =
            "Get key-set (karma_score, post_id) paginated posts of currently logged-in user.";

    public static final String OP_BASE_DESC_FIND_PERSONAL_POST_RATINGS = """
            Get key-set (karma_score, post_id) paginated posts ratings of currently logged-in user. If two posts
            karma_scores are the same, post ratings with greater post_id is first. Post ratings can be returned in the
            same way as posts. This endpoint can be used with endpoints for returning paginated posts at the same time
            to show to the user which posts were rated by him and in what way (positive/negative).
            """;

    public static final String OP_BASE_SUM_FIND_PERSONAL_POST_RATINGS =
            "Get key-set (karma_score, post_id) paginated posts ratings of currently logged-in user.";

    public static final String OP_DESC_FIND_PERSONAL_POST_RATINGS_FOR_MOD = OP_BASE_DESC_FIND_PERSONAL_POST_RATINGS +
            "This endpoint requires at least MOD privilege, thanks to this, hidden posts ratings can be returned.";

    public static final String OP_SUM_FIND_PERSONAL_POST_RATINGS_FOR_MOD =
            "Get key-set (karma_score, post_id) paginated posts ratings of currently logged-in user with mod privilege.";

    public static final String OP_DESC_FIND_PERSONAL_POST_RATINGS_FOR_ADMIN = OP_BASE_DESC_FIND_PERSONAL_POST_RATINGS +
            "This endpoint requires at least ADMIN privilege, thanks to this, hidden and deleted posts ratings can be returned.";

    public static final String OP_SUM_FIND_PERSONAL_POST_RATINGS_FOR_ADMIN =
            "Get key-set (karma_score, post_id) paginated posts ratings of currently logged-in user with admin privilege.";

    public static final String OP_DESC_FIND_IMAGE_BY_ID = """
            Get image by post id. User does not need to be logged-in to use this endpoint.
            """;

    public static final String OP_SUM_FIND_IMAGE_BY_ID = "Get image by post id.";

    public static final String OP_DESC_CREATE_POST = "Create new post. User needs to be logged-in.";

    public static final String OP_SUM_CREATE_POST = "Create new post.";

    public static final String OP_DESC_RATE_POST = """
            Rate existing post. User needs to be logged-in. This operation is idempotent, if client user rates post in
            some way, and later decides to do the same action, only result of the first action is persisted - post gets
            rated, second action does not do anything. User can change his rating decision from positive to negative or
            the other way around, and post's score will get updated accordingly.
            """;

    public static final String OP_SUM_RATE_POST = "Rate existing post.";

    public static final String OP_DESC_UNRATE_POST = """
            Unrate existing post. User needs to be logged-in. This operation is idempotent, if client unrates post, and
            later decides to do the same action, only result of the first action is persisted - post is unrated, second
            action does not do anything. If post was not rated in the fist place, nothing happens.
            """;

    public static final String OP_SUM_UNRATE_POST = "Unrate existing post.";

    public static final String OP_DESC_HIDE_POST_BY_USER = """
            Change post visibility from active to hidden. User needs to be logged-in and post must have been created by
            that user. The post cannot be deleted This operation is idempotent, if client hides a post, and later decides
            to do the same action, only result of the first action is persisted - post is hidden, second action does
            not do anything.
            """;

    public static final String OP_SUM_HIDE_POST_BY_USER =
            "Change owned post's visibility from active to hidden.";

    public static final String OP_DESC_UNHIDE_POST_BY_USER = """
            Change post visibility from hidden to active. User needs to be logged-in and post must have been created
            by that user. The post cannot be deleted. This operation is idempotent, if client unhides a post, and later
            decides to do the same action, only result of the first action is persisted - post is active, second action
            does not do anything.
            """;

    public static final String OP_SUM_UNHIDE_POST_BY_USER =
            "Change owned post's visibility from hidden to active";

    public static final String OP_DESC_HIDE_POST_BY_MOD = """
            Change post visibility from active to hidden. User needs to be logged-in and have at least mod privilege.
            The post cannot be deleted. This operation is idempotent, if client hides a post, and later decides to do
            the same action, only result of the first action is persisted - post is hidden, second action does not do
            anything. Admin user can use this endpoint to change post visibility from any to hidden.
            """;

    public static final String OP_SUM_HIDE_POST_BY_MOD =
            "Change post's visibility from active to hidden.";

    public static final String OP_DESC_DELETE_POST_BY_USER = """
            Change post visibility from any to deleted. User needs to be logged-in and post must have been created by 
            that user. This operation is idempotent, if client deletes a post, and later decides to do the same action, 
            only result of the first action is persisted - post is deleted, second action does not do anything.
            """;

    public static final String OP_SUM_DELETE_POST_BY_USER =
            "Change owned post's visibility from any to deleted.";

    public static final String OP_DESC_DELETE_POST_BY_ADMIN = """
            Change post visibility from any to deleted. User needs to be logged-in and have at least admin privilege.
            This operation is idempotent, if client deletes a post, and later decides to do the same action, only result
            of the first action is persisted - post is deleted, second action does not do anything.
            """;

    public static final String OP_SUM_DELETE_POST_BY_ADMIN =
            "Change post's visibility from any to deleted.";

    public static final String OP_DESC_ACTIVE_POST_BY_ADMIN = """
            Change post visibility from any to active. User needs to logged-in and have at least admin privilege.
            This operation is idempotent, if client activates a post, and later decides to do the same action, only
            result of the first action is persisted - post is active, second action does not do anything.
            """;

    public static final String OP_SUM_ACTIVATE_POST_BY_ADMIN =
            "Change post's visibility from any to active.";

    public static final String OP_DESC_UPDATE_WITH_USER_PRIVILEGE = """
            Update user account of currently logged-in user. Only sub-set of all fields can be updated with this endpoint.
            These fields contain every field which is safe to be changed by regular user.
            """;

    public static final String OP_SUM_UPDATE_WITH_USER_PRIVILEGE =
            "Update user account of currently logged-in user.";

    public static final String OP_DESC_UPDATE_WITH_ADMIN_PRIVILEGE = """
            Update user account. This endpoint requires user to be logged-in and have at least admin privilege.
            All users accounts and all fields can be modified with this endpoint.
            """;

    public static final String OP_SUM_UPDATE_WITH_ADMIN_PRIVILEGE =
            "Update user account with admin privilege.";

    public static final String OP_DESC_REGISTER = """
            Used to create new user account.
            """;

    public static final String OP_SUM_REGISTER = "Create new user account.";

    public static final String OP_DESC_LOGIN = """
            Used for login. Returns JWT string which should be set in Authorization header in the form of bearer token.
            If Authorization header is not set, user is considered to not be logged-in.
            Bearer: JWT_STRING
            """;

    public static final String OP_SUM_LOGIN = "Login and get JWT.";

    // PARAMETERS
    public static final String PARAM_DESC_SIZE = """
            Amount of posts to be returned. If there are less posts than requested, as many posts as possible are
            returned.
            """;

    public static final String PARAM_DESC_POST_ID = """
            Id of the last returned post. Used for pagination. Pagination to work requires both karma_score and
            post_id to be set, otherwise top posts are returned.
            """;

    public static final String PARAM_DESC_KARMA_SCORE = """
            Score of the last returned post. Used for pagination. Pagination to work requires both karma_score
            and post_id to be set, otherwise top posts are returned.
            """;

    public static final String PARAM_DESC_USERNAME = """
            Creator user's username, makes endpoint return only these posts whose creator has this username.
            Can be omitted, to not filter by username.
            """;

    public static final String PARAM_DESC_ACTIVE = """
            Post visibility, makes returned post have active visibility. At the same time other visibilities
            can also be selected.
            """;

    public static final String PARAM_DESC_HIDDEN = """
            Post visibility, makes returned post have hidden visibility. At the same time other visibilities
            can also be selected.
            """;

    public static final String PARAM_DESC_DELETED = """
            Post visibility, makes returned post have deleted visibility. At the same time other visibilities
            can also be selected.
            """;

    public static final String PARAM_DESC_POST_CREATION_REQUEST = """
            Json with data for creating new post.
            """;

    public static final String PARAM_DESC_IMAGE_DATA = "Binary data of a image. Should be uploaded as a file.";

    public static final String PARAM_DESC_IS_POSITIVE = "Indicates whether post rating is positive or negative.";

    public static final String PARAM_DESC_USER_UPDATE_REQUEST_WITH_USER_PRIVILEGE = """
            Json with data for performing user update. Each field is optional, only set fields will be used for update.
            This objects contains fields which can be modified with user privilege.
            """;

    public static final String PARAM_DESC_USER_UPDATE_REQUEST_WITH_ADMIN_PRIVILEGE = """
            Json with data for performing user update. Each field is optional, only set fields will be used for update.
            This objects contains fields which can be modified with user and admin privilege, thus to use it, one must
            have admin privilege.
            """;

    public static final String PARAM_DESC_REGISTER_REQUEST = """
            Json with data for creating new user account.
            """;

    public static final String PARAM_DESC_LOGIN_REQUEST = """
            Json with data for logging-in.
            """;

    // PATH PARAMETERS
    public static final String PATH_DESC_POST_ID = "Id of post on which specified operation is being performed.";

    public static final String PATH_DESC_USER_ID = "Id of user on which specified operation is being performed.";

    // RESPONSES
    public static final String RESP_OK_DESC_PAGINATED_POSTS = "Returned paginated posts.";

    public static final String RESP_OK_DESC_PAGINATED_POSTS_RATINGS = "Returned paginated posts ratings.";

    public static final String RESP_INTERNAL_DESC_PAGINATED_POSTS =
            "Could not get posts from the database for some reason.";

    public static final String RESP_INTERNAL_DESC_PAGINATED_POSTS_RATINGS =
            "Could not get posts ratings from the database for some reason.";

    public static final String RESP_OK_DESC_FIND_IMAGE = "Returned post's image data";

    public static final String RESP_OK_CREATE_POST = "Post was created successfully.";

    public static final String RESP_OK_RATE_POST = "Post was rated successfully.";

    public static final String RESP_INTERNAL_DESC_RATE_POST = "Could not rate post for some reason.";

    public static final String RESP_OK_UNRATE_POST = "Post was unrated successfully.";

    public static final String RESP_INTERNAL_DESC_UNRATE_POST = "Could not unrate post for some reason.";

    public static final String RESP_OK_HIDE_POST = "Post was hidden successfully.";

    public static final String RESP_OK_DELETE_POST = "Post was deleted successfully.";

    public static final String RESP_OK_ACTIVATE_POST = "Post was activated successfully.";

    public static final String RESP_INTERNAL_DESC_CHANGE_POST_VISIBILITY =
            "Could not change visibility of a post for some reason";

    public static final String RESP_UNAUTH_DESC_CHANGE_POST_VISIBILITY =
            "Could not change visibility of a post because of not sufficient privilege or not being the owner.";

    public static final String RESP_OK_ACCOUNT_UPDATE = "User account was updated successfully.";

    public static final String RESP_CONF_NOT_UNIQUE_OBJECT_FIELD =
            "At least one field set in request object which should be unique is not.";

    public static final String RESP_BAD_REQ_ACCOUNT_UPDATE =
            "Zero fields were set in update object.";

    public static final String RESP_OK_REGISTER = "User account was registered successfully.";

    public static final String RESP_OK_LOGIN = """
            User logged-in successfully. JWT string is returned. It should be set Authorization like: Bearer JWT_STRING
            for user to be considered logged-in. JWT is signed with sha256 and is valid for one hour. JWT has user's 
            id encoded as subject.
            """;

}