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


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.ecomap.android.app.R;
import org.ecomap.android.app.fragments.AddProblemFragment;
import org.ecomap.android.app.fragments.EcoMapFragment;
import org.ecomap.android.app.sync.EcoMapService;
import org.ecomap.android.app.fragments.LoginFragment;

/**
 * This example illustrates a common usage of the DrawerLayout widget
 * in the Android support library.
 * <p/>
 * <p>When a navigation (left) drawer is present, the host activity should detect presses of
 * the action bar's Up affordance as a signal to open and close the navigation drawer. The
 * ActionBarDrawerToggle facilitates this behavior.
 * Items within the drawer should fall into one of two categories:</p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic policies as
 * list or tab navigation in that a view switch does not create navigation history.
 * This pattern should only be used at the root activity of a task, leaving some form
 * of Up navigation active for activities further down the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an alternate
 * parent for Up navigation. This allows a user to jump across an app's navigation
 * hierarchy at will. The application should treat this as it treats Up navigation from
 * a different task, replacing the current task stack using TaskStackBuilder or similar.
 * This is the only form of navigation drawer that should be used outside of the root
 * activity of a task.</li>
 * </ul>
 * <p/>
 * <p>Right side drawers should be used for actions, not navigation. This follows the pattern
 * established by the Action Bar that navigation should be to the left and actions to the right.
 * An action should be an operation performed on the current contents of the window,
 * for example enabling or disabling a data overlay on top of the current content.</p>
 */
public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mScreenTitles;
    private ActionBar actionBar;

    public static final String API_URL = "http://176.36.11.25:8000/api/";

    public static final int NAV_MAP = 0;
    public static final int NAV_DETAILS = 2;
    public static final int NAV_RESOURCES = 3;
    public static final int NAV_LOGIN = 5;

    private static String userFirstName;
    private static String userSecondName;
    private static String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();

        mTitle = mDrawerTitle = getTitle();
        mScreenTitles = getResources().getStringArray(R.array.navigation_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mScreenTitles));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        // enable ActionBar app icon to behave as action to toggle nav drawer
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                actionBar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                actionBar.setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }

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
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_details).setVisible(!drawerOpen);
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
            case R.id.action_details:
                // create intent to perform web search for this planet
                Intent intent = new Intent(this, ProblemDetailsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_add_polygon:
                EcoMapFragment.setMarkerClickType(1);
                return true;
            case R.id.action_update:
                Intent intentService = new Intent(this, EcoMapService.class);
                startService(intentService);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void selectItem(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;
        boolean stop = false;
        String tag;
        switch (position) {
            case NAV_MAP:
                tag = EcoMapFragment.class.getSimpleName();
                fragment = fragmentManager.findFragmentByTag(tag);
                if(fragment == null) {
                    fragment = new EcoMapFragment();
                }
                break;
            case NAV_RESOURCES:
                tag = FiltersFragment.class.getSimpleName();
                fragment = fragmentManager.findFragmentByTag(tag);
                if(fragment == null) {
                    fragment = new FiltersFragment();
                }
                break;
            case NAV_DETAILS:
                tag = AddProblemFragment.class.getSimpleName();
                fragment = fragmentManager.findFragmentByTag(tag);
                if(fragment == null) {
                    fragment = new MockFragment();
                }
                break;
            case NAV_LOGIN:                
                tag = LoginFragment.class.getSimpleName();
                fragment = fragmentManager.findFragmentByTag(tag);
                if(fragment == null) {
                    fragment = new LoginFragment();
                }                
                break;
            default:
                tag = MockFragment.class.getSimpleName();
                fragment = fragmentManager.findFragmentByTag(tag);
                if(fragment == null) {
                    fragment = new MockFragment();
                }
                break;
        }

        Bundle args = new Bundle();
        args.putInt(MockFragment.ARG_NAV_ITEM_NUMBER, position);
        fragment.setArguments(args);

        //Main magic happens here
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mScreenTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        actionBar.setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        ImageLoader.getInstance().stop();
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public static class MockFragment extends Fragment {
        public static final String ARG_NAV_ITEM_NUMBER = "navigation_menu_item_number";

        public MockFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_empty, container, false);
            int i = getArguments().getInt(ARG_NAV_ITEM_NUMBER);
            String planet = getResources().getStringArray(R.array.navigation_array)[i];


//            int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
//                            "drawable", getActivity().getPackageName());
//            ((ImageView) rootView.findViewById(R.id.image)).setImageResource(imageId);
            getActivity().setTitle(planet);
            return rootView;
        }
    }

    public static class FiltersFragment extends Fragment {
        public static final String ARG_NAV_ITEM_NUMBER = "navigation_menu_item_number";

        public FiltersFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_map_filters, container, false);

            //int i = getArguments().getInt(ARG_NAV_ITEM_NUMBER);
            String[] planet = getResources().getStringArray(R.array.navigation_array);
            ListView mListView = (ListView)rootView.findViewById(R.id.filter_list_view);
            FiltersAdapter mFiltersAdapter = new FiltersAdapter(getActivity(), planet);
            mListView.setAdapter(mFiltersAdapter);

//            int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
//                            "drawable", getActivity().getPackageName());
//            ((ImageView) rootView.findViewById(R.id.image)).setImageResource(imageId);
            getActivity().setTitle("Filters");

            return rootView;
        }
    }

    public static class FiltersAdapter extends ArrayAdapter<String>{

        Context mContext;

        FiltersAdapter(Context context, String[] objects) {
            super(context, R.layout.filter_listview_item, 0, objects);
            this.mContext = context;

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View view;
            if (convertView == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.filter_listview_item, parent, false);
            } else {
                view = convertView;
            }

            TextView txtListItem = (TextView)view.findViewById(R.id.txtCaption);
            String text = getItem(position);
            txtListItem.setText(text);
            CheckBox chkBox = (CheckBox)view.findViewById(R.id.checkBox);
            chkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Toast.makeText(mContext, "You select: " + position, Toast.LENGTH_SHORT).show();
                }
            });
            return view;
            //super.getView(position, convertView, parent);
        }
    }

    public static String getUserFirstName() {
        return userFirstName;
    }

    public static void setUserFirstName(String userFirstName) {
        MainActivity.userFirstName = userFirstName;
    }

    public static String getUserSecondName() {
        return userSecondName;
    }

    public static void setUserSecondName(String userSecondName) {
        MainActivity.userSecondName = userSecondName;
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        MainActivity.userId = userId;
    }

    public static boolean isEmailValid(CharSequence email){
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
        
    }
}




