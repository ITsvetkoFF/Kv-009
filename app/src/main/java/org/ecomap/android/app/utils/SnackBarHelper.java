package org.ecomap.android.app.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import org.ecomap.android.app.R;

/**
 * Created by y.ridkous@gmail.com on 26.07.2015.
 */
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
    public static void showSnackBar(Context context, View view, String message, int duration, int backgroundColor) {
        Snackbar snackbar = Snackbar.make(view.findViewById(android.R.id.content), message, duration);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(context.getResources().getColor(backgroundColor));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    /**
     * Overrides showInfoSnackBar in order to use strings from xml
     */
    public static void showInfoSnackBar(Context context, View view, int messageResource, int duration) {
        showInfoSnackBar(context, view, context.getString(messageResource), duration);
    }

    //Shows information snack bar
    public static void showInfoSnackBar(Context context, View view, String message, int duration) {
        showSnackBar(context, view, message, duration, R.color.snackBarInfo);
    }

    public static void showWarningSnackBar(Context context, View view, int messageResource, int duration) {
        showWarningSnackBar(context, view, context.getString(messageResource), duration);
    }

    //Shows warning snack bar
    public static void showWarningSnackBar(Context context, View view, String message, int duration) {
        showSnackBar(context, view, message, duration, R.color.snackBarWarning);
    }

    public static void showSuccessSnackBar(Context context, View view, int messageResource, int duration) {
        showSuccessSnackBar(context, view, context.getString(messageResource), duration);
    }

    //Shows success snack bar
    public static void showSuccessSnackBar(Context context, View view, String message, int duration) {
        showSnackBar(context, view, message, duration, R.color.snackBarSuccess);
    }
}
