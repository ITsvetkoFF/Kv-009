package org.ecomap.android.app.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import org.ecomap.android.app.R;

public class SnackBarHelper {
    /**
     * SnackBar stencils
     *
     * @param context
     * @param view
     * @param message
     * @param duration
     * @param backgroundColor
     */
    private static void showSnackBar(Context context, View view, String message, int duration, int backgroundColor) {
        Snackbar snackbar = Snackbar.make(view.findViewById(android.R.id.content), message, duration);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(context.getResources().getColor(backgroundColor));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }


    /**
     * INFO SNACK BARS
     * Overrides showInfoSnackBar in order to use strings from xml
     */
    private static void showInfoSnackBar(Activity activity, String message, int duration) {
        showSnackBar(activity, activity.getWindow().getDecorView(), message, duration, R.color.snackBarInfo);
    }

    public static void showInfoSnackBar(Activity activity, int messageResource, int duration) {
        showInfoSnackBar(activity, activity.getString(messageResource), duration);
    }

    public static void showInfoSnackBar(Context context, View view, String message, int duration) {
        showSnackBar(context, view, message, duration, R.color.snackBarInfo);
    }

    public static void showInfoSnackBar(Context context, View view, int messageResource, int duration) {
        showInfoSnackBar(context, view, context.getString(messageResource), duration);
    }



    /**
     * WARNING SNACK BARS
     *
     */

    private static void showWarningSnackBar(Activity activity, String message, int duration) {
        showSnackBar(activity, activity.getWindow().getDecorView(), message, duration, R.color.snackBarWarning);
    }

    public static void showWarningSnackBar(Activity activity, int messageResource, int duration) {
        showWarningSnackBar(activity, activity.getString(messageResource), duration);
    }

    public static void showWarningSnackBar(Context context, View view, int messageResource, int duration) {
        showWarningSnackBar(context, view, context.getString(messageResource), duration);
    }

    private static void showWarningSnackBar(Context context, View view, String message, int duration) {
        showSnackBar(context, view, message, duration, R.color.snackBarWarning);
    }


    /**
     * SUCCESS SNACK BARS
     */

    public static void showSuccessSnackBar(Activity activity, String message, int duration) {
        showSnackBar(activity, activity.getWindow().getDecorView(), message, duration, R.color.snackBarSuccess);
    }

    public static void showSuccessSnackBar(Activity activity, int messageResource, int duration) {
        showSuccessSnackBar(activity, activity.getString(messageResource), duration);
    }

    private static void showSuccessSnackBar(Context context, View view, String message, int duration) {
        showSnackBar(context, view, message, duration, R.color.snackBarSuccess);
    }

    public static void showSuccessSnackBar(Context context, View view, int messageResource, int duration) {
        showSuccessSnackBar(context, view, context.getString(messageResource), duration);
    }



}
