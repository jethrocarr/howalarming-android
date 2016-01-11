package com.jethrocarr.howalarming;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
/*
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.samples.apps.friendlyping.constants.PingerKeys;
import com.google.samples.apps.friendlyping.R;
import com.google.samples.apps.friendlyping.constants.GlobalConstants;
import com.google.samples.apps.friendlyping.util.FriendlyPingUtil;
*/

import java.io.IOException;

/**
 * Deal with registration of the user with the GCM instance.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        try {
            // Just in case that onHandleIntent has been triggered several times in short
            // succession we used synchronized, which is essentially locking across all
            // threads of the application. :-)
            synchronized (TAG) {
                // Fetch the token - initially via the network, subsequently it's available locally.
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                Log.d(TAG, "GCM registration token: " + token);

                // Register to the server and subscribe to the topic of interest.
                sendRegistrationToServer(token);

                // The list of topics we can subscribe to is being implemented within the server.
                GcmPubSub.getInstance(this).subscribe(token, "/topics/newclient", null);


                // We store a boolean to indicate whether or not we've sent the token
                final SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(GlobalConstants.SENT_TOKEN_TO_SERVER, true);
                editor.putString(GlobalConstants.TOKEN, token);
                editor.apply();
            }
        } catch (IOException e) {
            Log.d(TAG, "Failed to complete token refresh", e);

            // By marking this as false, we know we should re-try sending it at a future point.
            sharedPreferences.edit().putBoolean(GlobalConstants.
                    SENT_TOKEN_TO_SERVER, false).apply();

        }

        // Tell Android we're done.
        Intent registrationComplete = new Intent(GlobalConstants.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Sends the registration to the server.
     *
     * @param token The token to send.
     * @throws IOException Thrown when a connection issue occurs.
     */
    private void sendRegistrationToServer(String token) throws IOException {
        // TODO: implement a way of getting the registration token to us.

        /*
        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
        googleApiClient.blockingConnect();

        Bundle registration = createRegistrationBundle(googleApiClient);
        registration.putString(PingerKeys.REGISTRATION_TOKEN, token);

        // Register the user at the server.
        GoogleCloudMessaging.getInstance(this).send(FriendlyPingUtil.getServerUrl(this),
                String.valueOf(System.currentTimeMillis()), registration);

        */
    }

}