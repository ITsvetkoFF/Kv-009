package org.ecomap.android.app.tabs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.database.Cursor;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;

import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.fragments.EcoMapFragment;
import org.ecomap.android.app.utils.MapClustering;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Top10Tab1 extends Fragment {

    ArrayList top10pop = new ArrayList();


    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_top_1, container, false);

       final Cursor cursor1 = getActivity().getContentResolver()
                .query(EcoMapContract.ProblemsEntry.CONTENT_URI, null, null, null,"("+EcoMapContract.ProblemsEntry.COLUMN_NUMBER_OF_VOTES + ") DESC");

        cursor1.moveToFirst();

        for(int i=0;i<10;i++){

            String votes =cursor1.getString(cursor1.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_NUMBER_OF_VOTES));
            top10pop.add( i,"❤ "+votes+"  " + cursor1.getString(cursor1.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_TITLE)) +".");
            cursor1.moveToNext();
        }

        ListView lvTopPop = (ListView) v.findViewById(R.id.lvPop);  // создаем адаптер
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this.getActivity() ,R.layout.my_list_view1, top10pop);
        lvTopPop.setAdapter(adapter1);// присваиваем адаптер списку

        lvTopPop.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {

                cursor1.moveToPosition(position);
                ((MainActivity) getActivity()).selectItem(0);

               /// EcoMapFragment.isOpenSlidingLayer = true;

                EcoMapFragment.lastOpenProblem = new Problem(cursor1, getActivity());
                MapClustering.zoomCamera();

            }
        });


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();



    }
}



