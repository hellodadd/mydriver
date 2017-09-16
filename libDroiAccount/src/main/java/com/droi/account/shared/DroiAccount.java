package com.droi.account.shared;

import com.droi.account.MyResource;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class DroiAccount implements AccountInterface{
	
	public static final int LOIGN_THEME_PORTRAIT = 0;
	public static final int LOIGN_THEME_LANDSCAPE = 1;
	
	private static final String ACCOUNT_APK_PACKAGE = "com.droi.account";
	private static final String USE_LOGIN_TYPE = "user_login_type";
	
	
	//intent broadcast
	public static final String INTENT_ACCOUNT_LOGINSUCCESS = "android.accounts.LOGIN_ACCOUNTS_CHANGED";
	public static final String INTENT_ACCOUNT_DELETED = "droi.account.sdk.intent.action.ACCOUNT_DELETED";
	public static final String INTENT_ACCOUNT_LOGIN = "droi.account.sdk.intent.action.ACCOUNT_LOGIN";
	public static final String INTENT_ACCOUNT_UPDATED = "droi.account.sdk.intent.action.ACCOUNT_UPDATED";
	public static final String INTENT_ACCOUNT_CHANGE_ACCOUNT = "droi.account.sdk.intent.action.CHANGE_ACCOUNT";
	
	//called by other apps , operation
	public static final String BIND_ACCOUNT_TYPE = "bind_account_type";
	public static final int BIND_ACCOUNT_PHONE = 1;
	public static final int BIND_ACCOUNT_EMAIL = 2;
	
	//send broadcast info to apps
	public static final String DROI_ACCOUNT_SYNCINFO_LOGIN_CANCELLED = "droi.account.intent.syncinfo.LOGIN_CANCELLED";
	public static final String DROI_ACCOUNT_SYNCINFO_PHONE_BINDED = "droi.account.intent.syncinfo.PHONE_BINDED";
	public static final String DROI_ACCOUNT_SYNCINFO_MAIL_BINDED = "droi.account.intent.syncinfo.MAIL_BINDED";
	public static final String KEY_DROI_ACCOUNT_SYNCINFO_PHONE_BINDED = "phone";
	public static final String KEY_DROI_ACCOUNT_SYNCINFO_MAIL_BINDED = "mail";
	
	public static final String AUTHENCATICATOR_TYPE_KEY = "droi_account_authenticator_type";
	
//	private static DroiAccount sInstance;
	private AccountInterface interFace;
	private DroiAPKHelper apkHelper;
	private DroiSDKHelper sdkHelper;
	private Context mContext;
	private MyResource mMyResource;
	
	private DroiAccount(Context context){
		apkHelper = DroiAPKHelper.getInstance(context);
		sdkHelper = DroiSDKHelper.getInstance(context);
		mMyResource = new MyResource(context);
		mContext = context;
		
		if(!apkHelper.checkAccount() && !sdkHelper.checkAccount()){
			setUseAccountFramework(false);
		}
		
		if(isAppInstalled(mContext, ACCOUNT_APK_PACKAGE) && isUseAccountFramework()){
			interFace = apkHelper;
		}else{
			interFace = sdkHelper;
		}
	}
	
	public static DroiAccount getInstance(Context context){
//		if(sInstance ==null){
//			sInstance = new DroiAccount(context);
//		}
		return new DroiAccount(context);
	}
	
	
	public void init(String tencentAppId, String sinaAppId) {
		apkHelper.init(tencentAppId, sinaAppId);
		sdkHelper.init(tencentAppId, sinaAppId);
	}

	public boolean isUseAccountFramework() {
		SharedPreferences sharePrefs = mContext.getSharedPreferences(USE_LOGIN_TYPE,Context.MODE_PRIVATE);
		return sharePrefs.getBoolean("isUseAccountFramework", false);
	}

	private void setUseAccountFramework(Boolean used) {
		SharedPreferences sharePrefs = mContext.getSharedPreferences(USE_LOGIN_TYPE,Context.MODE_PRIVATE);
		sharePrefs.edit().putBoolean("isUseAccountFramework", used).commit();
	}

	public void login(final int type) {
		if(isAppInstalled(mContext, ACCOUNT_APK_PACKAGE)){
			boolean isAPKLogin = apkHelper.checkAccount();
			if(!isAPKLogin){		//如果有框架但是未登录则直接选择登录
				interFace = apkHelper;
				exuteLogin(type);
			}else{					//如果有框架而且已登录 提示用户使用选择登录
				String userName = apkHelper.getUserName();
				userName = TextUtils.isEmpty(userName)?"": "\""+userName+"\"";
				String useMessage =mContext.getString(mMyResource.getString("lib_droi_account_useframe_message"));
				useMessage = String.format(useMessage, userName);
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				View view = LayoutInflater.from(mContext).inflate(mMyResource.getLayout("lib_droi_account_dialog_whether_useframe"), null);
				final AlertDialog mDialog = builder.setView(view).create();
				TextView mTVMessage = (TextView)view.findViewById(mMyResource.getId("lib_droi_account_useframe_message_textview"));
				mTVMessage.setText(useMessage);
				view.findViewById(mMyResource.getId("lib_droi_account_dialog_changebtn")).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mDialog.dismiss();
						interFace = sdkHelper;
						exuteLogin(type);
					}
				});
				view.findViewById(mMyResource.getId("lib_droi_account_dialog_usebtn")).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mDialog.dismiss();
						interFace = apkHelper;
						exuteLogin(type);
					}
				});
				
				mDialog.show();			
			}
		}else{
			interFace = sdkHelper;
			exuteLogin(type);
		}
	}
	
	private void exuteLogin(int type){
		if(interFace.getAccountType().equals(apkHelper.getAccountType())){
			setUseAccountFramework(true);
		}else{
			setUseAccountFramework(false);
		}
		
		if(interFace.checkAccount()){
			Intent boradToUpdate = new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
			mContext.sendBroadcast(boradToUpdate);
		}else{
			interFace.login(type);
		}
	}
	
	public boolean checkAccount(){
		return interFace.checkAccount();
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

	@Override
	public String getUserName() {
		return interFace.getUserName();
	}

	@Override
	public String getBindPhone() {
		return interFace.getBindPhone();
	}

	@Override
	public String getOpenId() {
		return interFace.getOpenId();
	}

	@Override
	public String getToken() {
		return interFace.getToken();
	}

	@Override
	public String getExpire() {
		return interFace.getExpire();
	}

	@Override
	public String getUid() {
		return interFace.getUid();
	}

	@Override
	public void tokenInvalidate() {
		if(interFace.getAccountType().equals(sdkHelper.getAccountType())){
			interFace.tokenInvalidate();
		}else{
			Intent intent = new Intent(INTENT_ACCOUNT_DELETED);
			mContext.sendBroadcast(intent);
			interFace = sdkHelper;
			setUseAccountFramework(false);
		}
	}

//	@Override
//	public void changeAccount() {
//		interFace.changeAccount();
//	}

	@Override
	public String getAvatarUrl() {
		return interFace.getAvatarUrl();
	}

	@Override
	public String getNickName() {
		return interFace.getNickName();
	}

	@Override
	public String getAccountType() {
		return interFace.getAccountType();
	}

	@Override
	public Intent getSettingsIntent(String appName) {
		return interFace.getSettingsIntent(appName);
	}

	public static void  setEmailLogin(boolean enable) {
		DroiSDKHelper.setEmailLogin(enable);
	}

	public static void setPhoneLogin(boolean enable) {
		DroiSDKHelper.setPhoneLogin(enable);
	}

	public static void setVersionForeign(boolean enable) {
		DroiSDKHelper.setVersionForeign(enable);
	}
}