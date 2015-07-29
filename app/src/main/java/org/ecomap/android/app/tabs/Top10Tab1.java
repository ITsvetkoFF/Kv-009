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
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.data.EcoMapContract;

import java.util.ArrayList;


/**
 * Created by hp1 on 21-01-2015.
 */
public class Top10Tab1 extends Fragment implements View.OnClickListener{

    ArrayList top10pop = new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_top_1, container, false);

        Cursor cursor = getActivity().getContentResolver()
                .query(EcoMapContract.ProblemsEntry.CONTENT_URI, null, null, null,"("+EcoMapContract.ProblemsEntry.COLUMN_NUMBER_OF_VOTES + ") DESC");

        cursor.moveToFirst();

        for(int i=0;i<10;i++){

            top10pop.add(cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_TITLE)));
            String votes =cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_NUMBER_OF_VOTES));

            top10pop.add( i,"❤ "+votes+"  " + top10pop.get(i) +".");
            cursor.moveToNext();
        }

        cursor.close();



        ListView lvTopPop = (ListView) v.findViewById(R.id.lvPop);  // создаем адаптер
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this.getActivity() ,R.layout.my_list_view1, top10pop);

        lvTopPop.setAdapter(adapter1);// присваиваем адаптер списку
        lvTopPop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {



                String message = "Деталі проблеми:  " + top10pop.get(position) + ".";
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });




        return v;
    }

    @Override
    public void onClick(View v) {



    }
}
