package com.droi.account.statis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.droi.account.DebugUtils;

public class StaticsUtils {

	private static final String TAG = "StaticsUtils";
	
	private static String encryptByBase64(String rawMsg){
		String result = "";
		if(!TextUtils.isEmpty(rawMsg)){
			//result = Base64Encoder.encode(rawMsg.getBytes());
			result = Base64.encodeToString(rawMsg.getBytes(),  Base64.NO_WRAP);
		}
		return result;
	}
	
	private static String encryptByDroi(String msg){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "encryptByDroi msg = " + msg);
		}
		if(!TextUtils.isEmpty(msg)){
			return DroiEncoder.encode(msg);
		}
		
		return "";
	}
	
	public static String encryptLoginfo(String logInfo){
		String result = "";
		if(!TextUtils.isEmpty(logInfo)){
			result = encryptByBase64(logInfo);
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "after encrypt by base64 : " + result);
			}
			result = encryptByDroi(result);
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "after encrypt by droi : " + result);
			}
		}
		
		int length = result.length();
		if(result.charAt(length - 1) == '='){
			result = result.substring(0, length - 1);
		}
		//result.replaceAll("\r\n","");
		return result;
	}
    private static String getFreemeOSVersion() {
        Class<?> mClassType = null;  
        Method mGetMethod = null;  

        String freemeosVersion = "";
        try {  
            if (mClassType == null) {  
                mClassType = Class.forName("android.os.SystemProperties");  
                  
                mGetMethod = mClassType.getDeclaredMethod("get", String.class);  
                freemeosVersion = (String) mGetMethod.invoke(mClassType, "ro.build.version.freemeos"); 
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
        int dot = 0;
        if ((dot = freemeosVersion.indexOf(".")) >= 0 && freemeosVersion.length() >= (dot += 2))
        	freemeosVersion = freemeosVersion.substring(0, dot);
            
        return freemeosVersion;
    }
    
    private static String getWifiMac(Context context){
		String macAddr = "";
		try {
			WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

			WifiInfo info = wifi.getConnectionInfo();
			macAddr = info.getMacAddress();
		} catch (Exception e) {

		}
		return macAddr;
    }
    
    public static String getTotalRAM(Context context) {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            long size = Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024l;
            String ramSize = (double) size / (1024 * 1024) + "M";
            return ramSize;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public static String getTotalROM() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        long size = totalBlocks * blockSize;
        String romSize = (double) size / (1024 * 1024) + "M";
        return romSize;
    }
    
	public static void addCommonStaticsInfo(Context context, JSONObject jsonObj){
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = "";
		String imei = "";
		String lcdResolution = "";
		String mf = android.os.Build.HARDWARE;//厂商信息
		String platform = android.os.Build.HARDWARE;//平台信息
		String ram = getTotalRAM(context);
		String rom = getTotalROM();
		String androidVersion = android.os.Build.VERSION.RELEASE;
		String md = android.os.Build.MODEL;
		String freemeos_version = getFreemeOSVersion();
		String mac_addr = getWifiMac(context);
		
		if(tm != null){
			imei = tm.getDeviceId() == null ? "" : tm.getDeviceId();
			imsi = tm.getSubscriberId() == null ? "" : tm.getSubscriberId();
		}
		
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displayMestrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(displayMestrics);
		lcdResolution = Integer.toString(displayMestrics.widthPixels) + "x" + Integer.toString(displayMestrics.heightPixels);
		
		try {
			jsonObj.put("IE", imei);
			jsonObj.put("IS", imsi);
			jsonObj.put("LCD", lcdResolution);
			jsonObj.put("MF", mf);
			jsonObj.put("PT", platform);
			jsonObj.put("RAM", ram);
			jsonObj.put("ROM", rom);
			jsonObj.put("AND", androidVersion);
			jsonObj.put("MD", md);
			jsonObj.put("f_v", freemeos_version);
			jsonObj.put("mac", mac_addr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static JSONObject buildRegisterStatics(Context context, String accountName, int accountType, String openId, boolean success) {

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("ac_id", Integer.toString(AccountOperation.ACCOUNT_REGISTER));
			Long tsLong = System.currentTimeMillis()/1000;
			jsonObject.put("dt", tsLong.toString());
			jsonObject.put("openid", openId);
			jsonObject.put("acc", accountName);
			switch(accountType){
				case AccountOperation.ACCOUNT_REGISTER_TYPE_PHONE:
				case AccountOperation.ACCOUNT_REGISTER_TYPE_MAIL:{
					jsonObject.put("type", Integer.toString(accountType));
				}
				break;
				default:
					jsonObject.put("type", "");
					break;
			}
			if(success){
				jsonObject.put("sta", "1");
			}else{
				jsonObject.put("sta", "0");
			}
			addApkInfo(context, jsonObject);
			jsonObject.put("from", "1");//from account apk
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	private static void addApkInfo(Context context, JSONObject jsonObject){
		ApplicationInfo info = context.getApplicationInfo();
		try {
			jsonObject.put("apk", info.packageName);
			jsonObject.put("apk_n", info.loadLabel(context.getPackageManager()));
			PackageInfo pi = context.getPackageManager().getPackageInfo(info.packageName, 0);
			jsonObject.put("apk_v", Integer.toString(pi.versionCode));
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}catch (NameNotFoundException e) {
		}
	}
	
	public static JSONObject buildLoginStatics(Context context, String accountName, String openId, int loginType, int flag, int mark){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("ac_id", Integer.toString(AccountOperation.ACCOUNT_LOGIN));
			Long tsLong = System.currentTimeMillis()/1000;
			jsonObject.put("dt", tsLong.toString());
			jsonObject.put("openid", openId);
			jsonObject.put("user", accountName);
			
			switch (loginType) {
			case AccountOperation.ACCOUNT_LOGIN_TYPE_PHONE:
			case AccountOperation.ACCOUNT_LOGIN_TYPE_MAIL:
			case AccountOperation.ACCOUNT_LOGIN_TYPE_QQ:
			case AccountOperation.ACCOUNT_LOGIN_TYPE_WEIBO:
			case AccountOperation.ACCOUNT_LOGIN_TYPE_VISITOR:
			case AccountOperation.ACCOUNT_LOGIN_TYPE_WECHAT:{
				jsonObject.put("type", Integer.toString(loginType));
			}
				break;
			default:
				jsonObject.put("type", "");
				break;
			}

			addApkInfo(context, jsonObject);
			jsonObject.put("from", "1");//from account apk
			jsonObject.put("flag", Integer.toString(flag));
			switch(mark){
			case AccountOperation.ACCOUNT_BINDED_NONE:
			case AccountOperation.ACCOUNT_BINDED_PHONE:
			case AccountOperation.ACCOUNT_BINDED_MAIL:
			case AccountOperation.ACCOUNT_BINDED_PHONE_MAIL:{
				jsonObject.put("mark", Integer.toString(mark));
			}
			break;
			default:
				jsonObject.put("mark", "");
				break;
			}
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	public static JSONObject buildBindStatics(Context context, String accountName, String openId, int loginType, int bindType){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("ac_id", Integer.toString(AccountOperation.ACCOUNT_BIND));
			Long tsLong = System.currentTimeMillis()/1000;
			jsonObject.put("dt", tsLong.toString());
			jsonObject.put("openid", openId);
			jsonObject.put("user", accountName);
			
			switch (loginType) {
			case AccountOperation.ACCOUNT_LOGIN_TYPE_PHONE:
			case AccountOperation.ACCOUNT_LOGIN_TYPE_MAIL:
			case AccountOperation.ACCOUNT_LOGIN_TYPE_QQ:
			case AccountOperation.ACCOUNT_LOGIN_TYPE_WEIBO:
			case AccountOperation.ACCOUNT_LOGIN_TYPE_VISITOR:
			case AccountOperation.ACCOUNT_LOGIN_TYPE_WECHAT:{
				jsonObject.put("type", Integer.toString(loginType));
			}
				break;
			default:
				jsonObject.put("type", "");
				break;
			}

			addApkInfo(context, jsonObject);
			jsonObject.put("from", "1");//from account apk
			switch (bindType) {
			case AccountOperation.ACCOUNT_QQ_BIND_PHONE:
			case AccountOperation.ACCOUNT_QQ_BIND_MAIL:
			case AccountOperation.ACCOUNT_WEIBO_BIND_PHONE:
			case AccountOperation.ACCOUNT_WEIBO_BIND_MAIL:
			case AccountOperation.ACCOUNT_PHONE_BIND_QQ:
			case AccountOperation.ACCOUNT_PHONE_BIND_WEIBO:
			case AccountOperation.ACCOUNT_MAIL_BIND_QQ:
			case AccountOperation.ACCOUNT_MAIL_BIND_WEIBO:
			case AccountOperation.ACCOUNT_WECHAT_BIND_PHONE:
			case AccountOperation.ACCOUNT_WECHAT_BIND_MAIL:
			case AccountOperation.ACCOUNT_PHONE_BIND_WECHAT:
			case AccountOperation.ACCOUNT_EMAIL_BIND_WECHAT: {
				jsonObject.put("bind", Integer.toString(bindType));
			}
				break;
			default:
				jsonObject.put("bind", "");
				break;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject;
	}
	
}