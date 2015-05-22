package com.acistek.cls;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by acistek on 5/13/2015.
 */
public class BuildingActivity {

    private AppVar var = new AppVar();
    private static final String TAG = "BuildingActivity";
    private final String building_url = var.cls_link + "/json/building_dsp.cfm?acfcode=" + var.acfcode ;
    private final Activity context;

    private Menu optionsMenu;
    private ProgressBar progressBuilding;
    private double latitude;
    private double longitude;


    // JSON Node names
    private static final String TAG_RESULTS = "results";
    private static final String TAG_BUILDING = "building";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_STATE = "state";
    private static final String TAG_CITY = "city";
    private static final String TAG_ZIPCODE = "zipcode";
    private static final String TAG_COUNTRY = "country";
    private static final String TAG_DISTANCE = "";
    private ListView building_lv;

    // results JSONArray
    private JSONArray buildingInfo = null;
    public SeekBar seekBar;
    private TextView textViewDistance;
    private int b_distance_i;
    private int distance_value;

    // Hashmap for Building ListView
    ArrayList<HashMap<String, String>> buildingList;
    public int current_seekbar_value;
    public int current_progress;

    GPSTracker gps;

    public BuildingActivity(Activity context){
        this.context = context;

        buildingList = new ArrayList<HashMap<String, String>>();

        building_lv = (ListView)context.findViewById(R.id.building_list);
        seekBar = (SeekBar) context.findViewById(R.id.seekBar);
        textViewDistance = (TextView) context.findViewById(R.id.distance_miles);
        progressBuilding = (ProgressBar) context.findViewById(R.id.progressbuilding);

        seekBar.setProgress(60);
        seekBar.incrementProgressBy(10);
        seekBar.setMax(60);
        current_seekbar_value = 3000;
        current_progress = 60;

        final int[] distance_values = {25, 50, 100, 200, 500, 1000, 3000};

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                current_progress = progress;
                progress = distance_values[progress/10];
                distance_value = progress;
                textViewDistance.setText(String.valueOf(progress)+".0 miles");
                current_seekbar_value = distance_value;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getBuildingInfo(distance_value);
                    }
                }).start();
            }
        });

        building_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // getting values from selected ListItem
                String address = ((TextView) view.findViewById(R.id.address))
                        .getText().toString();
                String city = ((TextView) view.findViewById(R.id.city))
                        .getText().toString();
                String state = ((TextView) view.findViewById(R.id.state))
                        .getText().toString();
                String zipcode = ((TextView) view.findViewById(R.id.zipcode))
                        .getText().toString();
                Uri gmmIntentUri = Uri.parse("geo:0,0?q="+address+", "+city+state+zipcode);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                BuildingActivity.this.context.startActivity(mapIntent);
            }
        });

    }

    public void setGPS(){
        gps = new GPSTracker(context);

        if(gps.canGetLocation()){
            seekBar.setEnabled(true);
//            seekBar.setProgress(60);
//            current_seekbar_value = 3000;
//            current_progress = 60;
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        }
        else{
            seekBar.setEnabled(false);
            seekBar.setProgress(60);
            current_seekbar_value = 3000;
            current_progress = 60;
            gps.showSettingsAlert();
        }
    }

    public void getBuildingInfo(final int distance_control){

        final String get_building = building_url;
        AsyncHttpClient client = new SyncHttpClient();

        client.post(get_building, new TextHttpResponseHandler() {

            @Override
            public void onStart() {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBuilding.setVisibility(View.VISIBLE);
                        building_lv.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void onFinish() {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      progressBuilding.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SearchActivity.isBuildingLoaded = false;
                        var.showAlert(context, context.getResources().getString(R.string.alert_error),  context.getResources().getString(R.string.alert_no_internet));
                        seekBar.setEnabled(false);
                        seekBar.setProgress(60);
                        current_progress = 60;
                        current_seekbar_value = 3000;
                    }
                });
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    JSONObject response = new JSONObject(responseString);

                    // Getting JSON Array node
                    buildingInfo = response.getJSONArray(TAG_RESULTS);
                    buildingList = new ArrayList<HashMap<String, String>>();

                    // looping through All buildingInfo
                    for (int i = 0; i < buildingInfo.length(); i++) {
                        JSONObject c = buildingInfo.getJSONObject(i);

                        String building_name = c.getString(TAG_BUILDING);
                        String address = c.getString(TAG_ADDRESS);
                        String city = c.getString(TAG_CITY);
                        city = city + ", ";
                        String state1 = c.getString(TAG_STATE);
                        String state = state1 + " ";
                        String zipcode = c.getString(TAG_ZIPCODE);
                        String country = c.getString(TAG_COUNTRY);
                        // tmp hashmap for single building
                        HashMap<String, String>  building = new HashMap<String, String>();

                        Geocoder geocoder = new Geocoder(BuildingActivity.this.context, Locale.getDefault());
                        List<Address> b_address;

                        try{
                            b_address = geocoder.getFromLocationName(address + "," + city + state + zipcode, 1);
                            if(b_address != null && b_address.size() > 0){
                                Address b_location = b_address.get(0);
                                Double b_lat = b_location.getLatitude();
                                Double b_lon = b_location.getLongitude();
                                float[] result = new float[1];
                                Location.distanceBetween(latitude, longitude, b_lat, b_lon, result);
                                float b_distance = result[0]*0.000621371192f;
                                DecimalFormat fnum = new DecimalFormat("##0.0");
                                String b_distance_s = fnum.format(b_distance);
                                String[] fn = b_distance_s.split("\\.");
                                b_distance_i = Integer.valueOf(fn[0]);
                                b_distance_s = b_distance_s + " miles";
                                if(b_distance_i <= distance_control){
                                    building.put(TAG_DISTANCE, b_distance_s);
                                }
                            }
                            else{
                                Log.e("distance error","Couldn't reverse geocode b_location");
                            }
                        }
                        catch (IOException e){
                            Log.e(TAG, "Problem with geocoder.");
                        }

                        // adding each child node to HashMap key => value
                        if(b_distance_i <= distance_control) {
                            // adding building to building list
                            building.put(TAG_BUILDING, building_name);
                            building.put(TAG_ADDRESS, address);
                            building.put(TAG_CITY, city);
                            building.put(TAG_STATE, state);
                            building.put(TAG_ZIPCODE, zipcode);
                            building.put(TAG_COUNTRY, country);
                            buildingList.add(building);
                            current_seekbar_value = distance_control;
                        }
                    }

                    final ListAdapter adapter = new BuildingAdapter(
                            BuildingActivity.this.context, buildingList,
                            R.layout.building_list_item, new String[] { TAG_BUILDING, TAG_ADDRESS,
                            TAG_CITY,TAG_STATE,TAG_ZIPCODE,TAG_COUNTRY,TAG_DISTANCE}, new int[] { R.id.building_name,
                            R.id.address, R.id.city,R.id.state,R.id.zipcode,R.id.country,R.id.distance});

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            building_lv.setAdapter(adapter);
                            SearchActivity.isBuildingLoaded = true;
                        }
                    });

                } catch (JSONException e) {
                    Log.e(TAG, "Problem retrieving Buildings.");
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SearchActivity.isBuildingLoaded = false;
                        }
                    });
                }
            }
        });
    }

    class BuildingAdapter extends SimpleAdapter {
        private int[] colors = new int[] { 0xFFFFFFFF, 0xFFF6F3EE };

        public BuildingAdapter(Context context, List<HashMap<String, String>> items, int resource, String[] content, int[] id) {
            super(context, items, resource, content, id);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            int colorPos = position % colors.length;
            view.setBackgroundColor(colors[colorPos]);
            return view;
        }
    }
}
