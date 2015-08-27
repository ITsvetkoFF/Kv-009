package org.ecomap.android.app.tabs;


import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.fragments.EcoMapFragment;
import org.ecomap.android.app.utils.top10Adapter;

import java.util.ArrayList;


public class Top10Tab extends Fragment {
    private Activity mActivity;

    public static Top10Tab newInstance(int tabId) {
        Top10Tab f = new Top10Tab();
        Bundle bdl = new Bundle(tabId);
        bdl.putInt("tabID", tabId);
        f.setArguments(bdl);

        //Log.d(Top10Tab.class.getSimpleName(), "newInstance: " + tabId);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.top10_tab, container, false);

        int tabId = getArguments().getInt("tabID");

        //Log.d(Top10Tab.class.getSimpleName(), "onCreateView: " + tabId);

        String sortOrder = "";

        switch (tabId) {
            case 0:
                sortOrder = "( " + EcoMapContract.ProblemsEntry.COLUMN_NUMBER_OF_VOTES + ") DESC LIMIT 10";
                break;
            case 1:
                sortOrder = "( " + EcoMapContract.ProblemsEntry.COLUMN_SEVERITY + ") DESC LIMIT 10";
                break;
            case 2:
                sortOrder = "( " + EcoMapContract.ProblemsEntry.COLUMN_COMMENTS_NUMBER + ") DESC LIMIT 10";
                break;
        }

        ArrayList<Problem> top10Problems = new ArrayList<>(10);

        //get data
        Cursor cursor = mActivity.getContentResolver()
                .query(EcoMapContract.ProblemsEntry.CONTENT_URI, null, null, null, sortOrder);

        if (cursor != null) {

            while (cursor.moveToNext()) {
                top10Problems.add(new Problem(cursor, getActivity()));
            }

            cursor.close();
        }

        ListView lvTopPop = (ListView) v.findViewById(R.id.lvPop);

        final top10Adapter adapter = new top10Adapter(getActivity(), top10Problems, tabId);

        lvTopPop.setAdapter(adapter);

        lvTopPop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {

                //to open the problem on a map, set lastOpenProblem and open SlidingPanel
                EcoMapFragment.lastOpenProblem = (Problem) adapter.getItem(position);
                EcoMapFragment.isOpenSlidingLayer = true;

                //to open the map, we use empty filter
                ((MainActivity) mActivity).filter("");

            }
        });

        return v;
    }

}
