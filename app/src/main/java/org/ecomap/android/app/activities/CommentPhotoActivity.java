package org.ecomap.android.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.data.model.ProblemPhotoEntry;
import org.ecomap.android.app.fragments.EcoMapFragment;
import org.ecomap.android.app.sync.UploadingServiceSession;
import org.ecomap.android.app.tasks.GetPhotosTask;
import org.ecomap.android.app.utils.AddPhotoImageAdapter;
import org.ecomap.android.app.utils.NetworkAvailability;
import org.ecomap.android.app.widget.NonScrollableListView;

import java.util.ArrayList;

public class CommentPhotoActivity extends AppCompatActivity {

    private ArrayList<ProblemPhotoEntry> selectedPhotos = new ArrayList<>();
    private int problem_id;

    private UploadingServiceSession uploadingSession;


    private static class CallbackListener implements UploadingServiceSession.Callbacks {

        @Override
        public void allTasksFinished() {
            new GetPhotosTask(MainActivity.getEcoMapFragment()).execute(EcoMapFragment.getLastOpenProblem().getId());
        }

    }

    public CommentPhotoActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_photos_layout);

        Toolbar mToolbar = (Toolbar) this.findViewById(R.id.comments_toolbar);
        this.setSupportActionBar(mToolbar);
        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(me.iwf.photopicker.R.string.images);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        ArrayList<String> list = new ArrayList<>();
        if (extras != null) {
            problem_id = extras.getInt("problem_id");
            list = extras.getStringArrayList("selectedPhotos");
            for (String imgURL : list) {
                selectedPhotos.add(new ProblemPhotoEntry("", imgURL));
            }
        }

        NonScrollableListView nonScrollableListView = (NonScrollableListView) findViewById(R.id.nonScrollableListView);

        AddPhotoImageAdapter imgAdapter = new AddPhotoImageAdapter(selectedPhotos);
        nonScrollableListView.setAdapter(imgAdapter);

        uploadingSession = new UploadingServiceSession(this, getClass().getCanonicalName(), new CallbackListener());


    }

    @Override
    protected void onResume() {
        uploadingSession.doBindService();
        super.onResume();
    }

    @Override
    protected void onStop() {
        uploadingSession.doUnbindService();
        uploadingSession.mContext = null;
        super.onStop();
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

    private void sendPhoto() {
        View view;
        EditText editText;
        String path;
        String comment;

        ArrayList<ProblemPhotoEntry> photos = new ArrayList<>();

        NonScrollableListView nonScrollableListView = (NonScrollableListView) findViewById(R.id.nonScrollableListView);

        uploadingSession.doStartService();

        //Checking selected photos
        for (int i = 0; i < selectedPhotos.size(); i++) {
            //Get path for each photo
            ProblemPhotoEntry photoEntry = selectedPhotos.get(i);
            path = photoEntry.getImgURL();
            comment = photoEntry.getCaption();

            photos.add(new ProblemPhotoEntry(comment, path));

            if (uploadingSession.isBound()) {
                uploadingSession.sendUploadRequest(problem_id, path, comment);
            }

        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View focusedView = getCurrentFocus();

        if (focusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }

        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("photos", photos);
        setResult(RESULT_OK, intent);
        finish();

    }

}
