package org.ecomap.android.app.tabs;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.fragments.EcoMapFragment;

import java.util.ArrayList;


public class Top10Tab2 extends Fragment {

    ArrayList top10hot = new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_top_2, container, false);

        final Cursor cursor2 = getActivity().getContentResolver()
                .query(EcoMapContract.ProblemsEntry.CONTENT_URI, null, null, null,"("+EcoMapContract.ProblemsEntry.COLUMN_SEVERITY + ") DESC");

        cursor2.moveToFirst();

        for(int i=0;i<10;i++){

            String votes =cursor2.getString(cursor2.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_SEVERITY));
            top10hot.add(i,"★ "+votes+"  " + cursor2.getString(cursor2.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_TITLE)) + ".");
            cursor2.moveToNext();
        }

        ListView lvTopHot = (ListView) v.findViewById(R.id.lvHot); // создаем адаптер
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this.getActivity() ,R.layout.my_list_view2, top10hot);
        lvTopHot.setAdapter(adapter2); // присваиваем адаптер списку

        lvTopHot.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {

                cursor2.moveToPosition(position);
                ((MainActivity) getActivity()).selectItem(0);

                EcoMapFragment.isOpenSlidingLayer = true;
                EcoMapFragment.lastOpenProblem = new Problem(cursor2, getActivity());

            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
