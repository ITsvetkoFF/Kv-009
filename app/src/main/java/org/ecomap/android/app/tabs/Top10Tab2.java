package org.ecomap.android.app.tabs;



import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.ecomap.android.app.R;
import org.ecomap.android.app.data.EcoMapContract;

import java.util.ArrayList;

/**
 * Created by hp1 on 21-01-2015.
 */
public class Top10Tab2 extends Fragment {

    ArrayList top10hot = new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_top_2, container, false);

        Cursor cursor = getActivity().getContentResolver()
                .query(EcoMapContract.ProblemsEntry.CONTENT_URI, null, null, null,"("+EcoMapContract.ProblemsEntry.COLUMN_SEVERITY + ") DESC");

        cursor.moveToFirst();

        for(int i=0;i<10;i++){


            String votes =cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_SEVERITY));
            top10hot.add(i,"★ "+votes+"  " + cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_TITLE)) + ".");
            cursor.moveToNext();
        }

        cursor.close();

        ListView lvTopHot = (ListView) v.findViewById(R.id.lvHot);
        // создаем адаптер
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this.getActivity() ,R.layout.my_list_view2, top10hot);
        // присваиваем адаптер списку
        lvTopHot.setAdapter(adapter2);

        return v;
    }
}
