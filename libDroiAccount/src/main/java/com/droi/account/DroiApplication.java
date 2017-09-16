package com.droi.account;

import com.droi.account.manager.ShareInfoManager;

import android.app.Application;
import android.os.StrictMode;

public class DroiApplication extends Application {

    public static final boolean DEBUG_STRICT_MODE = false;
    
	private ShareInfoManager mManager;
	
	@Override
	public void onCreate() {
        if (DEBUG_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
		// TODO Auto-generated method stub
		super.onCreate();
		mManager = ShareInfoManager.getInstance(this);
	}
	
}
