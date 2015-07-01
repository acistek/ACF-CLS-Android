package com.acistek.cls;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.hoang8f.android.segmented.SegmentedGroup;


public class LoginActivity extends Activity implements ConnectionStateListener{

    private WebView webView;
    private ProgressBar progress;
    private EditText usernameView, passwordView;
    private RadioButton hhsButton;
    private SegmentedGroup segmented;
    private boolean isInternetConnected;
    private IntentFilter connFilter;
    private ConnectionStateReceiver csr;

    private AppVar var = new AppVar();
    private GcmClient gcmClient;
    private static final String TAG = "LoginActivity";
    private final String terms_url = var.cls_link + "/index.cfm?switchid=term_dsp";
    private boolean isPageLoaded = true;

    SessionManager session;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        resources = getResources();
        gcmClient = new GcmClient(getApplicationContext(), this);
        session = new SessionManager(getApplicationContext());

        if(session.isLoggedIn() && !session.isExpired()){
            Intent i = new Intent(LoginActivity.this, SearchActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
        else{

            segmented = (SegmentedGroup) findViewById(R.id.segmentedlogin);
            hhsButton = (RadioButton) findViewById(R.id.hhsbutton);
            usernameView = (EditText) findViewById(R.id.login_username);
            passwordView = (EditText) findViewById(R.id.login_password);
            webView = (WebView) findViewById(R.id.webviewterms);
            progress = (ProgressBar) findViewById(R.id.progresslogin);

            connFilter = new IntentFilter();
            connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            csr = new ConnectionStateReceiver();
            registerReceiver(csr, connFilter);

            segmented.setTintColor(Color.parseColor("#336A90"), Color.parseColor("#FFFFFF"));
            passwordView.setFilters(new InputFilter[]{var.filter, new InputFilter.LengthFilter(30)});

            usernameView.addTextChangedListener(new TextWatcher() {
                boolean shouldShowAlert = true;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.length() == 30){
                        if(shouldShowAlert){
                            shouldShowAlert = false;

                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setMessage("Please enter less than 30 characters.");
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
                public void afterTextChanged(Editable s) {

                }
            });

            passwordView.addTextChangedListener(new TextWatcher() {
                boolean shouldShowAlert = true;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.length() == 30){
                        if(shouldShowAlert){
                            shouldShowAlert = false;

                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setMessage("Please enter less than 30 characters.");
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
                public void afterTextChanged(Editable s) {

                }
            });

            var.outOfFocus(usernameView, this);
            var.outOfFocus(passwordView, this);

            webView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    progress.setVisibility(View.VISIBLE);
                }
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    progress.setVisibility(View.GONE);
                }
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    String errorHTML = "<html><body><center>An error occurred: The Internet connection appears to be offline.</center></body></html>";
                    webView.loadData(errorHTML, "text/html", "UTF-8");
                    isPageLoaded = false;
                }
            });

            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(this.terms_url);
        }

    }

    @Override
    public void onStart(){
        super.onStart();
        MainApplication.uiInForeground = true;
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
    public void onBackPressed(){
        moveTaskToBack(true);
    }

    @Override
    public void isConnected(boolean isConnected) {
        if(isConnected){
            if(this.isInternetConnected == false && isPageLoaded == false){
                isPageLoaded = true;
                this.webView.loadUrl(this.terms_url);
            }

            this.isInternetConnected = true;
        }
        else{
            this.isInternetConnected = false;
        }
    }

    public void login(View view) {
        final String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();
        String domainSelect = (hhsButton.isChecked() ? "ITSC":"external");
        String acfcode = var.acfcode;
//        String deviceToken = session.getRegistrationID();
        final String deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        final String deviceType = getDeviceName();
        final String URL = var.cls_link + "/json/login_act.cfm?android=1";

        if(username.trim().equalsIgnoreCase("") || password.trim().equalsIgnoreCase("")){
            var.showAlert(this, resources.getString(R.string.alert_sign_in), resources.getString(R.string.alert_sign_in_no_user));
        }
        else {
            JSONObject sendJSON = new JSONObject();
            try{
                sendJSON.put("domainSelect", domainSelect);
                sendJSON.put("username", username);
                sendJSON.put("password", password);
                sendJSON.put("acfcode", acfcode);
//                sendJSON.put("deviceToken", deviceToken);
//                sendJSON.put("deviceType", deviceType);
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
            client.post(null, URL, se, "application/json", new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    if(LoginActivity.this.isInternetConnected == false)
                        var.showAlert(LoginActivity.this, resources.getString(R.string.alert_sign_in), resources.getString(R.string.alert_no_internet));
                    else
                        var.showAlert(LoginActivity.this, resources.getString(R.string.alert_sign_in), resources.getString(R.string.alert_no_service));

                    Log.e(TAG, responseString);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Log.e(TAG, responseString);
                    try {
                        JSONObject response = new JSONObject(responseString);
                        if(response.getInt("success") == 1){
                            session.createLoginSession(response.getString("userName"), response.getString("contactListID"), response.getString("coopID"), deviceType, username, deviceID);

                            if (gcmClient.checkPlayServices()) {
                                gcmClient.gcm = GoogleCloudMessaging.getInstance(LoginActivity.this);
                                gcmClient.regid = gcmClient.getRegistrationId(getApplicationContext());

                                if (gcmClient.regid.isEmpty()) {
                                    gcmClient.registerInBackground(username, deviceType, deviceID);
                                }
                            } else {
                                Log.i(TAG, "No valid Google Play Services APK found.");
                            }

                            Intent i = new Intent(LoginActivity.this, SearchActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            LoginActivity.this.startActivity(i);
                            LoginActivity.this.finish();

                        }
                        else{
                            var.showAlert(LoginActivity.this, resources.getString(R.string.alert_sign_in), response.getString("error_message"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error converting response to JSON");
                    }
                }
            });
        }
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

}
