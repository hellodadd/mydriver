package com.droi.account.shared;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.droi.account.DebugUtils;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;

public class DroiAccountReceiver extends BroadcastReceiver {
	private final static String DROI_ACCOUNT_STATE = "droi_account_state";
	private final static String DROI_ACCOUNT_LOGIN_STATE = "droi_account_login_state";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		String action = intent.getAction();
		if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
			/*
			if(Utils.isAppInstalled(context, Utils.DROID_ACCOUNT_PACKAGENAME)){
				disableComponent(context);
			}else{
				
			}*/
		}else if(Intent.ACTION_PACKAGE_ADDED.equals(action)){
			//final String packageName = intent.getData().getSchemeSpecificPart();
			//if(DebugUtils.DEBUG){
				//DebugUtils.i("package", "install "+packageName);
			//}
			//if(Utils.DROID_ACCOUNT_PACKAGENAME.equals(packageName)){
				//disableComponent(context);
			//}
			
		}else if(Intent.ACTION_PACKAGE_REMOVED.equals(action)){
			/*
			final String packageName = intent.getData().getSchemeSpecificPart();
			
			if(DebugUtils.DEBUG){
				DebugUtils.i("package", "unInstall "+packageName);
			}
			if(Utils.DROID_ACCOUNT_PACKAGENAME.equals(packageName)){
				enableComponent(context);
			}*/
		}else if("droi.account.intent.action.ACCOUNT_TOKEN_INVALIDATE".equals(action)){
			
		}else if(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION.equals(action)){
			//if(DebugUtils.DEBUG){
			   DebugUtils.i("account", "LOGIN_ACCOUNTS_CHANGED_ACTION");
		    //}
			String packageName = getAuthenticatorPackageName(context, Constants.ACCOUNT_TYPE);
			String locakPackage = context.getApplicationContext().getPackageName();
			SharedPreferences perf = context.getSharedPreferences(DROI_ACCOUNT_STATE, Context.MODE_PRIVATE);
			boolean accountLogin = perf.getBoolean(DROI_ACCOUNT_LOGIN_STATE, false);
			if(!TextUtils.isEmpty(packageName) && packageName.equals(locakPackage)){
				DroiSDKHelper account = DroiSDKHelper.getInstance(context);
				if(account != null){
					if(account.checkAccount()){
						//if(DebugUtils.DEBUG){
							//DebugUtils.i("package", "ADD ACCOUNT");
						//}
						if(accountLogin == false){
							account.onAccountLogin();
							setLoginState(context, true);
						}
					}else if(accountLogin == true){
						//if(DebugUtils.DEBUG){
							//DebugUtils.i("package", "REMOVE ACCOUNT");
						//}
						account.onAccountDeleted();
						setLoginState(context, false);
					}
				}
			}

			
		}
	}

	
	private void disableComponent(Context context){
		String localPackage = context.getApplicationContext().getPackageName();
		if(!Utils.DROID_ACCOUNT_PACKAGENAME.equals(localPackage)){
			ComponentName componentName = new ComponentName(localPackage, "com.droi.account.authenticator.AuthenticationService");
			PackageManager packageManager = context.getPackageManager();
			packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		}
	}
	
	private void enableComponent(Context context){
		String localPackage = context.getApplicationContext().getPackageName();
		if(!Utils.DROID_ACCOUNT_PACKAGENAME.equals(localPackage)){
			ComponentName componentName = new ComponentName(localPackage, "com.droi.account.authenticator.AuthenticationService");
			PackageManager packageManager = context.getPackageManager();
			packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		}
	}
	
	private String getAuthenticatorPackageName(Context context, String type){
		AuthenticatorDescription[] mAuthDescs = AccountManager.get(context).getAuthenticatorTypes();
        for (int i = 0; i < mAuthDescs.length; i++) {
        	if(type.equals(mAuthDescs[i].type)){
        		return mAuthDescs[i].packageName;
        	}
        }
        return null;
	}
	
	private void setLoginState(Context context, boolean state){
        SharedPreferences.Editor editor =
        		context.getSharedPreferences(DROI_ACCOUNT_STATE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(DROI_ACCOUNT_LOGIN_STATE, state);
        editor.commit();
	}
}
