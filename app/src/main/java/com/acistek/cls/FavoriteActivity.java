package com.acistek.cls;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;


public class FavoriteActivity extends ActionBarActivity implements ConnectionStateListener {

    private AppVar var = new AppVar();
    private static final String TAG = "FavoriteActivity";
    private final String favorites_url = var.cls_link + "/json/android_favorite_act.cfm";

    private ExpandableListView favoriteExpandableList;
    private FavoriteExpandableListAdapter favoriteExpandableListAdapter;
    private ProgressBar progress;
    private TextView favoriteErr;

    private ArrayList<String> groups;
    private ArrayList<UserSearch> groupItems;
    private LinkedHashMap<String, ArrayList<UserSearch>> groupCollection;

    private boolean isInternetConnected = true;
    private boolean isPageLoaded = false;

    private IntentFilter connFilter;
    private ConnectionStateReceiver csr;
    private TextView actionbarText;

    private Menu menu;

    SessionManager session;
    Resources resources;

    static int groupCounter = -1;
    static int childCounter = -1;
    static boolean isEditMenuClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        resources = getResources();
        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF336A90));
        actionBar.setCustomView(R.layout.abs_layout);

        actionbarText = (TextView) findViewById(R.id.search_title);
        actionbarText.setText("Favorites");

        favoriteExpandableList = (ExpandableListView) findViewById(R.id.favorite_expandable_list);
        progress = (ProgressBar) findViewById(R.id.progressfavorite);
        favoriteErr = (TextView) findViewById(R.id.favorite_err);

        connFilter = new IntentFilter();
        connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        csr = new ConnectionStateReceiver();
        registerReceiver(csr, connFilter);

        favoriteExpandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (!FavoriteActivity.this.isEditMenuClicked) {
                    groupCounter = groupPosition;
                    childCounter = childPosition;
                    favoriteExpandableList.setItemChecked(childPosition, true);
                    TextView userID = (TextView) v.findViewById(R.id.favorite_user_id);

                    Intent i = new Intent(FavoriteActivity.this, ProfileActivity.class);
                    i.putExtra("contactlistid", userID.getText());
                    FavoriteActivity.this.startActivity(i);
                    return true;
                } else {
                    return false;
                }
            }
        });

        favoriteExpandableList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!FavoriteActivity.this.isEditMenuClicked) {
                    if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                        int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                        int childPosition = ExpandableListView.getPackedPositionChild(id);
                        final View finalView = view;
                        TextView messageTV = (TextView) view.findViewById(R.id.favorite_user_name);
                        String message = messageTV.getText().toString();

                        groupCounter = groupPosition;
                        childCounter = childPosition;
                        favoriteExpandableList.setItemChecked(childPosition, true);

                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        final View layout = inflater.inflate(R.layout.alert_list, (ViewGroup) findViewById(R.id.alert_remove_hold));

                        final TextView gmessage = (TextView) layout.findViewById(R.id.alert_remove_message);
                        gmessage.setText(message);

                        AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteActivity.this).setView(layout);
                        final AlertDialog d = builder.create();

                        d.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                Button adelete = (Button) layout.findViewById(R.id.alert_remove_delete);
                                Button acancel = (Button) layout.findViewById(R.id.alert_remove_cancel);

                                adelete.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        FavoriteActivity.this.deleteFavoriteUser(finalView);
                                        d.dismiss();
                                    }
                                });

                                acancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        d.dismiss();
                                    }
                                });
                            }
                        });

                        d.show();

                        return true;
                    }
                }

                return false;
            }
        });

        processFavorites();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favorite, menu);
        this.menu = menu;

        isEditMenuClicked = false;
        groupCounter = -1;
        childCounter = -1;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_favorite) {
            isEditMenuClicked = true;
            groupCounter = -1;
            childCounter = -1;
            item.setVisible(false);
            menu.findItem(R.id.action_done_favorite).setVisible(true);

            if(favoriteExpandableListAdapter != null)
                favoriteExpandableListAdapter.notifyDataSetChanged();
            return true;
        }
        else if (id == R.id.action_done_favorite){
            isEditMenuClicked = false;
            item.setVisible(false);
            menu.findItem(R.id.action_edit_favorite).setVisible(true);

            if(favoriteExpandableListAdapter != null)
                favoriteExpandableListAdapter.notifyDataSetChanged();
            return true;
        }
        else if (id == android.R.id.home){
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
                processFavorites();
            }

            this.isInternetConnected = true;
        }
        else{
            this.isInternetConnected = false;
        }
    }

    public void processFavorites(){
        String acfcode = var.acfcode;
        String userID = session.getContactlistid();
        String action = "view";
        groupCounter = -1;
        childCounter = -1;

        try {
            userID = URLEncoder.encode(userID, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            userID = "0";
            Log.e(TAG, "Failed to encode ids");
        }

        if(!userID.equalsIgnoreCase("0")){
            final String get_favorites = favorites_url + "?contactlistid=" + userID + "&acfcode=" + acfcode + "&action=" + action;
            AsyncHttpClient client = new AsyncHttpClient();

            client.post(get_favorites, new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    progress.setVisibility(View.VISIBLE);
                    favoriteExpandableList.setVisibility(View.VISIBLE);
                    favoriteErr.setVisibility(View.GONE);
                }

                @Override
                public void onFinish() {
                    progress.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    if(!FavoriteActivity.this.isFinishing()){
                        if(FavoriteActivity.this.isInternetConnected == false)
                            var.showAlert(FavoriteActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                        else
                            var.showAlert(FavoriteActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));
                    }
                    isPageLoaded = false;
                    favoriteExpandableList.setVisibility(View.GONE);
                    favoriteErr.setVisibility(View.VISIBLE);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    try {
                        JSONObject response = new JSONObject(responseString);
                        int success = response.getInt("success");
                        int resultCount = response.getInt("resultCount");

                        if(success == 1){
                            if(resultCount == 0){
                                var.showAlert(FavoriteActivity.this, "", response.getString("message"));
                                if(favoriteExpandableListAdapter != null){
                                    groupCollection.clear();
                                    groups.clear();
                                    favoriteExpandableListAdapter.notifyDataSetChanged();
                                }
                                menu.findItem(R.id.action_edit_favorite).setVisible(true);
                                menu.findItem(R.id.action_edit_favorite).setEnabled(false);
                                menu.findItem(R.id.action_done_favorite).setVisible(false);
                            }
                            else {
                                JSONArray groupList = response.getJSONArray("groups");
                                JSONArray groupListItems = response.getJSONArray("results");

                                groups = new ArrayList<String>();
                                groupItems = new ArrayList<UserSearch>();

                                for (int i = 0; i < groupList.length(); i++) {
                                    JSONObject jsonGroupItem = groupList.getJSONObject(i);
                                    String name = jsonGroupItem.getString("groupName");
                                    groups.add(name);
                                }

                                for (int i = 0; i < groupListItems.length(); i++) {
                                    JSONObject jsonItem = groupListItems.getJSONObject(i);

                                    UserSearch user = new UserSearch();
                                    user.setFirstname(jsonItem.getString("firstname"));
                                    user.setLastname(jsonItem.getString("lastname"));
                                    user.setContactlistid(jsonItem.getString("contactlistid"));
                                    user.setEmail(jsonItem.getString("emailaddress"));
                                    user.setGroupName(jsonItem.getString("groupName"));

                                    groupItems.add(user);
                                }

                                FavoriteActivity.this.createCollection();

                                favoriteExpandableListAdapter = new FavoriteExpandableListAdapter(FavoriteActivity.this, groups, groupCollection);
                                favoriteExpandableList.setAdapter(favoriteExpandableListAdapter);

                                int gcount = favoriteExpandableListAdapter.getGroupCount();
                                for(int i = 1; i <= gcount; i++)
                                    favoriteExpandableList.expandGroup(i - 1);
                            }
                        }
                        else{
                            var.showAlert(FavoriteActivity.this, "", response.getString("error_message"));
                            favoriteExpandableList.setVisibility(View.GONE);
                            favoriteErr.setVisibility(View.VISIBLE);
                        }
                        isPageLoaded = true;

                    } catch (JSONException e) {
                        Log.e(TAG, "Problem retrieving Favorites.");
                        var.showAlert(FavoriteActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.profile_favorite_error));
                        favoriteExpandableList.setVisibility(View.GONE);
                        favoriteErr.setVisibility(View.VISIBLE);
                        isPageLoaded = false;
                    }
                }
            });
        }
        else{
            var.showAlert(FavoriteActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.profile_favorite_error));
            isPageLoaded = false;
        }
    }

    public void createCollection(){
        groupCollection = new LinkedHashMap<String, ArrayList<UserSearch>>();

        for(String gname: groups){
            ArrayList<UserSearch> sameGroup = new ArrayList<UserSearch>();

            for(UserSearch user: groupItems){
                if(user.getGroupName().equals(gname)){
                    sameGroup.add(user);
                }
            }

            groupCollection.put(gname, sameGroup);
        }
    }

    public void deleteFavoriteUser(View v){
        TextView favTextView = (TextView) v.findViewById(R.id.favorite_user_id_remove);
        TextView gnameTextView = (TextView) v.findViewById(R.id.favorite_group_name_remove);

        String acfcode = var.acfcode;
        String userID = session.getContactlistid();
        String action = "del";
        String favID = favTextView.getText().toString();
        String group_name = gnameTextView.getText().toString();

        try {
            userID = URLEncoder.encode(userID, "UTF-8");
            favID = URLEncoder.encode(favID, "UTF-8");
            group_name = URLEncoder.encode(group_name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            userID = "0";
            favID = "0";
            group_name = "";
            Log.e(TAG, "Failed to encode ids");
        }

        if(!userID.equalsIgnoreCase("0") && !favID.equalsIgnoreCase("0")){
            final String favorite_del_user = favorites_url + "?contactlistid=" + userID + "&acfcode=" + acfcode + "&favoriteid=" + favID + "&action=" + action + "&group=" + group_name;
            AsyncHttpClient client = new AsyncHttpClient();

            client.post(favorite_del_user, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    if(!FavoriteActivity.this.isFinishing()) {
                        if (FavoriteActivity.this.isInternetConnected == false)
                            var.showAlert(FavoriteActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                        else
                            var.showAlert(FavoriteActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));
                    }
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    try {
                        JSONObject response = new JSONObject(responseString);
                        int success = response.getInt("success");

                        if(success == 1){
                            AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteActivity.this);
                            builder.setMessage(response.getString("message"));
                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FavoriteActivity.this.processFavorites();
                                }
                            });
                            AlertDialog dialog = builder.show();

                            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
                            messageView.setGravity(Gravity.CENTER);
                        }
                        else{
                            var.showAlert(FavoriteActivity.this, "", response.getString("error_message"));
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "Problem adding user.");
                        var.showAlert(FavoriteActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.profile_favorite_error));
                    }
                }
            });
        }
        else{
            var.showAlert(FavoriteActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.profile_favorite_error));
        }
    }
}
