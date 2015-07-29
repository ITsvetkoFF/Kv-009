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

/**
 * Created by hp1 on 21-01-2015.
 */
public class Top10Tab2 extends Fragment {

    String  top10hot []={"","","","","","","","","",""};


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_top_2, container, false);

        Cursor cursor = getActivity().getContentResolver()
                .query(EcoMapContract.ProblemsEntry.CONTENT_URI, null, null, null,"("+EcoMapContract.ProblemsEntry.COLUMN_SEVERITY + ") DESC");


        cursor.moveToFirst();
        top10hot[0] =cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_TITLE));
        top10hot[0]= "1."+top10hot[0]+".";


        int i=1;
        int j=2;
        for(i=1;i<10;i++){

            cursor.moveToNext();
            top10hot[i] =cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_TITLE));
            top10hot[i]= j+ "." + top10hot[i]+".";
           j++;
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
