package com.droi.account.shared;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.droi.account.DebugUtils;
import com.droi.account.authenticator.AuthenticatorActivity;
import com.droi.account.manager.ShareInfoManager;
import com.droi.account.setup.ActivitySettings;
import com.droi.account.statis.StaticsCallback;
import com.droi.account.weibosdk.SinaConstants;

public class DroiSDKHelper implements AccountChangeListener, AccountInterface{
	public static final String TAG = "DroiAccount";
	public static String ACCOUNT_TYPE = "com.freeme.account.sdk.android.samplesync";
	private static final String AUTHORITY_SUFFIX = ".droidatabase";
	private static final String SHARED_UID = "uid";
	private static final String SHARED_OPENID = "openid";
	private static final String SHARED_TOKEN = "token";
	private static final String SHARED_DATA = "data";
	private static final String SHARED_REGTYPE = "regtype";
	public static final String SHARED_EXPIRE = "expire";
	public static final String SHARED_BINDPHONE = "bindphone";
	public static final String SHARED_BINDMAIL = "bindemail";
	public static final String SHARED_USERNAME = "username";
	public static final String SHARED_AVATAR = "avatar";
	public static final String SHARED_NICKNAME = "nickname";
	//intent broadcast
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
	
//	public static final String TENCENT_APP_ID = "tencent_app_id";
//	public static final String SINA_APP_ID = "sina_app_id";
	public static final String AUTHENCATICATOR_TYPE_KEY = "droi_account_authenticator_type";
//	private static final String KEY_PUBLIC_ACCOUNT  = "droi-account-userinfo";
	private Context mContext;
	private AccountManager mAccountManager; 
	private static DroiSDKHelper sInstance;
    //private String mTencentAppId = "1103957320";
    //private String mSinaAppKey = "400102941";
	//private DroiAccountReceiver mReceiver;
    //add douyuqing
  	public static boolean PHONE_LOGIN = true;
  	public static boolean EMAIL_LOGIN = true;
  	public static boolean VERSION_FOREIGN = true;
	
  	public static void setPhoneLogin(boolean p){
  		PHONE_LOGIN = p;
  	}
  	
  	public static void setEmailLogin(boolean e){
  		EMAIL_LOGIN = e;
  	}
  	
  	public static void setVersionForeign(boolean v){
  		VERSION_FOREIGN = v;
  	}
  	
  	public static boolean getPhoneLogin(boolean p){
  		return PHONE_LOGIN;
  	}
  	
  	public static boolean getEmailLogin(boolean e){
  		return EMAIL_LOGIN;
  	}
  	
  	public static boolean getVersionForeign(boolean v){
  		return VERSION_FOREIGN;
  	}
	//end
	private DroiSDKHelper(Context context){
		mContext = context;
		mAccountManager = AccountManager.get(context);
		ACCOUNT_TYPE = context.getPackageName();
		//mReceiver = new DroiAccountReceiver();
		StaticsCallback.getInstance(context);
	}
	
	public static DroiSDKHelper getInstance(Context context){
		if(sInstance == null){
			sInstance = new DroiSDKHelper(context.getApplicationContext());
		}
		return sInstance;
	}
	
//	public void init(String tencentAppId, String sinaAppId){
//		mTencentAppId = tencentAppId;
//		mSinaAppId = sinaAppId;
//		SinaConstants.APP_KEY = sinaAppId;
//	}
	public void init(String sinaAppKey){
		SinaConstants.APP_KEY = sinaAppKey;
	}
	
