package org.ecomap.android.app.data.model;

import android.os.Parcel;
import android.os.Parcelable;


public class ProblemPhotoEntry implements Parcelable {
    @SuppressWarnings("unused")
    public static final Creator<ProblemPhotoEntry> CREATOR = new Creator<ProblemPhotoEntry>() {
        @Override
        public ProblemPhotoEntry createFromParcel(Parcel in) {
            return new ProblemPhotoEntry(in);
        }

        @Override
        public ProblemPhotoEntry[] newArray(int size) {
            return new ProblemPhotoEntry[size];
        }
    };
    private String title;
    private final String imgURL;

    public ProblemPhotoEntry(String title, String imgURL) {
        this.title = title;
        this.imgURL = imgURL;
    }

    private ProblemPhotoEntry(Parcel in) {
        title = in.readString();
        imgURL = in.readString();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaption() {
        return title;
    }

    public String getImgURL() {
        return imgURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(imgURL);
    }
}
