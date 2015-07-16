package org.ecomap.android.app.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.plus.PlusOneButton;

import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.data.model.CommentEntry;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A fragment with the comments block and a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link CommentsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CommentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentsFragment extends Fragment {

    public static final String TAG = "CommentsFragment";

    private static final String ARG_PROBLEM = "problem";

    // The request code must be 0 or greater.
    private static final int PLUS_ONE_REQUEST_CODE = 0;

    // The URL to +1.  Must be a valid URL.
    private final String PLUS_ONE_URL = "http://developer.android.com";

    private Problem mProblem;
    private PlusOneButton mPlusOneButton;

    private OnFragmentInteractionListener mListener;
    private CommentsAdapter<CommentEntry> mCommentsAdapter;


    public CommentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param problem Parameter 1.
     * @return A new instance of fragment CommentsFragment.
     */
    public static CommentsFragment newInstance(Problem problem) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_PROBLEM, problem);
        fragment.setArguments(args);

        return fragment;
    }

    public static CommentsFragment newInstance() {
        CommentsFragment fragment = new CommentsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mProblem = getArguments().getParcelable(ARG_PROBLEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        //Find the +1 button
        //mPlusOneButton = (PlusOneButton) view.findViewById(R.id.plus_one_button);
        ListView lstComments = (ListView) view.findViewById(R.id.lstComments);
        mCommentsAdapter = new CommentsAdapter<>(getActivity(), new ArrayList<CommentEntry>());

        lstComments.setAdapter(mCommentsAdapter);
        new AsyncRequestComments().execute();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the state of the +1 button each time the activity receives focus.
        //mPlusOneButton.initialize(PLUS_ONE_URL, PLUS_ONE_REQUEST_CODE);
    }

/*
    // T O D O: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
*/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
/*
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // T O D O: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private static class CommentsAdapter<T extends CommentEntry> extends BaseAdapter {

        private List<T> mCommentsArray;
        private final Context mContext;

        public CommentsAdapter(Context mContext, List<T> commentsArray) {
            this.mContext = mContext;
            this.mCommentsArray = commentsArray;
        }

        @Override
        public int getCount() {
            return mCommentsArray.size();
        }

        @Override
        public T getItem(int position) {
            return mCommentsArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.filter_listview_item, parent, false);
            } else {
                view = convertView;
            }

            TextView txtListItem = (TextView) view.findViewById(R.id.txtCaption);
            String text = ((CommentEntry) getItem(position)).getContent();
            txtListItem.setText(text);

            return view;
        }

        /**
         * Update adapter data set
         */
        public void updateDataSet(List<T> data) {
            mCommentsArray = data;
            notifyDataSetChanged();

        }
    }

    private class AsyncRequestComments extends AsyncTask<Void, Void, List<CommentEntry>> {

        private static final String ECOMAP_COMMENTS_URL = EcoMapAPIContract.ECOMAP_API_URL + "/problems/" + 1 + "/comments";
        private final String LOG_TAG = AsyncRequestComments.class.getSimpleName();

        String JSONStr = null;

        @Override
        protected List<CommentEntry> doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            List<CommentEntry> ret = new ArrayList<>();

            try {
                // Getting input stream from URL

                URL url = new URL(ECOMAP_COMMENTS_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return ret;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return ret;
                }

                JSONStr = buffer.toString();

                // Starting method for parsing data from JSON and writing them to database
                ret = getCommentsFromJSON();

            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                }
            }

            return ret;
        }

        // parsing data from JSON
        private List<CommentEntry> getCommentsFromJSON() {

            try {

                JSONObject root = new JSONObject(JSONStr);
                JSONArray jArr = root.getJSONArray("data");

                List<CommentEntry> syncedList = Collections.synchronizedList(new ArrayList<CommentEntry>(JSONStr.length()));

                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject obj = jArr.getJSONObject(i);
                    CommentEntry commentEntry = CommentEntry.fromJSON(obj);
                    if (null != commentEntry) {
                        syncedList.add(commentEntry);
                    }
                }

                return syncedList;

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            return new ArrayList<CommentEntry>();
        }


        @Override
        protected void onPostExecute(List<CommentEntry> commentsArray) {
            mCommentsAdapter.updateDataSet(commentsArray);
        }
    }


}
