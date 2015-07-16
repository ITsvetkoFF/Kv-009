package org.ecomap.android.app.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;

public class LogoutFragment extends DialogFragment {

    private Button yesB, noB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.logout_layout, container, false);
        getDialog().setTitle("Logout");
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        yesB = (Button) getView().findViewById(R.id.logout_button_yes);
        yesB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.cookieManager.getCookieStore().removeAll();
                MainActivity.setUserId(null);
                MainActivity.changeAuthorizationState();
                dismiss();
            }
        });

        noB = (Button) getView().findViewById(R.id.logout_button_no);
        noB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
