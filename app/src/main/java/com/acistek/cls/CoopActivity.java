package com.acistek.cls;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

public class CoopActivity extends ActionBarActivity implements ConnectionStateListener {

    private AppVar var = new AppVar();
    private static final String TAG = "CoopActivity";
    private final String coop_url = var.cls_link + "/json/coop_dsp.cfm";

    private ProgressBar progress;
    private ListView coopListView;

    private boolean isInternetConnected = false;
    private boolean isPageLoaded = false;

    private IntentFilter connFilter;
    private ConnectionStateReceiver csr;
    private TextView actionbarText;
    private TextView online;
    private TextView offline;
    private LayoutInflater inflater;

    private SQLController coopDbController;

    SessionManager session;
    Resources resources;

    // JSON Node names
    static final String TAG_RESULT_CNT = "resultCount";
    static final String TAG_ORDERID = "orderID";
    static final String TAG_DATATYPEID = "dataTypeID";
    static final String TAG_RESULTS = "results";
    static final String TAG_GROUP_NAME = "groupName";
    static final String TAG_CONTACTLISTID = "contactListID";
    static final String TAG_COOP_NAME = "coopName";
    static final String TAG_CELL_PHONE = "cellPhone";
    static final String TAG_OFFICE_PHONE = "officePhone";
    static final String TAG_HOME_PHONE = "homePhone";
    static final String TAG_EMAIL = "emailAddress";
    static final String TAG_COOP_TITLE = "coopTitle";

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> contactList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coop);

        resources = getResources();
        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF336A90));
        actionBar.setCustomView(R.layout.abs_layout);

        actionbarText = (TextView) findViewById(R.id.search_title);
        actionbarText.setText("COOP");

        connFilter = new IntentFilter();
        connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        csr = new ConnectionStateReceiver();
        registerReceiver(csr, connFilter);

        contactList = new ArrayList<HashMap<String, String>>();

        coopListView = (ListView) findViewById(R.id.coop_list);
        progress = (ProgressBar) findViewById(R.id.progresscoop);
        online = (TextView) findViewById(R.id.online);
        offline = (TextView) findViewById(R.id.offline);

        processCOOP();

        coopDbController = new SQLController(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_coop, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    public void isConnected(boolean isConnected) {
        if(isConnected){
            if(this.isInternetConnected == false && isPageLoaded == false){
                isPageLoaded = true;
                processCOOP();
            }
            this.isInternetConnected = true;
            offline.setTextColor(Color.parseColor("#8AB4CD"));
            online.setTextColor(Color.parseColor("#ffffff"));
        }
        else{
            this.isInternetConnected = false;
            online.setTextColor(Color.parseColor("#8AB4CD"));
            offline.setTextColor(Color.parseColor("#ffffff"));
        }
    }

    public void processCOOP(){
        String acfcode = var.acfcode;
        String userID = session.getContactlistid();

        try {
            userID = URLEncoder.encode(userID, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            userID = "0";
            Log.e(TAG, "Failed to encode ids");
        }

        if(!userID.equalsIgnoreCase("0")){
            final String get_coop = coop_url + "?contactlistid=" + userID + "&acfcode=" + acfcode;
            AsyncHttpClient client = new AsyncHttpClient();
            client.post(get_coop, new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    progress.setVisibility(View.VISIBLE);
                    coopListView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFinish() {
                    progress.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    isPageLoaded = false;

                    //Retrieve List from database
                    getOffline();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    try {
                        contactList = new ArrayList<HashMap<String, String>>();
                        JSONObject response = new JSONObject(responseString);
                        JSONArray coopInfo = response.getJSONArray(TAG_RESULTS);
                        int resultCount = response.getInt(TAG_RESULT_CNT);

                        try{
                            coopDbController.open();
                        }
                        catch(SQLException e){
                            e.printStackTrace();
                        }
                        coopDbController.deleteCOOP();

                        for (int i = 0; i < coopInfo.length(); i++) {
                            JSONObject c = coopInfo.getJSONObject(i);

                            String group_name = c.getString(TAG_GROUP_NAME);
                            String id = c.getString(TAG_CONTACTLISTID);
                            String name = c.getString(TAG_COOP_NAME);

                            String cell = c.getString(TAG_CELL_PHONE);
                            String office = c.getString(TAG_OFFICE_PHONE);

                            coopDbController.insertCOOP(id,group_name,name,cell,office);

                            // tmp hashmap for single coop
                            HashMap<String, String> contact = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            contact.put(TAG_GROUP_NAME, group_name);
                            contact.put(TAG_CONTACTLISTID, id);
                            contact.put(TAG_COOP_NAME, name);
                            contact.put(TAG_CELL_PHONE, cell);
                            contact.put(TAG_OFFICE_PHONE, office);

                            // adding contact to contact list
                            contactList.add(contact);
                        }
                        coopDbController.closeCOOP();
                        CoopListAdapter adapter = new CoopListAdapter(CoopActivity.this, contactList);
                        coopListView.setAdapter(adapter);

                        isPageLoaded = true;

                    } catch (JSONException e) {
                        Log.e(TAG, "Problem retrieving COOP.");

                        //Retrieve list from database
                        getOffline();

                        isPageLoaded = false;
                    }
                }
            });
        }
        else{
            //var.showAlert(CoopActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.profile_favorite_error));
            //Retrieve list from database
            getOffline();

            isPageLoaded = false;
        }
    }

    public void getOffline(){
        //get coop from internal database
        try{
            coopDbController.open();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        Cursor cursor = coopDbController.fetchCOOP();

        MyCursorAdapter adapter = new MyCursorAdapter(this, cursor);
        coopListView.setAdapter(adapter);
    }

    private class MyCursorAdapter extends BaseAdapter{
        private Context context;
        private Cursor cursor;
        private LayoutInflater inflater;
        private LinearLayout pageLayout;

        public MyCursorAdapter(Context context, Cursor cursor) {
            super();
            this.context = context;
            this.cursor = cursor;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount(){
            return cursor.getCount();
        }

        @Override
        public Object getItem(int position){
            return position;
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            cursor.moveToPosition(position);
            pageLayout = (LinearLayout) inflater.inflate(R.layout.list_item, null);
            TextView name = (TextView) pageLayout.findViewById(R.id.name);
            TextView cell_phone = (TextView) pageLayout.findViewById(R.id.cell_phone);
            TextView office_phone = (TextView) pageLayout.findViewById(R.id.office_phone);

            String group_name = cursor.getString(1);
            String coop_name = cursor.getString(2);
            String cell_phone_number = cursor.getString(3);
            String office_phone_number = cursor.getString(4);

            if (coop_name.equals("coop") || coop_name.equals("header")) {
                if (coop_name.equals("coop")) {
                    pageLayout.setBackgroundColor(Color.parseColor("#AAAAAA"));
                } else {
                    pageLayout.setBackgroundColor(Color.parseColor("#CFC3AE"));
                }
                pageLayout.setPadding(10, 10, 10, 10);
                name.setText(group_name);
                pageLayout.findViewById(R.id.cell_phone_label).setVisibility(View.GONE);
                pageLayout.findViewById(R.id.office_phone_label).setVisibility(View.GONE);
                pageLayout.findViewById(R.id.cell_phone).setVisibility(View.GONE);
                pageLayout.findViewById(R.id.office_phone).setVisibility(View.GONE);
                pageLayout.findViewById(R.id.detail_icon).setVisibility(View.GONE);
            }
            else {
                if (cursor.getPosition() % 2 == 0) {
                    pageLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                } else {
                    pageLayout.setBackgroundColor(Color.parseColor("#F6F3EE"));
                }

                name.setText(coop_name);
                cell_phone.setText(cell_phone_number);
                office_phone.setText(office_phone_number);
                final CharSequence[] items = {
                        "Call Cell " + cell_phone_number, "Text Cell " + cell_phone_number, "Call Office " + office_phone_number
                };

                pageLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CoopActivity.this);
                        builder.setTitle("Please select the action below");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                                if (item == 0) {
                                    phoneIntent.setData(Uri.parse("tel:" + items[item].toString().substring(10)));
                                    CoopActivity.this.startActivity(phoneIntent);
                                } else if (item == 2) {
                                    phoneIntent.setData(Uri.parse("tel:" + items[item].toString().substring(12)));
                                    CoopActivity.this.startActivity(phoneIntent);
                                } else {
                                    sendIntent.setData(Uri.parse("sms:" + items[item].toString().substring(10)));
                                    CoopActivity.this.startActivity(sendIntent);
                                }
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                        alert.setCanceledOnTouchOutside(true);
                    }
                });
            }
            return pageLayout;
        }
    }
}