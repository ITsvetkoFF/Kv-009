package org.ecomap.android.app.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.sync.EditProblemTask;
import org.ecomap.android.app.utils.NetworkAvailability;

public class EditProblemFragment extends Fragment {
    private View view;
    private EditText problemTitle, problemDescription, problemSolution;
    private TextInputLayout tilProblemTitle, tilProblemDescription, tilProblemSolution;
    private RadioButton solvedProblem, unsolvedProblem;
    private RatingBar ratingBar;
    private Spinner spinner;
    private Button sendProblemButton;
    private int problemType;
    private MapView mapView;
    private GoogleMap mMap;
    private Marker marker;
    private UiSettings uiSettings;
    private Problem problem;
    String [] params;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        view = inflater.inflate(R.layout.edit_problem_layout, container, false);

        mapView = (MapView) view.findViewById(R.id.edit_littleMap);
        mapView.onCreate(null);

        MapsInitializer.initialize(getActivity());

        mMap = mapView.getMap();

        uiSettings = mMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);

        problem = MainActivity.currentProblem;

        if (problem != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(problem.getPosition(), 16));

            marker = mMap.addMarker(new MarkerOptions().draggable(true).position(problem.getPosition()));
            marker.setIcon(BitmapDescriptorFactory.fromResource(problem.getResId()));
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        problemTitle = (EditText) view.findViewById(R.id.edit_problemTitle);

        problemDescription = (EditText) view.findViewById(R.id.edit_problemDescription);
        problemDescription.setText(problem.getDescription());

        problemSolution = (EditText) view.findViewById(R.id.edit_problemSolution);
        problemSolution.setText(problem.getSolution());

        solvedProblem = (RadioButton) view.findViewById(R.id.edit_rb_solved);
        unsolvedProblem = (RadioButton) view.findViewById(R.id.edit_rb_unsolved);
        if (problem.getStatus().equalsIgnoreCase("SOLVED")){
            solvedProblem.setChecked(true);
        } else {
            unsolvedProblem.setChecked(true);
        }

        ratingBar = (RatingBar) view.findViewById(R.id.edit_ratingBar);
        ratingBar.setRating(Integer.valueOf(problem.getSeverity()));

        spinner = (Spinner) view.findViewById(R.id.edit_spinner);
        spinner.setSelection(problem.getTypeId() - 1);

        sendProblemButton = (Button) view.findViewById(R.id.edit_send_problem);

        tilProblemTitle = (TextInputLayout) view.findViewById(R.id.til_edit_problemTitle);
        tilProblemTitle.setErrorEnabled(true);
        problemTitle.setText(problem.getTitle());
        problemTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (!hasFocus) {
                    if (problemTitle.getText().toString().isEmpty()) {
                        tilProblemTitle.setError(getString(R.string.problem_title_blank));
                        sendProblemButton.setClickable(false);
                    } else {
                        tilProblemTitle.setErrorEnabled(false);
                        sendProblemButton.setClickable(true);
                    }
                } else {
                    if (!problemTitle.getText().toString().isEmpty()) {
                        tilProblemTitle.setErrorEnabled(false);
                        sendProblemButton.setClickable(true);
                    }
                }
            }
        });

        tilProblemDescription = (TextInputLayout) view.findViewById(R.id.til_edit_problemDescription);
        tilProblemDescription.setErrorEnabled(true);

        tilProblemSolution = (TextInputLayout) view.findViewById(R.id.til_edit_problemSolution);
        tilProblemSolution.setErrorEnabled(true);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                problemType = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                ((MainActivity) getActivity()).selectItem(MainActivity.NAV_MAP);
            }
        });

        sendProblemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new NetworkAvailability(getActivity().getSystemService(Context.CONNECTIVITY_SERVICE)).
                        isNetworkAvailable()){
                    params = new String[10];

                    params[0] = getStatus();
                    params[1] = String.valueOf(ratingBar.getNumStars());
                    params[2] = problemTitle.getText().toString();
                    params[3] = String.valueOf(problemType);
                    params[4] = problemDescription.getText().toString();
                    params[5] = problemSolution.getText().toString();
                    params[6] = "1";
                    params[7] = String.valueOf(problem.getPosition().latitude);
                    params[8] = String.valueOf(problem.getPosition().longitude);
                    params[9] = String.valueOf(problem.getId());

                    new EditProblemTask(getActivity()).execute(params);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(getString(R.string.edit_problem_fragment));
    }

    private String getStatus(){
        if (solvedProblem.isChecked()){
            return "SOLVED";
        } else {
            return "UNSOLVED";
        }
    }

}
