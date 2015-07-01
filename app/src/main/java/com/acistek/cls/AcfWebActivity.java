package com.acistek.cls;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;


public class AcfWebActivity extends ActionBarActivity implements ConnectionStateListener{

    private AppVar var = new AppVar();
    private static final String TAG = "AcfWebActivity";

    private boolean isInternetConnected;
    private boolean isPageLoaded = true;

    private IntentFilter connFilter;
    private ConnectionStateReceiver csr;
    private TextView actionbarText;
    private Menu menu;

    private WebView acfWebView;
    private ProgressBar acfProgress;
    private ImageButton acf_back_button;
    private ImageButton acf_refresh_button;
    private ImageButton acf_forward_button;
    private String acf_url = var.acf_link;

    SessionManager session;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acf_web);

        resources = getResources();
        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF336A90));
        actionBar.setCustomView(R.layout.abs_layout);

        actionbarText = (TextView) findViewById(R.id.search_title);

        connFilter = new IntentFilter();
        connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        csr = new ConnectionStateReceiver();
        registerReceiver(csr, connFilter);

        actionbarText.setText(getString(R.string.title_section3));

        setUpWebSite();
        acfWebView.loadUrl(this.acf_url);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_acf_web, menu);
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
                acfWebView.loadUrl(this.acf_url);
            }

            this.isInternetConnected = true;
        }
        else{
            this.isInternetConnected = false;
        }
    }

    public void setUpWebSite(){
        acfWebView = (WebView) findViewById(R.id.acf_website_view);
        acfProgress = (ProgressBar) findViewById(R.id.progresswebsite);
        acf_back_button = (ImageButton) findViewById(R.id.web_back_button);
        acf_refresh_button = (ImageButton) findViewById(R.id.web_refresh_button);
        acf_forward_button = (ImageButton) findViewById(R.id.web_forward_button);

        acf_back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(acfWebView.canGoBack()){
                    acfWebView.goBack();
                }
            }
        });
        acf_refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                acfWebView.reload();
            }
        });
        acf_forward_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(acfWebView.canGoForward()){
                    acfWebView.goForward();
                }
            }
        });

        acfWebView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                acfProgress.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url){
                acfProgress.setVisibility(View.GONE);
            }
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);

                if(AcfWebActivity.this.isInternetConnected == false){
                    if(!AcfWebActivity.this.isFinishing()) {
                        showSearchAlert(AcfWebActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                    }
                }
                else{
                    if(!AcfWebActivity.this.isFinishing()) {
                        showSearchAlert(AcfWebActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));
                    }
                }

                String errorHTML = "<html><body><h1><center>An error occurred: The Internet connection appears to be offline.</center></h1></body></html>";
                acfWebView.loadData(errorHTML, "text/html", "UTF-8");
                isPageLoaded = false;
            }
        });

        WebSettings settings = acfWebView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        acfWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        acfWebView.setScrollbarFadingEnabled(false);
    }

    public void showSearchAlert(Context context, String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                editText.setText("");
//                isAlertDialogOpen = false;
            }
        });
        AlertDialog dialog = builder.show();

        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);

        TextView titleView = (TextView) dialog.findViewById(context.getResources().getIdentifier("alertTitle", "id", "android"));
        if(titleView != null)
            titleView.setGravity(Gravity.CENTER);
    }
}
