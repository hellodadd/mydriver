package com.droi.account.shared;

import android.content.Intent;

public interface AccountInterface {
	
	public void init(String tencentAppId, String sinaAppId); // 填写新浪appkey,需要到新浪中注册

	public void login(int type); // 登陆

	public boolean checkAccount();// 检测帐户是否存在

	public String getUserName();// 帐户名或昵称

	public String getBindPhone();// 绑定手机

	public String getOpenId();// 账号id

	public String getToken();// 令牌

	public String getExpire();// 登陆有效期

	public String getUid();

	public void tokenInvalidate(); // token 过期时调用，会调起登陆界面

//	public void changeAccount();// 更换帐号

	public String getAvatarUrl(); // 获取头像文件本地路径

	public String getNickName();// 过去昵称

	public String getAccountType();
	
	public Intent getSettingsIntent(String appName);
	
//	public void setEmailLogin(boolean enable);
//	public void setPhoneLogin(boolean enable);
//	public void setVersionForeign(boolean enable);

}
