package com.jj.ifriendidentification;

import android.app.Application;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

public class BaseApplication extends Application {
	private static Context context;
    public static Context getContext() {
        return context;
    }
    
    private static LocalBroadcastManager localBroadcastManager;
    public static LocalBroadcastManager getBroadcastManager() {
		if(localBroadcastManager==null) {
			localBroadcastManager = LocalBroadcastManager.getInstance(getContext()); 
		}
		return localBroadcastManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
    }
}
