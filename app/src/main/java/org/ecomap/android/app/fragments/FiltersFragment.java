package org.ecomap.android.app.fragments;

/**
 * Created by izhaltc on 13.07.2015.
 */

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.ecomap.android.app.R;
import org.ecomap.android.app.data.EcoMapContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class FiltersFragment extends ListFragment {

    //these are never used. Though we need them to understand the type of problems.

    public static final int WOODS_PROBLEM = 1;
    public static final int TRASH_PROBLEM = 2;
    public static final int BUILDING_PROBLEM = 3;
    public static final int WATER_PROBLEM = 4;
    public static final int LIFEFORMS_PROBLEM = 5;
    public static final int POACHING_PROBLEM = 6;
    public static final int OTHER_PROBLEM = 7;
    private final String LOG_TAG = "FilterFragment";
    public String problemType = EcoMapContract.ProblemsEntry.COLUMN_PROBLEM_TYPE_ID + " = ";
    public String dateInterval=") AND ("+ EcoMapContract.ProblemsEntry.COLUMN_DATE+" BETWEEN ";
    public String resultCondition;
    Button okBtn;
    Button resetBtn;
    ListView listView;
    TextView startDate,endDate;
    ArrayList<Integer> selectedIdees;
    private View mainView;
    private Filterable ourActivity;
    private SparseBooleanArray sbArray;
    int startYear,startMonth, startDay, endYear, endMonth, endDay;
    private String dateCondition;
    private boolean startDateInitialized;
    private boolean endDateInitialized;

    String beginDate;
    String finishDate;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.i(LOG_TAG, "OnCreate");


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ourActivity = (Filterable) activity;

        Log.i(LOG_TAG, "onAttach");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        Log.i(LOG_TAG, "onCreateView");

        mainView = inflater.inflate(R.layout.filters_fragment_main, container, false);

        getActivity().setTitle(getActivity().getString(R.string.filters_menu_item_title));
        //getActivity().invalidateOptionsMenu();

        ScrollView scrollView=(ScrollView)mainView.findViewById(R.id.scroll_view);
        scrollView.scrollTo(0, 0);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);

    }
    @Override
    public void onResume() {
        super.onResume();

        Log.i(LOG_TAG, "onResume");
        listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.types,
                android.R.layout.simple_list_item_multiple_choice);

        listView.setAdapter(adapter);

        //set all of the items checked in the start
        if (sbArray == null) {
            for (int i = 0; i < listView.getCount(); i++) {
                listView.setItemChecked(i, true);
            }
        }
        selectedIdees = new ArrayList<>();

//if we already did some checking in filters, we get our previous selections.
        if (sbArray != null) {
            Log.i(LOG_TAG, "sbArray size " + sbArray.size());

            for (int i = 0; i < sbArray.size(); i++) {
                int key = sbArray.keyAt(i);
                if (sbArray.get(key)) {
                    listView.setItemChecked(key, true);
                }
            }
        }
        okBtn = (Button) mainView.findViewById(R.id.ok);
        resetBtn = (Button) mainView.findViewById(R.id.reset);
        startDate=(TextView) mainView.findViewById(R.id.start_date);
        endDate=(TextView)mainView.findViewById(R.id.end_date);

        final Calendar c = Calendar.getInstance();

       if(!startDateInitialized)
       {

           beginDate="2014-02-18";
           startYear=2014;
           startMonth=2;
           startDay=18;
       }

        if(!endDateInitialized) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            finishDate = sdf.format(c.getTime());

            endYear=c.get(Calendar.YEAR);
            endMonth=c.get(Calendar.MONTH);
            endDay=c.get(Calendar.DAY_OF_MONTH);
        }

        startDate.setText(beginDate);
        endDate.setText(finishDate);



        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sbArray = listView.getCheckedItemPositions();

                Log.i(LOG_TAG, "sbArray created. size= " + sbArray.size());
                for (int i = 0; i < sbArray.size(); i++) {
                    int key = sbArray.keyAt(i);

                    if (sbArray.get(key)) {
                        selectedIdees.add(key + 1);
                        Log.i(LOG_TAG, "added + " + (key + 1));
                    }
                }
                if (selectedIdees.size() == 0) {
                    resultCondition = "("+problemType + 120;

                } else if (selectedIdees.size() == 1) {
                    resultCondition = "("+problemType + selectedIdees.get(0);

                } else {
                    resultCondition = "("+problemType + selectedIdees.remove(0);
                    for (int i = 0; i < selectedIdees.size(); i++) {
                        resultCondition += " OR " + problemType + selectedIdees.get(i);
                    }
                }
                dateCondition=dateInterval + "\'"+beginDate+"\'" + " AND " + "\'"+finishDate+"\')";
                resultCondition+=dateCondition;
                Log.i(LOG_TAG, resultCondition);

                ourActivity.filter(resultCondition);
            }
        });
        //set all problems as checked
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < listView.getCount(); i++) {
                    listView.setItemChecked(i, true);
                }
                //aaand delete our previous choises
                sbArray = null;
            }
        });

        View.OnClickListener showDatePicker = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View vv = v;
                DatePickerDialog datePickerDialog;

                if(vv.getId()==R.id.start_date){
                    datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            Calendar c = Calendar.getInstance();
                            c.set(year, monthOfYear, dayOfMonth);

                            startYear=year;
                            startMonth=monthOfYear;
                            startDay=dayOfMonth;

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            beginDate = sdf.format(c.getTime());

                            startDateInitialized=true;
                            startDate.setText(beginDate);

                                ;

                            }


                    }, startYear, startMonth, startDay);

                }
                else{
                    datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                            Calendar c = Calendar.getInstance();
                            c.set(year, monthOfYear, dayOfMonth);

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            finishDate = sdf.format(c.getTime());

                            endYear=year;
                            endMonth=monthOfYear;
                            endDay=dayOfMonth;

                            endDateInitialized=true;
                            endDate.setText(finishDate);;

                        }
                    }, endYear, endMonth, endDay);
                }


        datePickerDialog.show();
    }
};      startDate.setOnClickListener(showDatePicker);
        endDate.setOnClickListener(showDatePicker);


    }
    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "on pause");
    }




    public interface Filterable {
        public void filter(String string);
    }



}



















