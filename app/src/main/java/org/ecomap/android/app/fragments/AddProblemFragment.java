package org.ecomap.android.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.ecomap.android.app.R;
import org.ecomap.android.app.sync.AddProblemTask;
import org.ecomap.android.app.ui.components.NonScrollableListView;
import org.ecomap.android.app.utils.AddPhotoImageAdapter;
import java.util.ArrayList;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

public class AddProblemFragment extends DialogFragment{

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

    private Button cancelButton;
    private Button sendProblemButton;
    private Button addPhotoButton;

    public static final int REQUEST_CODE = 1;
    private int problemType;
    private String[] params;

    public static AddProblemFragment newInstance(){

        AddProblemFragment fragment = new AddProblemFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.add_problem_layout, container, false);
        getDialog().setTitle("Add Problem Description");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity();

        problemTitle = (EditText) view.findViewById(R.id.problemTitle);
        problemDescription = (EditText) view.findViewById(R.id.problemDescription);
        problemSolution = (EditText) view.findViewById(R.id.problemSolution);

        spinner = (Spinner) view.findViewById(R.id.spinner);
        cancelButton = (Button) view.findViewById(R.id.cancel);
        sendProblemButton = (Button) view.findViewById(R.id.send_problem);
        addPhotoButton = (Button) view.findViewById(R.id.add_photo);

        tilProblemTitle = (TextInputLayout) view.findViewById(R.id.til_problemTitle);
        tilProblemTitle.setErrorEnabled(true);

        tilProblemDescription = (TextInputLayout) view.findViewById(R.id.til_problemDescription);
        tilProblemDescription.setErrorEnabled(true);

        tilProblemSolution = (TextInputLayout) view.findViewById(R.id.til_problemSolution);
        tilProblemSolution.setErrorEnabled(true);

        nonScrollableListView = (NonScrollableListView) view.findViewById(R.id.nonScrollableListView);
        imgAdapter = new AddPhotoImageAdapter(mContext, selectedPhotos);
        nonScrollableListView.setAdapter(imgAdapter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                problemType = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
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

                params = new String[9];

                params[0] = "UNSOLVED";
                params[1] = "3";
                params[2] = problemTitle.getText().toString();
                params[3] = String.valueOf(problemType);
                params[4] = problemDescription.getText().toString();
                params[5] = problemSolution.getText().toString();
                params[6] = "1";
                params[7] = String.valueOf(EcoMapFragment.getMarkerPosition().latitude);
                params[8] = String.valueOf(EcoMapFragment.getMarkerPosition().longitude);

                new AddProblemTask(mContext).execute(params);
            }
        });
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
}


