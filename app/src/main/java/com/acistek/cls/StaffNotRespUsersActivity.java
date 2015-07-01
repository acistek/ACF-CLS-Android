package com.acistek.cls;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class StaffNotRespUsersActivity extends ActionBarActivity implements ConnectionStateListener{

    private AppVar var = new AppVar();
    private static final String TAG = "StaffNotRespUsersAct";
    private String po_detail_url = var.cls_link + "/json/PODetail.cfm";;

    private boolean isInternetConnected;
    private boolean isPageLoaded = false;

    private ListView poDetailListView;
    private ProgressBar progress;
    private ArrayList<PODetail> poDetailArrayList;

    private PODetailListAdapter poDetailListAdapter;

    private IntentFilter connFilter;
    private ConnectionStateReceiver csr;
    private TextView actionbarText;

    private String po_abbrev;
    private String po_full_name;

    SessionManager session;
    Resources resources;

    static int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_not_resp_users);

        resources = getResources();
        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF336A90));
        actionBar.setCustomView(R.layout.abs_layout);

        actionbarText = (TextView) findViewById(R.id.search_title);
        po_abbrev = getIntent().getExtras().getString("po_abbrev");
        po_full_name = getIntent().getExtras().getString("po_full_name");
        actionbarText.setText("Staff Not Responded");

        progress = (ProgressBar) findViewById(R.id.staff_user_progress);
        poDetailListView = (ListView) findViewById(R.id.staff_user_listview);

        connFilter = new IntentFilter();
        connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        csr = new ConnectionStateReceiver();
        registerReceiver(csr, connFilter);

        poDetailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                counter = position;

                TextView tv = (TextView) view.findViewById(R.id.po_detail_contactlistid);
                Intent i = new Intent(StaffNotRespUsersActivity.this, ProfileActivity.class);
                i.putExtra("contactlistid", tv.getText().toString());
                StaffNotRespUsersActivity.this.startActivity(i);
            }
        });

        processPODetail();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_staff_not_resp_users, menu);
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
                processPODetail();
            }

            this.isInternetConnected = true;
        }
        else{
            this.isInternetConnected = false;
        }
    }

    public void processPODetail(){
        final String get_notifications = po_detail_url + "?deviceIdentifier=" + session.getDeviceID() + "&loginUUID=" + session.getUUID() + "&office=" + po_abbrev;
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(get_notifications, new TextHttpResponseHandler() {

            @Override
            public void onStart() {
                progress.setVisibility(View.VISIBLE);
                poDetailListView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if(!StaffNotRespUsersActivity.this.isFinishing()){
                    if(StaffNotRespUsersActivity.this.isInternetConnected == false)
                        var.showAlert(StaffNotRespUsersActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                    else
                        var.showAlert(StaffNotRespUsersActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));
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
                        var.showAlert(StaffNotRespUsersActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));
                    }
                    else{
                        JSONArray poListItems = response.getJSONArray("results");
                        poDetailArrayList = new ArrayList<PODetail>();


                        for (int i = 0; i < poListItems.length(); i++) {
                            JSONObject jsonItem = poListItems.getJSONObject(i);
                            String lastname = jsonItem.getString("LastName");

                            PODetail po = new PODetail();
                            po.setLastname(lastname);
                            po.setFirstname(jsonItem.getString("FirstName"));
                            po.setDivision(jsonItem.getString("Division"));
                            po.setDaysNotResponded(jsonItem.getString("DaysNotResponded"));
                            po.setClsID(jsonItem.getString("CLSID"));
                            po.setSubtitle(jsonItem.getString("subtitle"));

                            if(lastname.equalsIgnoreCase("POName") || lastname.equalsIgnoreCase("officeTitle") ||
                                    lastname.equalsIgnoreCase("withEmail") || lastname.equalsIgnoreCase("ExternalEmail") || lastname.equalsIgnoreCase("noEmail")){
                                po.setIsUser(false);
                                if(lastname.equalsIgnoreCase("POName"))
                                    po.setFirstname(po_full_name);
                            }
                            else{
                                po.setIsUser(true);
                            }

                            poDetailArrayList.add(po);
                        }

                        poDetailListAdapter = new PODetailListAdapter(StaffNotRespUsersActivity.this, poDetailArrayList);
                        poDetailListView.setAdapter(poDetailListAdapter);
                    }

                    isPageLoaded = true;

                } catch (JSONException e) {
                    Log.e(TAG, "Problem retrieving Divisions of PO");
                    var.showAlert(StaffNotRespUsersActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.profile_favorite_error));
                    isPageLoaded = false;
                }
            }
        });
    }
}
