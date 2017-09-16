package com.droi.account.login;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.os.AsyncTask;

import com.droi.account.DebugUtils;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;

public class CheckPwdTask extends AsyncTask<Void, Void, String> {

	private static final String TAG = "CheckPwdTask";
	private CheckPwdListener mCallback;
	private String mOpenId;
	private String mToken;
	private String mPasswd;
	private boolean mResult = false;
	
	public CheckPwdTask(String openId, String token, String pwd, CheckPwdListener callback){
		mCallback = callback;
		mOpenId = openId;
		mToken = token;
		mPasswd = pwd;
	}

	@Override
	protected String doInBackground(Void... params) {
		// TODO Auto-generated method stub
		final Map<String, String> loginParams = new HashMap<String, String>();
		loginParams.put(Constants.JSON_S_OPENID, mOpenId);
		loginParams.put(Constants.JSON_S_PWD, mPasswd);
		loginParams.put(Constants.JSON_S_TOKEN, mToken);
		loginParams.put("sign", MD5Util.md5(mOpenId+mToken + mPasswd +Constants.SIGNKEY));
		
		String loginResult = null;
		try{
			loginResult = HttpOperation.postRequest(Constants.ACCOUNT_CHECK_PWD, loginParams);
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "result : " + loginResult);
			}
			JSONObject jsonObject = new JSONObject(loginResult);
			int result = jsonObject.getInt("result");
			if(result == 0){
				mResult = true;
			}else{
				mResult = false;
			}

		}catch(Exception e){
			e.printStackTrace();
			mResult = false;
		}
		
		return loginResult;
	}
	
	@Override
    protected void onPostExecute(String result){
    	super.onPostExecute(result);
    	mCallback.onResult(mResult);
    	/*
    	if (!TextUtils.isEmpty(result)){
    		 try {
				JSONObject jsonObject = new JSONObject(result);
				int rs = jsonObject.getInt("result");
				if(DebugUtils.DEBUG){
					DebugUtils.i(TAG, "result : " + result);
				}
				if(rs == 0){
					mCallback.onResult(true);
				}else{
					mCallback.onResult(false);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mCallback.onResult(false);
			}
    	}else{
    		mCallback.onResult(false);
    	}
    	*/
    }
    
    @Override
    protected void onCancelled(){
    	mCallback.onResult(mResult);
    }
}
