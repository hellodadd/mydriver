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

public class OtherUserLoginTask extends AsyncTask<Void, Void, String> {

	 private static final String TAG = "OtherUserLoginTask";
	 private static final boolean DEBUG = Utils.DEBUG;
	 
	 private User mUser;
	 private Context mContext;
	 private String mData;
	 private String mUtype;
	 private Callback mCallback;
	 private String loginResult;
	 protected MyResource mMyResources = null;
	 
	 public OtherUserLoginTask(Context context, String data, String type, 
			 User user, Callback callback){
		 mContext = context;
		 mUser = user;
		 mData = data;
		 mUser.setData(data);
		 mUtype = type;
		 mCallback = callback;
		 mMyResources = new MyResource(context);
	 }
	
	@Override
	protected String doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		final Map<String, String> loginParams = new HashMap<String, String>();
		String signString = MD5Util.md5(mUser.getUID() + mUser.getToken() + mUtype + mData + " " + Constants.SIGNKEY);
        loginParams.put("uid", mUser.getUID());
        loginParams.put("passwd", mUser.getToken());
        loginParams.put("utype", mUtype);
        loginParams.put("data", mData);
        loginParams.put("sign", signString);
        loginParams.put("devinfo", " ");
        String loginfo = "";
		StaticsCallback staticsCallback = StaticsCallback.getInstance(mContext);
		if(Constants.UTYPE_QQ.equals(mUtype)){
			loginfo = staticsCallback.encryptLogingInLoginfo(mUser.getUID(), mUser, AccountOperation.ACCOUNT_LOGIN_TYPE_QQ);
		}else if(Constants.UTYPE_WEIBO.equals(mUtype)){
			loginfo = staticsCallback.encryptLogingInLoginfo(mUser.getUID(), mUser, AccountOperation.ACCOUNT_LOGIN_TYPE_WEIBO);
		}
		
		loginParams.put("loginfo", loginfo);
        loginResult = HttpOperation.postRequest(Constants.AUTH, loginParams);
        if(DebugUtils.DEBUG){
        	DebugUtils.i(TAG, "uid = " + mUser.getUID() +", data = " + mData);
        	DebugUtils.i(TAG, "return = " + loginResult);
        }
        try{
        	JSONObject jsonObject = new JSONObject(loginResult);
        	int result = jsonObject.has("result") ? jsonObject.getInt("result") : -1;
        	
        	if (result == 0){
				String accountName = jsonObject.has("fs_name") ? jsonObject.getString("fs_name") : null;
				
				if(TextUtils.isEmpty(accountName)){
					//bind phone 
					accountName = jsonObject.has("username")?jsonObject.getString("username"): null;
				}
				String nickName = jsonObject.has("nickname")?jsonObject.getString("nickname"): null;
				if(TextUtils.isEmpty(accountName)){
					accountName = jsonObject.has("nickname")?jsonObject.getString("nickname"):
	        			TextUtils.isEmpty(mUser.getName())?mUtype:mUser.getName();
				}
				
				mUser.setName(accountName);
				mUser.setNickName(nickName);
        		mUser.setToken(jsonObject.has("token")?jsonObject.getString("token"):null);
        		mUser.setOpenId(jsonObject.has("openid")?jsonObject.getString("openid"):null);
        		mUser.setRecode(jsonObject.has("score")?jsonObject.getInt("score"):0);
        		mUser.setGender(jsonObject.has("gender")?jsonObject.getString("gender"):null);
        		mUser.setBindPhone(jsonObject.has("username")?jsonObject.getString("username"):null);
        		mUser.setBindEmail(jsonObject.has("mail")?jsonObject.getString("mail"):null);
        		//mUser.setLogoUrl(jsonObject.has("avatar") ? jsonObject.getString("avatar") : null);
        		//判断第三方登陆的使用是否绑定了账户，是否设置了密码
        		mUser.setPasswdVal(jsonObject.has("passwdval")?jsonObject.getInt("passwdval"):-1);
        		mUser.setExpires(jsonObject.has("expire")?jsonObject.getString("expire"):"");
                //if(TextUtils.isEmpty(mUser.getLogoUrl())){
                //	deleteUserLogo();
                // }else{
                	//AuthenticatorActivity.downloadUserLogo(mUser.getLogoUrl());
                // }
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
	            
        	}else if(result == -2010){
        		//user account is locked
        		String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
            		mContext.getResources().getString(mMyResources.getString("lib_droi_account_login_fail"));
               	if(DebugUtils.DEBUG){
               		DebugUtils.i(TAG, desc);
               	}
        	}else{
        		String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
            		mContext.getResources().getString(mMyResources.getString("lib_droi_account_login_fail"));
               	if(DebugUtils.DEBUG){
               		DebugUtils.i(TAG, desc);
               	}
        	}
        	mUser.setRegtype(mUtype);
        	mUser.setResult(result);
        	mUser.setDesc(jsonObject.has("desc")?jsonObject.getString("desc"):null);
        }catch (Exception e){
            e.printStackTrace();
        }
		return loginResult;
	}
	
	@Override
    protected void onPostExecute(final String result){
		
		if(!TextUtils.isEmpty(result)){
			try {
				JSONObject jsonObject = new JSONObject(result);
				int rs = jsonObject.has("result") ? jsonObject.getInt("result") : -1;
				if(rs != 0){
	        		String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
	            		mContext.getResources().getString(mMyResources.getString("lib_droi_account_login_fail"));
	        		if(!TextUtils.isEmpty(desc)){
	        			Utils.showMessage(mContext, desc);
	        		}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(mCallback != null){
    		mCallback.run();
    	}
	}
	
	@Override
    protected void onCancelled(){
		if(mCallback != null){
    		mCallback.onCancelled();
    	}
	}

	public interface Callback{
		public void run();
		public void onCancelled();
	}
	
}
