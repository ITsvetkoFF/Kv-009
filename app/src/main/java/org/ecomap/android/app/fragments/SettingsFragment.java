package org.ecomap.android.app.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import org.ecomap.android.app.R;

/**
 * Created by Stanislav on 31.07.2015.
 */
public class SettingsFragment extends Fragment {
    private View v;
    private Spinner mLanguageSpinner;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        v = inflater.inflate(R.layout.settings_fragment,container, false);

        getActivity().setTitle(getString(R.string.item_settings));
        getActivity().invalidateOptionsMenu();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        mLanguageSpinner = (Spinner) v.findViewById(R.id.language_spinner);
        /*mLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Resources res = getResources();;
                DisplayMetrics dm = res.getDisplayMetrics();;
                Configuration conf = res.getConfiguration();
                Intent refresh = new Intent(getActivity(), MainActivity.class);
                switch (position) {
                    case 0:
                        conf.locale = new Locale("en");
                        res.updateConfiguration(conf, dm);
                        startActivity(refresh);
                        break;
                    case 1:
                        conf.locale = new Locale("ru");
                        res.updateConfiguration(conf, dm);
                        startActivity(refresh);
                        break;
                    case 2:
                        conf.locale = new Locale("uk");
                        res.updateConfiguration(conf, dm);
                        startActivity(refresh);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

    }
}
