package com.acistek.cls;

import android.app.Activity;
import android.app.Application;

/**
 * Created by acistek on 4/15/2015.
 */
public class MainApplication extends Application {
    static public boolean uiInForeground = false;
    static public boolean inSearch = false;
    static public boolean inNotifications = false;
    static public Activity currentActivity = null;
    static public int notification_count = 0;
}
