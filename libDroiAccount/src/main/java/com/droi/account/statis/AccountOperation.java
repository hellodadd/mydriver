package com.droi.account.statis;


public class AccountOperation {

	//注册操作
	public static final int ACCOUNT_REGISTER = 1;
	
	//登陆操作
	public static final int ACCOUNT_LOGIN = 2;
	
	//绑定操作
	public static final int ACCOUNT_BIND = 3;
	
	//来源，记录该操作的数据，来自帐号框架apk
	public static final int ACCOUNT_DATA_FROM_APK = 0;
	
	//来源，记录该操作的数据，来自帐号框架sdk
	public static final int ACCOUNT_DATA_FROM_SDK = 1;
	
	//手机注册
	public static final int ACCOUNT_REGISTER_TYPE_PHONE = 0;
	
	//邮箱注册
	public static final int ACCOUNT_REGISTER_TYPE_MAIL = 1;
	
	//通过qq转换的注册
	public static final int ACCOUNT_REGISTER_TYPE_QQ = 2;
	
	//通过微博转换的注册
	public static final int ACCOUNT_REGISTER_TYPE_WEIBO = 3;
	
	//通过微信转换的注册
	public static final int ACCOUNT_REGISTER_TYPE_WECHAT = 4;
	
	//登陆操作相关参数
	public static final int ACCOUNT_LOGIN_TYPE_PHONE = 0;
	public static final int ACCOUNT_LOGIN_TYPE_MAIL = 1;
	public static final int ACCOUNT_LOGIN_TYPE_QQ = 2;
	public static final int ACCOUNT_LOGIN_TYPE_WEIBO = 3;
	public static final int ACCOUNT_LOGIN_TYPE_VISITOR = 4;
	public static final int ACCOUNT_LOGIN_TYPE_WECHAT = 5;
	
	//自主登陆
	public static final int ACCOUNT_LOGIN_FLAG_AUTO = 0;
	//表示从框架中获取信息
	public static final int ACCOUNT_LOGIN_SHARE_INFO = 1;
	
	//帐号没有绑定任何信息
	public static final int ACCOUNT_BINDED_NONE = 0;
	
	//帐号已绑定手机
	public static final int ACCOUNT_BINDED_PHONE = 1;
	
	//帐号已绑定邮箱
	public static final int ACCOUNT_BINDED_MAIL = 2;
	
	//帐号绑定手机和邮箱
	public static final int ACCOUNT_BINDED_PHONE_MAIL = 3;
	
	//QQ绑定手机关系
	public static final int ACCOUNT_QQ_BIND_PHONE = 0;
	//QQ绑定邮箱
	public static final int ACCOUNT_QQ_BIND_MAIL = 1;
	
	//微博绑定手机
	public static final int ACCOUNT_WEIBO_BIND_PHONE = 2;
	//微博绑定邮箱
	public static final int ACCOUNT_WEIBO_BIND_MAIL = 3;
	//手机绑定QQ
	public static final int ACCOUNT_PHONE_BIND_QQ = 4;
	//手机绑定微博
	public static final int ACCOUNT_PHONE_BIND_WEIBO = 5;
	
	public static final int ACCOUNT_MAIL_BIND_QQ = 6;
	public static final int ACCOUNT_MAIL_BIND_WEIBO = 7;
	
	public static final int ACCOUNT_WECHAT_BIND_PHONE = 8;
	public static final int ACCOUNT_WECHAT_BIND_MAIL = 9;
	
	public static final int ACCOUNT_PHONE_BIND_WECHAT = 10;
	public static final int ACCOUNT_EMAIL_BIND_WECHAT = 11;
	
	public static final int ACCOUNT_PHONE_BIND_MAIL = 12;
	public static final int ACCOUNT_MAIL_BIND_PHONE = 13;
	
}
