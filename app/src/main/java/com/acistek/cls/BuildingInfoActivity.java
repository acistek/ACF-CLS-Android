package com.acistek.cls;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class BuildingInfoActivity extends ActionBarActivity implements ConnectionStateListener {

    private AppVar var = new AppVar();
    private static final String TAG = "BuildingActivity";
    private final String building_url = var.cls_link + "/json/building_dsp.cfm";

    private boolean isInternetConnected;
    private boolean isPageLoaded = true;

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
    private SeekBar seekBar;
    private TextView textViewDistance;
    private int b_distance_i;
    private int distance_value;

    // Hashmap for Building ListView
//    ArrayList<HashMap<String, String>> buildingList;
    private ArrayList<BuildingInfo> buildingListInfo;
    private BuildingInfoListAdapter buildingInfoListAdapter;

    private int current_seekbar_value;
    private int current_progress;

    private IntentFilter connFilter;
    private ConnectionStateReceiver csr;
    private TextView actionbarText;
    private Menu menu;

    GPSTracker gps;
    SessionManager session;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_info);

        resources = getResources();
        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF336A90));
        actionBar.setCustomView(R.layout.abs_layout);

        actionbarText = (TextView) findViewById(R.id.search_title);

        connFilter = new IntentFilter();
        connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        csr = new ConnectionStateReceiver();
        registerReceiver(csr, connFilter);

        actionbarText.setText(getString(R.string.title_section2));

        building_lv = (ListView) findViewById(R.id.building_list);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textViewDistance = (TextView) findViewById(R.id.distance_miles);
        progressBuilding = (ProgressBar) findViewById(R.id.progressbuilding);

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
                progress = distance_values[progress / 10];
                distance_value = progress;
                textViewDistance.setText(String.valueOf(progress) + ".0 miles");
                current_seekbar_value = distance_value;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                displayBuildingInfo(distance_value);
            }
        });

        building_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // getting values from selected ListItem
                String address = ((TextView) view.findViewById(R.id.address)).getText().toString();
                String city = ((TextView) view.findViewById(R.id.city)).getText().toString();
                String state = ((TextView) view.findViewById(R.id.state)).getText().toString();
                String zipcode = ((TextView) view.findViewById(R.id.zipcode)).getText().toString();
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + address + ", " + city + state + zipcode);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                BuildingInfoActivity.this.startActivity(mapIntent);
            }
        });

        setGPS();
        seekBar.setProgress(current_progress);

        getBuildingInfo(current_seekbar_value);
    }

    @Override
    public void onStart(){
        super.onStart();
        MainApplication.uiInForeground = true;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        MainApplication.uiInForeground = false;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(csr != null)
            unregisterReceiver(csr);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_building_info, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home){
            super.onBackPressed();
            return true;
        }
        else if(id == R.id.building_refresh)  {
            menu.findItem(R.id.building_refresh).setEnabled(false);
            setGPS();
            getBuildingInfo(current_seekbar_value);
            menu.findItem(R.id.building_refresh).setEnabled(true);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void isConnected(boolean isConnected) {
        if(isConnected){
            if(this.isInternetConnected == false && isPageLoaded == false){
                isPageLoaded = true;
                setGPS();
                seekBar.setProgress(60);
                getBuildingInfo(3000);
            }

            this.isInternetConnected = true;
        }
        else{
            this.isInternetConnected = false;
        }
    }

    public void setGPS(){
        gps = new GPSTracker(this);

        if(gps.canGetLocation()){
            seekBar.setEnabled(true);
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

        final String get_building = building_url + "?deviceIdentifier=" + session.getDeviceID() + "&loginUUID=" + session.getUUID();
        AsyncHttpClient client = new AsyncHttpClient();

        Log.e(TAG, get_building);

        client.post(get_building, new TextHttpResponseHandler() {

            @Override
            public void onStart() {
                progressBuilding.setVisibility(View.VISIBLE);
                building_lv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                progressBuilding.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                BuildingInfoActivity.this.isPageLoaded = false;
                if(!BuildingInfoActivity.this.isFinishing()) {
                    var.showAlert(BuildingInfoActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                }
                seekBar.setEnabled(false);
                seekBar.setProgress(60);
                current_progress = 60;
                current_seekbar_value = 3000;
                building_lv.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    JSONObject response = new JSONObject(responseString);

                    // Getting JSON Array node
                    buildingInfo = response.getJSONArray(TAG_RESULTS);
                    buildingListInfo = new ArrayList<BuildingInfo>();

                    // looping through All buildingInfo
                    for (int i = 0; i < buildingInfo.length(); i++) {
                        BuildingInfo b_info = new BuildingInfo();

                        JSONObject c = buildingInfo.getJSONObject(i);

                        String building_name = c.getString(TAG_BUILDING);
                        String address = c.getString(TAG_ADDRESS);
                        String city = c.getString(TAG_CITY) + ", ";
                        String state = c.getString(TAG_STATE) + " ";
                        String zipcode = c.getString(TAG_ZIPCODE);
                        String country = c.getString(TAG_COUNTRY);

                        b_info.setBuilding_name(building_name);
                        b_info.setAddress(address);
                        b_info.setCity(city);
                        b_info.setState(state);
                        b_info.setZipcode(zipcode);
                        b_info.setCountry(country);

                        Geocoder geocoder = new Geocoder(BuildingInfoActivity.this, Locale.getDefault());
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

                                b_info.setDistance_miles(b_distance_s);
                                b_info.setDistance(b_distance_i);
                                buildingListInfo.add(b_info);

                                BuildingInfoActivity.this.isPageLoaded = true;
                                BuildingInfoActivity.this.displayBuildingInfo(distance_control);
                            }
                            else{
                                Log.e("distance error", "Couldn't reverse geocode b_location");
                                BuildingInfoActivity.this.isPageLoaded = false;
                            }
                        }
                        catch (IOException e){
                            Log.e(TAG, "Problem with geocoder.");
                            BuildingInfoActivity.this.isPageLoaded = false;
                        }

                    }

                    BuildingInfoActivity.this.isPageLoaded = true;

                } catch (JSONException e) {
                    Log.e(TAG, "Problem retrieving Buildings.");

                    BuildingInfoActivity.this.isPageLoaded  = false;
                }
            }
        });
    }

    public void displayBuildingInfo(int distance_control){
        current_seekbar_value = distance_control;

        ArrayList<BuildingInfo> buildingListInfoTemp = new ArrayList<BuildingInfo>();

        for(BuildingInfo i: buildingListInfo){
            if(i.getDistance() <= distance_control){
                buildingListInfoTemp.add(i);
            }
        }

        buildingInfoListAdapter = new BuildingInfoListAdapter(this, buildingListInfoTemp);
        building_lv.setAdapter(buildingInfoListAdapter);
    }

}
