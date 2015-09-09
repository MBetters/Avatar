package org.cubrc.avatar.samplerdfactivity;

/**
 * Created by douglas.calderon on 6/17/2015.
 */
public class Constants
{
    public static final String ACTION_BROADCAST = "org.cubrc.avatar.BROADCAST";
    public static final String EXTENDED_DATA_TYPE = "org.cubrc.avatar.TYPE";
    public static final String EXTENDED_DATA_STATUS = "org.cubrc.avatar.STATUS";
    public static final String EXTENDED_DATA_RESPONSE = "org.cubrc.avatar.RESPONSE";

    public static final String EXTENDED_DATA_REQUEST_ID = "org.cubrc.avatar.REQUEST_ID";

    public static final String ACTION_SCAN = "org.cubrc.avatar.SCAN";
    public static final String ACTION_TRIPLES = "org.cubrc.avatar.TRIPLES";
    public static final String ACTION_SELECT = "org.cubrc.avatar.SELECT";
    public static final String ACTION_CONSTRUCT = "org.cubrc.avatar.CONSTRUCT";
    public static final String ACTION_ASK = "org.cubrc.avatar.ASK";
    public static final String ACTION_UPDATE = "org.cubrc.avatar.UPDATE";
    public static final String ACTION_PURGE = "org.cubrc.avatar.PURGE";

    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAIL = "FAIL";

    public static final String TRIPLE_SUBJECT = "SUBJECT";
    public static final String TRIPLE_OBJECT = "OBJECT";
    public static final String TRIPLE_PREDICATE = "PREDICATE";

    public static final String SUBJECT_TYPE = "SUBJECT_TYPE";
    public static final String SUBJECT_TYPE_URI = "SUBJECT_TYPE_URI";
    public static final String SUBJECT_TYPE_BLANK = "SUBJECT_TYPE_BLANK";

    public static final String OBJECT_TYPE = "OBJECT_TYPE";
    public static final String OBJECT_TYPE_URI = "OBJECT_TYPE_URI";
    public static final String OBJECT_TYPE_BLANK = "OBJECT_TYPE_BLANK";
    public static final String OBJECT_TYPE_LITERAL = "OBJECT_TYPE_LITERAL";

    public static final int QUERY_PAGE_IDX = 0;
    public static final int CREATE_PERSON_PAGE_IDX = 1;
    public static final int NEWS_PAGE_IDX = 2;
//    public static final int FIND_PERSON_PAGE_IDX = 2;

    public static final String avatarNs = "avatar:";

    public static final String LOG_TAG = "SimpleRdfActivity";

    //register some enums to the fully qualified path name for handing intents back to their fragment
    public static final String CREATE_PERSON_KEY = "org.cubrc.avatar.samplerdfactivity.CreatePersonFragment";
    public static final String NEWS_KEY = "org.cubrc.avatar.samplerdfactivity.NewsFragment";
    public static final String QUERY_KEY = "org.cubrc.avatar.samplerdfactivity.QueryFragment.Select";
    public static final String UPDATE_KEY = "org.cubrc.avatar.samplerdfactivity.QueryFragment.Update";

}
