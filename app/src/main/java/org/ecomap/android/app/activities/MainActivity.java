/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ecomap.android.app.activities;


import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.ecomap.android.app.PersistentCookieStore;
import org.ecomap.android.app.R;
import org.ecomap.android.app.fragments.EcoMapFragment;
import org.ecomap.android.app.fragments.FiltersFragment;
import org.ecomap.android.app.fragments.LanguageFragment;
import org.ecomap.android.app.fragments.LoginFragment;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.ecomap.android.app.utils.SnackBarHelper;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


public class MainActivity extends AppCompatActivity implements FiltersFragment.Filterable {

    public static final String FIRST_NAME_KEY = "firstName";
    public static final String LAST_NAME_KEY = "lastName";
    public static final String EMAIL_KEY = "email";
    public static final String ROLE_KEY = "role";
    public static final String PASSWORD_KEY = "password";

    public static final int NAV_MAP = 0;
    public static final int NAV_DETAILS = 2;
    public static final int NAV_FILTER = 3;
    public static final int NAV_RESOURCES = 4;
    public static final int NAV_PROFILE = 5;

    public static CookieManager cookieManager;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String LAST_FRAGMENT_TAG = "LAST_FRAGMENT_TAG";
    private static String userId;
    private static boolean userIsAuthorized = false;
    private static String filterCondition = "";
    private static Context mContext;

