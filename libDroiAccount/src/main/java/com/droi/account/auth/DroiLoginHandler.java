package com.droi.account.auth;

import android.content.Context;
import android.content.Intent;

import com.droi.account.DebugUtils;
import com.droi.account.MyResource;
import com.droi.account.authenticator.Constants;

public class DroiLoginHandler {

	private Context mContext;
	public static final String KEY_AUTHLISTENER = "droiauthlistener";
	
	private MyResource mMyResource;
	
	public DroiLoginHandler(Context context){
		mContext = context;
		mMyResource = new MyResource(context);
	}
	
	public void authorize(DroiAuthListener listener, String url){
		DroiCallbackInstance instance = DroiCallbackInstance.getInstance(mContext);
		instance.setDroiAuthListener(KEY_AUTHLISTENER, listener);
		Intent intent = new Intent();
		intent.setClass(mContext, DroiAuthPage.class);
		intent.putExtra("title", mContext.getString(mMyResource.getString("lib_droi_account_droi_auth_title")));
		intent.putExtra("url", url);
		//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		if(DebugUtils.DEBUG){
			DebugUtils.i("DroiAuth", "startActivity");
		}
		mContext.startActivity(intent);
		
	}
}
