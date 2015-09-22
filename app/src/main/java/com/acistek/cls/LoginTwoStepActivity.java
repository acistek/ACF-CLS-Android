package com.acistek.cls;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class LoginTwoStepActivity extends Activity implements ConnectionStateListener {

    private String username;
    private String contactlistID;
    private String coopID;
    private String deviceType;
    private String deviceID;
    private String loginUsername;
    private String cellPhone;
    private String hiddenCellPhone;
    private String formattedCellPhone;

    private EditText cellView;
    private ImageView imageView;
    private WebView webView;
    private ProgressBar progress;
    private boolean isInternetConnected;
    private IntentFilter connFilter;
    private ConnectionStateReceiver csr;

    private AppVar var = new AppVar();
    private static final String TAG = "LoginTwoStepActivity";
    private final String pinverify_url = var.cls_link + "/index.cfm?switchid=pinverify_dsp";
    private boolean isPageLoaded = true;

    SessionManager session;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_two_step);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        resources = getResources();
        session = new SessionManager(getApplicationContext());

        if(session.isLoggedIn() && !session.isExpired()){
            Intent i = new Intent(LoginTwoStepActivity.this, SearchActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
        else{
            Bundle b = getIntent().getExtras();
            username = b.getString("username");
            contactlistID = b.getString("contactlistid");
            coopID = b.getString("coopid");
            deviceType = b.getString("devicetype");
            loginUsername = b.getString("loginusername");
            deviceID = b.getString("deviceid");
            cellPhone = b.getString("cellphone");

            if(cellPhone.length() == 10) {
                hiddenCellPhone = "(xxx) xxx-" + cellPhone.substring(cellPhone.length() - 4);
                formattedCellPhone = "(" + cellPhone.substring(0, 3) + ") " + cellPhone.substring(3, 6) + "-" + cellPhone.substring(cellPhone.length() - 4);
            }
            else{
                hiddenCellPhone = "";
                formattedCellPhone = "";
            }

            webView = (WebView) findViewById(R.id.webviewtwofactor);
            progress = (ProgressBar) findViewById(R.id.progresslogintwo);
            cellView = (EditText) findViewById(R.id.logintwo_cellphone);
            imageView = (ImageView) findViewById(R.id.cellphone_edit);

            connFilter = new IntentFilter();
            connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            csr = new ConnectionStateReceiver();
            registerReceiver(csr, connFilter);

            var.outOfFocus(cellView, this);
            cellView.setHint("Enter your number");
            cellView.setText(hiddenCellPhone);
            cellView.addTextChangedListener(new PhoneTextWatcher(cellView));

            if(hiddenCellPhone.equalsIgnoreCase("")){
                var.showAlert(this, "", "Please enter your cell phone number to receive a PIN.");
                cellView.setEnabled(true);
                imageView.setClickable(false);
                imageView.setVisibility(View.INVISIBLE);
            }
            else
                cellView.setEnabled(false);

            webView.setWebViewClient(new WebViewClient() {
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
            webView.loadUrl(this.pinverify_url);

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
    public void isConnected(boolean isConnected) {
        if(isConnected){
            if(this.isInternetConnected == false && isPageLoaded == false){
                isPageLoaded = true;
                this.webView.loadUrl(this.pinverify_url);
            }

            this.isInternetConnected = true;
        }
        else{
            this.isInternetConnected = false;
        }
    }

    public void loginTwoEditCell(View view) {
        final String groupCharacterSet = "\'1234567890() -";

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

        LinearLayout glayout = new LinearLayout(LoginTwoStepActivity.this);
        glayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 20, 20, 20);

        final EditText cellnum = new EditText(LoginTwoStepActivity.this);

        cellnum.setBackgroundDrawable(resources.getDrawable(R.drawable.login_textfield));
        cellnum.setFilters(new InputFilter[]{groupFilter, new InputFilter.LengthFilter(14)});
        cellnum.setInputType(InputType.TYPE_CLASS_NUMBER);
        cellnum.setHint("Enter number");

        cellnum.addTextChangedListener(new PhoneTextWatcher(cellnum));
        glayout.addView(cellnum, params);



        final AlertDialog d = new AlertDialog.Builder(LoginTwoStepActivity.this)
                .setTitle("Cell Phone Verification")
                .setMessage("Please enter the cell phone number associated with your account.")
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
                        if (cellnum.getText().toString().trim().equalsIgnoreCase("")) {
                            var.showAlert(LoginTwoStepActivity.this, "", "Please enter your 10-digit cell phone number.");
                            cellnum.setText("");
                        } else {
                            String verifyCell = cellnum.getText().toString().replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                            String verifyPhone = "1";
                            String deviceUUID = session.getUUID();
                            String edit_url = var.cls_link + "/json/login_act.cfm?android=1";

                            JSONObject sendJSON = new JSONObject();
                            try{
                                sendJSON.put("cellPhone", verifyCell);
                                sendJSON.put("verifyPhone", verifyPhone);
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

                            client.post(null, edit_url, se, "application/json", new TextHttpResponseHandler() {
                                @Override
                                public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                                    if (!LoginTwoStepActivity.this.isFinishing()) {
                                        if (LoginTwoStepActivity.this.isInternetConnected == false)
                                            var.showAlert(LoginTwoStepActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                                        else
                                            var.showAlert(LoginTwoStepActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));
                                    }
                                }

                                @Override
                                public void onSuccess(int i, Header[] headers, String s) {
                                    try {
                                        JSONObject response = new JSONObject(s);
                                        Log.e(TAG, response.toString());

                                        int success = response.getInt("success");
                                        String message = response.getString("error_message");

                                        if (success == 0) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginTwoStepActivity.this);
                                            builder.setTitle("Verification Failed");
                                            builder.setMessage(message);
                                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    cellnum.setText("");
                                                }
                                            });
                                            AlertDialog dialog = builder.show();

                                            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
                                            messageView.setGravity(Gravity.CENTER);

                                            TextView titleView = (TextView) dialog.findViewById(LoginTwoStepActivity.this.getResources().getIdentifier("alertTitle", "id", "android"));
                                            if(titleView != null)
                                                titleView.setGravity(Gravity.CENTER);

                                        } else {
                                            d.dismiss();
                                            LoginTwoStepActivity.this.cellView.setEnabled(true);
                                            LoginTwoStepActivity.this.imageView.setClickable(false);
                                            LoginTwoStepActivity.this.imageView.setVisibility(View.INVISIBLE);
                                            cellView.setText(formattedCellPhone);

                                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginTwoStepActivity.this);
                                            builder.setTitle("Verification Successful");
                                            builder.setMessage(message);
                                            builder.setPositiveButton(android.R.string.ok, null);
                                            AlertDialog dialog = builder.show();

                                            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
                                            messageView.setGravity(Gravity.CENTER);

                                            TextView titleView = (TextView) dialog.findViewById(LoginTwoStepActivity.this.getResources().getIdentifier("alertTitle", "id", "android"));
                                            if(titleView != null)
                                                titleView.setGravity(Gravity.CENTER);
                                        }

                                    } catch (JSONException e) {
                                        Log.e(TAG, "Problem Verifying Cell Phone.");
                                        var.showAlert(LoginTwoStepActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.profile_favorite_error));
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        d.show();

        TextView messageView = (TextView) d.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);
        messageView.setTypeface(null, Typeface.BOLD);

        TextView titleView = (TextView) d.findViewById(LoginTwoStepActivity.this.getResources().getIdentifier("alertTitle", "id", "android"));
        if(titleView != null)
            titleView.setGravity(Gravity.CENTER);

    }

    public void loginTwoCancel(View view) {
        session.logoutUser();
    }

    public void loginTwoSendPin(View view) {
        String cellnum = "";

        if(cellView.isEnabled())
            cellnum = cellView.getText().toString().replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
        else
            cellnum = cellPhone;

        if(cellnum.trim().equalsIgnoreCase("") || cellnum.length() != 10){
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginTwoStepActivity.this);
            builder.setMessage("Please enter your cell phone number to receive a PIN.");
            builder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.show();

            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
            messageView.setGravity(Gravity.CENTER);
        }
        else{
            String sendPin = "1";
            String deviceUUID = session.getUUID();
            String edit_url = var.cls_link + "/json/login_act.cfm?android=1";

            JSONObject sendJSON = new JSONObject();
            try{
                sendJSON.put("cellPhone", cellnum);
                sendJSON.put("sendPin", sendPin);
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

            client.post(null, edit_url, se, "application/json", new TextHttpResponseHandler() {
                @Override
                public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                    if (!LoginTwoStepActivity.this.isFinishing()) {
                        if (LoginTwoStepActivity.this.isInternetConnected == false)
                            var.showAlert(LoginTwoStepActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                        else
                            var.showAlert(LoginTwoStepActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));
                    }
                }

                @Override
                public void onSuccess(int i, Header[] headers, String s) {
                    try {
                        JSONObject response = new JSONObject(s);
                        Log.e(TAG, response.toString());

                        int success = response.getInt("success");
                        String message = response.getString("error_message");

                        if (success == 0) {
                            String title = response.getString("error_title");
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginTwoStepActivity.this);
                            builder.setTitle(title);
                            builder.setMessage(message);
                            builder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.show();

                            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
                            messageView.setGravity(Gravity.CENTER);

                            TextView titleView = (TextView) dialog.findViewById(LoginTwoStepActivity.this.getResources().getIdentifier("alertTitle", "id", "android"));
                            if(titleView != null)
                                titleView.setGravity(Gravity.CENTER);

                        } else {
                            LoginTwoStepActivity.this.pinCodeCheck(message);
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "Problem Verifying Cell Phone.");
                        var.showAlert(LoginTwoStepActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.profile_favorite_error));
                    }
                }
            });
        }
    }

    public void pinCodeCheck(String message){
        final String groupCharacterSet = "\'1234567890";

        final InputFilter groupFilter = new InputFilter() {
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
                return Character.isDigit(c) || groupCharacterSet.contains("" + c);
            }
        };

        LinearLayout glayout = new LinearLayout(LoginTwoStepActivity.this);
        glayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 20, 20, 20);

        final EditText pinNum = new EditText(LoginTwoStepActivity.this);
        pinNum.setBackgroundDrawable(resources.getDrawable(R.drawable.login_textfield));
        pinNum.setFilters(new InputFilter[]{groupFilter, new InputFilter.LengthFilter(6)});
        pinNum.setInputType(InputType.TYPE_CLASS_NUMBER);
        pinNum.setHint("Enter PIN");

        glayout.addView(pinNum, params);

        final AlertDialog d = new AlertDialog.Builder(LoginTwoStepActivity.this)
                .setTitle("Verify PIN")
                .setMessage(message)
                .setView(glayout)
                .setPositiveButton("Verify", null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    if (pinNum.getText().toString().trim().equalsIgnoreCase("")) {
                        var.showAlert(LoginTwoStepActivity.this, "Verify PIN", "Enter your 6-digit PIN.");
                        pinNum.setText("");
                    } else {

                        String pinCode = pinNum.getText().toString();
                        String deviceID = LoginTwoStepActivity.this.deviceID;
                        String deviceUUID = session.getUUID();
                        String edit_url = var.cls_link + "/json/login_act.cfm?android=1";

                        JSONObject sendJSON = new JSONObject();
                        try{
                            sendJSON.put("pinCode", pinCode);
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

                        client.post(null, edit_url, se, "application/json", new TextHttpResponseHandler() {
                            @Override
                            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                                if (!LoginTwoStepActivity.this.isFinishing()) {
                                    if (LoginTwoStepActivity.this.isInternetConnected == false)
                                        var.showAlert(LoginTwoStepActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                                    else
                                        var.showAlert(LoginTwoStepActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));
                                }
                            }

                            @Override
                            public void onSuccess(int i, Header[] headers, String s) {
                                try {
                                    JSONObject response = new JSONObject(s);
                                    int success = response.getInt("success");
                                    String message = response.getString("error_message");

                                    if (success == 1) {
                                        session.createLoginSession(LoginTwoStepActivity.this.username, LoginTwoStepActivity.this.contactlistID, LoginTwoStepActivity.this.coopID,
                                                LoginTwoStepActivity.this.deviceType, LoginTwoStepActivity.this.loginUsername, LoginTwoStepActivity.this.deviceID);

                                        Intent intent = new Intent(LoginTwoStepActivity.this, SearchActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        LoginTwoStepActivity.this.startActivity(intent);
                                        LoginTwoStepActivity.this.finish();
                                    } else if (success == 2 || success == 3) {
                                        d.dismiss();
                                        String title = response.getString("error_title");
                                        var.showAlert(LoginTwoStepActivity.this, title, message);
                                    } else {
                                        pinNum.setText("");
                                        String title = response.getString("error_title");
                                        var.showAlert(LoginTwoStepActivity.this, title, message);
                                    }

                                } catch (JSONException e) {
                                    Log.e(TAG, "Problem verifying Pin Code.");
                                    var.showAlert(LoginTwoStepActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.profile_favorite_error));
                                }
                            }
                        });
                    }
                    }
                });
            }
        });

        d.show();

        TextView messageView = (TextView) d.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);
        messageView.setTypeface(null, Typeface.BOLD);

        TextView titleView = (TextView) d.findViewById(LoginTwoStepActivity.this.getResources().getIdentifier("alertTitle", "id", "android"));
        if(titleView != null)
            titleView.setGravity(Gravity.CENTER);
    }

}
