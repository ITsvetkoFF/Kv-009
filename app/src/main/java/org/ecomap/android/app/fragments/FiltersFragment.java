package org.ecomap.android.app.fragments;

/**
 * Created by izhaltc on 13.07.2015.
 */
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import org.ecomap.android.app.R;
import org.ecomap.android.app.data.EcoMapContract;

import java.util.ArrayList;

public class FiltersFragment extends ListFragment {

    private View mainView;

    public static final int WOODS_PROBLEM=1; //�������� ���
    public static final int TRASH_PROBLEM=2; //�������� ����������
    public static final int BUILDING_PROBLEM=3; // ��������� ��������
    public static final int WATER_PROBLEM=4; // ����������� ������
    public static final int LIFEFORMS_PROBLEM=5; // ������� ����������
    public static final int POACHING_PROBLEM=6; // �������������
    public static final int OTHER_PROBLEM=7; // ��� ��������
    private Filterable ourActivity;
    public String problemType=EcoMapContract.ProblemsEntry.COLUMN_PROBLEM_TYPE_ID+" = ";//������� �����, ���� ����������� ��� �� ���������� � ���� �����
    public String resultCondition;

    CheckBox woodsChb;
    CheckBox trashChb;
    CheckBox buildingChb;
    CheckBox waterChb;
    CheckBox lifeformsChb;
    CheckBox poachingChb;
    CheckBox otherChb;
    Button okBtn;
    Button resetBtn;
    ListView listView;
    ArrayList<Integer> selectedIdees;
    private final String LOG_TAG="FilterFragment";




    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ourActivity=(Filterable)activity;

        Log.i(LOG_TAG, "onAttach");
    }






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        Log.i(LOG_TAG, "onCreateView");

      mainView=inflater.inflate(R.layout.filters_fragment_main, container, false);
        return mainView;
    }
    @Override
    public void onResume(){
        super.onResume();

        Log.i(LOG_TAG, "onResume");
        listView=getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.types,
                android.R.layout.simple_list_item_multiple_choice);

        listView.setAdapter(adapter);
        selectedIdees=new ArrayList<>();








        okBtn=(Button)mainView.findViewById(R.id.ok);
        resetBtn=(Button)mainView.findViewById(R.id.reset);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SparseBooleanArray sbArray = listView.getCheckedItemPositions();
                for (int i = 0; i < sbArray.size(); i++) {
                    int key = sbArray.keyAt(i);
                    if (sbArray.get(key)) {
                        selectedIdees.add(key + 1);
                    }
                }
                if (selectedIdees.size() == 0) {
                    resultCondition = problemType+8;

                } else if (selectedIdees.size() == 1) {
                    resultCondition = problemType + selectedIdees.get(0);

                } else {
                    resultCondition = problemType + selectedIdees.remove(0);
                    for (int i = 0; i < selectedIdees.size(); i++) {
                        resultCondition += " OR " + problemType + selectedIdees.get(i);
                    }
                }


                ourActivity.filter(resultCondition);


            }
        });
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultCondition="";
                ourActivity.filter(resultCondition);
            }
        });


    }





    public interface Filterable {
        public void filter(String string);
    }

}



















