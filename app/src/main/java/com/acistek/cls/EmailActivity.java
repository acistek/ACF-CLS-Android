package com.acistek.cls;

import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;


public class EmailActivity extends ActionBarActivity implements ConnectionStateListener{

    private String toEmail;
    private String groupName;
    private String mContactListID;

    private AppVar var = new AppVar();
    private static final String TAG = "EmailActivity";
    private String user_email_url = var.cls_link + "/?switchID=email_dsp&android=1";

    private TextView actionbarText;

    private WebView emailWebView;
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
        setContentView(R.layout.activity_email);

        resources = getResources();
        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF336A90));
        actionBar.setCustomView(R.layout.abs_layout);

        actionbarText = (TextView) findViewById(R.id.search_title);
        emailWebView = (WebView) findViewById(R.id.webviewemail);
        progressBar = (ProgressBar) findViewById(R.id.progressemail);

        connFilter = new IntentFilter();
        connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        csr = new ConnectionStateReceiver();
        registerReceiver(csr, connFilter);

        toEmail = getIntent().getExtras().getString("email");
        groupName = getIntent().getExtras().getString("groupName");
        mContactListID = session.getContactlistid();

        actionbarText.setText("Mail");

        if(!toEmail.equalsIgnoreCase(""))
            user_email_url = user_email_url + "&contactlistid=" + mContactListID + "&toemail=" + toEmail;
        else
            user_email_url = user_email_url + "&contactlistid=" + mContactListID + "&groupName=" + groupName;

        setUpWebView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_email, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_send_email) {
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
                emailWebView.loadUrl(this.user_email_url);
            }

            this.isInternetConnected = true;
        }
        else{
            this.isInternetConnected = false;
        }
    }

    public void setUpWebView() {
        emailWebView.setWebViewClient(new WebViewClient(){
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
                emailWebView.loadData(errorHTML, "text/html", "UTF-8");
                var.showAlert(EmailActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                isPageLoaded = false;
            }
        });

        WebSettings webSettings = emailWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);


        emailWebView.addJavascriptInterface(new JSInterface(this, resources, emailWebView), "JSInterface");
        emailWebView.loadUrl(this.user_email_url);
    }

    public void sendEmail(){
        emailWebView.loadUrl("javascript:sendMail(1)");
    }
}
