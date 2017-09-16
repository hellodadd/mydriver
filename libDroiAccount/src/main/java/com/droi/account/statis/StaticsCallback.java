package com.droi.account.statis;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.droi.account.DebugUtils;
import com.droi.account.authenticator.Constants;
import com.droi.account.login.SharedInfo;
import com.droi.account.login.User;
import com.zhuoyi.appStatistics.entry.ZYAgent;

public class StaticsCallback {

	private static final String TAG = "Statics";
	
	private static final String APP_iD = "manual000001";
	private static final String CHANNEL_ID = "zyzha0002";
	private static StaticsCallback sInstance = null;
	private Context mContext;
	
	private StaticsCallback(Context application) {
		// TODO Auto-generated constructor stub
		mContext = application.getApplicationContext();
		try{
			ZYAgent.init(mContext, APP_iD,CHANNEL_ID);
		}catch(Exception e){
			DebugUtils.e(TAG, "ZYAgent init failed : " + e);
		}
	}
	
	public synchronized static StaticsCallback getInstance(Context context){
		if(sInstance == null){
			sInstance = new StaticsCallback(context);
		}
		return sInstance;
	}

	public String encryptRegisteringLogInfo(String accountName, int accountType){
		String rawData = "";
		JSONObject data = StaticsUtils.buildRegisterStatics(mContext, accountName, accountType, "", false);
		StaticsUtils.addCommonStaticsInfo(mContext, data);
		rawData = data.toString();
		rawData = StaticsUtils.encryptLoginfo(rawData);
		return rawData;
	}
	
