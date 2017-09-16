package com.droi.edriver.net;

import android.os.Handler;
import android.os.Message;

import com.droi.edriver.bean.UserInfo;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by ZhuQichao on 2016/3/29.
 */
public class Request {

	private Handler mHandler;

	public Request() {
		mHandler = new MyHandler();
	}

	public Request(Handler handler) {
		mHandler = handler;
	}

	public void updateUserInfo(UserInfo value) {
		request(NetMsgCode.UPDATE_USER_INFO, value.getUpdateParam(), "updateUserInfo");
	}

	public void getUserInfo(String openid) {
		HashMap<String, String> param = new HashMap<>();
		param.put(NetMsgCode.KeyUserInfo.OPENID, openid);
		request(NetMsgCode.GET_USER_INFO, param, "getUserInfo");
	}

	private void request(int msgCode, HashMap<String, String> map, String tag) {
		GetDataFromVolly.execute(msgCode, mHandler, map, NetMsgCode.URL, tag);
	}

	private static class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
		}
	}
}
