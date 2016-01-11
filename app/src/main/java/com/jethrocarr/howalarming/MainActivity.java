package com.jethrocarr.howalarming;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    final int RQS_GooglePlayServices = 1;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    // testing
    ArrayList<String> myDataset=new ArrayList<String>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Is this a clean launch of the application?
        if (savedInstanceState == null) {
            // Start IntentService to register this application with GCM.
            Intent service = new Intent(this, RegistrationIntentService.class);
            startService(service);
        }

        // Setup the recyclerview
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerAlarmEvents);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        // specify an adapter (see also next example)
        mAdapter = new AlarmEventsAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // We check that Google Play Services work when the user loads the activity, this is a good
        // way of advising them if their phone is missing Google Play Services or other key components.
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (resultCode != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices);
        }

        // Register to be told about new messages by the GCM service.
        LocalBroadcastManager.getInstance(this).registerReceiver(
                newEventReciever, new IntentFilter("newEventMessage"));

        // Update the UI with all current messages
        updateEventMessages();
        updateStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // We don't care about new messages any more
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newEventReciever);
    }


    private BroadcastReceiver newEventReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
             * We don't need any of the data in the message, we just need to trigger a new poll
             * for data.
             */
            updateEventMessages();
            updateStatus();
        }
    };

    protected void updateEventMessages() {
        /*
         * Pulls all alarm event messages from the SQLite DB (via ORM) and updates the UI
         */

        List<ModelEvent> events = ModelEvent.listAll(ModelEvent.class, "id DESC");
        myDataset.clear();

        for (ModelEvent event : events) {
            myDataset.add(event.message);
        }

        mAdapter.notifyDataSetChanged();
    }


    protected void updateStatus() {
        /*
         * Determine the current status. We don't use the most recent message, rather we use the state
         * in shared preferences since this has been passed through some logic first (eg not going
         * from "alarming" to "faulting", since alarming is more serious).
         */

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String currentStatus          = sharedPreferences.getString(GlobalConstants.ALARM_STATUS, GlobalConstants.ALARM_STATUS_UNKNOWN);

        ColorDrawable colorDrawable = new ColorDrawable();

        switch (currentStatus) {
            case GlobalConstants.ALARM_STATUS_ALARMING:
            case GlobalConstants.ALARM_STATUS_FAULT:
                colorDrawable.setColor(0xFFFF4444);
                break;

            case GlobalConstants.ALARM_STATUS_ARMED:
                colorDrawable.setColor(0xFF33B5E5);
                break;

            case GlobalConstants.ALARM_STATUS_DISARMED:
                colorDrawable.setColor(0xFF99CC00);
                break;

            case GlobalConstants.ALARM_STATUS_UNKNOWN:
            default:
                colorDrawable.setColor(0xffFEBB31);
                break;
        }


        /*
          Update the action bar
         */
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(colorDrawable);
        bar.setTitle(currentStatus);


    }

}
