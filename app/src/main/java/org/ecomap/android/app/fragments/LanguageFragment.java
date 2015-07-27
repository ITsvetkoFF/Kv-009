package org.ecomap.android.app.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;

import java.util.Locale;

/**
 * Created by Stanislav on 22.07.2015.
 */
public class LanguageFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.choose_language)
                .setItems(R.array.supported_languages, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Resources res = getResources();;
                        DisplayMetrics dm = res.getDisplayMetrics();;
                        Configuration conf = res.getConfiguration();
                        Intent refresh = new Intent(getActivity(), MainActivity.class);
                        switch (which) {
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
                });
        return builder.create();
    }
}
