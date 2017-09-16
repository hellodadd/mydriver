package com.droi.edriver.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.adroi.sdk.AdSize;
import com.adroi.sdk.AdView;
import com.adroi.sdk.AdViewListener;
import com.droi.account.authenticator.AuthenticatorActivity;
import com.droi.account.shared.DroiAccount;
import com.droi.edriver.R;
import com.droi.edriver.tools.Settings;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends Activity {

	private DroiAccount mDroidAccount;

	private Button btnLogin;
	private RelativeLayout rootView;

	private static final int TIME_TO_LOGIN = 3000;
	private static final int TIME_OUT = 4000;
	private static final int MSG_TO_LOGIN = 0x101;
	private static final int MSG_SHOW_AD = 0x102;
	private static final int MSG_TIME_OUT = 0x103;
	private static final int MSG_SHOW_LOGIN_BTN = 0x104;

	private AdView interstialAdView;
	private AdView init;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("zhuqichao", "SplashActivity.onCreate");
		setContentView(R.layout.activity_splash);
		btnLogin = (Button) findViewById(R.id.btn_login);
		rootView = (RelativeLayout) findViewById(R.id.root_view);
		mDroidAccount = DroiAccount.getInstance(this);
		AuthenticatorActivity.onBackClickListener = onBackClickListener;
		mDroidAccount.init("2851137750", "1707964849");
		DroiAccount.setEmailLogin(true);
		DroiAccount.setPhoneLogin(true);
		DroiAccount.setVersionForeign(true);
		initReceiver();
		if (getIntent().getBooleanExtra("from_logout", false)) {
			handler.sendEmptyMessage(MSG_TO_LOGIN);
			return;
		}
		if (TextUtils.isEmpty(mDroidAccount.getToken()) || !mDroidAccount.checkAccount()) {
			mDroidAccount.tokenInvalidate();
		}
		if (Settings.isFirstStartApp()) {//首次进入应用不显示广告
			handler.sendEmptyMessageDelayed(MSG_TO_LOGIN, TIME_TO_LOGIN);
		} else {
			handler.sendEmptyMessageDelayed(MSG_TIME_OUT, TIME_OUT);
			//此处准备展示广告
			AdView.preLoad(this, "7dead41a");
			init = new AdView(this, AdSize.InitialNoAnimation, "sb8f5fee");
			//interstialAdView = new AdView(this, AdSize.Interstitial, "s5c11d93");
			init.setListener(mAdViewListener);
		}
	}

	private AdViewListener mAdViewListener = new AdViewListener() {
		@Override
		public void onAdReady() {
			Log.i("zhuqichao", "onAdReady");
			handler.removeMessages(MSG_TIME_OUT);
			handler.sendEmptyMessage(MSG_SHOW_AD);
		}

		@Override
		public void onAdShow() {
			Log.i("zhuqichao", "onAdShow");
			if (init != null) {
				handler.sendEmptyMessageDelayed(MSG_TO_LOGIN, TIME_TO_LOGIN);
			}
		}

		@Override
		public void onAdClick() {
			Log.i("zhuqichao", "onAdClick");
		}

		@Override
		public void onAdFailed(String s) {
			Log.i("zhuqichao", "onAdFailed=" + s);
			handler.removeMessages(MSG_TIME_OUT);
			handler.sendEmptyMessageDelayed(MSG_TO_LOGIN, TIME_TO_LOGIN);
		}

		@Override
		public void onEvent(String s) {
			Log.i("zhuqichao", "onEvent=" + s);
			try {
				JSONObject json = new JSONObject(s);
				if (json.optBoolean("needFinish")) {
					handler.removeMessages(MSG_TO_LOGIN);
					handler.removeMessages(MSG_SHOW_AD);
				} else if (json.optBoolean("lpClose")) {
					handler.sendEmptyMessage(MSG_TO_LOGIN);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onDismiss() {
			Log.i("zhuqichao", "onDismiss");
		}
	};

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_TO_LOGIN:
					rootView.removeView(init);
					if (mDroidAccount.checkAccount()) {
						gotoMain();
					} else {
						mDroidAccount.login(DroiAccount.LOIGN_THEME_LANDSCAPE);
						//handler.sendEmptyMessageDelayed(MSG_SHOW_LOGIN_BTN, 1000);
					}
					if (Settings.isFirstStartApp()) {
						startActivity(new Intent(SplashActivity.this, GuidePageActivity.class));
						Settings.setFirstStartApp(false);
					}
					break;
				case MSG_SHOW_AD:
					if (init != null) {
						addContentView(init, new FrameLayout.LayoutParams(-1, -1));
					}
					break;
				case MSG_TIME_OUT:
					Log.i("zhuqichao", "广告超时！");
					init = null;
					handler.removeMessages(MSG_TO_LOGIN);
					handler.removeMessages(MSG_SHOW_AD);
					handler.sendEmptyMessage(MSG_TO_LOGIN);
					break;
				case MSG_SHOW_LOGIN_BTN:
					btnLogin.setVisibility(View.VISIBLE);
					break;
				default:
					break;
			}
		}
	};

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_login:
				mDroidAccount.login(DroiAccount.LOIGN_THEME_PORTRAIT);
				break;
			default:
				break;
		}
	}

	private void initReceiver() {
		IntentFilter filter = new IntentFilter();
		//filter.addAction(DroiAccount.INTENT_ACCOUNT_CHANGE_ACCOUNT);
		//filter.addAction(DroiAccount.INTENT_ACCOUNT_DELETED);
		filter.addAction(DroiAccount.INTENT_ACCOUNT_LOGIN);
		//filter.addAction(DroiAccount.INTENT_ACCOUNT_UPDATED);
		//filter.addAction(DroiAccount.INTENT_ACCOUNT_LOGINSUCCESS);
		registerReceiver(mBroadReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i("zhuqichao", "SplashActivity.onDestroy");
		unregisterReceiver(mBroadReceiver);
		handler.removeMessages(MSG_TO_LOGIN);
		handler.removeMessages(MSG_SHOW_AD);
		handler.removeMessages(MSG_TIME_OUT);
	}

	private BroadcastReceiver mBroadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("zhuqichao", "action=" + intent.getAction());
			switch (intent.getAction()) {
				case DroiAccount.INTENT_ACCOUNT_LOGIN:
					gotoMain();
					btnLogin.setVisibility(View.GONE);
					showUserInfo();
					break;
				default:
					break;
			}
		}
	};

	private AuthenticatorActivity.OnBackClickListener onBackClickListener = new AuthenticatorActivity.OnBackClickListener() {
		@Override
		public void onBackClick() {
			finish();
		}
	};

	private void showUserInfo() {
		String userName = mDroidAccount.getUserName();
		String nickName = mDroidAccount.getNickName();
		String bindPhone = mDroidAccount.getBindPhone();
		String expire = mDroidAccount.getExpire();
		String result = "UserName : " + userName + "\nNickName : " + nickName + "\nBindPhone : " + bindPhone + "\nExpire : " + expire;
		Log.i("zhuqichao", "userInfo=" + result);
	}

	private void gotoMain() {
		Intent intent = new Intent();
		intent.setClass(SplashActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
