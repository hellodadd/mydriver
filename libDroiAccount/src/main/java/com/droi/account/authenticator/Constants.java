package com.droi.account.authenticator;

import com.droi.account.shared.DroiSDKHelper;

public class Constants {
    /**
     * Account type string.
     */
    public static String ACCOUNT_TYPE = DroiSDKHelper.ACCOUNT_TYPE;
    public static final String AVATAR_URL_S = "avatar";//avatarurl
    public static final String AVATAR_URL_QQ_WB_DEFAULT = "avatar";
    public static final String AVATAR_USER_SPECIFY = "avatarurl";
    /**
     * Authtoken type string.
     */
    public static final String AUTHTOKEN_TYPE = "com.freeme.account.android.samplesync";
    
    public static final String TEST_BASE_URL = "http://testlapi.tt286.com:7892";
    public static final String OVERSEAS_URL = "http://service-account.dd351.com:7890";
    public static final String NORMAL_URL = "http://lapi.tt286.com:7892";
    
    public static final String TEST_AVATAR_URL_S = "avatarurl";
    private static final String TEST_BASE_URL_2 = "http://192.168.0.254:7892";
    public static final String BASE_URL = NORMAL_URL; //TEST_BASE_URL;//"http://lapi.tt286.com:7892";// OVERSEAS_URL
    
    public static final String ACCOUNT_REGISTER = BASE_URL + "/lapi/getrandcode";
    /**
     * sign up
     */
    public static final String DROI_AUTH_QQ = BASE_URL + "/lapi/qq_login";
    
    public static final String DROI_AUTH_WECHAT = BASE_URL + "/lapi/webchat_login";
    
    public static final String DROI_QQ_LOGIN = BASE_URL + "/lapi/qq_user_info";
    
    public static final String DROI_WECHAT_LOGIN = BASE_URL + "/lapi/webchat_getuserinfo";
    
    public static final String ACCOUNT_REGISTER_SIGNUP = BASE_URL + "/lapi/signup";
    
    public static final String ACCOUNT_MODIFY_PWD = BASE_URL + "/lapi/changepass";
    
    public static final String ACCOUNT_DELETE = BASE_URL + "/lapi/deleteuser";
    
    public static final String ACCOUNT_RESET_PWD = BASE_URL + "/lapi/resetpass";
    
    public static final String ACCOUNT_REGISTER_BY_EMAIL = BASE_URL + "/lapi/getmail";//"/lapi/mailsignup";
    
    public static final String ACCOUNT_RESEND_MAIL = BASE_URL + "/lapi/getmail";;
    
    public static final String ACCOUNT_FIND_PWD_BY_EMAIL = ACCOUNT_REGISTER_BY_EMAIL;
    
    public static final String AUTH = BASE_URL + "/lapi/auth";
    
    public static final String ACCOUNT_BIND_MOBILE = BASE_URL + "/lapi/bindmobile";
    
    public static final String ACCOUNT_BIND_EMAIL = BASE_URL + "/lapi/getmail";//"/lapi/bindmail";
    
    public static final String ACCOUNT_GET_USER_INFO = BASE_URL + "/lapi/userinfo";
    
    public static final String ACCOUNT_EDIT_USER_INFO = BASE_URL + "/lapi/useredit";

    public static final String ACCOUNT_CHECK_PWD = BASE_URL + "/lapi/checkpasswd";
    
    public static final String ACCOUNT_CHECK_EXIST = BASE_URL + "/lapi/checkexist";
    
    public static final String ACCOUNT_CHECK_LOCKED = BASE_URL + "/lapi/checklocked";
    /**
     * LOGIN,login url
     */
    public static final String ACCOUNT_LOGIN = BASE_URL + "/lapi/login";
    
    public static final String ACCOUNT_CHECK_SECURITY = BASE_URL + "/lapi/checkrandcode";
    
    public static final String DELIVERY_INFO = BASE_URL + "/lapi/delivery_info";
    
    /**
     * signkey md5
     */
    public static final String SIGNKEY = "ZYK_ac17c4b0bb1d5130bf8e0646ae2b4eb4";
    
    public static final String JSON_S_TOKEN = "token";
    
    public static final String JSON_S_PWD = "passwd";
    
    public static final String JSON_S_REGTYPE = "regtype";
    
    public static final String JSON_S_UID = "uid";
    
    public static final String JSON_S_OPENID = "openid";
    
    public static final String CODE_TYPE = "codetype";
    
	public static final String CODE_TYPE_REG = "userreg";
	
	public static final String CODE_TYPE_RESET = "resetpasswd";
	
	public static final String CODE_TYPE_BIND_MOBILE = "bindmobile";
	
	public static final String CODE_TYPE_GETPWD = "getpasswd";
	
	public static final String ACCOUNT_PWD_VAL = "passwdval";
	//Util
	public static final String REQUEST_TYPE = "request_type";
	public static final String PASSWD_RESET = "reset_pwd";
	public static final String PASSWD_MODIFY = "modify_pwd";
	public static final String SET_PWD_WHEN_REGISTER_BY_MOBILE = "set_pwd_when_register_by_mobile";
	public static final String PASSWD_REGISTER_BY_EMAIL = "set_pwd_register_by_email";
	public static final String PASSWD_REGISTER_BY_MOBILE = "set_pwd_register_by_mobile";
	
	
	public static final int ACCOUNT_TYPE_PHONENUMBER = 1001;
	public static final int ACCOUNT_TYPE_EMAIL = 1002;
	
	public static final String UTYPE_QQ = "openqq";
	public static final String UTYPE_WEIBO = "openweibo";
	public static final String UTYPE_PHONE = "phone";
	public static final String UTYPE_MAIL = "mail";
	
	public static final String BIND_TO_PHONE = "bind_to_phone";
	public static final String BIND_TO_MAIL = "bind_to_mail";
}
