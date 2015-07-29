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
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.data.model.ProblemPhotoEntry;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.ecomap.android.app.sync.EcoMapService;
import org.ecomap.android.app.widget.ExpandableHeightGridView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ecomap.android.app.data.EcoMapDBHelper;


/**
 * Created by hp1 on 21-01-2015.
 */
public class Top10Tab1 extends Fragment implements View.OnClickListener{



    String [] top10pop = {"","","","","","","","","",""};

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_top_1,container,false);

        Cursor cursor = getActivity().getContentResolver()
                .query(EcoMapContract.ProblemsEntry.CONTENT_URI, null, null, null,"("+EcoMapContract.ProblemsEntry.COLUMN_NUMBER_OF_VOTES + ") DESC");


        cursor.moveToFirst();
        top10pop[0] =cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_TITLE));
        top10pop[0]= "1."+top10pop[0]+".";


        int i=1;
        int j=2;
        for(i=1;i<10;i++){

            cursor.moveToNext();
            top10pop[i] =cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_TITLE));
            top10pop[i]= j+ "." + top10pop[i]+".";
            j++;
        }

        cursor.close();

        ListView lvTopPop = (ListView) v.findViewById(R.id.lvPop);
        // создаем адаптер
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this.getActivity() ,R.layout.my_list_view1, top10pop);
        // присваиваем адаптер списку
        lvTopPop.setAdapter(adapter1);
        lvTopPop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {


                String message = "Деталі проблеми:  " + top10pop[position] + ".";
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });




        return v;
    }

    @Override
    public void onClick(View v) {



    }
}
