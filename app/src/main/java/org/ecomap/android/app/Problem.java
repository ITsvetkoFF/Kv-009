package org.ecomap.android.app;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Stanislav on 23.06.2015.
 */
public class Problem implements ClusterItem {
    LatLng mPos;
    String mTitle;
    int type_id, res_id;
    Context mContext;

    Problem(double latitude, double longitude, String title, int type_id, Context mContext){
        this.mPos = new LatLng(latitude, longitude);
        this.mTitle = title;
        this.type_id = type_id;
        this.mContext = mContext;
        chooseIcon();
    }

    public String getTitle(){
        return mTitle;
    }

    public void chooseIcon(){
        switch (type_id){
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
