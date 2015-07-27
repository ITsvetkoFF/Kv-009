package org.ecomap.android.app.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.ecomap.android.app.fragments.AddProblemFragment;
import org.ecomap.android.app.fragments.EcoMapFragment;

/**
* Created by Stanislav on 27.07.2015.
*/
public class PagerAdapter extends FragmentPagerAdapter {

    int numOfTabs;
    FragmentManager fragmentManager;

    public PagerAdapter (FragmentManager fm, int numOfTabs){
        super(fm);
        this.numOfTabs = numOfTabs;
        this.fragmentManager = fm;
    }

    @Override
    public Fragment getItem(int index) {
        Fragment fragment = null;
        String tag = null;
        switch (index){
            case 0:
/*
                    tag = EcoMapFragment.class.getSimpleName();
                    fragment = fragmentManager.findFragmentByTag(tag);
                    if (fragment == null) {
                        fragment = new EcoMapFragment();
                        return fragment;
                    } else {
                        return fragment;
                    }
*/

                return new EcoMapFragment();

            case 1:
                tag = AddProblemFragment.class.getSimpleName();
                fragment = fragmentManager.findFragmentByTag(tag);
                if (fragment == null) {
                    fragment = new AddProblemFragment();
                }

                return fragment;
        }
/*
            Bundle args = new Bundle();
            args.putDoubleArray("Position", EcoMapFragment.getMarkerPosition());
            fragment.setArguments(args);
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.addToBackStack(null);
            transaction.replace(R.id.content_frame, fragment, tag).commit();
*/
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
