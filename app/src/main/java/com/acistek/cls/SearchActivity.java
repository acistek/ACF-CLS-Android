package com.acistek.cls;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class SearchActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, ConnectionStateListener, VariableChangedListener {

    private WebView webView;
    private ProgressBar progress;
    private EditText editText;
    private TextView welcome;
    private TextView searchRecordCnt;
    private ListView searchResultList;
    private boolean isInternetConnected;
    private IntentFilter connFilter;
    private ConnectionStateReceiver csr;
    private TextView actionbarText;
    private ImageView clearSearchImage;
    private ImageView searchAlertImage;
    private BadgeView badge;

    private AppVar var = new AppVar();
    private static final String TAG = "SearchActivity";
    private final String graph_url = var.cls_link + "/?switchID=staffGraph_dsp&android=1";
    private final String user_search_url = var.cls_link + "/json/search_dsp.cfm";
    private ArrayList<UserSearch> userSearchArrayList = new ArrayList<UserSearch>();
    private SearchListAdapter searchListAdapter;
    private int searchResultCnt = 0;
    private int start = 1;
    private int end = 25;
    private int searchLimit = 200;
    private boolean isLoadingMore = false;
    private boolean isPageLoaded = true;
    private boolean isAlertDialogOpen = false;

    private Drawable editTextDrawable;
    private Drawable clearEditTextDrawable;
    private Menu menu;

    SessionManager session;
    Resources resources;

    static int counter = -1;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
//    private CharSequence mTitle;
//    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.abs_layout);

        resources = getResources();
        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        Crashlytics.getInstance().setUserName(session.getUsername());

        welcome = (TextView)findViewById(R.id.welcome_name);
        webView = (WebView) findViewById(R.id.webviewgraph);
        progress = (ProgressBar) findViewById(R.id.progressgraph);
        editText = (EditText) findViewById(R.id.search_name);
        actionbarText = (TextView) findViewById(R.id.search_title);
        searchRecordCnt = (TextView) findViewById(R.id.search_record_number);
        searchResultList = (ListView) findViewById(R.id.search_listview);
        clearSearchImage = (ImageView) findViewById(R.id.clear_search);
        searchAlertImage = (ImageView) findViewById(R.id.search_alert_image);
        badge = new BadgeView(this, searchAlertImage);

        connFilter = new IntentFilter();
        connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        csr = new ConnectionStateReceiver();
        registerReceiver(csr, connFilter);

        welcome.setText("Welcome " + session.getUsername());

        setUpWebView();

        clearEditTextDrawable = resources.getDrawable(R.drawable.clear);
        clearEditTextDrawable.setBounds(0, 0, 30, 30);

        editTextDrawable = resources.getDrawable(R.drawable.search_icon);
        editTextDrawable.setBounds(0, 0, 60, 60);
        editText.setCompoundDrawables(editTextDrawable, null, null, null);

        var.outOfFocus(editText, this);

        setUpListeners();

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
//        mTitle = getTitle();
//        mPosition = 2;

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onBackPressed(){
        editText.clearFocus();

        if(mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.close();
        else if(webView.getVisibility() == View.GONE){
            editText.setText("");
            searchRecordCnt.setText("");

            if(searchListAdapter != null)
                searchListAdapter.clear();

            searchResultList.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(this.graph_url);
        }
        else
            moveTaskToBack(true);

    }

    @Override
    public void onResume(){
        super.onResume();
        if(MainApplication.notification_count > 0)
            showNotificationCount(MainApplication.notification_count);
        else
            hideNotificationCount();
    }

    @Override
    public void onStart(){
        super.onStart();
        MainApplication.uiInForeground = true;
        MainApplication.inSearch = true;
        MainApplication.currentActivity = this;
    }

    @Override
    public void onPause(){
        super.onPause();
        MainApplication.uiInForeground = false;
        MainApplication.inSearch = false;
        MainApplication.currentActivity = null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MainApplication.inSearch = false;
//        MainApplication.currentActivity = null;
        if(csr != null)
            unregisterReceiver(csr);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 2:
//                mTitle = getString(R.string.title_section1);
//                mPosition = number;
                webView.loadUrl(this.graph_url);
                break;
            case 3:
//                mTitle = getString(R.string.title_section2);
//                mPosition = number;
                Intent i = new Intent(SearchActivity.this, BuildingInfoActivity.class);
                this.startActivity(i);

                break;
            case 4:
//                mTitle = getString(R.string.title_section3);
//                mPosition = number;
                Intent ii = new Intent(SearchActivity.this, AcfWebActivity.class);
                this.startActivity(ii);
                break;
            case 6:
//                mTitle = getString(R.string.title_section3);
//                mPosition = number;
                Intent iii = new Intent(SearchActivity.this, ContactUsActivity.class);
                this.startActivity(iii);
                break;
            default:
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionbarText.setText(getString(R.string.title_section1));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.search, menu);

            this.menu = menu;
            restoreActionBar();

            if(session.getCoopid().equalsIgnoreCase("0")){
                menu.findItem(R.id.action_coop).setVisible(false);
                menu.findItem(R.id.search_blank_ac).setVisible(true);
            }
            else {
                menu.findItem(R.id.action_coop).setVisible(true);
                menu.findItem(R.id.search_blank_ac).setVisible(false);
            }

            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_coop) {
            Intent i = new Intent(SearchActivity.this, CoopActivity.class);
            this.startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void isConnected(boolean isConnected) {
        if(isConnected){
            if(this.isInternetConnected == false){
                if(isPageLoaded == false){
                    isPageLoaded = true;
                    this.webView.loadUrl(this.graph_url);
                }
            }

            this.isInternetConnected = true;
        }
        else{
            this.isInternetConnected = false;
        }
    }

    @Override
    public void newNotifications() {
        if(MainApplication.notification_count > 0)
            showNotificationCount(MainApplication.notification_count);
        else
            hideNotificationCount();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_search, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((SearchActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public void processSearchTerm(int from, int to, String term){
        isLoadingMore = true;
        counter = -1;
        final int fromRow = from;

        final int toRow = to;
        String acfcode = var.acfcode;

        try {
            term = URLEncoder.encode(term, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            term = "";
            Log.e(TAG, "Failed to encode search term");
        }

        String search_url = user_search_url + "?term=" + term + "&acfcode=" + acfcode + "&from=" + fromRow + "&to=" + toRow + "&deviceIdentifier=" + session.getDeviceID() + "&loginUUID=" + session.getUUID();
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(search_url, new TextHttpResponseHandler(){

            @Override
            public void onStart() {
                webView.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                searchResultList.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if(isAlertDialogOpen == false){
                    isAlertDialogOpen = true;
                    var.dismissKeyboard(SearchActivity.this);

                    showSearchAlert(SearchActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                }

                isLoadingMore = false;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if(webView.getVisibility() == View.VISIBLE)
                    editText.setText("");
                else {

                    try {
                        JSONObject response = new JSONObject(responseString);
                        JSONArray list = response.getJSONArray("results");
                        if (fromRow == 1) {
                            searchResultCnt = response.getInt("resultCount");
                            userSearchArrayList = new ArrayList<UserSearch>();
                            if (searchResultCnt == 0)
                                searchRecordCnt.setText("No record");
//                            else if (searchResultCnt == 1)
//                                searchRecordCnt.setText(searchResultCnt + " record");
                            else
                                searchRecordCnt.setText(searchResultCnt + " record(s)");
                        }

                        for (int i = 0; i < list.length(); i++) {
                            JSONObject jsonItem = list.getJSONObject(i);

                            UserSearch user = new UserSearch();
                            user.setFirstname(jsonItem.getString("firstName"));
                            user.setLastname(jsonItem.getString("lastName"));
                            user.setContactlistid(jsonItem.getString("contactListID"));
                            user.setEmail(jsonItem.getString("emailAddress"));

                            userSearchArrayList.add(user);
                        }

                        if (fromRow == 1) {
                            searchListAdapter = new SearchListAdapter(SearchActivity.this, userSearchArrayList);
                            searchResultList.setAdapter(searchListAdapter);
                        } else {
                            searchListAdapter.notifyDataSetChanged();
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "Error converting search term response to JSON");
                    }
                }

                isLoadingMore = false;
            }
        });
    }

    public void setUpWebView() {
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

                if(SearchActivity.this.isInternetConnected == false)
                    showSearchAlert(SearchActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_internet));
                else
                    showSearchAlert(SearchActivity.this, resources.getString(R.string.alert_error), resources.getString(R.string.alert_no_service));

                String errorHTML = "<html><body><h1><center>An error occurred: The Internet connection appears to be offline.</center></h1></body></html>";
                webView.loadData(errorHTML, "text/html", "UTF-8");
                isPageLoaded = false;
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            webSettings.setDisplayZoomControls(false);
        }

        webView.addJavascriptInterface(new JSInterface(this, resources, webView), "JSInterface");
        webView.loadUrl(this.graph_url);
    }

    public void setUpListeners(){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(editText.getText().toString().equalsIgnoreCase(""))
                    clearSearchImage.setImageDrawable(null);
                else
                    clearSearchImage.setImageDrawable(clearEditTextDrawable);

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(editText.getText().toString().trim().equalsIgnoreCase("")){
                    searchRecordCnt.setText("");
                    userSearchArrayList.clear();

                    if(searchListAdapter != null)
                        searchListAdapter.clear();

                    searchResultList.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                }
                else{
                    processSearchTerm(SearchActivity.this.start, SearchActivity.this.end, editText.getText().toString());
                }
            }
        });

        searchResultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchActivity.this.counter = position;
                searchResultList.setItemChecked(position, true);
                TextView userID = (TextView) view.findViewById(R.id.search_list_id);

                Intent i = new Intent(SearchActivity.this, ProfileActivity.class);
                i.putExtra("contactlistid", userID.getText());
                SearchActivity.this.startActivity(i);
            }
        });

        searchResultList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                var.dismissKeyboard(SearchActivity.this);
                return false;
            }
        });

        searchResultList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0){
                    if(isLoadingMore == false && firstVisibleItem != 0 && searchResultCnt != totalItemCount && totalItemCount < searchLimit){
                        Log.e(TAG, "firstvisibleitem: " + firstVisibleItem + " visibleItemCount: " + visibleItemCount + " totalItemCount: " + totalItemCount);
                        processSearchTerm(totalItemCount + 1, totalItemCount + 25, editText.getText().toString());
                    }
                }
            }
        });
    }

    public void clearSearch(View view){
        editText.setText("");
    }

    public void showSearchAlert(Context context, String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editText.setText("");
                isAlertDialogOpen = false;
            }
        });
        AlertDialog dialog = builder.show();

        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);

        TextView titleView = (TextView) dialog.findViewById(context.getResources().getIdentifier("alertTitle", "id", "android"));
        if(titleView != null)
            titleView.setGravity(Gravity.CENTER);
    }

    public void goProfile(View view){
        Intent i = new Intent(SearchActivity.this, ProfileActivity.class);
        i.putExtra("contactlistid", session.getContactlistid());
        SearchActivity.this.startActivity(i);
    }

    public void goFavorites(View view){
        Intent i = new Intent(SearchActivity.this, FavoriteActivity.class);
        this.startActivity(i);
    }

    public void goNotification(View view){
        Intent i = new Intent(SearchActivity.this, NotificationActivity.class);
        this.startActivity(i);
    }

    public void goLogout(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(resources.getString(R.string.alert_sign_out));
        builder.setMessage(resources.getString(R.string.alert_sign_out_message));
        builder.setPositiveButton(R.string.search_signout, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                session.logoutUser();
                SearchActivity.this.finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.show();

        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);

        TextView titleView = (TextView) dialog.findViewById(resources.getIdentifier("alertTitle", "id", "android"));
        if(titleView != null)
            titleView.setGravity(Gravity.CENTER);
    }

    public void showNotificationCount(final Integer count){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                badge.setText(count.toString());
                badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                badge.show();
            }
        });
    }

    public void hideNotificationCount(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                badge.hide();
            }
        });
    }

}
