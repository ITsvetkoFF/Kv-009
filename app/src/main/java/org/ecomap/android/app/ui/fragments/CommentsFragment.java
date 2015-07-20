package org.ecomap.android.app.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.plus.PlusOneButton;

import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.data.model.CommentEntry;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.ecomap.android.app.ui.components.ExpandableListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

    private static int PROBLEM_NUMBER = 185;

    // The request code must be 0 or greater.
    private static final int PLUS_ONE_REQUEST_CODE = 0;

    // The URL to +1.  Must be a valid URL.
    private final String PLUS_ONE_URL = "http://developer.android.com";

    private Problem mProblem;
    private PlusOneButton mPlusOneButton;

    private OnFragmentInteractionListener mListener;
    private CommentsAdapter<CommentEntry> mCommentsAdapter;

    private ViewGroup mRootView;
    private EditText mTxtComment;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_comments, container, false);
        ExpandableListView lstComments = new ExpandableListView(getActivity(), null, 0);

        lstComments.setId(R.id.email);
        lstComments.setExpanded(true);
        lstComments.setClickable(false);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.editComment);

        mRootView.addView(lstComments, layoutParams);

        //Find the +1 button
        //mPlusOneButton = (PlusOneButton) view.findViewById(R.id.plus_one_button);
        mTxtComment = (EditText) mRootView.findViewById(R.id.editComment);
        final Button addButton = (Button) mRootView.findViewById(R.id.addCommentButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //small validation
                String comment = mTxtComment.getText().toString();
                if (!comment.isEmpty() && MainActivity.isUserIsAuthorized()) {
                    new AsyncSendComment().execute(comment,String.valueOf(mProblem.getId()));
                }

                if(!MainActivity.isUserIsAuthorized()){
                    MainActivity.showInfoSnackBar(getActivity(), getActivity().getWindow().getDecorView(), R.string.message_log_in_to_leave_comments, Snackbar.LENGTH_SHORT);
                }else if(comment.isEmpty()){
                    MainActivity.showInfoSnackBar(getActivity(), getActivity().getWindow().getDecorView(), R.string.message_write_something_to_post, Snackbar.LENGTH_SHORT);
                }
            }
        });

        mCommentsAdapter = new CommentsAdapter<>(getActivity(), new ArrayList<CommentEntry>());

        lstComments.setAdapter(mCommentsAdapter);
        new AsyncRequestComments().execute(mProblem.getId());

        lstComments.setAdapter(mCommentsAdapter);
        new AsyncRequestComments().execute();
>>>>>>> master

        return mRootView;
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
        public void onFragmentInteraction(Uri uri);
    }

    private static class CommentsAdapter<T extends CommentEntry> extends BaseAdapter {

        private final Context mContext;
        private List<T> mCommentsArray;

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
                view = LayoutInflater.from(mContext).inflate(R.layout.item_comments_listview, parent, false);
            } else {
                view = convertView;
            }

            view.setClickable(false);

            final CommentEntry currentItem = (CommentEntry) getItem(position);

            if (currentItem != null) {

                final TextView txtUserName = (TextView) view.findViewById(R.id.textUserName);
                txtUserName.setText(currentItem.getCreatedBy());

                final TextView txtListItem = (TextView) view.findViewById(R.id.txtCaption);
                txtListItem.setText(currentItem.getContent());
            }

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

    private class AsyncRequestComments extends AsyncTask<Integer, Integer, List<CommentEntry>> {

        private final String LOG_TAG = AsyncRequestComments.class.getSimpleName();

        String JSONStr = null;

        @Override
        protected List<CommentEntry> doInBackground(Integer... params) {

            Integer id = params[0];
            String ECOMAP_COMMENTS_URL = EcoMapAPIContract.ECOMAP_API_URL + "/problems/" + id + "/comments";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            List<CommentEntry> ret = new ArrayList<CommentEntry>();

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            List<CommentEntry> ret = new ArrayList<>();
>>>>>>> master

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
                    buffer.append(line).append("\n");
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

    private class AsyncSendComment extends AsyncTask<String, Void, Boolean> {

        private final String LOG_TAG = AsyncSendComment.class.getSimpleName();
        private Integer problem_id;

        @Override
        protected Boolean doInBackground(String... params) {
            URL url = null;
            Boolean result = Boolean.FALSE;
            problem_id = Integer.parseInt(params[1]);

            //validation
            if (MainActivity.isUserIsAuthorized()) {
                if (params.length > 0 && params[0] != null && !params[0].isEmpty()) {

                    HttpURLConnection connection = null;

                    try {

                        //creating JSONObject for request
                        JSONObject request = new JSONObject();
                        request.put("content", params[0]);

                        url = new URL(EcoMapAPIContract.ECOMAP_API_URL + "/problems/" + problem_id + "/comments");

                        connection = (HttpURLConnection) url.openConnection();
                        //connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        connection.setDoOutput(true);
                        connection.connect();

                        /**
                         * sending request
                         * request.toString() - translate our object into appropriate JSON text
                         * {
                         *      "content": "your comment"
                         * }
                         */
                        OutputStream outputStream = connection.getOutputStream();
                        outputStream.write(request.toString().getBytes("UTF-8"));

                        //handling result from server
                        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            result = Boolean.TRUE;
                        }

                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                new AsyncRequestComments().execute(problem_id);
                mTxtComment.setText("");
            }
        }

    }


}
