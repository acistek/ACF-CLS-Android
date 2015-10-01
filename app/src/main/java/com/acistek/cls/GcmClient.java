package com.acistek.cls;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by acistek on 5/14/2015.
 */
public class GcmClient {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String SENDER_ID = "678164484650";

    static final String TAG = "GcmClient";

    GoogleCloudMessaging gcm;
    AppVar var = new AppVar();
    Activity activity;
    Context context;
    SessionManager session;
    boolean successSendToBackend;

    String regid;

    public GcmClient(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
        this.session = new SessionManager(context);
        this.successSendToBackend = false;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    protected boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
//                activity.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    protected void storeRegistrationId(Context context, String regId) {
        int appVersion = getAppVersion(context);
        Log.e(TAG, "Saving regId: " + regId + " on app version " + appVersion);

        session.setRegistrationID(regId, appVersion);
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    protected String getRegistrationId(Context context) {
//        final SharedPreferences prefs = getGcmPreferences(context);
//        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
//        if (registrationId.isEmpty()) {
//            Log.i(TAG, "Registration not found.");
//            return "";
//        }
//        // Check if app was updated; if so, it must clear the registration ID
//        // since the existing regID is not guaranteed to work with the new
//        // app version.
//        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
//        int currentVersion = getAppVersion(context);
//        if (registeredVersion != currentVersion) {
//            Log.i(TAG, "App version changed.");
//            return "";
//        }
//        return registrationId;
//        regid = "";
        return session.getRegistrationID();
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    protected void registerInBackground(final String username, final String deviceType, final String deviceID) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
//                    boolean success = sendRegistrationIdToBackend(username, deviceType, deviceID, regid);

                    sendRegistrationIdToBackend(username, deviceType, deviceID, regid);

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
//                    if(success){
                        storeRegistrationId(context, regid);
//                    }
//                    else{
//                        msg = "Error Sending Reg ID to backend";
//                        unregisterInBackground(username, deviceType, deviceID, "0");
//                    }

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.e(TAG, msg);
            }
        }.execute(null, null, null);
    }

    public void unregisterInBackground(final String username, final String deviceType, final String deviceID, final String deviceToken) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    gcm.unregister();
                    regid = "";
                    msg = "Device unregistered, registration ID=" + regid;
                    sendRegistrationIdToBackend(username, deviceType, deviceID, deviceToken);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.e(TAG, msg);
            }
        }.execute(null, null, null);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    protected static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    protected void sendRegistrationIdToBackend(String username, String deviceType, String deviceID, String deviceToken) {
        final String URL = var.cls_link + "/json/login_act.cfm?android=1&android_device=1";
        final String acfcode = var.acfcode;

        Log.e(TAG, var.cls_link);

        JSONObject sendJSON = new JSONObject();
        try{
            sendJSON.put("username", username);
            sendJSON.put("acfcode", acfcode);
            sendJSON.put("devicetoken", deviceToken);
            sendJSON.put("devicetype", deviceType);
            sendJSON.put("deviceidentifier", deviceID);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON Object");
        }

        AsyncHttpClient client = new SyncHttpClient();
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
                GcmClient.this.successSendToBackend = false;
//                Log.e(TAG, responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    Log.e(TAG, responseString);

                    JSONObject response = new JSONObject(responseString);
                    if(response.getInt("success") == 1){
                        GcmClient.this.session.setUUID(response.getString("loginUUID"));
                        Log.e(TAG, response.getString("loginUUID"));
                        GcmClient.this.successSendToBackend = true;
                    }
                    else{
                        GcmClient.this.successSendToBackend = false;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error converting response to JSON");
                    GcmClient.this.successSendToBackend = false;
                }
            }
        });

    }

}
