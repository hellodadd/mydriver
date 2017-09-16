package com.droi.account;

import android.util.Log;

public class DebugUtils {

	public static final boolean DEBUG = Utils.DEBUG;
	private static final String TAG = "Freeme OS";
	
	
	public static void i(String tag, String msg){
		Log.i(TAG, tag + ", " + msg);
	}
	
	
	public static void d(String tag, String msg){
		Log.d(TAG, tag + ", " + msg);
	}
	
	public static void v(String tag, String msg){
		Log.v(TAG, tag + ", " + msg);
	}
	
	public static void w(String tag, String msg){
		Log.w(TAG, tag + ", " + msg);
	}
	
	public static void e(String tag, String msg){
		Log.e(TAG, tag + ", " + msg);
	}	
	
	
}
