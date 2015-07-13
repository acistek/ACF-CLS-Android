package com.acistek.cls;

import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

public class ContactUsActivity extends ActionBarActivity implements ConnectionStateListener{

    private String mContactListID;

    private AppVar var = new AppVar();
    private String user_email_url = var.cls_link + "/?switchID=help_dsp";

    private TextView actionbarText;

    private WebView contactusWebView;
    private ProgressBar progressBar;

    private boolean isInternetConnected;
    private boolean isPageLoaded = false;

    private IntentFilter connFilter;
    private ConnectionStateReceiver csr;

    SessionManager session;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

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
        contactusWebView = (WebView) findViewById(R.id.webviewcontactus);
        progressBar = (ProgressBar) findViewById(R.id.progresscontactus);

        connFilter = new IntentFilter();
        connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        csr = new ConnectionStateReceiver();
        registerReceiver(csr, connFilter);

        mContactListID = session.getContactlistid();

        actionbarText.setText("Contact Us");

        user_email_url = user_email_url + "&contactlistid=" + mContactListID + "&android=1";
        setUpWebView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contactus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_send_contact_us_email) {
            sendEmail();
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
                contactusWebView.loadUrl(this.user_email_url);
            }

            this.isInternetConnected = true;
        }
        else{
            this.isInternetConnected = false;
        }
    }

    public void setUpWebView() {
        contactusWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                String errorHTML = "<html><body><center>An error occurred: The Internet connection appears to be offline.</center></body></html>";
                contactusWebView.loadData(errorHTML, "text/html", "UTF-8");
                var.showAlert(ContactUsActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                isPageLoaded = false;
            }
        });

        WebSettings webSettings = contactusWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);


        contactusWebView.addJavascriptInterface(new JSInterface(this, resources, contactusWebView), "JSInterface");
        contactusWebView.loadUrl(this.user_email_url);
    }

    public void sendEmail(){
        contactusWebView.loadUrl("javascript:sendMail(1)");
    }
}
