package com.droi.account.setup;

import com.droi.account.MyResource;
import com.droi.account.Utils;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;


public class AccountSettings extends Activity{
	
	private MyResource mMyResource;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 mMyResource = new MyResource(this);
		 String appName = getApplicationName();
		 String sAPPFormat = getString(mMyResource.getString("lib_droi_account_please_addaccount_byApp"));  
		 String sNotifacation = String.format(sAPPFormat, appName);  
		 Utils.showMessage(this, sNotifacation);
		 finish();
	}
	
	private String getApplicationName() {
		String applicationName = " APP ";
		try { 
			PackageManager packageManager = null; 
			ApplicationInfo applicationInfo = null; 
			packageManager = getApplicationContext().getPackageManager(); 
			applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0); 
			applicationName = (String) packageManager.getApplicationLabel(applicationInfo); 
		} catch (Exception e) { 
		} 
		return applicationName; 
	} 
}
