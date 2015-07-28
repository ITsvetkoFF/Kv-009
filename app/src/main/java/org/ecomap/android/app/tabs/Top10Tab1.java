package org.ecomap.android.app.tabs;




import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
public class Top10Tab1 extends Fragment implements View.OnClickListener {



    String [] top10pop = {"1. ТОП 10 популярних проблем","2. ТОП 10 популярних проблем","3. ТОП 10 популярних проблем","4. ТОП 10 популярних проблем","5. ТОП 10 популярних проблем","6. ТОП 10 популярних проблем","7. ТОП 10 популярних проблем","8. ТОП 10 популярних проблем","9. ТОП 10 популярних проблем","10. ТОП 10 популярних проблем"};

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_top_1,container,false);

        ListView lvTopPop = (ListView) v.findViewById(R.id.lvPop);
        // создаем адаптер
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this.getActivity() ,R.layout.my_list_view1, top10pop);
        // присваиваем адаптер списку
        lvTopPop.setAdapter(adapter1);

        return v;
    }

    @Override
    public void onClick(View v) {



    }
}
