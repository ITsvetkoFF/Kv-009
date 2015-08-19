package org.ecomap.android.app.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.data.model.ProblemPhotoEntry;
import org.ecomap.android.app.fragments.EcoMapFragment;
import org.ecomap.android.app.fragments.LoginFragment;
import org.ecomap.android.app.sync.AddProblemTask;
import org.ecomap.android.app.sync.UploadPhotoTask;
import org.ecomap.android.app.ui.components.NonScrollableListView;
import org.ecomap.android.app.utils.AddPhotoImageAdapter;
import org.ecomap.android.app.utils.NetworkAvailability;

import java.util.ArrayList;

public class CommentPhotoActivity extends AppCompatActivity {

    private NonScrollableListView nonScrollableListView;
    private AddPhotoImageAdapter imgAdapter;
    private ArrayList<String> selectedPhotos = new ArrayList<>();
    private int problem_id;
    private Button sendProblemButton;

    public CommentPhotoActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_photos_layout);

        Toolbar mToolbar = (Toolbar)this.findViewById(R.id.comments_toolbar);
        this.setSupportActionBar(mToolbar);
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle(me.iwf.photopicker.R.string.images);
        try {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            problem_id = extras.getInt("problem_id");
            selectedPhotos = extras.getStringArrayList("selectedPhotos");
        }

        nonScrollableListView = (NonScrollableListView) findViewById(R.id.nonScrollableListView);
        imgAdapter = new AddPhotoImageAdapter(this, selectedPhotos);
        nonScrollableListView.setAdapter(imgAdapter);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            super.onBackPressed();
            return true;

        } else if (item.getItemId() == R.id.done_menu_item) {
            if (selectedPhotos.size() > 0) {

                if (new NetworkAvailability(getSystemService(Context.CONNECTIVITY_SERVICE))
                        .isNetworkAvailable()) {
                    sendPhoto();

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.check_internet), Toast.LENGTH_LONG).show();
                }

            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.comments_photos, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void sendPhoto(){
        View view;
        EditText editText;
        String path;
        String comment;

        ArrayList<ProblemPhotoEntry> photos = new ArrayList<ProblemPhotoEntry>();

        nonScrollableListView = (NonScrollableListView) findViewById(R.id.nonScrollableListView);

        //Checking selected photos
        for (int i = 0; i < selectedPhotos.size(); i++) {
            //Get each ListView item
            view = nonScrollableListView.getChildAt(i);
            editText = (EditText) view.findViewById(R.id.add_photo_edit_text);
            //Get comment
            comment = editText.getText().toString();
            //Get path for each photo
            path = selectedPhotos.get(i);

            photos.add(new ProblemPhotoEntry(comment,path));

        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View focusedView = getCurrentFocus();

        if (focusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }

        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("photos",photos);
        setResult(RESULT_OK,intent);
        finish();

    }

}
