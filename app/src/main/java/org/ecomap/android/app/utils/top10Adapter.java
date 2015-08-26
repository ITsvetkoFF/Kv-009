package org.ecomap.android.app.utils;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.tabs.Top10Tab;

import java.util.List;

public class top10Adapter extends BaseAdapter {

    private String LOG_TAG = top10Adapter.class.getSimpleName();

    private final List<Problem> rowItems;
    private final LayoutInflater lInflater;
    private final int tabId; //0 - votes, 1 - rates, 2 - comments

    public top10Adapter(Context context, List<Problem> rowItems, int tabId) {
        this.rowItems = rowItems;
        this.tabId = tabId;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Log.d(Top10Tab.class.getSimpleName(), "top10Adapter: " + tabId);

    }

    @Override
    public int getCount() {
        return rowItems.size();
    }

    @Override
    public Object getItem(int position) {
        return rowItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rowItems.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = lInflater.inflate(R.layout.top10_list_item, parent, false);
        }

        Problem problem = (Problem) getItem(position);

        ((TextView) view.findViewById(R.id.textView1)).setText(problem.getTitle());

        //Log.d(LOG_TAG, "getView: " + tabId + " likes:" + problem.getNumberOfLikes() + " rates:" + problem.getSeverity() + " comments: " + problem.getNumber_of_comments());

        switch (tabId) {
            case 0:
                ((ImageView) view.findViewById(R.id.show_like)).setImageResource(R.mipmap.heart_black);
                ((TextView) view.findViewById(R.id.numVotes)).setText(problem.getNumberOfLikes());
                break;
            case 1:
                ((ImageView) view.findViewById(R.id.show_like)).setImageResource(R.mipmap.star_black);
                ((TextView) view.findViewById(R.id.numVotes)).setText(problem.getSeverity());
                break;
            case 2:
                ((ImageView) view.findViewById(R.id.show_like)).setImageResource(R.mipmap.comments_black);
                ((TextView) view.findViewById(R.id.numVotes)).setText(String.valueOf(problem.getNumber_of_comments()));
                break;
        }

        return view;
    }
}
