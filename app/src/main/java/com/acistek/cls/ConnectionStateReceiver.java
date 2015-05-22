package com.acistek.cls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Created by acistek on 4/14/2015.
 */
public class ConnectionStateReceiver extends BroadcastReceiver {
    private ConnectionStateListener connectionStateListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
        this.connectionStateListener = (ConnectionStateListener) context;

        if(MainApplication.uiInForeground){
            if (isConnected && !isAirplaneModeOn(context)){
                if(this.connectionStateListener != null)
                    this.connectionStateListener.isConnected(true);
            }
            else {
                if(this.connectionStateListener != null)
                    this.connectionStateListener.isConnected(false);
            }
        }

    }

    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }
}
