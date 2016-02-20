package com.jethrocarr.howalarming;


import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.app.TaskStackBuilder;
import android.app.PendingIntent;
import android.app.NotificationManager;
import android.provider.Settings;
import android.widget.RemoteViews;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v4.app.NotificationCompat;
import android.widget.TextView;

import com.jethrocarr.howalarming.R;

import java.util.ArrayList;

/**
 * This service listens for messages from GCM, makes them usable for this application and then
 * sends them to their destination.
 */
public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private static int numMessages;
    private int mId;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (from == null) {
            Log.w(TAG, "Couldn't determine origin of message. Skipping.");
            return;
        }

        //try {
            recordEvents(data);
            notifyEvents(data);
            //} catch (JSONException e) {
            //  Log.e(TAG, "onMessageReceived: Could not digest data", e);
        //}
    }

    private void recordEvents(Bundle data) {
        /*
         * Add to the event database for persistency and further usage
         */

        final String type = data.getString("type");
        NotificationCompat.Builder mBuilder;

        Log.d(TAG, "Event type received: " + type);

        if (type == null) {
            Log.w(TAG, "Unset event type received, unable to action.");
            return;
        }

        ModelEvent event = new ModelEvent(data.getString("type"),
                data.getString("code"),
                data.getString("message"),
                data.getString("raw"));
        event.save();


        /*
         * Flag the current status, eg armed/unarmed, alarmed/recovered, faulting, etc.
         */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final String currentStatus              = sharedPreferences.getString(GlobalConstants.ALARM_STATUS, GlobalConstants.ALARM_STATUS_UNKNOWN);
        final SharedPreferences.Editor editor   = sharedPreferences.edit();

        switch (type) {
            case "armed":
                editor.putString(GlobalConstants.ALARM_STATUS, GlobalConstants.ALARM_STATUS_ARMED);
                break;

            case "disarmed":
                editor.putString(GlobalConstants.ALARM_STATUS, GlobalConstants.ALARM_STATUS_DISARMED);
                break;

            case "alarm":
                editor.putString(GlobalConstants.ALARM_STATUS, GlobalConstants.ALARM_STATUS_ALARMING);
                break;

            case "recovery":
                editor.putString(GlobalConstants.ALARM_STATUS, GlobalConstants.ALARM_STATUS_RECOVERED);
                break;

            case "fault":
                if (currentStatus == GlobalConstants.ALARM_STATUS_ALARMING) {
                    // An alarming state takes precedence over a fault - the fault could be an attacker
                    // currently tearing out the alarm unit! Do nothing, keep the alarming state.
                } else {
                    editor.putString(GlobalConstants.ALARM_STATUS, GlobalConstants.ALARM_STATUS_FAULT);
                }
                break;

        }

        editor.apply();
    }

    /*
        Issue a notification event to the user in response to the events relayed from the
        HowAlarming servers.
     */
    private void notifyEvents(Bundle data) {
        final String type = data.getString("type");
        NotificationCompat.Builder mBuilder;

        Log.d(TAG, "Event type received: " + type);

        if (type == null) {
            Log.w(TAG, "Unset event type received, unable to action.");
            return;
        }

        /*
         * Advise the activity that there is new data and to do a refresh immeditely. This results
         * in nice real-time updates of the view if the user is looking at it at the time. We don't
         * need to send any data since we just re-poll the SQLite DB for all new messages.
         */

        Intent newEventMessage = new Intent("newEventMessage");
        LocalBroadcastManager.getInstance(this).sendBroadcast(newEventMessage);


        /*
         * Issue Notifications
         */
        switch (type) {
            case "armed":
                /* Info message: Alarm now armed */

                mBuilder =
                        new NotificationCompat.Builder(this)
                                .setAutoCancel(true)
                                .setNumber(++numMessages)
                                .setSmallIcon(R.drawable.ic_visibility_black_48dp)
                                .setContentTitle("House Armed")
                                .setContentText(data.getString("message"))
                                .setVibrate(new long[]{500, 1000})
                                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                break;

            case "disarmed":
                /* Info message: Alarm now disarmed */

                mBuilder =
                        new NotificationCompat.Builder(this)
                                .setAutoCancel(true)
                                .setNumber(++numMessages)
                                .setSmallIcon(R.drawable.ic_visibility_off_black_48dp)
                                .setContentTitle("House Unarmed")
                                .setContentText(data.getString("message"))
                                .setVibrate(new long[]{500, 1000})
                                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                break;

            case "alarm":
                /*
                    An alarm is a pretty serious issue, we need to generate a suitably high-priority
                    notification to the user.
                 */

                // Issue notification to the user.
                mBuilder =
                        new NotificationCompat.Builder(this)
                                .setAutoCancel(true)
                                .setNumber(++numMessages)
                                .setSmallIcon(R.drawable.ic_warning_black_48dp)
                                .setContentTitle("ALARM TRIGGERED!")
                                .setContentText(data.getString("message"))
                                .setVibrate(new long[]{500, 9000})
                                .setSound(Settings.System.DEFAULT_ALARM_ALERT_URI) // we figure the default alarm sound is probably the best.
                                .setPriority(NotificationCompat.PRIORITY_MAX);

                break;

            case "recovery":
                /*
                    An alarm that was previously sounding has now recovered.
                 */

                mBuilder =
                        new NotificationCompat.Builder(this)
                                .setAutoCancel(true)
                                .setNumber(++numMessages)
                                .setSmallIcon(R.drawable.ic_warning_black_48dp)
                                .setContentTitle("Alarm Recovered")
                                .setContentText(data.getString("message"))
                                .setVibrate(new long[]{500, 9000})
                                .setSound(Settings.System.DEFAULT_ALARM_ALERT_URI) // we figure the default alarm sound is probably the best.
                                .setPriority(NotificationCompat.PRIORITY_MAX);

                break;


            case "fault":
                /*
                    A fault could be minor or serious, we treat it like an alarm event since it could
                    be malicious, eg cut power, cut phone.
                 */

                mBuilder =
                        new NotificationCompat.Builder(this)
                                .setAutoCancel(true)
                                .setNumber(++numMessages)
                                .setSmallIcon(R.drawable.ic_error_black_48dp)
                                .setContentTitle("Alarm Fault Detected")
                                .setContentText(data.getString("message"))
                                .setVibrate(new long[]{500, 9000})
                                .setSound(Settings.System.DEFAULT_ALARM_ALERT_URI) // we figure the default alarm sound is probably the best.
                                .setPriority(NotificationCompat.PRIORITY_MAX);

                break;

            default:
                Log.w(TAG, "Unsupported/unknown event type of " + type + "received, unsure how to action.");
                return;
        }


        // Activity to direct the user to.
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the started
        // Activity. This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());



    }
        /*
        TODO

        final String action = data.getString("action");
        Log.d(TAG, "Action: " + action);
        if (action == null) {
            Log.w(TAG, "onMessageReceived: Action was null, skipping further processing.");
            return;
        }
        Intent broadcastIntent = new Intent(action);
        switch (action) {
            case GcmAction.SEND_CLIENT_LIST:
                final ArrayList<Pinger> pingers = getPingers(data);
                broadcastIntent.putParcelableArrayListExtra(IntentExtras.PINGERS, pingers);
                break;
            case GcmAction.BROADCAST_NEW_CLIENT:
                Pinger newPinger = getNewPinger(data);
                broadcastIntent.putExtra(IntentExtras.NEW_PINGER, newPinger);
                break;
            case GcmAction.PING_CLIENT:
                Ping newPing = getNewPing(data);
                broadcastIntent.putExtra(IntentExtras.NEW_PING, newPing);
                break;
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
    */
}
