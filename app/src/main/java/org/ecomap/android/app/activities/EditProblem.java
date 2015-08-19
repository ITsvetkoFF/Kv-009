package org.ecomap.android.app.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

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
import org.ecomap.android.app.sync.EditProblemTask;
import org.ecomap.android.app.utils.NetworkAvailability;

/**
 * Created by Stanislav on 17.08.2015.
 */
public class EditProblem extends AppCompatActivity {
    private EditText problemTitle, problemDescription, problemSolution;
    private TextInputLayout tilProblemTitle, tilProblemDescription, tilProblemSolution;
    private SwitchCompat switchProblemStatus;
    private RatingBar ratingBar;
    private Spinner spinner;
    TextView switcherText;
    private int problemType;
    private MapView mapView;
    private GoogleMap mMap;
    private Marker marker;
    private UiSettings uiSettings;
    private Problem problem;
    String [] params;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_problem);

        problem = getIntent().getParcelableExtra("problem");

        setTitle(getString(R.string.edit_problem_activity));

        final Toolbar toolbar = (Toolbar) findViewById(R.id.edit_problem_toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout)findViewById(R.id.edit_problem_collapsing_toolbar);
        collapsingToolbar.setTitle(getString(R.string.edit_problem_activity));
        collapsingToolbar.setExpandedTitleColor(android.R.color.transparent);

        mapView = (MapView) findViewById(R.id.edit_backdrop_littleMap);
        mapView.onCreate(null);

        MapsInitializer.initialize(this);

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

        switcherText = (TextView) findViewById(R.id.edit_problem_switch_text);

        problemTitle = (EditText) findViewById(R.id.edit_problemTitle);

        problemDescription = (EditText) findViewById(R.id.edit_problemDescription);
        problemDescription.setText(problem.getDescription());

        problemSolution = (EditText) findViewById(R.id.edit_problemSolution);
        problemSolution.setText(problem.getSolution());

        switchProblemStatus = (SwitchCompat) findViewById(R.id.edit_problem_switch);
        switchProblemStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switcherText.setText(getString(R.string.solved_problem));
                    switcherText.setTextColor(getResources().getColor(R.color.green1));
                } else {
                    switcherText.setText(getString(R.string.unsolved_problem));
                    switcherText.setTextColor(Color.GRAY);
                }
            }
        });
        if (problem.getStatus().equalsIgnoreCase("SOLVED")){
            switchProblemStatus.setChecked(true);
            switcherText.setText(getString(R.string.solved_problem));
            switcherText.setTextColor(getResources().getColor(R.color.green1));
        } else {
            switchProblemStatus.setChecked(false);
            switcherText.setText(getString(R.string.unsolved_problem));
            switcherText.setTextColor(Color.GRAY);
        }

        ratingBar = (RatingBar) findViewById(R.id.edit_ratingBar);
        ratingBar.setRating(Integer.valueOf(problem.getSeverity()));
        Drawable progress = ratingBar.getProgressDrawable();
        DrawableCompat.setTint(progress, Color.GRAY);

        spinner = (Spinner) findViewById(R.id.edit_spinner);
        spinner.setSelection(problem.getTypeId() - 1);

        tilProblemTitle = (TextInputLayout) findViewById(R.id.til_edit_problemTitle);
        tilProblemTitle.setErrorEnabled(true);
        problemTitle.setText(problem.getTitle());


        tilProblemDescription = (TextInputLayout) findViewById(R.id.til_edit_problemDescription);

        tilProblemSolution = (TextInputLayout) findViewById(R.id.til_edit_problemSolution);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                problemType = position + 1;
                problem.setTypeId(problemType);
                problem.chooseIcon();
                marker.setIcon(BitmapDescriptorFactory.fromResource(problem.getResId()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });

        invalidateOptionsMenu();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_problem, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done_edit_problem_menu_item:
                if (new NetworkAvailability(getSystemService(Context.CONNECTIVITY_SERVICE)).
                        isNetworkAvailable()){
                    params = new String[10];

                    params[0] = getStatus();
                    params[1] = String.valueOf(Math.round(ratingBar.getRating()));
                    params[2] = problemTitle.getText().toString();
                    params[3] = String.valueOf(problemType);
                    params[4] = problemDescription.getText().toString();
                    params[5] = problemSolution.getText().toString();
                    params[6] = "1";
                    params[7] = String.valueOf(problem.getPosition().latitude);
                    params[8] = String.valueOf(problem.getPosition().longitude);
                    params[9] = String.valueOf(problem.getId());

                    new EditProblemTask(this).execute(params);

                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                            hideSoftInputFromWindow(getWindow().getDecorView().findViewById(android.R.id.content).getWindowToken(), 0);
                }

                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        setTitle(getString(R.string.edit_problem_fragment));

        problemTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (!hasFocus) {
                    if (problemTitle.getText().toString().isEmpty()) {
                        tilProblemTitle.setError(getString(R.string.problem_title_blank));
                        //doneItem.setVisible(false);
                        invalidateOptionsMenu();
                    } else {
                        tilProblemTitle.setErrorEnabled(false);
                        //doneItem.setVisible(true);
                        invalidateOptionsMenu();
                    }
                } else {
                    if (!problemTitle.getText().toString().isEmpty()) {
                        tilProblemTitle.setErrorEnabled(false);
                        //doneItem.setVisible(true);
                        invalidateOptionsMenu();
                    }
                }
            }
        });
    }

    private String getStatus(){
        if (switchProblemStatus.isChecked()){
            return "SOLVED";
        } else {
            return "UNSOLVED";
        }
    }
}
