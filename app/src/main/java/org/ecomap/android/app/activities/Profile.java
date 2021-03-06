package org.ecomap.android.app.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.ecomap.android.app.R;
import org.ecomap.android.app.User;
import org.ecomap.android.app.utils.SharedPreferencesHelper;

import java.util.Random;

public class Profile extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        setTitle(getString(R.string.profile));

        TextView firstName = (TextView) findViewById(R.id.profile_firstname);
        TextView lastName = (TextView) findViewById(R.id.profile_lastname);
        TextView role = (TextView) findViewById(R.id.profile_role);
        TextView email = (TextView) findViewById(R.id.profile_email);
        //TextView resetPassword = (TextView) findViewById(R.id.profile_change_password);
        Button logout = (Button) findViewById(R.id.profile_logout);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        try {
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.profile_collapsing_toolbar);
        collapsingToolbar.setTitle(getString(R.string.profile));
        final ImageView profileBackdrop = (ImageView) findViewById(R.id.profile_backdrop);
        profileBackdrop.setImageResource(setRandomHead());

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(Profile.this);
                alert.setMessage(getString(R.string.want_logout));
                alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.cookieManager.getCookieStore().removeAll();
                        MainActivity.setUserId(null);
                        MainActivity.changeAuthorizationState();

                        SharedPreferencesHelper.onLogOutClearPref(Profile.this);

                        User.deleteUserInstance();

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

        firstName.setText(User.getFirstName());
        lastName.setText(User.getLastName());
        email.setText(User.getEmail());
        role.setText(User.getRole());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private int setRandomHead() {
        Random random = new Random();
        int id = random.nextInt((12 - 1) + 1) + 1;

        switch (id) {
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


