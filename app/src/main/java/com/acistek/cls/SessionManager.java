package com.acistek.cls;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by acistek on 4/14/2015.
 */
public class SessionManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    protected static final String pref_name = "CLSPrefs";
    private static final String IS_LOGIN = "IsLoggedIn";

    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String username, String clsid, String coopid, String deviceType, String loginUsername, String deviceID){
        Date today = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_YEAR, 57);
        Date today60 = cal.getTime();

        editor.putBoolean(IS_LOGIN, true);
        editor.putString("username", username);
        editor.putString("contactlistid", clsid);
        editor.putString("coopid", coopid);
        editor.putString("expire_date", today60.toString());
        editor.putString("devicetype", deviceType);
        editor.putString("loginusername", loginUsername);
        editor.putString("deviceidentifier", deviceID);

        editor.commit();
    }

    public void setRegistrationID(String regID, int appVersion){
        editor.putString("regid", regID);
        editor.putInt("appVersion", appVersion);
        editor.commit();
    }

    public void setUUID(String uuid){
        editor.putString("uuid", uuid);
        editor.commit();
    }

    public String getUsername(){
        return pref.getString("username", null);
    }

    public String getLoginUsername(){
        return pref.getString("loginusername", null);
    }

    public String getDeviceType(){
        return pref.getString("devicetype", null);
    }

    public String getDeviceID(){
        return pref.getString("deviceidentifier", null);
    }

    public String getUUID(){
        return pref.getString("uuid", null);
    }

    public String getRegistrationID(){
        return pref.getString("regid", "");
    }

    public String getContactlistid(){
        return pref.getString("contactlistid", null);
    }

    public String getCoopid(){
        return pref.getString("coopid", null);
    }

    public void logoutUser(){
        GcmClient gcmClient = new GcmClient(_context, null);
        gcmClient.unregisterInBackground(getLoginUsername(), getDeviceType(), getDeviceID(), "0");

        editor.clear();
        editor.commit();

        Intent i = new Intent(_context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }

    public void clearSession(){
        editor.clear();
        editor.commit();
    }

    public void checkLogin(){
        if(!this.isLoggedIn() || this.isExpired()){
            Intent i = new Intent(_context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public boolean isExpired(){
        String user_date = pref.getString("expire_date", null);
        DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Date today = new Date();

        try {
            Date expired_date = format.parse(user_date);
            return today.after(expired_date);
        } catch (ParseException e) {
            Log.e("SessionManager", "Failed to parse date.");
        }

        return true;
    }
}
