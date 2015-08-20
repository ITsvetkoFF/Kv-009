package org.ecomap.android.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.ecomap.android.app.R;
import org.ecomap.android.app.fragments.EcoMapFragment;
import org.ecomap.android.app.sync.AddProblemTask;
import org.ecomap.android.app.sync.UploadingServiceSession;
import org.ecomap.android.app.ui.components.NonScrollableListView;
import org.ecomap.android.app.utils.AddPhotoImageAdapter;
import org.ecomap.android.app.utils.MapClustering;
import org.ecomap.android.app.utils.NetworkAvailability;
import org.ecomap.android.app.utils.SnackBarHelper;

import java.util.ArrayList;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;


public class AddProblemActivity extends AppCompatActivity implements UploadingServiceSession.Callbacks {

    private EditText problemTitle;
    private EditText problemDescription;
    private EditText problemSolution;

    private TextInputLayout tilProblemTitle;
    private TextInputLayout tilProblemDescription;
    private TextInputLayout tilProblemSolution;

    private Spinner spinner;
    private Button addPhotoButton;
    private MenuItem doneMenu;

    private static NonScrollableListView nonScrollableListView;
    public static ArrayList<String> selectedPhotos = new ArrayList<>();
    public AddPhotoImageAdapter imgAdapter;

    public static final int REQUEST_CODE = 1;
    private int problemType;
    private String[] params;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private MapClustering mapClusterer;

    private UiSettings uiSettings;
    private LatLng markerPosition;
    private CameraPosition cameraPosition;

    private static Context mContext;
    private UploadingServiceSession mServiceSession;

    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;

    @Override
    protected void onResume() {
        super.onResume();

        mServiceSession.doBindService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_problem);

        mContext = this;

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.add_problem_map);
        mMap = mapFragment.getMap();

        uiSettings = mMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);

        mapClusterer = new MapClustering(mMap, mContext);
        markerPosition = EcoMapFragment.getMarkerPosition();
        cameraPosition = EcoMapFragment.getCameraPosition();

        setMarkerToMap();

        mServiceSession = new UploadingServiceSession(mContext, getClass().getCanonicalName(), this);

        problemTitle = (EditText) findViewById(R.id.problemTitle);
        problemDescription = (EditText) findViewById(R.id.problemDescription);
        problemSolution = (EditText) findViewById(R.id.problemSolution);

        tilProblemTitle = (TextInputLayout) findViewById(R.id.til_problemTitle);
        tilProblemTitle.setErrorEnabled(true);


        tilProblemDescription = (TextInputLayout) findViewById(R.id.til_problemDescription);
        tilProblemDescription.setErrorEnabled(true);

        tilProblemSolution = (TextInputLayout) findViewById(R.id.til_problemSolution);
        tilProblemSolution.setErrorEnabled(true);

        spinner = (Spinner) findViewById(R.id.spinner);
        addPhotoButton = (Button) findViewById(R.id.add_photo);

        nonScrollableListView = (NonScrollableListView) findViewById(R.id.add_problem_non_scrollable_list_view);
        imgAdapter = new AddPhotoImageAdapter(mContext, selectedPhotos);
        nonScrollableListView.setAdapter(imgAdapter);

        toolbar = (Toolbar) findViewById(R.id.add_problem_toolbar);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        collapsingToolbar = (CollapsingToolbarLayout)findViewById(R.id.add_problem_collapsing_toolbar);
        collapsingToolbar.setTitle(getString(R.string.item_addProblem));
        collapsingToolbar.setExpandedTitleColor(android.R.color.transparent);

        problemTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (!hasFocus) {

                    if (problemTitle.getText().toString().isEmpty()) {
                        tilProblemTitle.setError(getString(R.string.problem_title_blank));
                    } else {
                        tilProblemTitle.setErrorEnabled(false);
                    }
                } else {

                    if (!problemTitle.getText().toString().isEmpty()) {
                        tilProblemTitle.setErrorEnabled(false);
                    }
                }
            }
        });

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
                markerPosition = latLng;
                EcoMapFragment.setMarkerPosition(markerPosition);

                mapClusterer.addMarkerToMap(markerPosition);
            }
        });

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send intent to library for picking photos
                PhotoPickerIntent intent = new PhotoPickerIntent(mContext);
                intent.setPhotoCount(8);
                intent.setShowCamera(true);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Getting photo paths from lib
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data != null) {
                selectedPhotos.clear();
                selectedPhotos.addAll(data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS));
                imgAdapter.updateDataSet(selectedPhotos);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_problem, menu);
        doneMenu = menu.findItem(R.id.add_problem_done_menu_item);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                EcoMapFragment.disableAddProblemMode();
                startActivity(new Intent(mContext, MainActivity.class));
                selectedPhotos.clear();

                return true;

            case R.id.add_problem_done_menu_item:

                sendProblemToServer();
                hideSoftInput();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        mServiceSession.doUnbindService();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        EcoMapFragment.disableAddProblemMode();
        selectedPhotos.clear();
        super.onBackPressed();
    }

    @Override
    public void allTasksFinished() {

    }

    public static NonScrollableListView getNonScrollableListView() {
        return nonScrollableListView;
    }

    private void hideSoftInput () {
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(this.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }

    private void setMarkerToMap() {
        if (markerPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, cameraPosition.zoom));

            mapClusterer.addMarkerToMap(markerPosition);
        }
    }

    private void sendProblemToServer() {

        if (!problemTitle.getText().toString().isEmpty()) {

            tilProblemTitle.setErrorEnabled(false);
            params = new String[9];

            params[0] = "UNSOLVED";
            params[1] = "3";
            params[2] = problemTitle.getText().toString();
            params[3] = String.valueOf(problemType);
            params[4] = problemDescription.getText().toString();

            params[5] = problemSolution.getText().toString();
            params[6] = "1";
            params[7] = String.valueOf(markerPosition.latitude);
            params[8] = String.valueOf(markerPosition.longitude);

            if (new NetworkAvailability(mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).isNetworkAvailable()) {

                if (mServiceSession.isBound()) {
                    new AddProblemTask(mContext, mServiceSession).execute(params);
                }

            } else {
                SnackBarHelper.showInfoSnackBar(this, R.string.check_internet, Snackbar.LENGTH_LONG);
            }
        } else {
            tilProblemTitle.setError(getString(R.string.problem_title_blank));
            SnackBarHelper.showWarningSnackBar(this, R.string.problem_title_blank, Snackbar.LENGTH_SHORT);
        }
    }

}
