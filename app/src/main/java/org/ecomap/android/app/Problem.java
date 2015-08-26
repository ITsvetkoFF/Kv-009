package org.ecomap.android.app;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.utils.SharedPreferencesHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Stanislav on 23.06.2015.
 */
public class Problem implements ClusterItem, Parcelable {

    private static final String LOG_TAG = Problem.class.getSimpleName();

    private final LatLng mPos;

    private int id;
    private int typeId;
    int resId;
    private int numberOfVotes;
    private final int regionId;
    private final int numberOfComments;
    int userId;
    private final String mTitle;
    private final String status;
    private final String firstName;
    private final String lastName;
    private final String severity;
    private final String date;
    private final String content;
    private final String proposal;

    private boolean liked;

    private Context mContext;

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
        this.typeId = cursor.getInt(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_PROBLEM_TYPE_ID));
        this.mContext = mContext;
        this.status = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_STATUS));
        this.firstName = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_USER_FIRST_NAME));
        this.lastName = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_USER_LAST_NAME));
        this.severity = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_SEVERITY));
        this.numberOfVotes = cursor.getInt(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_NUMBER_OF_VOTES));
        this.date = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_DATE));
        this.content = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_CONTENT));
        this.proposal = cursor.getString(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_PROPOSAL));
        this.regionId = cursor.getInt(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_REGION_ID));
        this.numberOfComments = cursor.getInt(cursor
                .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_COMMENTS_NUMBER));
        this.userId = cursor.getInt(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_USER_ID));

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
        switch (typeId) {
            case 1:
                resId = R.drawable.type_1;
                break;
            case 2:
                resId = R.drawable.type_2;
                break;
            case 3:
                resId = R.drawable.type_3;
                break;
            case 4:
                resId = R.drawable.type_4;
                break;
            case 5:
                resId = R.drawable.type_5;
                break;
            case 6:
                resId = R.drawable.type_6;
                break;
            case 7:
                resId = R.drawable.type_7;
                break;
            default:
                resId = R.drawable.type_7;
                break;
        }
    }

    public int getResId(){
        return resId;
    }

    public int getResBigImage(){
        switch (typeId) {
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
        switch (typeId) {
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

    public String getUserDate(){

        if (this.firstName.isEmpty() && this.lastName.isEmpty()){

            String no_name = "(" + mContext.getString(R.string.string_anonymous) + ")";

            return (no_name + ": " + date);
        }

        return (firstName + ": " + date);
    }

    public String getContent(){
        return content;
    }

    public String getSeverity() {
        return severity;
    }

    public int getNumber_of_comments() {
        return numberOfComments;
    }

    public String getNumberOfLikes(){
        return String.valueOf(numberOfVotes);
    }

    public void addLike(){
        numberOfVotes += 1;
    }

    public void setLiked(){

        this.liked = true;

        //save this problem into set in SharedPreferences
        SharedPreferencesHelper.addLikedProblem(mContext,getId());

    }

    public boolean isLiked(){

        //check if this problem is in set of SharedPreferences
        return liked||SharedPreferencesHelper.isLikedProblem(mContext,getId());

    }

    public String getStatus(){
        return status;
    }

    public String getDescription(){
        return content;
    }

    public String getSolution(){
        return proposal;
    }

    public int getTypeId(){
        return typeId;
    }

    public void setTypeId(int typeId){
        this.typeId = typeId;
    }

    @Override
    public LatLng getPosition() {
        return mPos;
    }

    
    private Problem(Parcel in) {
        mPos = (LatLng) in.readValue(LatLng.class.getClassLoader());
        mTitle = in.readString();
        typeId = in.readInt();
        resId = in.readInt();
        status = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        severity = in.readString();
        numberOfVotes = in.readInt();
        date = in.readString();
        content = in.readString();
        proposal = in.readString();
        regionId = in.readInt();
        numberOfComments = in.readInt();
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
        dest.writeInt(typeId);
        dest.writeInt(resId);
        dest.writeString(status);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(severity);
        dest.writeInt(numberOfVotes);
        dest.writeString(date);
        dest.writeString(content);
        dest.writeString(proposal);
        dest.writeInt(regionId);
        dest.writeInt(numberOfComments);
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

    public String getRelativeTime(){
        return DateUtils.getRelativeTimeSpanString(getCreatedDate().getTime(), new Date().getTime(), DateUtils.SECOND_IN_MILLIS).toString();
    }

    private Date getCreatedDate(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date d = null;
        try {

            long currentTimeMillis = System.currentTimeMillis();
            long tzOffset = TimeZone.getDefault().getOffset(currentTimeMillis);

            d = new Date(format.parse(date).getTime() + tzOffset);

        } catch (ParseException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
        }
        return d;
    }
}