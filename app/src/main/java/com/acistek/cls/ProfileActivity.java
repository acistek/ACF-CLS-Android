package com.acistek.cls;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileActivity extends ActionBarActivity implements ConnectionStateListener{

    private String mContactListID;
    private String searchContactListID;

    private AppVar var = new AppVar();
    private static final String TAG = "ProfileActivity";
    private final String user_profile_url = var.cls_link + "/json/user_dsp.cfm";
    private final String favorites_url = var.cls_link + "/json/favorite_act.cfm?android=1";

    private boolean isInternetConnected;
    private boolean isPageLoaded = true;
    private String mDefaultPhoneNumber = null;
    private String mDefaultAddress = null;
    private String mDefaultEmail = null;
    private boolean isPhoneSelected = false;
    private boolean isEmailSelected = false;
    private String selectedEmail = null;
    private String selectedPhone = null;

    private IntentFilter connFilter;
    private ConnectionStateReceiver csr;
    private TextView actionbarText;
    private TextView errorLoading;
    private ListView profileListView;
    private ListView groupListView;
    private ProgressBar progress;
    private ProfileListAdapter profileListAdapter;

    private LinearLayout profileFavTab;
    private LinearLayout profileCallTab;
    private LinearLayout profileTextTab;
    private LinearLayout profileMailTab;
    private LinearLayout profileMapTab;
    private LinearLayout groupListLayout;
    private LinearLayout profileBottomLayout;

    private ImageView profileFavTabImage;
    private ImageView profileCallTabImage;
    private ImageView profileTextTabImage;
    private ImageView profileMailTabImage;
    private ImageView profileMapTabImage;

    private TextView profileFavTabText;
    private TextView profileCallTabText;
    private TextView profileTextTabText;
    private TextView profileMailTabText;
    private TextView profileMapTabText;

    private String selectedGroupNameFromList;
    private ArrayList<String> groupListInfo;

    static int counter = 0;
    static int group_counter = -1;

    SessionManager session;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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
        errorLoading = (TextView) findViewById(R.id.profile_err);
        profileListView = (ListView) findViewById(R.id.profile_listview);
        groupListView = (ListView) findViewById(R.id.profile_select_grouplist);
        progress = (ProgressBar) findViewById(R.id.progressprofile);
        mContactListID = session.getContactlistid();
        searchContactListID = getIntent().getExtras().getString("contactlistid");

        profileFavTab = (LinearLayout) findViewById(R.id.profile_fav);
        profileCallTab = (LinearLayout) findViewById(R.id.profile_call);
        profileTextTab = (LinearLayout) findViewById(R.id.profile_text);
        profileMailTab = (LinearLayout) findViewById(R.id.profile_email);
        profileMapTab = (LinearLayout) findViewById(R.id.profile_maps);
        groupListLayout = (LinearLayout) findViewById(R.id.profile_layout_top);
        profileBottomLayout = (LinearLayout) findViewById(R.id.profile_layout_bottom);

        profileFavTabImage = (ImageView) findViewById(R.id.profile_fav_image);
        profileCallTabImage = (ImageView) findViewById(R.id.profile_call_image);
        profileTextTabImage = (ImageView) findViewById(R.id.profile_text_image);
        profileMailTabImage = (ImageView) findViewById(R.id.profile_email_image);
        profileMapTabImage = (ImageView) findViewById(R.id.profile_maps_image);

        profileFavTabText = (TextView) findViewById(R.id.profile_fav_text);
        profileCallTabText = (TextView) findViewById(R.id.profile_call_text);
        profileTextTabText = (TextView) findViewById(R.id.profile_text_text);
        profileMailTabText = (TextView) findViewById(R.id.profile_email_text);
        profileMapTabText = (TextView) findViewById(R.id.profile_maps_text);

        connFilter = new IntentFilter();
        connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        csr = new ConnectionStateReceiver();
        registerReceiver(csr, connFilter);

        if(searchContactListID.equalsIgnoreCase(mContactListID))
            actionbarText.setText("My Profile");
        else
            actionbarText.setText("User's Profile");

        profileListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                counter = position;
                profileListView.setItemChecked(position, true);

                TextView tvHead = (TextView) view.findViewById(R.id.profile_sub_header);
                String header = tvHead.getText().toString();

                if(header.equalsIgnoreCase("Phone:") || header.equalsIgnoreCase("Contact Phone:") ||
                        header.equalsIgnoreCase("Cell Phone:") || header.equalsIgnoreCase("Home Phone:")){
                    TextView tvData = (TextView) view.findViewById(R.id.profile_sub_data);

                    if(tvData.getText().toString().equalsIgnoreCase("N/A")){
                        ProfileActivity.this.disableButton(profileCallTab, profileCallTabImage, profileCallTabText);
                        ProfileActivity.this.disableButton(profileTextTab, profileTextTabImage, profileTextTabText);
                    }
                    else{
                        ProfileActivity.this.enableButton(profileCallTab, profileCallTabImage, profileCallTabText);
                        ProfileActivity.this.enableButton(profileTextTab, profileTextTabImage, profileTextTabText);
                        selectedPhone = tvData.getText().toString().substring(1, 4) + tvData.getText().toString().substring(6, 9) + tvData.getText().toString().substring(10);
                        isPhoneSelected = true;
                    }
                }
                else{
                    selectedPhone = mDefaultPhoneNumber;
                    if(selectedPhone == null){
                        ProfileActivity.this.disableButton(profileCallTab, profileCallTabImage, profileCallTabText);
                        ProfileActivity.this.disableButton(profileTextTab, profileTextTabImage, profileTextTabText);
                    }
                    else{
                        ProfileActivity.this.enableButton(profileCallTab, profileCallTabImage, profileCallTabText);
                        ProfileActivity.this.enableButton(profileTextTab, profileTextTabImage, profileTextTabText);
                    }
                    isPhoneSelected = false;
                }

                if(header.equalsIgnoreCase("Email Address:") || header.equalsIgnoreCase("Personal Email Address:")){
                    TextView tvData = (TextView) view.findViewById(R.id.profile_sub_data);

                    if(tvData.getText().toString().equalsIgnoreCase("N/A")){
                        ProfileActivity.this.disableButton(profileMailTab, profileMailTabImage, profileMailTabText);
                    }
                    else{
                        ProfileActivity.this.enableButton(profileMailTab, profileMailTabImage, profileMailTabText);
                        selectedEmail = tvData.getText().toString();
                        isEmailSelected = true;
                    }

                }
                else{
                    selectedEmail = mDefaultEmail;
                    if(selectedEmail == null){
                        ProfileActivity.this.disableButton(profileMailTab, profileMailTabImage, profileMailTabText);
                    }
                    else{
                        ProfileActivity.this.enableButton(profileMailTab, profileMailTabImage, profileMailTabText);
                    }
                    isEmailSelected = false;
                }
            }
        });

        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                group_counter = position;
                groupListView.setItemChecked(position, true);

                TextView t = (TextView) view.findViewById(R.id.favorite_group_list);
                selectedGroupNameFromList = t.getText().toString();
            }
        });

        processUserInfo(mContactListID, searchContactListID);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            super.onBackPressed();
            return true;
        }
        else if(id == R.id.blank_ac){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void isConnected(boolean isConnected) {
        if(isConnected){
            if(this.isInternetConnected == false && isPageLoaded == false){
                isPageLoaded = true;
                processUserInfo(mContactListID, searchContactListID);
            }

            this.isInternetConnected = true;
        }
        else{
            this.isInternetConnected = false;
        }
    }

    public void processUserInfo(String userID, String searchUserID){
        String acfcode = var.acfcode;

        try {
            userID = URLEncoder.encode(userID, "UTF-8");
            searchUserID = URLEncoder.encode(searchUserID, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            userID = "0";
            searchUserID = "0";
            Log.e(TAG, "Failed to encode ids");
        }

        String profile_url = user_profile_url + "?contactListID=" + searchUserID + "&acfcode=" + acfcode + "&adminID=" + userID + "&deviceIdentifier=" + session.getDeviceID() + "&loginUUID=" + session.getUUID();
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(profile_url, new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                progress.setVisibility(View.VISIBLE);
                profileListView.setVisibility(View.VISIBLE);
                errorLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFinish() {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if(!ProfileActivity.this.isFinishing()){
                    if(ProfileActivity.this.isInternetConnected == false)
                        var.showAlert(ProfileActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                    else
                        var.showAlert(ProfileActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));
                }
                isPageLoaded = false;
                profileListView.setVisibility(View.GONE);
                errorLoading.setVisibility(View.VISIBLE);

                ProfileActivity.this.disableButton(profileFavTab, profileFavTabImage, profileFavTabText);
                ProfileActivity.this.disableButton(profileCallTab, profileCallTabImage, profileCallTabText);
                ProfileActivity.this.disableButton(profileTextTab, profileTextTabImage, profileTextTabText);
                ProfileActivity.this.disableButton(profileMailTab, profileMailTabImage, profileMailTabText);
                ProfileActivity.this.disableButton(profileMapTab, profileMapTabImage, profileMapTabText);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    JSONObject response = new JSONObject(responseString);
                    JSONArray list = response.getJSONArray("results");
                    int resultCount = response.getInt("resultCount");
                    ArrayList<UserProfile> userProfileInfo = new ArrayList<UserProfile>();

                    ProfileActivity.this.enableButton(profileFavTab, profileFavTabImage, profileFavTabText);
                    ProfileActivity.this.enableButton(profileCallTab, profileCallTabImage, profileCallTabText);
                    ProfileActivity.this.enableButton(profileTextTab, profileTextTabImage, profileTextTabText);
                    ProfileActivity.this.enableButton(profileMailTab, profileMailTabImage, profileMailTabText);
                    ProfileActivity.this.enableButton(profileMapTab, profileMapTabImage, profileMapTabText);

                    if(resultCount != 0){
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject jsonItem = list.getJSONObject(i);

                            String header = jsonItem.getString("fieldName");
                            String info = jsonItem.getString("fieldValue");

                            info = processInfo(header, info);

                            UserProfile user = new UserProfile();
                            user.setTitle(header);
                            user.setDescription(info);

                            if(header.equalsIgnoreCase("header"))
                                user.setHeader(true);
                            else
                                user.setHeader(false);

                            if(header.equalsIgnoreCase("grouplist")){
                                ProfileActivity.this.populateGroupList(info);
                            }

                            userProfileInfo.add(user);
                        }

                        profileListAdapter = new ProfileListAdapter(ProfileActivity.this, userProfileInfo);
                        profileListView.setAdapter(profileListAdapter);
                        isPageLoaded = true;
                    }
                    else{
                        var.showAlert(ProfileActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));

                        profileListView.setVisibility(View.GONE);
                        errorLoading.setVisibility(View.VISIBLE);
                        isPageLoaded = false;
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Error processing response from url");
                    ProfileActivity.this.disableButton(profileFavTab, profileFavTabImage, profileFavTabText);
                    ProfileActivity.this.disableButton(profileCallTab, profileCallTabImage, profileCallTabText);
                    ProfileActivity.this.disableButton(profileTextTab, profileTextTabImage, profileTextTabText);
                    ProfileActivity.this.disableButton(profileMailTab, profileMailTabImage, profileMailTabText);
                    ProfileActivity.this.disableButton(profileMapTab, profileMapTabImage, profileMapTabText);
                    profileListView.setVisibility(View.GONE);
                    errorLoading.setVisibility(View.VISIBLE);
                    isPageLoaded = false;
                }
            }

            public String processInfo(String header, String info){

                if(header.equalsIgnoreCase("Phone:") || header.equalsIgnoreCase("Fax:") ||
                        header.equalsIgnoreCase("Contact Phone:") || header.equalsIgnoreCase("Cell Phone:") || header.equalsIgnoreCase("Home Phone:")){
                    if(!info.equalsIgnoreCase("") && !info.equalsIgnoreCase("N/A") && info.length() >= 10){
                        if(header.equalsIgnoreCase("Phone:"))
                            mDefaultPhoneNumber = info;

                        String area = info.substring(0, 3);
                        String prefix = info.substring(3, 6);
                        String line = info.substring(6);
                        info = "(" + area + ") " + prefix + "-" + line;
                    }
                    else{
                        info = "N/A";
                        if(header.equalsIgnoreCase("Phone:")){
                            ProfileActivity.this.disableButton(profileCallTab, profileCallTabImage, profileCallTabText);
                            ProfileActivity.this.disableButton(profileTextTab, profileTextTabImage, profileTextTabText);
                        }
                    }
                }
                else if(header.equalsIgnoreCase("Building Address:")){
                    if(!info.equalsIgnoreCase("") && !info.equalsIgnoreCase(", , ") && !info.equalsIgnoreCase("N/A"))
                        mDefaultAddress = info;
                    else{
                        info = "N/A";
                        ProfileActivity.this.disableButton(profileMapTab, profileMapTabImage, profileMapTabText);
                    }
                }
                else if(header.equalsIgnoreCase("Floor/Location/Cubicle:")){
                    if(info.equalsIgnoreCase("") || info.equalsIgnoreCase(" /  / ") || info.equalsIgnoreCase("N/A"))
                        info = "N/A";
                }
                else if(header.equalsIgnoreCase("Email Address:")){
                    if(!info.equalsIgnoreCase("") && !info.equalsIgnoreCase("N/A"))
                        mDefaultEmail = info;
                    else{
                        info = "N/A";
                        ProfileActivity.this.disableButton(profileMailTab, profileMailTabImage, profileMailTabText);
                    }
                }

                return info;
            }
        });
    }

    public void disableButton(LinearLayout layout, ImageView image, TextView text){
        layout.setEnabled(false);
        image.setAlpha(50);
        text.setTextColor(Color.argb(50, 51, 106, 144));
    }

    public void enableButton(LinearLayout layout, ImageView image, TextView text){
        layout.setEnabled(true);
        image.setAlpha(255);
        text.setTextColor(Color.argb(255, 51, 106, 144));
    }

    public void goCall(View view){
        String number;

        if(selectedPhone != null && isPhoneSelected){
            number = selectedPhone;
        }
        else{
            number = mDefaultPhoneNumber;
        }


        if(number != null) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
            if(intent.resolveActivity(getPackageManager()) != null){
                startActivity(intent);
            }
            else{
                //alert there was a problem
                var.showAlert(this, resources.getString(R.string.alert_error), "Problem initiating call.");
            }
        }
        else{
            //alert this profile does not have a number
            var.showAlert(this, resources.getString(R.string.alert_error), "This profile does not have a phone number.");
        }
    }

    public void goText(View view){
        String number;

        if(selectedPhone != null && isPhoneSelected){
            number = selectedPhone;
        }
        else{
            number = mDefaultPhoneNumber;
        }

        if(number != null){
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null));
            if(intent.resolveActivity(getPackageManager()) != null){
                startActivity(intent);
            }
            else{
                //alert there was a problem
                var.showAlert(this, resources.getString(R.string.alert_error), "Problem initiating text message.");
            }
        }
        else{
            //alert this profile does not have a number
            var.showAlert(this, resources.getString(R.string.alert_error), "This profile does not have a phone number.");
        }

    }

    public void goEmail(View view){
        String email;

        if(selectedEmail != null && isEmailSelected){
            email = selectedEmail;
        }
        else{
            email = mDefaultEmail;
        }

        if(email != null){
            Intent i = new Intent(ProfileActivity.this, EmailActivity.class);
            i.putExtra("email", email);
            i.putExtra("groupName", "");
            this.startActivity(i);
        }
        else{
            var.showAlert(this, resources.getString(R.string.alert_error), "This profile does not have a email.");
        }

    }

    public void goMap(View view){
        if(mDefaultAddress != null){
            String address = null;
            try {
                address = URLEncoder.encode(mDefaultAddress, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Problem encoding address");
            }

            if(address != null){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("geo:0,0?q=" + address));
                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivity(intent);
                }
                else{
                    //alert there was a problem
                    var.showAlert(this, resources.getString(R.string.alert_error), "There was an issue.");
                }
            }
            else{
                //alert there was a problem
                var.showAlert(this, resources.getString(R.string.alert_error), "There was an issue.");
            }
        }
        else{
            //alert no address available
            var.showAlert(this, resources.getString(R.string.alert_error), "This profile does not have an address.");
        }
    }

    public void goFav(View view){

        final String groupCharacterSet = "\'1234567890-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        final InputFilter groupFilter = new InputFilter() {
            boolean shouldShowAlert = true;

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                boolean keepOriginal = true;
                StringBuilder sb = new StringBuilder(end - start);
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (isCharAllowed(c)) // put your condition here
                        sb.append(c);
                    else
                        keepOriginal = false;
                }
                if (keepOriginal)
                    return null;
                else {
                    if(shouldShowAlert){
                        shouldShowAlert = false;

                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                        builder.setMessage("The Group Name must be in letters, numbers, hyphens, single quotes, and spaces only.");
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                shouldShowAlert = true;
                            }
                        });
                        AlertDialog dialog = builder.show();

                        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
                        messageView.setGravity(Gravity.CENTER);

                    }


                    if (source instanceof Spanned) {
                        SpannableString sp = new SpannableString(sb);
                        TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                        return sp;
                    } else {
                        return sb;
                    }
                }
            }

            private boolean isCharAllowed(char c) {
                return Character.isLetterOrDigit(c) || Character.isSpaceChar(c) || groupCharacterSet.contains("" + c);
            }
        };

        if(groupListInfo.isEmpty() || groupListInfo == null || groupListInfo.toString().equalsIgnoreCase("[]")) {
            LinearLayout glayout = new LinearLayout(ProfileActivity.this);
            glayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(20, 20, 20, 20);

            final EditText gname = new EditText(ProfileActivity.this);
            gname.setBackgroundDrawable(resources.getDrawable(R.drawable.login_textfield));
            gname.setFilters(new InputFilter[]{groupFilter, new InputFilter.LengthFilter(35)});
            gname.setHint(resources.getString(R.string.profile_enter_group));

            gname.addTextChangedListener(new TextWatcher() {
                boolean shouldShowAlert = true;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(s.length() == 35){
                        if(shouldShowAlert){
                            shouldShowAlert = false;

                            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                            builder.setMessage("The Group Name must be less than 35 characters.");
                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    shouldShowAlert = true;
                                }
                            });
                            AlertDialog dialog = builder.show();

                            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
                            messageView.setGravity(Gravity.CENTER);
                        }
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            glayout.addView(gname, params);

            final AlertDialog d = new AlertDialog.Builder(ProfileActivity.this)
                    .setMessage("Please enter a Group Name")
                    .setView(glayout)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            d.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (gname.getText().toString().trim().equalsIgnoreCase("")) {
                                var.showAlert(ProfileActivity.this, "", "Please enter a Group Name");
                                gname.setText("");
                            } else {
                                ProfileActivity.this.addGroupFavorite(gname.getText().toString().trim());
                                d.dismiss();
                            }
                        }
                    });
                }
            });

            d.show();

            TextView messageView = (TextView) d.findViewById(android.R.id.message);
            messageView.setGravity(Gravity.CENTER);
            messageView.setTypeface(null, Typeface.BOLD);
        }
        else {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.alert_favorite_group, (ViewGroup) findViewById(R.id.alert_favorite_layout));
            final EditText gname = (EditText) layout.findViewById(R.id.alert_favorite_group_name);
            gname.setFilters(new InputFilter[]{groupFilter, new InputFilter.LengthFilter(35)});
            gname.setHint(resources.getString(R.string.profile_enter_group));

            gname.addTextChangedListener(new TextWatcher() {
                boolean shouldShowAlert = true;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(s.length() == 35){
                        if(shouldShowAlert){
                            shouldShowAlert = false;

                            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                            builder.setMessage("The Group Name must be less than 35 characters.");
                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    shouldShowAlert = true;
                                }
                            });
                            AlertDialog dialog = builder.show();

                            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
                            messageView.setGravity(Gravity.CENTER);
                        }
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            final TextView gmessage = (TextView) layout.findViewById(R.id.alert_favorite_message);
            gmessage.setText("Enter New Group Name or Select From Group List");

            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this).setView(layout);
            final AlertDialog d = builder.create();

            d.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button aok = (Button) layout.findViewById(R.id.alert_favorite_ok);
                    Button acancel = (Button) layout.findViewById(R.id.alert_favorite_cancel);
                    Button agrouplist = (Button) layout.findViewById(R.id.alert_favorite_grouplist);

                    aok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(gname.getText().toString().trim().equalsIgnoreCase("")){
                                var.showAlert(ProfileActivity.this, "", "Please enter a Group Name");
                                gname.setText("");
                            }
                            else{
                                ProfileActivity.this.addGroupFavorite(gname.getText().toString().trim());
                                d.dismiss();
                            }
                        }
                    });

                    acancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            d.dismiss();
                        }
                    });

                    agrouplist.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            groupListLayout.setVisibility(View.VISIBLE);
                            d.dismiss();
                        }
                    });
                }
            });

            d.show();
        }
    }

    public void addGroupFavorite(String group_name){
        String userID = mContactListID;
        String favID = searchContactListID;
        String deviceID = session.getDeviceID();
        String deviceUUID = session.getUUID();

        JSONObject sendJSON = new JSONObject();
        try{
            sendJSON.put("contactListID", userID);
            sendJSON.put("favorite_contactListID", favID);
            sendJSON.put("groupName", group_name);
            sendJSON.put("deviceIdentifier", deviceID);
            sendJSON.put("loginUUID", deviceUUID);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON Object");
        }

        AsyncHttpClient client = new AsyncHttpClient();
        StringEntity se = null;

        try {
            se = new StringEntity(sendJSON.toString());
        } catch (UnsupportedEncodingException e){
            Log.e(TAG, "Error setting string entity.");
        }

        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        client.post(null, favorites_url, se, "application/json", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                if(!ProfileActivity.this.isFinishing()) {
                    if (ProfileActivity.this.isInternetConnected == false)
                        var.showAlert(ProfileActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                    else
                        var.showAlert(ProfileActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));
                }
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                try {
                    JSONObject response = new JSONObject(s);
                    String gList = response.getString("groupList");
                    String message = response.getString("message");

                    ProfileActivity.this.populateGroupList(gList);
                    var.showAlert(ProfileActivity.this, "", message);

                } catch (JSONException e) {
                    Log.e(TAG, "Problem adding user.");
                    var.showAlert(ProfileActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.profile_favorite_error));
                }
            }
        });
    }

    public void populateGroupList(String groupList){
        groupListInfo = new ArrayList<String>(Arrays.asList(groupList.split("\\|~\\|")));

        Log.e(TAG, "Array" + groupListInfo.toString());

        GroupListAdapter groupListAdapter = new GroupListAdapter(ProfileActivity.this, R.layout.favorite_grouplist_listview, groupListInfo);
        groupListView.setAdapter(groupListAdapter);
    }

    public void closeGroupList(View v){
        groupListLayout.setVisibility(View.GONE);
        group_counter = -1;
    }

    public void selectGroupList(View v){
        if(selectedGroupNameFromList != null){
            ProfileActivity.this.addGroupFavorite(selectedGroupNameFromList.trim());
            selectedGroupNameFromList = null;
            groupListLayout.setVisibility(View.GONE);
            group_counter = -1;
        }
        else {
            var.showAlert(this, "", "Please select a group from the list.");
        }
    }
}
