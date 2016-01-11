package com.jethrocarr.howalarming;

import android.app.Application;

import com.orm.SugarContext;

/**
 * Need this to launch/terminate the sqllite SugarORM properly.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(this);
    }

    @Override
    public void onTerminate() {
        SugarContext.terminate();
    }

}