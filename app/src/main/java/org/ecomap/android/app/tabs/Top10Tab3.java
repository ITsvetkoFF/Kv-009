package org.ecomap.android.app.tabs;



import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.ecomap.android.app.R;

/**
 * Created by hp1 on 21-01-2015.
 */
public class Top10Tab3 extends Fragment {

    String [] top10say = {"1. ТОП 10 обговорюваних проблем.","2. ТОП 10 обговорюваних проблем.","3. ТОП 10 обговорюваних проблем.","4. ТОП 10 обговорюваних проблем.","5. ТОП 10 обговорюваних проблем.","6. ТОП 10 обговорюваних проблем.","7. ТОП 10 обговорюваних проблем.","8. ТОП 10 обговорюваних проблем.","9. ТОП 10 обговорюваних проблем.","10. ТОП 10 обговорюваних проблем."};


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_top_3,container,false);

        ListView lvTopSay = (ListView) v.findViewById(R.id.lvSay);
        // создаем адаптер
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this.getActivity() ,R.layout.my_list_view3, top10say);
        // присваиваем адаптер списку
        lvTopSay.setAdapter(adapter3);

        return v;
    }
}
