package org.ecomap.android.app;

import android.content.Context;
import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.ecomap.android.app.data.EcoMapContract;

/**
 * Created by Stanislav on 23.06.2015.
 */
public class Problem implements ClusterItem {
    //position
    LatLng mPos;

    int problem_id;
    String mTitle;
    int type_id;
    int res_id;
    String status;
    String first_name;
    String last_name;
    String severity;
    int number_of_votes;
    String date;
    String content;
    String proposal;
    int region_id;
    int number_of_comments;

    Context mContext;

    public Problem(Cursor cursor, Context mContext) {

        double latitude = cursor.getDouble(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_LATITUDE));
        double longitude = cursor.getDouble(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_LONGTITUDE));
        this.mPos = new LatLng(latitude, longitude);

        this.mTitle = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_TITLE));
        this.type_id = cursor.getInt(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_PROBLEM_TYPE_ID));
        this.mContext = mContext;
        this.status = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_STATUS));
        this.first_name = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_USER_FIRST_NAME));
        this.last_name = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_USER_LAST_NAME));
        this.severity = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_SEVERITY));
        this.number_of_votes = cursor.getInt(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_NUMBER_OF_VOTES));
        this.date = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_DATE));
        this.content = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_CONTENT));
        this.proposal = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_PROPOSAL));
        this.region_id = cursor.getInt(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_REGION_ID));
        this.number_of_comments = cursor.getInt(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_COMMENTS_NUMBER));

        chooseIcon();
    }

    public String getTitle() {
        return mTitle;
    }

    public void chooseIcon() {
        switch (type_id) {
            case 1:
                res_id = R.drawable.type_1;
                break;
            case 2:
                res_id = R.drawable.type_2;
                break;
            case 3:
                res_id = R.drawable.type_3;
                break;
            case 4:
                res_id = R.drawable.type_4;
                break;
            case 5:
                res_id = R.drawable.type_5;
                break;
            case 6:
                res_id = R.drawable.type_6;
                break;
            case 7:
                res_id = R.drawable.type_7;
                break;
            default:
                break;
        }
    }

    @Override
    public LatLng getPosition() {
        return mPos;
    }
}
