package com.acistek.cls;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by acistek on 5/14/2015.
 */
public class GcmIntentService extends IntentService{

    public static final String TAG = "GcmIntentService";

    public GcmIntentService(){
        super("GcmIntentService");
    }

    private VariableChangedListener variableChangedListener;

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle

            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.e(TAG, "Received: " + extras.getString("msg") + " count: " + extras.getString("count"));
                Log.e(TAG, extras.toString());
                sendNotification(extras.getString("msg"), extras.getString("count"));

            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg, String count) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotificationActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("CLS Notification")
                .setContentText(msg)
                .setContentIntent(contentIntent)
                .getNotification();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(R.string.app_name, notification);

        MainApplication.notification_count = Integer.parseInt(count);
        if(MainApplication.inSearch && MainApplication.currentActivity != null){
            Log.e(TAG, "inSearch");
            this.variableChangedListener = (VariableChangedListener) MainApplication.currentActivity;
            this.variableChangedListener.newNotifications();
        }
        else if(MainApplication.inNotifications && MainApplication.currentActivity != null){
            Log.e(TAG, "inNotif");
            this.variableChangedListener = (VariableChangedListener) MainApplication.currentActivity;
            this.variableChangedListener.newNotifications();
        }
        else{
            Log.e(TAG, MainApplication.inSearch + "<Search-Notifications>" + MainApplication.inNotifications);
        }
    }
}
