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
public class Top10Tab2 extends Fragment {

    String [] top10hot = {"1. ТОП 10 важливих проблем","2. ТОП 10 важливих проблем","3. ТОП 10 важливих проблем","4. ТОП 10 важливих проблем","5. ТОП 10 важливих проблем","6. ТОП 10 важливих проблем","7. ТОП 10 важливих проблем","8. ТОП 10 важливих проблем","9. ТОП 10 важливих проблем","10. ТОП 10 важливих проблем"};


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_top_2,container,false);

        ListView lvTopHot = (ListView) v.findViewById(R.id.lvHot);
        // создаем адаптер
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this.getActivity() ,R.layout.my_list_view2, top10hot);
        // присваиваем адаптер списку
        lvTopHot.setAdapter(adapter2);

        return v;
    }
}
