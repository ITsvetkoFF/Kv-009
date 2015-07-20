package org.ecomap.android.app.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * Created by y.ridkous@gmail.com on 06.07.2015.
 */
public class ZoomableImageView extends ImageViewTouch {

    private long mDeBounce = 0;
    private List<OnSingleTouchListener> observers = new ArrayList<OnSingleTouchListener>();
    private int intStartX;
    private int intStartY;

    public interface OnSingleTouchListener {
        void OnSingleTouch();
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {

                Log.v("MotionEvent:", motionEvent.toString());

//                if (Math.abs(mDeBounce - motionEvent.getEventTime()) < 50) {
//                    //Ignore if it's been less then 250ms since
//                    //the item was last clicked
//                    return false;
//                }

                int intCurrentY = Math.round(motionEvent.getY());
                int intCurrentX = Math.round(motionEvent.getX());

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    intStartY = motionEvent.getHistorySize() > 0 ? Math.round(motionEvent.getHistoricalY(0)) : intCurrentY;
                    intStartX = motionEvent.getHistorySize() > 0 ? Math.round(motionEvent.getHistoricalX(0)) : intCurrentX;
                }

                if ((motionEvent.getAction() == MotionEvent.ACTION_UP) && (Math.abs(intCurrentX - intStartX) < 3) && (Math.abs(intCurrentY - intStartY) < 3)) {
//                    if (mDeBounce > motionEvent.getDownTime()) {
//                        //Still got occasional duplicates without this
//                        return false;
//                    }

                    //Handle the click
                    fireOnSingleTouch();
                    intStartX = 0; intStartY = 0;

                    mDeBounce = motionEvent.getEventTime();
                    return false;
                }
                return false;

            }
        });
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public boolean registerOnSingleTouchListener(OnSingleTouchListener listener){
        return observers.add(listener);
    }

    public boolean unregisterOnSingleTouchListener(OnSingleTouchListener listener){
        return observers.remove(listener);
    }


    public void fireOnSingleTouch() {
        for (OnSingleTouchListener observer : observers) {
            observer.OnSingleTouch();
        }

    }


}
