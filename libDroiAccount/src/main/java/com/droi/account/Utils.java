package com.droi.account;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.droi.account.authenticator.Constants;

public class Utils {

	public static final boolean DEBUG = true;
	//password_setup
	public static final int PWSSWD_LENGTH_MAX = 16;
	public static final int PASSWD_LENGTH_MIN = 6;
	public static final boolean PROJECT_2 = true;
	public static final String DOWNLOAD_DIR = "freeme";
	public static final String DROID_ACCOUNT_PACKAGENAME = "com.droi.account";
	public static final boolean SHOW_ACTION_BAR = true;
	
	public static final String EMAIL_REGULAR = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
	//private static Pattern phonePrefixPattern = Pattern.compile("^((13[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");  
	
    public static boolean isValidEmailAddress(String address) {
    	/*
        Pattern p = Pattern
                .compile("^((\\u0022.+?\\u0022@)|(([\\Q-!#$%&'*+/=?^`{}|~\\E\\w])+(\\.[\\Q-!#$%&'*+/=?^`{}|~\\E\\w]+)*@))"
                        + "((\\[(\\d{1,3}\\.){3}\\d{1,3}\\])|(((?=[0-9a-zA-Z])[-\\w]*(?<=[0-9a-zA-Z])\\.)+[a-zA-Z]{2,6}))$");*/
    	Pattern p = Pattern .compile(EMAIL_REGULAR);
        
        Matcher m = p.matcher(address);
        return m.matches();
    }
	
    public static boolean onlyNumber(String passWord){
    	if(!TextUtils.isEmpty(passWord)){
    		for(int i = 0; i < passWord.length(); i++){
    			char c = passWord.charAt(i);
                if (c < '0' ||  c > '9'){
                	return false;
                }
    		}
    	}
    	return true;
    }
    
    public static boolean isValidMobilePrefix(String prefix){
    	if(TextUtils.isEmpty(prefix)){
    		return false;
    	}
    	if(!onlyNumber(prefix)){
    		return false;
    	}
    	/*
    	Matcher m = phonePrefixPattern.matcher(prefix); 
    	return m.matches();*/

    	if(prefix.length()>= 1 && prefix.startsWith("1")){
        	if(prefix.length() >= 2 && (prefix.startsWith("13") || prefix.startsWith("15") || prefix.startsWith("17") || prefix.startsWith("18"))){
        		return true;
        	}else if(prefix.length() == 1){
        		return true;
        	}
    	}
    	return false;
    	
    }
    
	public static boolean isValidPassword(String passWord){
		if(passWord == null){
			return false;
		}
		
		if(passWord.getBytes().length > PWSSWD_LENGTH_MAX){
			return false;
		}
		//Pattern pattern = Pattern.compile("^[0-9a-zA-Z]{6,32}$");
		Pattern pattern = Pattern.compile("^[_0-9a-zA-Z]{"+PASSWD_LENGTH_MIN+","+PWSSWD_LENGTH_MAX+"}$");
		return pattern.matcher(passWord).matches(); 
	}
	
    public static int getAvailableNetWorkType(Context context) {

        int NO_NETWORK_AVAILABLE = -1;
        int netWorkType = NO_NETWORK_AVAILABLE;
        try {
            ConnectivityManager connetManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connetManager == null) {
                return NO_NETWORK_AVAILABLE;
            }
            NetworkInfo[] infos = connetManager.getAllNetworkInfo();
            if (infos == null) {
                return NO_NETWORK_AVAILABLE;
            }
            for (int i = 0; i < infos.length && infos[i] != null; i++){
                if (infos[i].isConnected() && infos[i].isAvailable()) {
                    netWorkType = infos[i].getType();
                    break;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return netWorkType;
    }
    
    public static String getDeviceInfo(Context context){
    	String info = "{}";
    	return info;
    }
    
	public static String getMacAddress(Context context){

		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo == null || TextUtils.isEmpty(wifiInfo.getMacAddress())){
			return null;
		}
		String mac = wifiInfo.getMacAddress();
		return mac;
	}
	
	public static  String getAccountUtype(String accountName){
		int accountType = getAccountType(accountName);
		if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
			return "zhuoyou";
		}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
			return "mail";
		}
		return " ";
	}
	
	public static int getAccountType(String accountName){
		if(accountName == null){
			return -1;
		}

		if(isNumber(accountName)){
			if(accountName.length() == 11 && accountName.startsWith("1")){
				return Constants.ACCOUNT_TYPE_PHONENUMBER;
			}
		}else if(isEmailAddress(accountName)){
			return Constants.ACCOUNT_TYPE_EMAIL;
		}
		return -1;
	}
	
	public static boolean isNumber(String accountName){
		if(accountName == null){
			return false;
		}
		if(TextUtils.isEmpty(accountName)){
			return false;
		}
		Pattern pattern = Pattern.compile("^1[34578]{1}[0-9]{1}[0-9]{8}$"); 
		return pattern.matcher(accountName).matches(); 
	}
	
	public static boolean isValidPhone(String accountName){
		if(Utils.isNumber(accountName)){
			if(accountName.length() == 11 && accountName.startsWith("1")){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isEmailAddress(String accountName){
		if(accountName == null){
			return false;
		}
		if(TextUtils.isEmpty(accountName)){
			return false;
		}
		return Utils.isValidEmailAddress(accountName);

	}
	
	public static String getStringFromJSON(JSONObject jsonObject, String key){
		String value = "";
		try {
			value = jsonObject.getString(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
	
	public static int getIntFromJSON(JSONObject jsonObject, String key){
		int value = -1;
		try {
			value = jsonObject.getInt(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
	
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int x, int y, int w, int h, 
            float rx, float ry) {   
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect srcRect = new Rect(x, y, x + w, y + h);
        final Rect destRect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(destRect);

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, rx, ry, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, destRect, paint);

        return output;
    }
    
	public static boolean isAppInstalled(Context context,String packagename)	{
		PackageInfo packageInfo;        
		try {
		    packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);
		}catch (NameNotFoundException e) {
		    packageInfo = null;
		    e.printStackTrace();
		}
		if(packageInfo == null){
		     return false;
		}else{
		    return true;
		}
	}
	
	public static void showMessage(Context context, int stringId){
		CustomToast.showToast(context,stringId,Toast.LENGTH_LONG);
	}
	
	public static void showMessage(Context context, String msg){
		CustomToast.showToast(context,msg,Toast.LENGTH_LONG);
	}
	
	public static void showMessage(Context context, CharSequence msg){
		CustomToast.showToast(context,String.valueOf(msg),Toast.LENGTH_LONG);
	}
	
	
	private static class CustomToast {

		private static Toast mToast;
		private static Handler mHandler = new Handler();
		private static Runnable r = new Runnable() {
			public void run() {
				mToast.cancel();
			}
		};

		public static void showToast(Context mContext, String text, int duration) {
			int delayMillis = 1000;

			mHandler.removeCallbacks(r);
			if (mToast != null) {
				mToast.setText(text);
			} else {
				mToast = Toast.makeText(mContext, text, duration);
			}
			mHandler.removeCallbacks(r);
			mHandler.postDelayed(r, delayMillis);

			mToast.show();
		}

		public static void showToast(Context mContext, int resId, int duration) {
			showToast(mContext, mContext.getResources().getString(resId),duration);
		}}
}
