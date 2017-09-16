package com.droi.account.login;

import android.util.Log;

import com.droi.account.DebugUtils;
import com.droi.account.Utils;

public class User {

	private static final boolean DEBUG = Utils.DEBUG;
	private static final String TAG = "User";
	
	private String mUserName = "";
	private String mNickName = "";
	private String mPassword = "";
	private String mToken = "";
	private String mUID = "";
	private String mDesc = "";
	private String mOpenKey = "";
	private String mOpenId;
	private int mResult = -1;
	private String mGender = "";
	private String mBindPhone = "";
	private String mBindEmail = "";
	private String mExPires = "0000000";
	private String mLogoUrl = "";
	private String mRegtype = "";
	private int mRecode = 0;
	private String mData = "";
	private int mPasswdVal = -1;
	
	private String mActiveMail;
	
	public User(){
		
	}
	
	public User(String userName, String password){
		mUserName = userName;
		mPassword = password; 
	}
	
	//判断用户是否为该账户设置了密码
	public void setPasswdVal(int value){
		mPasswdVal = value;
	}
	public int getPasswdVal(){
		return mPasswdVal;
	}
	
	public void setData(String data){
		mData = data;
	}
	
	public String getData(){
		return mData;
	}
	
    public int getRecode() {
        return mRecode;
    }

    public void setRecode(int recode){
        mRecode = recode;
    }
	
    public void setActiveMail(String mail){
    	mActiveMail = mail;
    }
    
    public String getActiveMail(){
    	return mActiveMail;
    }
    
    public String getRegtype(){
        return mRegtype;
    }

    public void setRegtype(String regtype) {
    	if(DEBUG){
    		Log.i(TAG, "setRegtype : " + regtype);
    	}
        mRegtype = regtype;
    }
	
    public String getLogoUrl(){
        return mLogoUrl;
    }

    public void setLogoUrl(String logoUrl){
        mLogoUrl = logoUrl;
    }
	
    public String getOpenKey(){
        return mOpenKey;
    }

    public void setOpenKey(String openKey){
        mOpenKey = openKey;
    }
	
	public void setToken(String token){
		mToken = token;
	}
	
	public void setExpires(String expoires){
		mExPires = expoires;
	}
	
	public String getExpires(){
		return mExPires;
	}
	
	public void setGender(String gender){
		mGender = gender;
	}
	
	public String getGender(){
		return mGender;
	}
	
	public void setNickName(String nickName){
		mNickName = nickName;
	}
	
	public String getNickName(){
		return mNickName;
	}
	
	public void setBindPhone(String phone){
		mBindPhone = phone;
	}
	
	public String getBindPhone(){
		return mBindPhone;
	}
	
	public void setBindEmail(String email){
		mBindEmail = email;
	}
	
	public String getBindEmail(){
		return mBindEmail;
	}
	
	public String getToken(){
		return mToken;
	}
	
	public void setUID(String uid){
		mUID = uid;
	}
	
	public String getUID(){
		return mUID;
	}
	
	public void setOpenId(String openId){
		mOpenId = openId;
	}
	
	public String getOpenId(){
		return mOpenId;
	}
	
	public void setName(String name){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "setName : " + name);
		}
		mUserName = name;
	}
	
	public String getName(){
		return mUserName;
	}
	
	public void setPassword(String pwd){
		mPassword = pwd;
	}
	
	public String getPassword(){
		return mPassword;
	}
	
	public void setDesc(String desc){
		mDesc = desc;
	}
	
	public String getDesc(){
		return mDesc;
	}
	
	public void setResult(int result){
		mResult = result;
	}
	
	public int getResult(){
		return mResult;
	}
	
	
}
