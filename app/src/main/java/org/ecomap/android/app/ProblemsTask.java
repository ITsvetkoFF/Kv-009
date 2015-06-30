package org.ecomap.android.app;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

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


public class ProblemsTask extends AsyncTask<Void, Void, Void> {
    private final GoogleMap mMap;
    private ArrayList<Problem> values;
    private ArrayList<LatLng> points;
    private ArrayList<Marker> markers;
    private String JSONStr;
    private final String LOG_TAG = ProblemsTask.class.getSimpleName();
    private ClusterManager<Problem> mClusterManager;
    private Context mContext;
    private static int markerClickType;
    private Problem clickedProblem;

    ProblemsTask(GoogleMap mMap, Context mContext){
        this.mMap = mMap;
        this.values = new ArrayList<>();
        this.markers = new ArrayList<>();
        this.points = new ArrayList<>();
        this.JSONStr = null;
        this.mContext = mContext;
        this.markerClickType = 0;
    }

    @Override
    protected Void doInBackground(Void... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            final String FORECAST_BASE_URL = "http://ecomap.org/api/problems";

            URL url = new URL(FORECAST_BASE_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            JSONStr = buffer.toString();
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
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        readToValues();
        setUpClusterer();

    }

    private void readToValues(){
        final String TITLE = "Title";
        final String LATITUDE = "Latitude";
        final String LONGITUDE = "Longtitude";
        final String PROBLEMS_TYPES_ID = "ProblemTypes_Id";

        try{
            JSONArray jArr = new JSONArray(JSONStr);
            for (int i = 0; i < jArr.length(); i++){
                String title;
                double latitude, longitude;
                int type_id;

                JSONObject obj = jArr.getJSONObject(i);
                title = obj.getString(TITLE);
                latitude = obj.getDouble(LATITUDE);
                longitude = obj.getDouble(LONGITUDE);
                type_id = obj.getInt(PROBLEMS_TYPES_ID);

                Problem p = new Problem(latitude, longitude, title, type_id, mContext);

                values.add(p);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void setUpClusterer(){
        //Position the map
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.461166, 30.417397), 5));

        //Initialize the manager with the mContext and the map.
        mClusterManager = new ClusterManager<>(mContext, mMap);

        //Point the map's listeners at the listeners implemented by the cluster.
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (markerClickType == 1){
                    Marker m = mMap.addMarker(new MarkerOptions().position(latLng));
                    m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    markers.add(m);
                    points.add(latLng);
                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            if (markerClickType == 1){
                                if (points.get(0).equals(marker.getPosition())) {
                                    countPolygonPoints();
                                    for (Marker m : markers){
                                        m.remove();
                                    }
                                }
                                return false;
                            }
                            return false;
                        }
                    });
                }
            }
        });

        //Add cluster items to the cluster manager.
        mClusterManager.addItems(values);
        mClusterManager.setOnClusterClickListener
                (new ClusterManager.OnClusterClickListener<Problem>() {
            @Override
            public boolean onClusterClick(Cluster cluster) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(),
                        mMap.getCameraPosition().zoom + 2));
                return false;
            }
        });
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Problem>() {
            @Override
            public boolean onClusterItemClick(Problem problem) {
                Toast.makeText(mContext, problem.getTitle(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        mClusterManager.setRenderer(new MyIconRendered(mContext, mMap, mClusterManager));
    }

    private void countPolygonPoints() {
        if (points.size() >= 3) {
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.addAll(points);
            polygonOptions.strokeColor(mContext.getResources().getColor(R.color.polygonEdges));
            polygonOptions.strokeWidth(7);
            polygonOptions.fillColor(mContext.getResources().getColor(R.color.polygon));
            mMap.addPolygon(polygonOptions);
            points.clear();
            markerClickType = 0;
            mMap.setOnMarkerClickListener(mClusterManager);
        }
    }

    public static void setMarkerClickType(int type){
        markerClickType = type;
    }
}

