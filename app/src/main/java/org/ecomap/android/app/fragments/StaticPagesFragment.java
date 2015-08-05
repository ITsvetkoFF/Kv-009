package org.ecomap.android.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.StaticPagesActivity;
import org.ecomap.android.app.data.EcoMapContract;

/**
 * Created by yura on 7/31/15.
 */
public class StaticPagesFragment extends Fragment {
    View view;
    Cursor resourcesCusor;
    ListView listView;
    ListCursorAdapter listCursorAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resourcesCusor = getActivity().getContentResolver().query(EcoMapContract.ResourcesEntry.CONTENT_URI, null, null, null, null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.static_pages_fragment, container, false);
        listView = (ListView) view.findViewById(R.id.static_pages_list);
        listCursorAdapter = new ListCursorAdapter(getActivity(), resourcesCusor);
        listView.setAdapter(listCursorAdapter);
        getActivity().setTitle(getString(R.string.item_resources));
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        resourcesCusor.close();
    }

    class ListCursorAdapter extends CursorAdapter{

        public ListCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.static_page_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            TextView title = (TextView) view.findViewById(R.id.static_pages_item_title);
            final String cursorTitle = cursor.getString(cursor.getColumnIndexOrThrow(EcoMapContract.ResourcesEntry.COLUMN_TITLE));
            final String cursorContent = cursor.getString(cursor.getColumnIndexOrThrow(EcoMapContract.ResourcesEntry.COLUMN_CONTENT));

            title.setText(cursorTitle);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), StaticPagesActivity.class);
                    intent.putExtra("content", cursorContent);
                    intent.putExtra("title", cursorTitle);
                    startActivity(intent);
                }
            });
        }
    }
}