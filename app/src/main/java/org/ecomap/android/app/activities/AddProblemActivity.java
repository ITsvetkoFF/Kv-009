package org.ecomap.android.app.activities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import org.ecomap.android.app.User;
import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.data.model.ProblemPhotoEntry;
import org.ecomap.android.app.fragments.EcoMapFragment;
import org.ecomap.android.app.sync.UploadingServiceSession;
import org.ecomap.android.app.tasks.AddProblemTask;
import org.ecomap.android.app.utils.AddPhotoImageAdapter;
import org.ecomap.android.app.utils.MapClustering;
import org.ecomap.android.app.utils.NetworkAvailability;
import org.ecomap.android.app.utils.SharedPreferencesHelper;
import org.ecomap.android.app.utils.SnackBarHelper;
import org.ecomap.android.app.widget.NonScrollableListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;


public class AddProblemActivity extends AppCompatActivity implements UploadingServiceSession.Callbacks {

    private EditText problemTitle;
    private EditText problemDescription;
    private EditText problemSolution;

    private TextInputLayout tilProblemTitle;

    private static NonScrollableListView nonScrollableListView;
    public static final ArrayList<ProblemPhotoEntry> selectedPhotos = new ArrayList<>();
    private AddPhotoImageAdapter imgAdapter;

    private final int REQUEST_CODE = 1;
    private int problemType;

    private GoogleMap mMap;
    private MapClustering mapClusterer;

    private LatLng markerPosition;
    private CameraPosition cameraPosition;

    private static Context mContext;
    private UploadingServiceSession mServiceSession;


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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.add_problem_map);
        mMap = mapFragment.getMap();

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);

        mapClusterer = new MapClustering(mMap, mContext);
        markerPosition = EcoMapFragment.getMarkerPosition();
        cameraPosition = EcoMapFragment.getCameraPosition();

        setMarkerToMap();

        mServiceSession = new UploadingServiceSession(this, getClass().getCanonicalName(), this);

        problemTitle = (EditText) findViewById(R.id.problemTitle);
        problemDescription = (EditText) findViewById(R.id.problemDescription);
        problemSolution = (EditText) findViewById(R.id.problemSolution);

        tilProblemTitle = (TextInputLayout) findViewById(R.id.til_problemTitle);
        tilProblemTitle.setErrorEnabled(true);

        TextInputLayout tilProblemDescription = (TextInputLayout) findViewById(R.id.til_problemDescription);
        tilProblemDescription.setErrorEnabled(true);

        TextInputLayout tilProblemSolution = (TextInputLayout) findViewById(R.id.til_problemSolution);
        tilProblemSolution.setErrorEnabled(true);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        Button addPhotoButton = (Button) findViewById(R.id.add_photo);

        nonScrollableListView = (NonScrollableListView) findViewById(R.id.add_problem_non_scrollable_list_view);
        imgAdapter = new AddPhotoImageAdapter(selectedPhotos);
        nonScrollableListView.setAdapter(imgAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.add_problem_toolbar);
        setSupportActionBar(toolbar);

        try {
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.add_problem_collapsing_toolbar);
        collapsingToolbar.setTitle(getString(R.string.item_addProblem));
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

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
                final ArrayList<String> list = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                for (String imgURL : list) {
                    selectedPhotos.add(new ProblemPhotoEntry("", imgURL));
                }
                imgAdapter.updateDataSet(selectedPhotos);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_problem, menu);
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
                selectedPhotos.clear();
                this.finish();

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
        selectedPhotos.clear();
    }

    public static NonScrollableListView getNonScrollableListView() {
        return nonScrollableListView;
    }

    private void hideSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        View v = this.getCurrentFocus();
        inputMethodManager.hideSoftInputFromWindow(v != null ? v.getWindowToken() : null, 0);
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
            String[] params = new String[9];

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
                    new AddProblemTask(mContext, mServiceSession, this).execute(params);
                }

            } else {

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

                ContentValues contentValuesProblems = new ContentValues();
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_STATUS, params[0]);
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_SEVERITY, params[1]);
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_TITLE, params[2]);
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_PROBLEM_TYPE_ID, params[3]);
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_CONTENT, params[4]);
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_PROPOSAL, params[5]);
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_REGION_ID, params[6]);
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_LATITUDE, params[7]);
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_LONGTITUDE, params[8]);
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_USER_FIRST_NAME, User.getFirstName());
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_USER_LAST_NAME, User.getLastName());
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_DATE, format.format(new Date()));
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_USER_ID, User.getUserId());
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_COMMENTS_NUMBER, 0);
                contentValuesProblems.put(EcoMapContract.ProblemsEntry.COLUMN_NUMBER_OF_VOTES, 0);

                JSONArray pendingIDsArray = new JSONArray();
                //Checking selected photos
                if (!selectedPhotos.isEmpty()) {
                    for (int i = 0; i < selectedPhotos.size(); i++) {
                        //Get path for each photo
                        final ProblemPhotoEntry photoEntry = selectedPhotos.get(i);
                        final String path = photoEntry.getImgURL();
                        final String comment = photoEntry.getCaption();

                        //Start new AsyncTask for each photo and comment (test problem ID is 361)
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("path", path);
                            jsonObject.put("comment", comment);
                            pendingIDsArray.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {

                    ContentValues contentValuesPendingIDs = new ContentValues();
                    Uri uri_id = getContentResolver().insert(EcoMapContract.ProblemsEntry.CONTENT_URI, contentValuesProblems);

                    long res_id = ContentUris.parseId(uri_id);
                    contentValuesPendingIDs.put(EcoMapContract.PendingProblemsEntry.COLUMN_PROBLEM_ID, String.valueOf(res_id));
                    contentValuesPendingIDs.put(EcoMapContract.PendingProblemsEntry.COLUMN_PHOTOS, pendingIDsArray.toString());

                    Uri res = getContentResolver().insert(EcoMapContract.PendingProblemsEntry.CONTENT_URI, contentValuesPendingIDs);
                    res_id = ContentUris.parseId(res);

                    if (res_id > 0)
                        SharedPreferencesHelper.setFlagPendingProblemsOn();

                }catch (SQLiteException e){
                    String LOG_TAG = AddProblemActivity.class.getSimpleName();
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
                EcoMapFragment.disableAddProblemMode();
                finish();

                SnackBarHelper.showInfoSnackBar(this, R.string.check_internet, Snackbar.LENGTH_LONG);
            }
        } else {
            tilProblemTitle.setError(getString(R.string.problem_title_blank));
            SnackBarHelper.showWarningSnackBar(this, R.string.problem_title_blank, Snackbar.LENGTH_SHORT);
        }
    }

}
