package com.droi.account.login;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.droi.account.DebugUtils;
import com.droi.account.MyResource;
import com.droi.account.Utils;
import com.droi.account.auth.DroiAuthListener;
import com.droi.account.auth.DroiLoginHandler;
import com.droi.account.authenticator.AuthenticatorActivity;
import com.droi.account.authenticator.Constants;
import com.droi.account.statis.AccountOperation;
import com.droi.account.statis.StaticsCallback;

public class DroiWechatAuthLoginUtils {
	
	private static final String TAG = "DroiAuthLoginUtils";

	private AuthListener mLoginListener = new AuthListener();
	
	private DroiLoginHandler mDroiLoginHandler;
	
	private AuthenticatorActivity mContext;
	
	private User mUser;
	
	private MyResource mMyResources;
	
	public DroiWechatAuthLoginUtils(AuthenticatorActivity context, User user){
		mContext = context;
		mUser = user;
		mMyResources = new MyResource(context);
	}
	
	public void login(){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "Droi auth login");
		}
		mDroiLoginHandler = new DroiLoginHandler(mContext);
		mDroiLoginHandler.authorize(mLoginListener, Constants.DROI_AUTH_WECHAT);
	}
	
	private class AuthListener implements DroiAuthListener {

		@Override
		public void onComplete(final String result) {
			// TODO Auto-generated method stub
			mContext.showProgress();
			if (DebugUtils.DEBUG) {
				DebugUtils.i(TAG, "onComplete result = " + result);
			}
			if (!TextUtils.isEmpty(result)) {
				new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub

						JSONObject jsonObject;
						try {
							jsonObject = new JSONObject(result);
							String openId = jsonObject.has("openid") ? jsonObject
									.getString("openid") : "";
							String token = jsonObject.has("token") ? jsonObject
									.getString("token") : "";
							handleLogin(token, openId);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Utils.showMessage(mContext, "Login Failed");
						}

					}
				}.start();
			}
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			mContext.hideProgress();
			Utils.showMessage(mContext, mMyResources.getString("lib_droi_account_toast_auth_canceled_again_bind"));
		}

		@Override
		public void onError(String message) {
			// TODO Auto-generated method stub
			mContext.hideProgress();
			Utils.showMessage(mContext, message);
		}

	}

	private void handleLogin(String token, String openId) {
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "token = " + token + ", openId = " + openId);
		}
		new DroiThirdPartyLoginTask(mContext, Constants.DROI_WECHAT_LOGIN, token, openId, mUser,
				new DroiThirdPartyLoginTask.Callback() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mContext.hideProgress();
						int result = mUser.getResult();
						if (DebugUtils.DEBUG) {
							DebugUtils.i(TAG, "DroiAuthloglin  result = " + result);
						}
						if (result == 0) {
							StaticsCallback staticsCallback = StaticsCallback.getInstance(mContext);
							staticsCallback.onLoginResult(mUser.getName(), mUser, AccountOperation.ACCOUNT_LOGIN_TYPE_WECHAT);
							mContext.onThirdPartyLoginFinish();
						}

					}

					@Override
					public void onCancelled() {
						// TODO Auto-generated method stub
						if (DebugUtils.DEBUG) {
							DebugUtils.i(TAG, "DroiQQLoginTask onCancelled");
						}
						mContext.hideProgress();
					}
				}).execute();
	}
}