	public String encryptLogingInLoginfo(String accountName, User user, int loginType){
		JSONObject data = buildLoginData(accountName, user, loginType);
		StaticsUtils.addCommonStaticsInfo(mContext, data);
		String rawData = data.toString();
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "entrypt login rawData info : " + rawData);
		}
		rawData = StaticsUtils.encryptLoginfo(rawData);
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "entrypt login info : " + rawData);
		}
		return rawData;
	}
	
	public String encryptBindingLoginfo(String bindTo){
		JSONObject data = buildBindData(bindTo);
		StaticsUtils.addCommonStaticsInfo(mContext, data);
		String rawData = data.toString();
		rawData = StaticsUtils.encryptLoginfo(rawData);
		return rawData;
	}
	
	public void onRegisterResult(String accountName, int accountType, String openId, boolean success){
		JSONObject data = StaticsUtils.buildRegisterStatics(mContext, accountName, accountType, openId, success);
		try{
			sendInfo(data);
		}catch(Exception e){
			DebugUtils.e(TAG, "sendinfo failed after register : " + e);
		}
	}
	
	public void onLoginResult(String accountName, User user, int loginType){
		JSONObject data = buildLoginData(accountName, user, loginType);
		try{
			sendInfo(data);
		}catch(Exception e){
			DebugUtils.e(TAG, "sendinfo failed after login : " + e);
		}
	}
	
	public void onBindResult(String bindTo){
		JSONObject data = buildBindData(bindTo);
		try{
			sendInfo(data);
		}catch(Exception e){
			DebugUtils.e(TAG, "sendinfo failed after bind : " + e);
		}
	}
	
	private JSONObject buildLoginData(String accountName, User user, int loginType){
		String bindPhone = user.getBindPhone();
		String bindMail = user.getBindEmail();
		JSONObject data = null;
		if(loginType == AccountOperation.ACCOUNT_LOGIN_TYPE_PHONE){
			if(TextUtils.isEmpty(bindMail)){
				data = buildLoginStatics(accountName, user.getOpenId(), 
						loginType, 0, AccountOperation.ACCOUNT_BINDED_PHONE);
			}else{
				data = buildLoginStatics(accountName, user.getOpenId(), 
						loginType, 0, AccountOperation.ACCOUNT_BINDED_PHONE_MAIL);
			}
		}else if(loginType == AccountOperation.ACCOUNT_LOGIN_TYPE_MAIL){
			if(TextUtils.isEmpty(bindPhone)){
				data = buildLoginStatics(accountName, user.getOpenId(), 
						loginType, 0, AccountOperation.ACCOUNT_BINDED_MAIL);
			}else{
				data = buildLoginStatics(accountName, user.getOpenId(), 
						loginType, 0, AccountOperation.ACCOUNT_BINDED_PHONE_MAIL);
			}
		}else if(!TextUtils.isEmpty(bindPhone) && !TextUtils.isEmpty(bindMail)){
			data = buildLoginStatics(user.getName(), user.getOpenId(), 
					loginType, 0, AccountOperation.ACCOUNT_BINDED_PHONE_MAIL);
		}else if(!TextUtils.isEmpty(bindMail)){
			data = buildLoginStatics(user.getName(), user.getOpenId(), 
					loginType, 0, AccountOperation.ACCOUNT_BINDED_MAIL);
		}else if(!TextUtils.isEmpty(bindPhone)){
			data = buildLoginStatics(user.getName(), user.getOpenId(), 
					loginType, 0, AccountOperation.ACCOUNT_BINDED_PHONE);
		}else{
			data = buildLoginStatics(user.getName(), user.getOpenId(), 
					loginType, 0, AccountOperation.ACCOUNT_BINDED_NONE);
		}
		
		return data;
	}
	
	private JSONObject buildLoginStatics(String accountName, String openId, int loginType, int flag, int mark){
		JSONObject data = StaticsUtils.buildLoginStatics(mContext, accountName, openId, loginType, flag, mark);
		return data;
		
	}
	
	private JSONObject buildBindData(String bindTo){
		SharedInfo sharedInfo = SharedInfo.getInstance();
		String accountName = sharedInfo.getData().getName();
		String openId = sharedInfo.getData().getOpenId();
		String utype = sharedInfo.getData().getRegtype();
		int loginType = -1;
		int bindType = -1;
		
		if(Constants.UTYPE_QQ.equals(utype)){
			loginType = AccountOperation.ACCOUNT_LOGIN_TYPE_QQ;
			if(Constants.BIND_TO_PHONE.equals(bindTo)){
				bindType = AccountOperation.ACCOUNT_QQ_BIND_PHONE;
			}else if(Constants.BIND_TO_MAIL.equals(bindTo)){
				bindType = AccountOperation.ACCOUNT_QQ_BIND_MAIL;
			}
			
		}else if(Constants.UTYPE_WEIBO.equals(utype)){
			loginType = AccountOperation.ACCOUNT_LOGIN_TYPE_WEIBO;
			if(Constants.BIND_TO_PHONE.equals(bindTo)){
				bindType = AccountOperation.ACCOUNT_WEIBO_BIND_PHONE;
			}else if(Constants.BIND_TO_MAIL.equals(bindTo)){
				bindType = AccountOperation.ACCOUNT_WEIBO_BIND_MAIL;
			}
			
		}else if(Constants.UTYPE_PHONE.equals(utype)){
			loginType = AccountOperation.ACCOUNT_LOGIN_TYPE_PHONE;
			if(Constants.BIND_TO_MAIL.equals(bindTo)){
				bindType = AccountOperation.ACCOUNT_PHONE_BIND_MAIL;
			}
		}else if(Constants.UTYPE_MAIL.equals(utype)){
			loginType = AccountOperation.ACCOUNT_LOGIN_TYPE_MAIL;
			if(Constants.BIND_TO_PHONE.equals(bindTo)){
				bindType = AccountOperation.ACCOUNT_MAIL_BIND_PHONE;
			}
		}
		
		JSONObject data = StaticsUtils.buildBindStatics(mContext, accountName, openId, loginType, bindType);
		return data;
	}
	
	private void sendInfo(JSONObject msg){
		ZYAgent.sendInfo(mContext, msg);
	}
}