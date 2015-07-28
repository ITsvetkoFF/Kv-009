package org.ecomap.android.app.activities;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.ecomap.android.app.R;
import org.ecomap.android.app.fragments.LanguageFragment;
import org.ecomap.android.app.utils.SharedPreferencesHelper;

import java.util.Random;

public class Profile extends AppCompatActivity {
    //private FrameLayout head;
    private TextView firstName, lastName, role, email, resetPassword;
    private Button logout;

    public Profile() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        //head = (FrameLayout) view.findViewById(R.id.profile_head);
        firstName = (TextView) findViewById(R.id.profile_firstname);
        lastName = (TextView) findViewById(R.id.profile_lastname);
        role = (TextView) findViewById(R.id.profile_role);
        email = (TextView) findViewById(R.id.profile_email);
        resetPassword = (TextView) findViewById(R.id.profile_change_password);
        logout = (Button) findViewById(R.id.profile_logout);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout)findViewById(R.id.profile_collapsing_toolbar);
        collapsingToolbar.setTitle("Profile");
        final ImageView profileBackdrop = (ImageView)findViewById(R.id.profile_backdrop);
        profileBackdrop.setImageResource(setRandomHead());

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = Profile.this;
                AlertDialog.Builder alert = new AlertDialog.Builder(Profile.this);
                alert.setMessage(getString(R.string.want_logout));
                alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.cookieManager.getCookieStore().removeAll();
                        MainActivity.setUserId(null);
                        MainActivity.changeAuthorizationState();

                        SharedPreferencesHelper.onLogOutClearPref(context);

                        onBackPressed();
                    }
                });


                alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alert.show();
            }
        });

        firstName.setText(SharedPreferencesHelper.getStringPref(this, getResources().getString(R.string.fileNamePreferences), MainActivity.FIRST_NAME_KEY, ""));
        lastName.setText(SharedPreferencesHelper.getStringPref(this, getResources().getString(R.string.fileNamePreferences), MainActivity.LAST_NAME_KEY, ""));
        email.setText(SharedPreferencesHelper.getStringPref(this, getResources().getString(R.string.fileNamePreferences), MainActivity.EMAIL_KEY, ""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.language:
                DialogFragment df = new LanguageFragment();
                df.show(getFragmentManager(), "language");
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private int setRandomHead(){
        Random random = new Random();
        int id = random.nextInt((12 - 1) + 1) + 1;

        switch (id){
            case 1:
                return R.drawable.profile_head_0;
            case 2:
                return R.drawable.profile_head_1;
            case 3:
                return R.drawable.profile_head_2;
            case 4:
                return R.drawable.profile_head_3;
            case 5:
                return R.drawable.profile_head_4;
            case 6:
                return R.drawable.profile_head_5;
            case 7:
                return R.drawable.profile_head_6;
            case 8:
                return R.drawable.profile_head_7;
            case 9:
                return R.drawable.profile_head_8;
            case 10:
                return R.drawable.profile_head_9;
            case 11:
                return R.drawable.profile_head_10;
            case 12:
                return R.drawable.profile_head_11;
            default:
                return R.drawable.profile_head_0;
        }
    }
}


