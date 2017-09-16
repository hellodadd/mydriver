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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

public class DroiAPKHelper implements AccountInterface{
	private static final String ACCOUNT_TYPE = "com.freeme.account.android.samplesync";
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
	private static final String SHARED_PWDVAL = "passwdval";
	
	//intent broadcast
	private static final String INTENT_ACCOUNT_DELETED = "droi.account.intent.action.ACCOUNT_DELETED";
	private static final String INTENT_ACCOUNT_LOGIN = "droi.account.intent.action.ACCOUNT_LOGIN";
	private static final String INTENT_ACCOUNT_UPDATED = "droi.account.intent.action.ACCOUNT_UPDATED";
	private static final String INTENT_ACCOUNT_CHANGE_ACCOUNT = "droi.account.intent.action.CHANGE_ACCOUNT";
	public static final String TENCENT_APP_ID = "tencent_app_id";
	public static final String SINA_APP_ID = "sina_app_id";
	
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
	
	private static final String KEY_PUBLIC_ACCOUNT  = "droi-account-userinfo";
	private Context mContext;
	private AccountManager mAccountManager; 
	private static DroiAPKHelper sInstance;
	//private DroiAccountReceiver mReceiver;
    private String mTencentAppId;
    private String mSinaAppId;
    
	private DroiAPKHelper(Context context){
		mContext = context;
		mAccountManager = AccountManager.get(context);
		//mReceiver = new DroiAccountReceiver();
	}
	
	protected static DroiAPKHelper getInstance(Context context){
		if(sInstance == null){
			sInstance = new DroiAPKHelper(context.getApplicationContext());
		}
		return sInstance;
	}
	
	public void init(String tencentAppId, String sinaAppId){
		mTencentAppId = tencentAppId;
		mSinaAppId = sinaAppId;
	}
	
	public void login(int type){
		if(checkAccount() == false){
			Intent intent;
			String packageName = getAuthenticatorPackageName(ACCOUNT_TYPE);
			if(!TextUtils.isEmpty(packageName)){
				intent = new Intent();
				ComponentName componentName = new ComponentName(packageName, "com.droi.account.authenticator.AuthenticatorActivity");
				intent.setComponent(componentName);
			}else if(isAppInstalled(mContext, "com.droi.account")){
				intent = new Intent();
				ComponentName componentName = new ComponentName("com.droi.account", "com.droi.account.authenticator.AuthenticatorActivity");
				intent.setComponent(componentName);
			}else{
				intent = new Intent("com.droi.account.login");
			}

		    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		    mContext.startActivity(intent);
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
				ComponentName componentName = new ComponentName(packageName, "com.droi.account.setup.AccountSettings");
				intent.setComponent(componentName);
				intent.putExtra("account_settings", appName);
			}else if(isAppInstalled(mContext, "com.droi.account")){
				intent = new Intent();
				ComponentName componentName = new ComponentName("com.droi.account", "com.droi.account.setup.AccountSettings");
				intent.setComponent(componentName);
				intent.putExtra("account_settings", appName);
			}else{
				intent = new Intent("com.freeme.account.activity.setup");
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
	
	private boolean isEmpty(String str){
		if(TextUtils.isEmpty(str) || (str != null && "null".equals(str))){
			return true;
		}else{
			return false;
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
	
	private Intent getBindPhoneIntent(){
		Intent intent = null;
		if(checkAccount()){
			String packageName = getAuthenticatorPackageName(ACCOUNT_TYPE);
			if(!TextUtils.isEmpty(packageName)){
				intent = new Intent();
				ComponentName componentName = new ComponentName(packageName, "com.droi.account.procedure.BindAccountActivity");
				intent.setComponent(componentName);
				intent.putExtra(BIND_ACCOUNT_TYPE, BIND_ACCOUNT_PHONE);
			}else if(isAppInstalled(mContext, "com.droi.account")){
				intent = new Intent();
				ComponentName componentName = new ComponentName("com.droi.account", "com.droi.account.procedure.BindAccountActivity");
				intent.setComponent(componentName);
				intent.putExtra(BIND_ACCOUNT_TYPE, BIND_ACCOUNT_PHONE);
			}else{
				intent = new Intent("com.freeme.account.activity.setup");
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
			}else if(isAppInstalled(mContext, "com.droi.account")){
				intent = new Intent();
				ComponentName componentName = new ComponentName("com.droi.account", "com.droi.account.procedure.BindAccountActivity");
				intent.setComponent(componentName);
				intent.putExtra(BIND_ACCOUNT_TYPE, BIND_ACCOUNT_EMAIL);
			}else{
				intent = new Intent("com.freeme.account.activity.setup");
				intent.putExtra(BIND_ACCOUNT_TYPE, BIND_ACCOUNT_EMAIL);
			}
		}
		
		return intent;
	}
	
	
	public String getTencentAppId(){
		return mTencentAppId;
	}
	
	public String getSinaAppId(){
		return mSinaAppId;
	}
	
	public boolean checkAccount(){
		Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
		if (accounts != null && accounts.length >= 1){
			return true;
		}
		return false;
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
	
	private String getPasswdVal(){
		String accountData = getDataFromAccount(mContext);
		String pwdVal = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				pwdVal = jsonObject.getString(SHARED_PWDVAL);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(isEmpty(pwdVal)){
			return null;
		}
		return pwdVal;
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
		String data = getDataFromDataBase();
		/*shound not get data from AccountManager for other apps donot have 
		 * the same signature as the com.droi.account app
		if(TextUtils.isEmpty(data)){
		    data = accountManager.getUserData(account, KEY_PUBLIC_ACCOUNT);
		}*/
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
		if(contentResolver == null){
		    return null;
		}
	    Cursor cur = contentResolver.query(uri, columns, null, null, null);
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
						//Intent intent = new Intent(INTENT_ACCOUNT_DELETED);
						//mContext.sendBroadcast(intent);
						/*
						Intent loginIntent = new Intent("com.droi.account.login");
						loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						mContext.startActivity(loginIntent);*/
						login(0);
					}
				}
				
			}, null);
		}
	}
	
	public void changeAccount(){
		if(checkAccount()){
			Intent intent;
			String packageName = getAuthenticatorPackageName(ACCOUNT_TYPE);
			String pwdVal = getPasswdVal();
			if("0".equals(pwdVal) == false){
				reLogin();
			}else{
				if(!TextUtils.isEmpty(packageName)){
					intent = new Intent();
					ComponentName componentName = new ComponentName(packageName, "com.droi.account.shared.ChangeAccountActivity");
					intent.setComponent(componentName);
				}else if(isAppInstalled(mContext, "com.droi.account")){
					intent = new Intent();
					ComponentName componentName = new ComponentName("com.droi.account", "com.droi.account.shared.ChangeAccountActivity");
					intent.setComponent(componentName);
				}else{
					intent = new Intent(INTENT_ACCOUNT_CHANGE_ACCOUNT);
				}
	
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			}
		}

	}
	
	private void reLogin(){
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
	public String getAccountType() {
		return ACCOUNT_TYPE;
	}
	
}

