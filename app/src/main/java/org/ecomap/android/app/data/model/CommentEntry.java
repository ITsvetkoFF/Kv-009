package org.ecomap.android.app.data.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by y.ridkous@gmail.com on 15.07.2015.
 */
public class CommentEntry {

    public static final String LOG_TAG = CommentEntry.class.getSimpleName();

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
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return null;
    }


    public CommentEntry(final String modifiedDate, final String modifiedBy, final String createdBy, final String content, final String createdDate, final long id) {
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

    public String getCreatedDateAsString() {
        return createdDate;
    }

    public long getId() {
        return id;
    }

    public Date getCreatedDate(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        Date date = null;
        try {

            long currentTimeMillis = System.currentTimeMillis();
            long tzOffset = TimeZone.getDefault().getOffset(currentTimeMillis);

            date = new Date(format.parse(createdDate).getTime() + tzOffset);

        } catch (ParseException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return date;
    }

}