	public void login(int type){
		if(checkAccount() == false){
			Intent intent;
			String packageName = getAuthenticatorPackageName(ACCOUNT_TYPE);
			if(!TextUtils.isEmpty(packageName)){
				intent = new Intent();
				ComponentName componentName = new ComponentName(packageName, "com.droi.account.authenticator.AuthenticatorActivity");
				intent.setComponent(componentName);
			}else{
				intent = new Intent(mContext,AuthenticatorActivity.class);
			}
			if(type == DroiAccount.LOIGN_THEME_PORTRAIT){
				intent.putExtra(AuthenticatorActivity.LOGIN_THEME, android.R.style.Theme_Holo_Light);
				intent.putExtra(AuthenticatorActivity.LOGIN_RequestedOrientation, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}else if(type == DroiAccount.LOIGN_THEME_LANDSCAPE){
				intent.putExtra(AuthenticatorActivity.LOGIN_THEME, android.R.style.Theme_Holo_Dialog_NoActionBar);
				intent.putExtra(AuthenticatorActivity.LOGIN_RequestedOrientation, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
		    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    mContext.startActivity(intent);
		}
	}
	
	protected void reLogin(){
		if(mAccountManager == null){
			mAccountManager = AccountManager.get(mContext.getApplicationContext());
		}
		
		Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
		if (accounts != null && accounts.length >= 1){
			
		}else{
			return;
		}
		if(mAccountManager != null && accounts[0] != null){
			mAccountManager.removeAccount(accounts[0], new AccountManagerCallback<Boolean>(){

				@Override
				public void run(
						AccountManagerFuture<Boolean> future) {
					// TODO Auto-generated method stub
					boolean failed = true;
					try {
                        if (future.getResult() == true) {
                            failed = false;
                        }
                    } catch (OperationCanceledException e) {
                        // handled below
                    } catch (IOException e) {
                        // handled below
                    } catch (AuthenticatorException e) {
                        // handled below
                    }
					if(failed){
						
					}else{
						login(0);
					}
				}
				
			}, null);
		}
	}
	
	private String getAuthenticatorPackageName(String type){
		AuthenticatorDescription[] mAuthDescs = AccountManager.get(mContext).getAuthenticatorTypes();
        for (int i = 0; i < mAuthDescs.length; i++) {
        	if(type.equals(mAuthDescs[i].type)){
        		return mAuthDescs[i].packageName;
        	}
        }
        return null;
	}
	
	public Intent getSettingsIntent(String appName){
		Intent intent = null;
		if(checkAccount()){
			String packageName = getAuthenticatorPackageName(ACCOUNT_TYPE);
			if(!TextUtils.isEmpty(packageName)){
				intent = new Intent();
				ComponentName componentName = new ComponentName(packageName, "com.droi.account.setup.ActivitySettings");
				intent.setComponent(componentName);
				intent.putExtra("account_settings", appName);
			}else{
				intent = new Intent(mContext,ActivitySettings.class);
				intent.putExtra("account_settings", appName);
			}
		}
		return intent;
	}
	
	private void bindPhone(){
		String bindPhone = getBindPhone();
		if(checkAccount() && isEmpty(bindPhone)){
			Intent intent = getBindPhoneIntent();
		    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    mContext.startActivity(intent);
		}
	}
	
	private void bindMail(){
		String bindEmail = getBindMail();
		if(checkAccount() && isEmpty(bindEmail)){
			Intent intent = getBindEmailIntent();
		    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    mContext.startActivity(intent);
		}
	}
	
	private boolean isEmpty(String str){
		if(TextUtils.isEmpty(str) || (str != null && "null".equals(str))){
			return true;
		}else{
			return false;
		}
	}
	
	private Intent getBindPhoneIntent(){
		Intent intent = null;
		if(checkAccount()){
			String packageName = getAuthenticatorPackageName(ACCOUNT_TYPE);
			if(!TextUtils.isEmpty(packageName)){
				intent = new Intent();
				ComponentName componentName = new ComponentName(packageName, "com.droi.account.procedure.BindAccountActivity");
				intent.setComponent(componentName);
				intent.putExtra(BIND_ACCOUNT_TYPE, BIND_ACCOUNT_PHONE);
			}else{
				intent = new Intent("com.freeme.account.sdk.activity.setup");
				intent.putExtra(BIND_ACCOUNT_TYPE, BIND_ACCOUNT_PHONE);
			}
		}
		
		return intent;
	}
	
	private Intent getBindEmailIntent(){
		Intent intent = null;
		if(checkAccount()){
			String packageName = getAuthenticatorPackageName(ACCOUNT_TYPE);
			if(!TextUtils.isEmpty(packageName)){
				intent = new Intent();
				ComponentName componentName = new ComponentName(packageName, "com.droi.account.procedure.BindAccountActivity");
				intent.setComponent(componentName);
				intent.putExtra(BIND_ACCOUNT_TYPE, BIND_ACCOUNT_EMAIL);
			}else{
				intent = new Intent("com.freeme.account.sdk.activity.setup");
				intent.putExtra(BIND_ACCOUNT_TYPE, BIND_ACCOUNT_EMAIL);
			}
		}
		
		return intent;
	}
	
	
//	public String getTencentAppId(){
//		return mTencentAppId;
//	}
	
	public String getSinaAppKey(){
		return SinaConstants.APP_KEY;
	}
	
	public String getUserName(){
		String accountData = getDataFromAccount(mContext);
		String name = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				name = jsonObject.getString(SHARED_USERNAME);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(isEmpty(name)){
			return null;
		}
		return name;
	}
	
	public String getNickName(){
		String accountData = getDataFromAccount(mContext);
		String nickName = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				nickName = jsonObject.getString(SHARED_NICKNAME);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(isEmpty(nickName)){
			return null;
		}
		return nickName;
	}
	
	public String getBindPhone(){
		String accountData = getDataFromAccount(mContext);
		String phone = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				phone = jsonObject.getString(SHARED_BINDPHONE);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(isEmpty(phone)){
			return null;
		}
		return phone;
	}
	
	private String getBindMail(){
		String accountData = getDataFromAccount(mContext);
		String mail = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				mail = jsonObject.getString(SHARED_BINDMAIL);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(isEmpty(mail)){
			return null;
		}
		return mail;
	}
	
	public boolean checkAccount(){
		Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
		if (accounts != null && accounts.length >= 1){
			return true;
		}
		return false;
	}
	
	public String getOpenId(){
		String accountData = getDataFromAccount(mContext);
		String openId = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				openId = jsonObject.getString(SHARED_OPENID);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(isEmpty(openId)){
			return null;
		}
		return openId;
	}
	
	public String getUid(){
		String accountData = getDataFromAccount(mContext);
		String uid = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				uid = jsonObject.getString(SHARED_UID);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(isEmpty(uid)){
			return null;
		}
		return uid;
	}
	
	public String getToken(){
		String accountData = getDataFromAccount(mContext);
		String token = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				token = jsonObject.getString(SHARED_TOKEN);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(isEmpty(token)){
			return null;
		}
		return token;
	}
	
	public String getExpire(){
		String accountData = getDataFromAccount(mContext);
		String expire = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				expire = jsonObject.getString(SHARED_EXPIRE);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(isEmpty(expire)){
			return null;
		}
		return expire;
	}
	
	public String getAvatarUrl(){
		String accountData = getDataFromAccount(mContext);
		String avatarUrl = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				avatarUrl = jsonObject.getString(SHARED_AVATAR);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(isEmpty(avatarUrl)){
			return null;
		}
		return avatarUrl;
	}
	
	public void tokenInvalidate(){
		if(mAccountManager == null){
			mAccountManager = AccountManager.get(mContext.getApplicationContext());
		}
		
		Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
		if (accounts != null && accounts.length >= 1){
			
		}else{
			return;
		}
		if(mAccountManager != null && accounts[0] != null){
			mAccountManager.removeAccount(accounts[0], new AccountManagerCallback<Boolean>(){

				@Override
				public void run(AccountManagerFuture<Boolean> future) {
					// TODO Auto-generated method stub
					boolean failed = true;
					try {
                        if (future.getResult() == true) {
                            failed = false;
                        }
                    } catch (OperationCanceledException e) {
                        // handled below
                    } catch (IOException e) {
                        // handled below
                    } catch (AuthenticatorException e) {
                        // handled below
                    }
					if(failed){
						
					}else{
						//Intent intent = new Intent(INTENT_ACCOUNT_DELETED);
						//mContext.sendBroadcast(intent);
						//login();
					}
				}
				
			}, null);
		}
	}
	
	public void changeAccount(){
		if(checkAccount()){
			Intent intent;
			String packageName = getAuthenticatorPackageName(ACCOUNT_TYPE);
			if(!TextUtils.isEmpty(packageName)){
				intent = new Intent();
				ComponentName componentName = new ComponentName(packageName, "com.droi.account.shared.ChangeAccountActivity");
				intent.setComponent(componentName);
			}else{
				intent = new Intent(INTENT_ACCOUNT_CHANGE_ACCOUNT);
			}

			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		}
	}
	/*
	public void registerReceiver(){
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mReceiver, filter);
	}
	
	public void unregisterReceiver(){
		mContext.unregisterReceiver(mReceiver);
	}*/
	
	
	
	
	private String getData(Context context){
		String accountData = getDataFromAccount(context);
		String token = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				token = jsonObject.getString(SHARED_DATA);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return token;
	}
	
	
	
	private String getDataFromAccount(Context context){
		AccountManager accountManager = AccountManager.get(context);
		String data = null;
		Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
		if (accounts != null && accounts.length >= 1){
			data = getDataFromAccount(context, accounts[0]);
		}
		
		return data;
	}
	
	private String getDataFromAccount(Context context, Account account){
		//AccountManager accountManager = AccountManager.get(context);
		//String data = accountManager.getUserData(account, KEY_PUBLIC_ACCOUNT);
		//
		String data = getDataFromDataBase();
		return data;
	}
	
	private String getDataFromDataBase(){
		String columns[] = new String[] {"_id", "shared_data"}; 
		String packageName = getAuthenticatorPackageName(ACCOUNT_TYPE);
		String autority = packageName + AUTHORITY_SUFFIX;
		Uri uri = Uri.parse("content://"+ autority + "/droidata");  
		
		ContentResolver contentResolver = null;
		if(mContext != null){
		    contentResolver =  mContext.getContentResolver();
		}
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "getDataFromDataBase contentResolver : " + contentResolver);
		}
		if(contentResolver == null){
		    return null;
		}
	    Cursor cur = contentResolver.query(uri, columns, null, null, null);
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "getDataFromDataBase cur : " + cur);
		}
		if(cur == null){
			return null;
		}
		
