package org.ecomap.android.app.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.fragments.EcoMapFragment;
import org.ecomap.android.app.sync.DeleteTask;

/**
 * Created by Stanislav on 13.08.2015.
 */
public class YesNoAlertDialog {
    private String message;
    private Context mContext;
    private Problem problem;

    public YesNoAlertDialog(String message, Context mContext, Problem problem){
        this.message = message;
        this.mContext = mContext;
        this.problem = problem;
    }

    public YesNoAlertDialog(String message, Context mContext){
        this.message = message;
        this.mContext = mContext;
    }

    public void showAlertDialogDeleteProblem(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

        alert.setMessage(message);
        alert.setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (new NetworkAvailability(mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).isNetworkAvailable()) {
                    new DeleteTask(mContext).execute(String.valueOf(problem.getId()));
                    MainActivity.slidingLayer.closeLayer(true);
                    EcoMapFragment.isOpenSlidingLayer = false;
                }
            }
        });

        alert.setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();
    }

    public void showAlertDialogEditProblem(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

        alert.setMessage(message);
        alert.setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();
    }
}
