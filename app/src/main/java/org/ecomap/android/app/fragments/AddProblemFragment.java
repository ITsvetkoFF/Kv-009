package org.ecomap.android.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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

import org.ecomap.android.app.R;
import org.ecomap.android.app.sync.AddProblemTask;
import org.ecomap.android.app.sync.UploadingServiceSession;
import org.ecomap.android.app.ui.components.NonScrollableListView;
import org.ecomap.android.app.utils.AddPhotoImageAdapter;
import org.ecomap.android.app.utils.NetworkAvailability;
import org.ecomap.android.app.utils.SnackBarHelper;

import java.util.ArrayList;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

public class AddProblemFragment extends Fragment implements UploadingServiceSession.Callbacks {

    private Context mContext;
    private View view;

    private EditText problemTitle;
    private EditText problemDescription;
    private EditText problemSolution;

    private TextInputLayout tilProblemTitle;
    private TextInputLayout tilProblemDescription;
    private TextInputLayout tilProblemSolution;
    private Spinner spinner;

    private static NonScrollableListView nonScrollableListView;
    public AddPhotoImageAdapter imgAdapter;
    public static ArrayList<String> selectedPhotos = new ArrayList<>();

    private Button sendProblemButton;
    private Button addPhotoButton;

    public static final int REQUEST_CODE = 1;
    private int problemType;
    private String[] params;

    private MapView mapView;
    private GoogleMap mMap;
    private Marker marker;
    private UiSettings uiSettings;

    public static LatLng markerPosition;

    private UploadingServiceSession mServiceSession;

    public static AddProblemFragment newInstance() {
        AddProblemFragment fragment = new AddProblemFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(getString(R.string.item_addProblem));

        //Connect to service for further uploading
        mServiceSession.doBindService();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        view = inflater.inflate(R.layout.add_problem_layout, container, false);

        mContext = getActivity();

        mapView = (MapView) view.findViewById(R.id.littleMap);
        mapView.onCreate(null);

        MapsInitializer.initialize(mContext);

        mMap = mapView.getMap();

        uiSettings = mMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);

        if (markerPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 16));

            marker = mMap.addMarker(new MarkerOptions().draggable(true).position(markerPosition));
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        }

        mServiceSession = new UploadingServiceSession(mContext, getClass().getCanonicalName(), this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        problemTitle = (EditText) view.findViewById(R.id.problemTitle);
        problemDescription = (EditText) view.findViewById(R.id.problemDescription);
        problemSolution = (EditText) view.findViewById(R.id.problemSolution);

        spinner = (Spinner) view.findViewById(R.id.spinner);
        sendProblemButton = (Button) view.findViewById(R.id.send_problem);
        addPhotoButton = (Button) view.findViewById(R.id.add_photo);

        tilProblemTitle = (TextInputLayout) view.findViewById(R.id.til_problemTitle);
        tilProblemTitle.setErrorEnabled(true);
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

        tilProblemDescription = (TextInputLayout) view.findViewById(R.id.til_problemDescription);
        tilProblemDescription.setErrorEnabled(true);

        tilProblemSolution = (TextInputLayout) view.findViewById(R.id.til_problemSolution);
        tilProblemSolution.setErrorEnabled(true);

        nonScrollableListView = (NonScrollableListView) view.findViewById(R.id.nonScrollableListView);
        imgAdapter = new AddPhotoImageAdapter(mContext, selectedPhotos);
        nonScrollableListView.setAdapter(imgAdapter);

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
                getActivity().onBackPressed();
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


        sendProblemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!problemTitle.getText().toString().isEmpty()) {
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

                    tilProblemTitle.setErrorEnabled(false);

                    if (new NetworkAvailability(getActivity().getSystemService(Context.CONNECTIVITY_SERVICE))
                            .isNetworkAvailable()) {
                        if (mServiceSession.isBound()) {
                            new AddProblemTask(mContext, mServiceSession).execute(params);
                        }


                    } else {
                        SnackBarHelper.showInfoSnackBar(mContext, view, R.string.check_internet, Snackbar.LENGTH_LONG);
                    }
                } else {
                    SnackBarHelper.showWarningSnackBar(mContext, view, R.string.problem_title_blank, Snackbar.LENGTH_SHORT);
                }


            }
        });
    }

    @Override
    public void onStop() {
        mServiceSession.doUnbindService();
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Getting photo paths from lib
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_CODE) {
            if (data != null) {
                selectedPhotos.clear();
                selectedPhotos = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                imgAdapter.updateDataSet(selectedPhotos);
            }
        }
    }

    public static NonScrollableListView getNonScrollableListView() {
        return nonScrollableListView;
    }

    public void setMarkerPosition(LatLng position) {
        markerPosition = position;
    }

    public boolean mustBeRemoved() {
        boolean result;

        if (!problemTitle.getText().toString().isEmpty() || !problemDescription.getText().toString().isEmpty() || !problemSolution.getText().toString().isEmpty()) {
            result = true;

        } else if (!selectedPhotos.isEmpty()) {
            result = true;

        } else if (problemType != 0) {
            result = true;

        } else {
            result = false;
        }

        return result;
    }

    @Override
    public void allTasksFinished() {
        //TODO: Show successful snack bar here
    }


}


