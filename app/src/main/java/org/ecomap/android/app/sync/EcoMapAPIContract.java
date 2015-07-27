package org.ecomap.android.app.sync;

/**
 * Defines column names IN JSONs ecomap's API
 */
public class EcoMapAPIContract {

    public static final String APP_PACKAGE_NAME = "org.ecomap.android.app";

    public static final String ECOMAP_SERVER_URL = "http://176.36.11.25";
    public static final String ECOMAP_HTTP_BASE_URL = ECOMAP_SERVER_URL + ":8000";
    public static final String ECOMAP_API_URL = ECOMAP_HTTP_BASE_URL + "/api";
    public static final String ECOMAP_BASE_URL = ECOMAP_API_URL + "/problems?";

    public static final String COOKIE_USER_ID = "user_id";

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String PROBLEMS_TYPES_ID = "problem_type_id";    //тип проблемы
    public static final String STATUS = "status";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String SEVERITY = "severity";
    public static final String NUMBER_OF_VOTES = "number_of_votes";//лайк
    public static final String NUMBER_OF_VOTES_UPDATE = "count";//in action VOTE number_of_votes is count...
    public static final String DATE = "datetime";
    public static final String CONTENT = "content";
    public static final String PROPOSAL = "proposal";
    public static final String REGION_ID = "region_id";
    public static final String NUMBER_OF_COMMENTS = "number_of_comments";//комент

    public static final String ACTION = "action";
    public static final String ACTION_DELETE = "DELETED";
    public static final String ACTION_VOTE = "VOTE";

}
