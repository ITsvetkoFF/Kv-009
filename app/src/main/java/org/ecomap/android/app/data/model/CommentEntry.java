package org.ecomap.android.app.data.model;

import android.util.Log;

import org.ecomap.android.app.ui.fragments.CommentsFragment;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yridk_000 on 15.07.2015.
 */
public class CommentEntry {

    private static final String MODIFIEDDATE = "modified_date";
    private static final String MODIFIEDBY = "modified_by";
    private static final String CREATEDBY = "created_by";
    private static final String CONTENT = "content";
    private static final String CREATEDDATE = "created_date";
    private static final String ID = "id";

    private String modifiedDate;
    private String modifiedBy;
    private String createdBy;
    private String content;
    private String createdDate;
    private long id;

    public static CommentEntry fromJSON(JSONObject obj){

        try {
            String modifiedDate = obj.getString(MODIFIEDDATE);
            String modifiedBy = obj.getString(MODIFIEDBY);
            String createdBy = obj.getString(CREATEDBY);
            String content = obj.getString(CONTENT);
            String createdDate = obj.getString(CREATEDDATE);
            long id = obj.getLong(ID);

            return new CommentEntry(modifiedDate, modifiedBy, createdBy, content, createdDate, id);

        } catch (JSONException e) {
            Log.e(CommentsFragment.TAG, e.getMessage(), e);
        }

        return null;
    }

    public CommentEntry(String modifiedDate, String modifiedBy, String createdBy, String content, String createdDate, long id) {
        this.modifiedDate = modifiedDate;
        this.modifiedBy = modifiedBy;
        this.createdBy = createdBy;
        this.content = content;
        this.createdDate = createdDate;
        this.id = id;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public long getId() {
        return id;
    }
}
