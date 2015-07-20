package org.ecomap.android.app;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.ecomap.android.app.data.EcoMapContract;

/**
 * Created by Stanislav on 23.06.2015.
 */
public class Problem implements ClusterItem, Parcelable {
    LatLng mPos;

    int id;
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

    boolean liked;

    Context mContext;

    public Problem(Cursor cursor, Context mContext) {

        this.liked = false;

        this.id = cursor.getInt(cursor.getColumnIndex(EcoMapContract.ProblemsEntry._ID));

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

    public int getId(){
        return id;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getProposal(){
        return proposal;
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
                res_id = R.drawable.type_7;
                break;
        }
    }

    public int getRes_id(){
        return res_id;
    }

    public int getResBigImage(){
        switch (type_id) {
            case 1:
                return R.drawable.problem_type_1_3x;
            case 2:
                return R.drawable.problem_type_2_3x;
            case 3:
                return R.drawable.problem_type_3_3x;
            case 4:
                return R.drawable.problem_type_4_3x;
            case 5:
                return R.drawable.problem_type_5_3x;
            case 6:
                return R.drawable.problem_type_6_3x;
            case 7:
                return R.drawable.problem_type_7_3x;
            default:
                return R.drawable.problem_type_7_3x;
        }
    }

    public String getTypeString(){
        switch (type_id) {
            case 1:
                return mContext.getString(R.string.problem_type_string_1);
            case 2:
                return mContext.getString(R.string.problem_type_string_2);
            case 3:
                return mContext.getString(R.string.problem_type_string_3);
            case 4:
                return mContext.getString(R.string.problem_type_string_4);
            case 5:
                return mContext.getString(R.string.problem_type_string_5);
            case 6:
                return mContext.getString(R.string.problem_type_string_6);
            case 7:
                return mContext.getString(R.string.problem_type_string_7);
            default:
                return mContext.getString(R.string.problem_type_string_7);
        }
    }

    public String getByTime(){
        return ("Added by:" + first_name + " " + last_name + "\n" + date);
    }

    public String getContent(){
        return content;
    }

    public String getNumberOfLikes(){
        return String.valueOf(number_of_votes);
    }

    public void setNumberOfLikes(int numb){
        number_of_votes += numb;
    }

    public void setLiked(boolean liked){
        this.liked = liked;
    }

    public boolean isLiked(){
        return liked;
    }

    public String getStatus(){
        return status;
    }

    @Override
    public LatLng getPosition() {
        return mPos;
    }

    
    protected Problem(Parcel in) {
        mPos = (LatLng) in.readValue(LatLng.class.getClassLoader());
        mTitle = in.readString();
        type_id = in.readInt();
        res_id = in.readInt();
        status = in.readString();
        first_name = in.readString();
        last_name = in.readString();
        severity = in.readString();
        number_of_votes = in.readInt();
        date = in.readString();
        content = in.readString();
        proposal = in.readString();
        region_id = in.readInt();
        number_of_comments = in.readInt();
        liked = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(mPos);
        dest.writeString(mTitle);
        dest.writeInt(type_id);
        dest.writeInt(res_id);
        dest.writeString(status);
        dest.writeString(first_name);
        dest.writeString(last_name);
        dest.writeString(severity);
        dest.writeInt(number_of_votes);
        dest.writeString(date);
        dest.writeString(content);
        dest.writeString(proposal);
        dest.writeInt(region_id);
        dest.writeInt(number_of_comments);
        dest.writeByte((byte) (liked ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Problem> CREATOR = new Parcelable.Creator<Problem>() {
        @Override
        public Problem createFromParcel(Parcel in) {
            return new Problem(in);
        }

        @Override
        public Problem[] newArray(int size) {
            return new Problem[size];
        }
    };
}