		String result = "";
		if (cur.moveToFirst()) {  
			do {
				result = cur.getString(cur.getColumnIndex("shared_data"));			
			} while (cur.moveToNext());  
		}
		cur.close();
		return result;
	}
	
	@Override
	public void onAccountLogin() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(INTENT_ACCOUNT_LOGIN);
		intent.putExtra(AUTHENCATICATOR_TYPE_KEY, ACCOUNT_TYPE);
		ShareInfoManager manager = ShareInfoManager.getInstance(mContext);
		intent.putExtra("userInfo", manager.getPublicDataFromInternalAccount());
		mContext.sendBroadcast(intent);
	}

	@Override
	public void onAccountDeleted() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(INTENT_ACCOUNT_DELETED);
		intent.putExtra(AUTHENCATICATOR_TYPE_KEY, ACCOUNT_TYPE);
		mContext.sendBroadcast(intent);
	}

	@Override
	public void onAccountUpdated(String data) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(INTENT_ACCOUNT_UPDATED);
		intent.putExtra(AUTHENCATICATOR_TYPE_KEY, ACCOUNT_TYPE);
		intent.putExtra("updatedInfo", data);
		mContext.sendBroadcast(intent);
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
	public void onAccountCancelled() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(DROI_ACCOUNT_SYNCINFO_LOGIN_CANCELLED);
		intent.putExtra(AUTHENCATICATOR_TYPE_KEY, ACCOUNT_TYPE);
		mContext.sendBroadcast(intent);
	}

	@Override
	public void onAccountBinded(String type, String data) {
		// TODO Auto-generated method stub
		Intent intent = null;
		if("phone".equals(type)){
			intent = new Intent(DROI_ACCOUNT_SYNCINFO_PHONE_BINDED);
			intent.putExtra(KEY_DROI_ACCOUNT_SYNCINFO_PHONE_BINDED, data);
		}else if("mail".equals(type)){
			intent = new Intent(DROI_ACCOUNT_SYNCINFO_MAIL_BINDED);
			intent.putExtra(KEY_DROI_ACCOUNT_SYNCINFO_MAIL_BINDED, data);
		}else{
			intent = new Intent(DROI_ACCOUNT_SYNCINFO_PHONE_BINDED);
			intent.putExtra(KEY_DROI_ACCOUNT_SYNCINFO_PHONE_BINDED, data);
		}
		if(intent != null){
			intent.putExtra(AUTHENCATICATOR_TYPE_KEY, ACCOUNT_TYPE);
			mContext.sendBroadcast(intent);
		}
		
	}

	@Override
	public void init(String tencentAppId, String sinaAppId) {
		init(sinaAppId);
	}

	@Override
	public String getAccountType() {
		return ACCOUNT_TYPE;
	}
}
