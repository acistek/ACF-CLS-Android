package com.acistek.cls;

import android.app.AlertDialog;
import android.app.Notification;
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
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;


public class NotificationActivity extends ActionBarActivity implements ConnectionStateListener, VariableChangedListener {

    private AppVar var = new AppVar();
    private static final String TAG = "NotificationActivity";
    private final String notification_url = var.cls_link + "/json/dms_dsp.cfm?acfcode=" + var.acfcode;

    private boolean isInternetConnected = true;
    private boolean isPageLoaded = false;

    private ListView notificationListView;
    private ProgressBar progress;
    private ArrayList<NotificationItem> notificationArray;

    private NotificationListAdapter notificationListAdapter;

    private IntentFilter connFilter;
    private ConnectionStateReceiver csr;
    private TextView actionbarText;

    SessionManager session;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        resources = getResources();
        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF336A90));
        actionBar.setCustomView(R.layout.abs_layout);

        actionbarText = (TextView) findViewById(R.id.search_title);
        actionbarText.setText("Notifications");

        progress = (ProgressBar) findViewById(R.id.progressnotification);
        notificationListView = (ListView) findViewById(R.id.notification_listview);

        connFilter = new IntentFilter();
        connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        csr = new ConnectionStateReceiver();
        registerReceiver(csr, connFilter);

        notificationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view.findViewById(R.id.notification_system_url);
                Intent i = new Intent(NotificationActivity.this, WebviewActivity.class);
                i.putExtra("url", tv.getText().toString());
                NotificationActivity.this.startActivity(i);
            }
        });

        processNotifications();
        MainApplication.notification_count = 0;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notification, menu);
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart(){
        super.onStart();
        MainApplication.uiInForeground = true;
        MainApplication.inNotifications = true;
        MainApplication.currentActivity = this;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        MainApplication.uiInForeground = false;
        MainApplication.inNotifications = false;
        MainApplication.currentActivity = null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MainApplication.inNotifications = false;
//        MainApplication.currentActivity = null;
        if(csr != null)
            unregisterReceiver(csr);
    }

    @Override
    public void isConnected(boolean isConnected) {
        if(isConnected){
            if(this.isInternetConnected == false && isPageLoaded == false){
                isPageLoaded = true;
                processNotifications();
            }

            this.isInternetConnected = true;
        }
        else{
            this.isInternetConnected = false;
        }
    }

    public void processNotifications(){
        final String get_notifications = notification_url;
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(get_notifications, new TextHttpResponseHandler() {

            @Override
            public void onStart() {
                progress.setVisibility(View.VISIBLE);
                notificationListView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if(!NotificationActivity.this.isFinishing()){
                    if(NotificationActivity.this.isInternetConnected == false)
                        var.showAlert(NotificationActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                    else
                        var.showAlert(NotificationActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));
                }
                isPageLoaded = false;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    JSONObject response = new JSONObject(responseString);
                    int resultCount = response.getInt("resultCount");

                    if(resultCount == 0){
                        Date date = new Date();
                        SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy, hh:mm aaa");

                        AlertDialog.Builder builder = new AlertDialog.Builder(NotificationActivity.this);
                        builder.setMessage(resources.getString(R.string.notifications_alert) + " " + format.format(date));
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NotificationActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        NotificationActivity.this.onBackPressed();
                                    }
                                });
                            }
                        });
                        AlertDialog dialog = builder.show();

                        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
                        messageView.setGravity(Gravity.CENTER);
                    }
                    else{
                        JSONArray notificationItems = response.getJSONArray("results");
                        notificationArray = new ArrayList<NotificationItem>();


                        for (int i = 0; i < notificationItems.length(); i++) {
                            JSONObject jsonItem = notificationItems.getJSONObject(i);

                            NotificationItem note = new NotificationItem();
                            note.setSystem_id(jsonItem.getString("systemID"));
                            note.setSystem_name(jsonItem.getString("systemName"));
                            note.setTime_down(jsonItem.getString("timeDown"));
                            note.setSystem_url(jsonItem.getString("systemURL"));
                            note.setResponsible(jsonItem.getString("responsible"));

                            notificationArray.add(note);
                        }

                        notificationListAdapter = new NotificationListAdapter(NotificationActivity.this, notificationArray, resources);
                        notificationListView.setAdapter(notificationListAdapter);

                    }

                    isPageLoaded = true;

                } catch (JSONException e) {
                    Log.e(TAG, "Problem retrieving Favorites.");
                    var.showAlert(NotificationActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.profile_favorite_error));
                    isPageLoaded = false;
                }
            }
        });
    }

    @Override
    public void newNotifications() {
        MainApplication.notification_count = 0;
    }
}
