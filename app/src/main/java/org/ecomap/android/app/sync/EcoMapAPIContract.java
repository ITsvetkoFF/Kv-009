package org.ecomap.android.app.sync;

/**
 * Defines column names IN JSONs ecomap's API
 */
public class EcoMapAPIContract {

    // Getting input stream from URL
    public static final String ECOMAP_BASE_URL = "http://176.36.11.25:8000/api/problems?";

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String PROBLEMS_TYPES_ID = "problem_type_id";
    public static final String STATUS = "status";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String SEVERITY = "severity";
    public static final String NUMBER_OF_VOTES = "number_of_votes";
    public static final String NUMBER_OF_VOTES_UPDATE = "count";//in action VOTE number_of_votes is count...
    public static final String DATE = "datetime";
    public static final String CONTENT = "content";
    public static final String PROPOSAL = "proposal";
    public static final String REGION_ID = "region_id";
    public static final String NUMBER_OF_COMMENTS = "number_of_comments";

    public static final String ACTION = "action";
    public static final String ACTION_DELETE = "DELETED";
    public static final String ACTION_VOTE = "VOTE";

}
