package com.droi.account.login;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.droi.account.DebugUtils;
import com.droi.account.MyResource;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;
import com.droi.account.statis.AccountOperation;
import com.droi.account.statis.StaticsCallback;

public class UserLoginTask extends AsyncTask<Void, Void, String>{

	 private static final String TAG = "UserLoginTask";
	 private String mUserName;
	 private String mPassword;
	 private Runnable mCallback;
	 private User mUser;
	 private Context mContext;
	 private String mUtype = "zhuoyou";
	 protected MyResource mMyResources = null;
	 private int mAccountType;
	 
	 public UserLoginTask(Context context, String userName, String password, 
			 User user, Runnable callback){
		 mUserName = userName;
		 mPassword = password;
		 mCallback = callback;
		 mUser = user;
		 mMyResources = new MyResource(context);
		 mContext = context;
		 mUtype = Utils.getAccountUtype(mUserName);
		 mAccountType = Utils.getAccountType(mUserName);
	 }
	 
	@Override
	protected String doInBackground(Void... params) {
		// TODO Auto-generated method stub
		String passwdMD5 = MD5Util.md5(mPassword);
		final Map<String, String> loginParams = new HashMap<String, String>();
		loginParams.put(Constants.JSON_S_UID, mUserName);
		loginParams.put(Constants.JSON_S_PWD, passwdMD5);
		loginParams.put("utype", mUtype);
		loginParams.put("devinfo", " ");
		StaticsCallback staticsCallback = StaticsCallback.getInstance(mContext);
		String loginfo = "";
		if(mAccountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
			loginfo = staticsCallback.encryptLogingInLoginfo(mUserName, mUser, AccountOperation.ACCOUNT_LOGIN_TYPE_PHONE);
		}else if(mAccountType == Constants.ACCOUNT_TYPE_EMAIL){
			loginfo = staticsCallback.encryptLogingInLoginfo(mUserName, mUser, AccountOperation.ACCOUNT_LOGIN_TYPE_MAIL);
		}
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "loginfo : " + loginfo);
		}
		loginParams.put("loginfo", loginfo);
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "user login username = " + mUserName);
		}
		loginParams.put("sign", MD5Util.md5(mUserName+passwdMD5 + mUtype + " "+Constants.SIGNKEY));
		String loginResult = null;
		try{
			loginResult = HttpOperation.postRequest(Constants.ACCOUNT_LOGIN, loginParams);
			JSONObject jsonObject = new JSONObject(loginResult);
			int result = jsonObject.getInt("result");
			if(result == 0){
				String accountName = jsonObject.has("fs_name") ? jsonObject.getString("fs_name") : null;
				if(TextUtils.isEmpty(accountName)){
					accountName = jsonObject.has("username")?jsonObject.getString("username"): mUserName;
				}
				String nickName = jsonObject.has("nickname")?jsonObject.getString("nickname"): null;
				if(TextUtils.isEmpty(accountName)){
					accountName = jsonObject.has("nickname")?jsonObject.getString("nickname"): mUserName;
				}
				mUser.setName(accountName);
				mUser.setNickName(nickName);
        		mUser.setBindPhone(jsonObject.has("username")?jsonObject.getString("username"):null);
        		mUser.setBindEmail(jsonObject.has("mail")?jsonObject.getString("mail"):null);
				mUser.setPassword(passwdMD5);
				mUser.setToken(jsonObject.getString("token"));
				mUser.setUID(jsonObject.has("uid")?jsonObject.getString("uid"): mUserName);
				mUser.setOpenId(jsonObject.getString("openid"));
				mUser.setGender(jsonObject.getString("gender"));
				mUser.setPasswdVal(jsonObject.has("passwdval") ? jsonObject.getInt("passwdval") : -1);
				mUser.setExpires(jsonObject.has("expire")?jsonObject.getString("expire"):"");
				
	            String avatarStr = Constants.AVATAR_USER_SPECIFY;//Constants.AVATAR_URL_QQ_WB_DEFAULT;
	            String avatarUrl = jsonObject.has(avatarStr) ? jsonObject.getString(avatarStr) : null;
	            //supprt wb-qq selfdef avatar
	            if(TextUtils.isEmpty(avatarUrl)){
	            	avatarUrl = jsonObject.has(Constants.AVATAR_URL_QQ_WB_DEFAULT) ? 
	            			jsonObject.getString(Constants.AVATAR_URL_QQ_WB_DEFAULT) : null;
	            }
	            
	            mUser.setLogoUrl(avatarUrl);
	            if(TextUtils.isEmpty(avatarUrl)){
	            	AvatarUtils.deleteUserAvatar();
	            }else{
	            	AvatarUtils.downloadUserAvatar(avatarUrl);
	            }
			}
			final int accountType = Utils.getAccountType(mUserName);
			if (accountType == Constants.ACCOUNT_TYPE_PHONENUMBER) {
				mUser.setRegtype(Constants.UTYPE_PHONE);
			} else if (accountType == Constants.ACCOUNT_TYPE_EMAIL) {
				mUser.setRegtype(Constants.UTYPE_MAIL);
			}
			mUser.setResult(result);
			mUser.setDesc(jsonObject.getString("desc"));
		}catch(Exception e){
			mUser.setResult(-1);
			e.printStackTrace();
		}
		
		return loginResult;
	}
	
    @Override
    protected void onPostExecute(String result){
    	super.onPostExecute(result);
    	if(mCallback != null){
    		mCallback.run();
    	}
    	if(DebugUtils.DEBUG){
    		DebugUtils.i(TAG, "login result : " + result);
    	}
    	if (!TextUtils.isEmpty(result)){
    		 try {
				JSONObject jsonObject = new JSONObject(result);
				int rs = jsonObject.getInt("result");
				if(rs != 0){
                	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                		mContext.getResources().getString(mMyResources.getString("lib_droi_account_login_fail"));
                	
                	Utils.showMessage(mContext, desc);
				}else{
					//Utils.showMessage(mContext, mMyResources.getString("lib_droi_account_login_success"));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}else{
    		Utils.showMessage(mContext, mMyResources.getString("lib_droi_account_login_fail"));
    	}
    }
    
    @Override
    protected void onCancelled(){
    	if(mCallback != null){
    		mCallback.run();
    	}
    }

}
