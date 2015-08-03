package org.ecomap.android.app.ui.components;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.wunderlist.slidinglayer.SlidingLayer;

public class EcoMapSlidingLayer extends SlidingLayer{

    private Context mContext;

    public EcoMapSlidingLayer(Context context) {
        this(context, null);
    }

    public EcoMapSlidingLayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EcoMapSlidingLayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        hideKeyBoard();
        return super.onTouchEvent(ev);
    }

    private void hideKeyBoard() {

        if (super.isOpened()) {

            Activity activity = (Activity) mContext;

            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            View focusedView = activity.getCurrentFocus();

            if (focusedView != null) {
                inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            }
        }
    }
}
