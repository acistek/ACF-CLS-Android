package com.acistek.cls;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class StaffNotRespPOActivity extends ActionBarActivity implements ConnectionStateListener{

    private AppVar var = new AppVar();
    private static final String TAG = "StaffNotRespPOActivity";
    private String po_list_url = var.cls_link + "/json/POList.cfm";

    private ListView poListView;
    private ProgressBar progress;
    private ArrayList<POList> poListArray;

    private POListAdapter poListAdapter;

    private boolean isInternetConnected;
    private boolean isPageLoaded = false;

    private IntentFilter connFilter;
    private ConnectionStateReceiver csr;
    private TextView actionbarText;

    SessionManager session;
    Resources resources;

    static int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_not_resp_po);

        resources = getResources();
        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        Crashlytics.getInstance().setUserName(session.getUsername());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF336A90));
        actionBar.setCustomView(R.layout.abs_layout);

        actionbarText = (TextView) findViewById(R.id.search_title);
        actionbarText.setText("Staff Not Responded");

        progress = (ProgressBar) findViewById(R.id.staff_po_progress);
        poListView = (ListView) findViewById(R.id.staff_po_listview);

        connFilter = new IntentFilter();
        connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        csr = new ConnectionStateReceiver();
        registerReceiver(csr, connFilter);

        poListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                counter = position;

                TextView tv = (TextView) view.findViewById(R.id.po_abbrev);
                TextView po = (TextView) view.findViewById(R.id.po_full_name);
                Intent i = new Intent(StaffNotRespPOActivity.this, StaffNotRespUsersActivity.class);
                i.putExtra("po_abbrev", tv.getText().toString());
                i.putExtra("po_full_name", po.getText().toString());
                StaffNotRespPOActivity.this.startActivity(i);
            }
        });

        processPOList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_staff_not_resp_po, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home){
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart(){
        super.onStart();
        MainApplication.uiInForeground = true;
    }

    @Override
    public void onResume(){
        super.onResume();
        counter = 0;
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
    public void isConnected(boolean isConnected) {
        if(isConnected){
            if(this.isInternetConnected == false && isPageLoaded == false){
                isPageLoaded = true;
                processPOList();
            }

            this.isInternetConnected = true;
        }
        else{
            this.isInternetConnected = false;
        }
    }

    public void processPOList(){
        final String get_notifications = po_list_url + "?deviceIdentifier=" + session.getDeviceID() + "&loginUUID=" + session.getUUID();
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(get_notifications, new TextHttpResponseHandler() {

            @Override
            public void onStart() {
                progress.setVisibility(View.VISIBLE);
                poListView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if(!StaffNotRespPOActivity.this.isFinishing()){
                    if(StaffNotRespPOActivity.this.isInternetConnected == false)
                        var.showAlert(StaffNotRespPOActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                    else
                        var.showAlert(StaffNotRespPOActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));
                }
                isPageLoaded = false;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    JSONObject response = new JSONObject(responseString);
                    Integer resultCount = null;

                    if(response.has("resultCount"))
                        resultCount = response.getInt("resultCount");

                    if(resultCount != null){
                        var.showAlert(StaffNotRespPOActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));
                    }
                    else{
                        JSONArray poListItems = response.getJSONArray("results");
                        poListArray = new ArrayList<POList>();


                        for (int i = 0; i < poListItems.length(); i++) {
                            JSONObject jsonItem = poListItems.getJSONObject(i);

                            POList po = new POList();
                            po.setPoAbbrev(jsonItem.getString("POShort"));
                            po.setPoName(jsonItem.getString("POName"));
                            po.setTotalUsers(jsonItem.getString("totalUsers"));
                            po.setSubtitle(jsonItem.getString("subtitle"));

                            if(po.getPoAbbrev().equalsIgnoreCase("header"))
                                po.setIsHeader(true);
                            else
                                po.setIsHeader(false);

                            poListArray.add(po);
                        }

                        poListAdapter = new POListAdapter(StaffNotRespPOActivity.this, poListArray);
                        poListView.setAdapter(poListAdapter);
                    }

                    isPageLoaded = true;

                } catch (JSONException e) {
                    Log.e(TAG, "Problem retrieving PO list");
                    var.showAlert(StaffNotRespPOActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.profile_favorite_error));
                    isPageLoaded = false;
                }
            }
        });
    }
}
