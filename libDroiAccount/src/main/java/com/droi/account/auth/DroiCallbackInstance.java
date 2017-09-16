package com.droi.account.auth;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

public class DroiCallbackInstance {

	private static DroiCallbackInstance sInstance;
	
	private Map<String, DroiAuthListener> mDroiAuthListenerMap;
	
	private DroiCallbackInstance(Context context){
		mDroiAuthListenerMap = new HashMap();
	}
	
	public static synchronized DroiCallbackInstance getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new DroiCallbackInstance(context);
		}

		return sInstance;
	}

	public synchronized DroiAuthListener getDroiAuthListener(String key) {
		if (TextUtils.isEmpty(key)) {
			return null;
		}
		return mDroiAuthListenerMap.get(key);
	}

	public synchronized void setDroiAuthListener(String key,
			DroiAuthListener listener) {
		if (TextUtils.isEmpty(key)) {
			return;
		}
		mDroiAuthListenerMap.put(key, listener);
	}
	
	public synchronized void removeDroiAuthListener(String key) {
		if (TextUtils.isEmpty(key)) {
			return;
		}
		mDroiAuthListenerMap.remove(key);
	}
}