    private DrawerLayout mDrawerLayout;
    private static NavigationView mNavigationView;

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private Toolbar toolbar;
    private Fragment mFragment;
    private FragmentManager mFragmentManager;
    private int mBackPressingCount;
    private long mLastBackPressMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBackPressingCount = 0;

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                switch (menuItem.getItemId()){
                    case R.id.map:
                        selectItem(0);
                        return true;
                    case R.id.statistics:
                        selectItem(1);
                        return true;
                    case R.id.top10:
                        selectItem(2);
                        return true;
                    case R.id.filters:
                        selectItem(3);
                        return true;
                    case R.id.resourses:
                        selectItem(4);
                        return true;
                    case R.id.login:
                        selectItem(5);
                        return true;
                    default:
                        selectItem(0);
                        return true;
                }
            }
        });
        cookieManager = new CookieManager(new PersistentCookieStore(this), CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);
        initUserIdFromCookies();

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                toolbar,                /* Toolbar */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                toolbar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                toolbar.setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        changeAuthorizationState();

        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(mFragment != null) {
            outState.putString(LAST_FRAGMENT_TAG, mFragment.getTag());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        String tag = savedInstanceState.getString(LAST_FRAGMENT_TAG, null);
        if(tag != null){
            mFragment = mFragmentManager.findFragmentByTag(tag);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.language:
                DialogFragment df = new LanguageFragment();
                df.show(getFragmentManager(), "language");
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        if(mFragment.getClass() == EcoMapFragment.class){
            EcoMapFragment frag = (EcoMapFragment)mFragment;
            if(frag.mSlidingLayer.isOpened()) {
                frag.mSlidingLayer.openPreview(true);
                return;
            }else if(frag.mSlidingLayer.isInPreviewMode()){
                frag.mSlidingLayer.closeLayer(true);
                return;
            }
        }

        if (mFragmentManager.getBackStackEntryCount() > 1 ) {
            super.onBackPressed();
        }else{

            mBackPressingCount++;
            if (System.currentTimeMillis() - mLastBackPressMillis > 1500) {
                mLastBackPressMillis = System.currentTimeMillis();
                mBackPressingCount = 1;
            }

            if (mBackPressingCount == 2) {
                ImageLoader.getInstance().stop();
                super.onBackPressed();
                return;
            }

            if (mBackPressingCount == 1) {
                mLastBackPressMillis = System.currentTimeMillis();
                SnackBarHelper.showInfoSnackBar(mContext, getWindow().getDecorView().findViewById(android.R.id.content), "Press back again to exit", Snackbar.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        toolbar.setTitle(mTitle);
    }

    @Override
    public void filter(String s) {
        filterCondition = s;
        selectItem(0);
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments

        boolean stop = false;
        String tag = null;

        switch (position) {
            case NAV_MAP:
                tag = EcoMapFragment.class.getSimpleName();
                chooseEcoMapFragment(filterCondition);
                break;
            case NAV_RESOURCES:
                /*tag = FiltersFragment.class.getSimpleName();
                mFragment = mFragmentManager.findFragmentByTag(tag);
                if(mFragment == null) {
                    mFragment = new FiltersFragment();
                }*/
                tag = MockFragment.class.getSimpleName();
                mFragment = mFragmentManager.findFragmentByTag(tag);
                if (mFragment == null) {
                    mFragment = new MockFragment();
                }
                break;
            case NAV_FILTER:
                tag = FiltersFragment.class.getSimpleName();
                mFragment = mFragmentManager.findFragmentByTag(tag);
                if (mFragment == null) {
                    mFragment = new FiltersFragment();
                }
                break;

            case NAV_DETAILS:
                tag = MockFragment.class.getSimpleName();
                mFragment = mFragmentManager.findFragmentByTag(tag);
                if (mFragment == null) {
                    mFragment = new MockFragment();
                }
                break;
            case NAV_PROFILE:
                if (isUserIdSet()) {
                    tag = LoginFragment.class.getSimpleName();
                    mFragment = mFragmentManager.findFragmentByTag(tag);

                    startActivity(new Intent(getApplicationContext(), Profile.class));
                    stop = true;
                    /*Snackbar snackbar = Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), getString(R.string.message_you_are_logged), Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.primary));
                    TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);//change Snackbar's text color;
                    snackbar.show();*/

                    break;
                } else {
                    tag = LoginFragment.class.getSimpleName();
                    mFragment = mFragmentManager.findFragmentByTag(tag);

                    if (mFragment == null) {
                        new LoginFragment().show(mFragmentManager, "login_layout");
                        stop = true;
                    }
                }

                break;
            default:
                tag = MockFragment.class.getSimpleName();
                mFragment = mFragmentManager.findFragmentByTag(tag);
                if (mFragment == null) {
                    mFragment = new MockFragment();
                }
                break;
        }

        if (!stop) {

            if (mFragment.getClass() == MockFragment.class && mFragment.getArguments() == null) {
                Bundle args = new Bundle();
                args.putInt(MockFragment.ARG_NAV_ITEM_NUMBER, position);
                mFragment.setArguments(args);
            }

            //Main magic happens here
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.addToBackStack(null);
            transaction.replace(R.id.content_frame, mFragment, tag).commit();

        }
    }

    private void chooseEcoMapFragment(String s) {
        String tag;
        tag = EcoMapFragment.class.getSimpleName();
        mFragment = mFragmentManager.findFragmentByTag(tag);
        if (mFragment == null) {
            mFragment = new EcoMapFragment();
        }
        EcoMapFragment frag = (EcoMapFragment) mFragment;
        frag.setFilterCondition(s);
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        MainActivity.userId = userId;
    }

    public static boolean isUserIdSet() {
        return userId != null;
    }

    public static boolean isEmailValid(CharSequence email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();

    }

    public static boolean isUserIsAuthorized() {
        return userIsAuthorized || getUserId() != null;
    }

    public static void changeAuthorizationState() {
        if (!isUserIdSet()) {
            mNavigationView.getMenu().getItem(5).setTitle(R.string.login);
        } else {
            mNavigationView.getMenu().getItem(5).setTitle(R.string.profile);
        }
    }

    /**
     * Sets logged in user id from COOKIE_USER_ID if cookieStore has it
     */
    private void initUserIdFromCookies() {
        CookieStore cookieStore = cookieManager.getCookieStore();
        try {
            List<HttpCookie> cookies = cookieStore.get(new URI(EcoMapAPIContract.ECOMAP_SERVER_URL));
            for (HttpCookie cookie : cookies) {
                if (cookie.getName().equals(EcoMapAPIContract.COOKIE_USER_ID)) {
                    setUserId(cookie.getValue());
                }
            }
        } catch (URISyntaxException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     * TODO: delete, after removing fragments "Under construction"
     */
    public static class MockFragment extends Fragment {
        public static final String ARG_NAV_ITEM_NUMBER = "navigation_menu_item_number";

        public MockFragment() {
            // Empty constructor required for mFragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_empty, container, false);
            int i = getArguments().getInt(ARG_NAV_ITEM_NUMBER);
            String planet = getResources().getStringArray(R.array.navigation_array)[i];

            getActivity().setTitle(planet);
            return rootView;
        }
    }
}