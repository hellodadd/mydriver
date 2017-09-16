package com.droi.account.login;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.droi.account.DebugUtils;
import com.droi.account.MyResource;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.statis.AccountOperation;
import com.droi.account.statis.StaticsCallback;

public class DroiThirdPartyLoginTask extends AsyncTask<Void, Void, String> {

	 private static final String TAG = "DroiQQLoginTask";
	 private User mUser;
	 private Context mContext;
	 private String mToken;
	 private String mOpenId;
	 private Callback mCallback;
	 private String loginResult;
	 private final String mUrl;
	 private MyResource mMyResources;
	 
	 public DroiThirdPartyLoginTask(Context context, String url,String token, String openId, User user, Callback callback){
		 mUrl = url;
		 mContext = context;
		 mToken = token;
		 mOpenId = openId;
		 mUser = user;
		 mCallback = callback;
		 mMyResources = new MyResource(context);
	 }
	
	@Override
	protected String doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		final Map<String, String> loginParams = new HashMap<String, String>();
		String signString = "";//MD5Util.md5(mUser.getUID() + mUser.getToken() + mUtype + mData + " " + Constants.SIGNKEY);
        loginParams.put("openid", mOpenId);
        loginParams.put("token", mToken);
        loginParams.put("sign", signString);
        if(Constants.DROI_WECHAT_LOGIN.equals(mUrl)){
        	loginParams.put("access_token", mToken);
        }
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "doInBackground mOpenId = " + mOpenId + ", mToken = " + mToken);
		}
		
		String loginfo = "";
		StaticsCallback staticsCallback = StaticsCallback.getInstance(mContext);
		//for now the droi third party auth is only for QQ
		loginfo = staticsCallback.encryptLogingInLoginfo(mUser.getUID(), mUser, AccountOperation.ACCOUNT_LOGIN_TYPE_QQ);
		loginParams.put("loginfo", loginfo);
        loginResult = HttpOperation.postRequest(mUrl, loginParams);
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "doInBackground loginParams, loginResult = " + loginParams + "   " + loginResult);
		}
        try{
        	JSONObject jsonObject = new JSONObject(loginResult);
        	int result = jsonObject.has("result") ? jsonObject.getInt("result") : -1;
        	
        	if (result == 0){
        		if(jsonObject.has("userInfo")){
        			String userinfo = jsonObject.getString("userInfo");
        			jsonObject = new JSONObject(userinfo);
        		}
        		
				String accountName = jsonObject.has("fs_name") ? jsonObject.getString("fs_name") : "";
				if(DebugUtils.DEBUG){
					DebugUtils.i(TAG, "doInBackground fs_name = " + accountName);
				}
				if(TextUtils.isEmpty(accountName)){
					//bind phone 
					accountName = jsonObject.has("username")?jsonObject.getString("username"): "";
				}
				
				if(DebugUtils.DEBUG){
					DebugUtils.i(TAG, "doInBackground username = " + accountName);
				}
				String nickName = jsonObject.has("nickname")?jsonObject.getString("nickname"): "";
				if(DebugUtils.DEBUG){
					DebugUtils.i(TAG, "doInBackground nickName = " + nickName);
				}
				if(TextUtils.isEmpty(accountName)){
					accountName = jsonObject.has("nickname")?jsonObject.getString("nickname") : "";
				}
				if(DebugUtils.DEBUG){
					DebugUtils.i(TAG, "doInBackground accountName = " + accountName);
				}
				mUser.setName(accountName);
				mUser.setNickName(nickName);
        		mUser.setToken(jsonObject.has("token")?jsonObject.getString("token"):"");
        		mUser.setOpenId(jsonObject.has("openid")?jsonObject.getString("openid"):"");
        		mUser.setRecode(jsonObject.has("score")?jsonObject.getInt("score"):0);
        		mUser.setGender(jsonObject.has("gender")?jsonObject.getString("gender"):"");
        		mUser.setBindPhone(jsonObject.has("username")?jsonObject.getString("username"):"");
        		mUser.setBindEmail(jsonObject.has("mail")?jsonObject.getString("mail"):"");
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
            		mContext.getString(mMyResources.getString("lib_droi_account_login_fail"));
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
        	mUser.setRegtype(Constants.UTYPE_QQ);
        	mUser.setResult(result);
        	mUser.setDesc(jsonObject.has("desc")?jsonObject.getString("desc"):null);
        }catch (Exception e){
            e.printStackTrace();
        }
		return loginResult;
	}
	
	@Override
    protected void onPostExecute(final String result){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "result = " + result);
		}
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
