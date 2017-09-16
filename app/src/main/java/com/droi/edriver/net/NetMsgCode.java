package com.droi.edriver.net;

/**
 * Created by ZhuQichao on 2016/3/29.
 */
public class NetMsgCode {

	public static final String URL = "http://ejia.yy845.com:3052";
	public static final String ENCODE_DECODE_KEY = "c_c0_c33";
	public static final int MSG_SUCCESS = 200;
	public static final int MSG_FAIL = 404;

	public static final int UPDATE_USER_INFO = 111001;//更新用户信息
	public static final int GET_USER_INFO = 111003;//获取用户信息

	public static class KeyUserInfo {
		public static final String JSONNAME = "carUserInfo";
		public static final String OPENID = "openid";//卓易账号openid
		public static final String NAME = "username";//用户名（昵称）
		public static final String SEX = "gender";//性别
		public static final String BIRTH = "birthday";//生日
		public static final String CAR = "carmodel";//车型
	}
}
