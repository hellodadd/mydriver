package com.droi.account.login;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.droi.account.DebugUtils;
import com.droi.account.MyResource;
import com.droi.account.Utils;
import com.droi.account.authenticator.AuthenticatorActivity;
import com.droi.account.authenticator.Constants;
import com.droi.account.manager.ShareInfoManager;
import com.droi.account.shared.DroiSDKHelper;

public class SharedInfo {
	//function in this app
	private static final String TAG = "SharedInfo";
	private static final boolean DEBUG = Utils.DEBUG;
	
	private static final String KEY_PRIVATE_ACCOUNT = "my_0045_userInfo";
	
	private static SharedInfo instance = new SharedInfo();
	private User mUser = new User();
	private AccountManager mAccountManager;
	
	public SharedInfo(){
		
	}
	
	public static SharedInfo getInstance(){
		return instance;
	}
	
	public void saveDataToAccount(Context context){
		if(mAccountManager == null){
			mAccountManager = AccountManager.get(context);
		}
		Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		if (accounts != null && accounts.length >= 1){
			saveDataToAccount(context, accounts[0]);
		}else{
			Utils.showMessage(context, MyResource.getString(context.getApplicationContext(),"lib_droi_account_save_data_fail"));
		}
	}
	
	public void saveDataToAccount(Context context, Account account){
		
		if(mAccountManager == null){
			mAccountManager = AccountManager.get(context);
		}
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "saveDataToAccount regType : " + mUser.getRegtype());
		}
		String data = buildUserData(mUser);
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "saveDataToAccount data : " + data);
		}
		
		updateData(context, account , data);
	}
	
	private void updateData(Context context, Account account, String data){
		if(mAccountManager == null){
			mAccountManager = AccountManager.get(context);
		}
		mAccountManager.setUserData(account, KEY_PRIVATE_ACCOUNT, data);
		ShareInfoManager manager = ShareInfoManager.getInstance(context);
		
		//manager.publicToApps(mUser, account);
		manager.publicToApps(data, account);
	}
	
	public void updatePasswdval(Context context, String passwdval){
		String data = getInternalDataFromAccount(context);
		try {
			JSONObject jsonObject = new JSONObject(data);
			if(jsonObject.has(ShareInfoManager.SHARED_PWDVAL)){
			   jsonObject.put(ShareInfoManager.SHARED_PWDVAL, passwdval);
			   String updatedData = jsonObject.toString();
			   Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
			   if (accounts != null && accounts.length >= 1){
				    updateData(context, accounts[0], updatedData);
			   }
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateAvatarUrl(Context context, String vatarUrl){
		String data = getInternalDataFromAccount(context);
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "updateAvatarUrl data= " + data);
		}
		try {
			JSONObject jsonObject = new JSONObject(data);
			if(jsonObject.has(ShareInfoManager.SHARED_AVATAR)){
			   jsonObject.put(ShareInfoManager.SHARED_AVATAR, vatarUrl);
			   String updatedData = jsonObject.toString();
			   Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
			   if (accounts != null && accounts.length >= 1){
				    updateData(context, accounts[0], updatedData);
			   }
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateLocalUserInfo(Context context, JSONObject updatedJsonData){
		String data = getInternalDataFromAccount(context);
		
		try {
			JSONObject jsonObject = new JSONObject(data);
			updateJsonObj(jsonObject, updatedJsonData, ShareInfoManager.SHARED_AVATAR);
			updateJsonObj(jsonObject, updatedJsonData, ShareInfoManager.SHARED_NICKNAME);
			updateJsonObj(jsonObject, updatedJsonData, ShareInfoManager.SHARED_GENDER);
			updateJsonObj(jsonObject, updatedJsonData, ShareInfoManager.SHARED_BINDPHONE);
			updateJsonObj(jsonObject, updatedJsonData, ShareInfoManager.SHARED_BINDMAIL);
			updateJsonObj(jsonObject, updatedJsonData, ShareInfoManager.SHARED_PWDVAL);
			String updatedData = jsonObject.toString();
			Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
			if (accounts != null && accounts.length >= 1){
			   updateData(context, accounts[0], updatedData);
			}
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void updateJsonObj(JSONObject ori, JSONObject newObj, String key){
		if(ori.has(key) && newObj.has(key)){
			try {
				ori.put(key, newObj.getString(key));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean accountExits(Context context){
		if(mAccountManager == null){
			mAccountManager = AccountManager.get(context);
		}
		Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		if (accounts != null && accounts.length >= 1){
			return true;
		}else{
			return false;
		}
	}
	
	public String getInternalDataFromAccount(Context context){
		if(mAccountManager == null){
			mAccountManager = AccountManager.get(context);
		}
		String data = null;
		Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		if (accounts != null && accounts.length >= 1){
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "accounts :" + accounts);
			}
			data = getInternalDataFromAccount(context, accounts[0]);
		}else{
			if(DebugUtils.DEBUG){
				MyResource mMyResources = new MyResource(context);
				DebugUtils.i(TAG, context.getString(mMyResources.getString("lib_droi_account_get_data_fail")));
			}
			//Utils.showMessage(context, mMyResources.getString("lib_droi_account_get_data_fail);
		}
		
		return data;
	}
	
	private String getInternalDataFromAccount(Context context, Account account){
		if(mAccountManager == null){
			mAccountManager = AccountManager.get(context);
		}
		String data = mAccountManager.getUserData(account, KEY_PRIVATE_ACCOUNT);
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "getInternalDataFromAccount data = " + data);
		}
		return data;
	}
	/*
	public String getUserName(Context context){
		String userName = mUser.getName();
		
		return userName;
	}*/
	
	public String getPassword(Context context){
		
		String data = getInternalDataFromAccount(context);
		String password = null;
		if(!TextUtils.isEmpty(data)){
			try {
				JSONObject jsonObject = new JSONObject(data);
				password = jsonObject.getString("pwdMD5");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return password;
	}
	
	public String getOpenId(Context context){
		String data = getInternalDataFromAccount(context);
		String openId = null;
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "getOpenId getData: " + data);
		}
		if(!TextUtils.isEmpty(data)){
			try {
				JSONObject jsonObject = new JSONObject(data);
				openId = jsonObject.getString("openid");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return openId;
	}
	
	//for qq or weibo
	public String getUid(Context context){
		String data = getInternalDataFromAccount(context);
		String Uid = null;
		if(!TextUtils.isEmpty(data)){
			try {
				JSONObject jsonObject = new JSONObject(data);
				Uid = jsonObject.getString("uid");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Uid;
	}

	public String getBindPhone(Context context){
		String data = getInternalDataFromAccount(context);
		String bindPhone = null;
		if(!TextUtils.isEmpty(data)){
			try {
				JSONObject jsonObject = new JSONObject(data);
				bindPhone = jsonObject.getString("bindphone");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		return bindPhone;
	}
	
	public String getBindMail(Context context){
		String data = getInternalDataFromAccount(context);
		String bindMail = null;
		if(!TextUtils.isEmpty(data)){
			try {
				JSONObject jsonObject = new JSONObject(data);
				bindMail = jsonObject.getString("bindemail");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return bindMail;
	}
	
	public String getData(Context context){
		String accountData = getInternalDataFromAccount(context);
		String data = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				data = jsonObject.getString("data");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return data;
	}
	
	public String getPasswdVal(Context context){
		String accountData = getInternalDataFromAccount(context);
		String data = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				data = jsonObject.getString(ShareInfoManager.SHARED_PWDVAL);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return data;
	}
	
	public String getAccessToken(Context context){
		String accountData = getInternalDataFromAccount(context);
		String token = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				token = jsonObject.getString("token");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return token;
	}
	
	public String getRegType(Context context){
		String accountData = getInternalDataFromAccount(context);
		String regType = null;
		if(!TextUtils.isEmpty(accountData)){
			try {
				JSONObject jsonObject = new JSONObject(accountData);
				regType = jsonObject.getString("regtype");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return regType;
	}
	//end
	
	public User getData(){
		return mUser;
	}
	
	
	public void deleteAccount(final Activity activity, final String accountName, final boolean showToast){
		if(mAccountManager == null){
			mAccountManager = AccountManager.get(activity.getApplicationContext());
		}
		
		Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
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
						if(activity != null){
							activity.setResult(Activity.RESULT_CANCELED);
							activity.finish();
						}
					}else{
						if(activity != null){
							
							DroiSDKHelper manager = DroiSDKHelper.getInstance(activity);
							/*
							if(manager != null){
								manager.onAccountDeleted();
							}*/
							if(!TextUtils.isEmpty(accountName) && showToast){
								String toastMsg = activity.getApplicationContext().
										getResources().getString(MyResource.getString(activity.getApplicationContext(), "lib_droi_account_delete_account_toast"), accountName);
								//Utils.showMessage(activity, toastMsg);
							}
							activity.setResult(Activity.RESULT_OK);
							activity.finish();
						}
					}
				}
				
			}, null);
		}
	}
	
	private String buildUserData(User user){
		 String jsonStringer = null;
		 try {
			jsonStringer = new JSONStringer().object()
					 .key("username").value(TextUtils.isEmpty(user.getName()) ? "" : user.getName())
					 .key("pwdMD5").value(user.getPassword())
					 .key("openid").value(user.getOpenId())
					 //.key("gender").value(user.getGender())
					 //for qq or weibo begin
					 .key("uid").value(user.getUID()) 
					 .key("token").value(user.getToken())
					 .key("data").value(user.getData())
					 .key("regtype").value(user.getRegtype())
					 .key(ShareInfoManager.SHARED_PWDVAL).value(user.getPasswdVal())
                     .key("expire").value(user.getExpires())
                     //for qq or weibo end
                     .key("recode").value(TextUtils.isEmpty(user.getRecode() + "") ? 100 : user.getRecode())
					 .key("nickname").value(user.getNickName())
					 .key("bindphone").value(user.getBindPhone())
					 .key("bindemail").value(user.getBindEmail())
					 .key("avatar").value(AvatarUtils.getAvatarPath())
					 .endObject().toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonStringer;
	}
	
}
