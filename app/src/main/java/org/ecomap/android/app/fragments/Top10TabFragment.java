package org.ecomap.android.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ecomap.android.app.R;
import org.ecomap.android.app.tabs.SlidingTabLayout;
import org.ecomap.android.app.tabs.ViewPagerAdapter;


public class Top10TabFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_top10_tab, container, false);

        getActivity().setTitle(getActivity().getString(R.string.nav_titles_top10));
        //getActivity().invalidateOptionsMenu();

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        int numboftabs = 3;
        final CharSequence[] titles = {getString(R.string.top_popular), getString(R.string.top_important), getString(R.string.top_discussed)};
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager(), titles, numboftabs);

        // Assigning ViewPager View and setting the adapter
        ViewPager pager = (ViewPager) v.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        SlidingTabLayout tabs = (SlidingTabLayout) v.findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColorTab);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
        return v;
    }

}
