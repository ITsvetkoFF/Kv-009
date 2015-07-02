package org.ecomap.android.app.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ecomap.android.app.R;

/**
 * Created by yridk_000 on 24.06.2015.
 */
public class ProblemDetailsFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.problem_details_layout, container, false);
//        TextView txt = (TextView) view.findViewById(R.id.tabNumber);
//        Bundle args = getArguments();
//        txt.setText(String.valueOf(args.getInt(ARG_SECTION_NUMBER)));
        return view;
    }
}
