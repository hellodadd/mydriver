package com.droi.account.procedure;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.droi.account.DebugUtils;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;
import com.droi.account.statis.StaticsCallback;

public class SendEmailFragment extends BackHandledFragment {
	private static final boolean DEBUG = Utils.DEBUG;
	private static final String TAG = "SendEmailFragment";
	private TextView mTextView;
	private Button mBtnResend;
	private Button mFinish;
	private String EMAIL;
	private String mWhat;
	//
	private String OPEN_ID;
	private String TOKEN;
	
	//mark if user press the reset button, if user resent success
	private boolean mResentYet = false;
	private AsyncTask<Void, Void, String> mTask;
	private TimeCounter mTimeCounter;
	private String mPasswdVal = "-1";
	private String mPassword;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(mMyResources.getLayout("lib_droi_account_send_email_layout"), null);
		findViewByIds(view);
		setupViews();
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		final Activity activity = getActivity();
		Bundle bundle = getArguments();
		if(bundle != null){
			String title = bundle.getString("title");
			if(!TextUtils.isEmpty(title)){
				activity.setTitle(title);
			}
			EMAIL = bundle.getString("email");
			mWhat = bundle.getString("what");
			OPEN_ID = bundle.getString("openid");
			TOKEN = bundle.getString("token");
			mPasswdVal = bundle.getString(Constants.ACCOUNT_PWD_VAL);
			mPassword = bundle.getString(Constants.JSON_S_PWD);
		}
		if(!TextUtils.isEmpty(EMAIL)){
			mTextView.setText(EMAIL);
		}
		mTimeCounter = new TimeCounter(60*1000, 1000);
		sendRequest(mWhat);
	}
	
	@Override
	protected void findViewByIds(View view) {
		// TODO Auto-generated method stub
		mTextView = (TextView)view.findViewById(mMyResources.getId("lib_droi_account_tv_address"));
		mBtnResend = (Button)view.findViewById(mMyResources.getId("lib_droi_account_btn_resend"));
		mFinish = (Button) view.findViewById(mMyResources.getId("lib_droi_account_btn_finish"));
	}

	@Override
	protected void setupViews() {
		// TODO Auto-generated method stub
		mBtnResend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//setResult(RESULT_OK);
				//finish();
				 if(Utils.getAvailableNetWorkType(getActivity()) == -1){
					  Utils.showMessage(getActivity(), mMyResources.getString("lib_droi_account_network_wrong_text"));
					  return;
				 }
				mResentYet = true;
				sendRequest(mWhat);
			}
		});
		
		mFinish.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("email", EMAIL);
				setResult(Activity.RESULT_OK, intent);
				//finish();
			}
		});
	}

	private void sendRequest(String what){
		if("bindmail".equals(what)){
			showDialog(DIALOG_ON_PROGRESS, null);
			mTask = new BindMailTask(OPEN_ID, TOKEN, EMAIL, mPasswdVal, mPassword);
			mTask.execute();
		}else if("findbackpwdbyemail".equals(what)){
			showDialog(DIALOG_ON_PROGRESS, null);
			mTask = new FindPwdByEmailTask(EMAIL);
			mTask.execute();
		}else if("setpwdforbindmail".equals(what)){
			showDialog(DIALOG_ON_PROGRESS, null);
			mTask = new SetPwdForBindEmailTask(EMAIL);
			mTask.execute();
		}
	}
	private class BindMailTask extends AsyncTask<Void, Void, String>{

		private final String mOpenId;
		private final String mToken;
		private final String mMail;
		private final String mPasswordValue;
		private final String mBindPassword;
		
		public BindMailTask(String openId, String token, String mail, String passwdVal, String passwd){
			mOpenId = openId;
			mToken = token;
			mMail = mail;
			mPasswordValue = passwdVal;
			mBindPassword = passwd;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			final Map<String, String> bindParams = new HashMap<String, String>();
			String codeType = "bindmail";
			String deviceInfo = " ";

			bindParams.put("mail", mMail);
			bindParams.put("codetype", codeType);
			bindParams.put("devinfo", deviceInfo);
			bindParams.put("sign", MD5Util.md5(mMail + codeType + deviceInfo + Constants.SIGNKEY));
			if("0".equals(mPasswordValue) == false){
				bindParams.put(Constants.JSON_S_PWD, mBindPassword);
			}
			bindParams.put(Constants.JSON_S_OPENID, mOpenId);
			bindParams.put(Constants.JSON_S_TOKEN, mToken);
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "BindMailTask  = " + bindParams.toString());
			}
			StaticsCallback staticsCallback = StaticsCallback.getInstance(getActivity());
			String loginfo = staticsCallback.encryptBindingLoginfo(Constants.BIND_TO_MAIL);
			bindParams.put("loginfo", loginfo);
			String loginResult = null;
			try{
				loginResult = HttpOperation.postRequest(Constants.ACCOUNT_BIND_EMAIL, bindParams);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return loginResult;
		}
		
        @Override
        protected void onPostExecute(String result){
        	super.onPostExecute(result);
        	hideProgress();
        	if (!TextUtils.isEmpty(result)){
        		try {
        			if(DebugUtils.DEBUG){
        				DebugUtils.i(TAG, "BindMailTask result = " + result);
        			}
        			if(mTimeCounter != null){
        				mTimeCounter.cancel();
        			}
					JSONObject jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == 0){
						if(DebugUtils.DEBUG){
							DebugUtils.i(TAG, "after bindMail : " + mPasswdVal);
						}
						StaticsCallback staticsCallback = StaticsCallback.getInstance(getActivity());
						staticsCallback.onBindResult(Constants.BIND_TO_MAIL);
						//bind email , async step, we do not know the result of bind email, need user to go their emails finish the bind operation 
						//JSONObject updateBindMail = new JSONObject();
						//updateBindMail.put(ShareInfoManager.SHARED_BINDMAIL, mMail);
						//SharedInfo sharedInfo = SharedInfo.getInstance();
						//sharedInfo.updateLocalUserInfo(getActivity(), updateBindMail);
				      	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
				      		getResources().getString(mMyResources.getString("lib_droi_account_toast_bind_fail"));
                    	Utils.showMessage(getActivity(), desc);
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_toast_bind_fail"));
                    	Utils.showMessage(getActivity(), desc);
					}
					if(mResentYet){
						mBtnResend.setEnabled(false);
						mBtnResend.setClickable(false);
						if(mTimeCounter != null){
							mTimeCounter.start();
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}else{
        		Utils.showMessage(getActivity(), mMyResources.getString("lib_droi_account_toast_bind_fail"));
        	}
        }
	}
	
	private class FindPwdByEmailTask extends AsyncTask<Void, Void, String>{

		private String mCodetype = "findpasswd";
		private String mEmail ;
		public FindPwdByEmailTask(String email){
			mEmail = email;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			String loginResult = null;
			final Map<String, String> findPwdParams = new HashMap<String, String>();
			findPwdParams.put("mail", mEmail);
			findPwdParams.put("codetype", mCodetype);
			findPwdParams.put("devinfo", " ");
			findPwdParams.put("sign", MD5Util.md5(mEmail+ mCodetype + " "+Constants.SIGNKEY));
			try{
				loginResult = HttpOperation.postRequest(Constants.ACCOUNT_FIND_PWD_BY_EMAIL, findPwdParams);
			}catch(Exception e){
				e.printStackTrace();
			}
			return loginResult;
		}
		
        @Override
        protected void onPostExecute(String result){
        	super.onPostExecute(result);
        	hideProgress();
        	if (!TextUtils.isEmpty(result)){
        		try {

					JSONObject jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == 0){
				      	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
				      		getResources().getString(mMyResources.getString("lib_droi_account_msg_find_pwd_by_email_success"));
                    	Utils.showMessage(getActivity(), desc);
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_msg_find_pwd_by_email_fail"));
                    	Utils.showMessage(getActivity(), desc);
					}
					if(mResentYet){
						mBtnResend.setEnabled(false);
						mBtnResend.setClickable(false);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}else{
        		Utils.showMessage(getActivity(), mMyResources.getString("lib_droi_account_msg_find_pwd_by_email_fail"));
        	}
        }
	}
	
	private class SetPwdForBindEmailTask extends AsyncTask<Void, Void, String>{

		
		public SetPwdForBindEmailTask(String email){

		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			String loginResult = null;

			
			return loginResult;
		}
		
        @Override
        protected void onPostExecute(String result){
        	super.onPostExecute(result);
        	hideProgress();
        	if (!TextUtils.isEmpty(result)){
        		try {

					JSONObject jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == 0){
				      	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
				      		getResources().getString(mMyResources.getString("lib_droi_account_set_pwd_fail"));
                    	Utils.showMessage(getActivity(), desc);
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_set_pwd_fail"));
                    	Utils.showMessage(getActivity(), desc);
					}
					if(mResentYet){
						mBtnResend.setEnabled(false);
						mBtnResend.setClickable(false);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}else{
        		Utils.showMessage(getActivity(), mMyResources.getString("lib_droi_account_set_pwd_fail"));
        	}
        }
	}
	
	private class TimeCounter extends CountDownTimer{
		
        public TimeCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        
		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			mBtnResend.setEnabled(true);
			mBtnResend.setClickable(true);
			mBtnResend.setText(getText(mMyResources.getString("lib_droi_account_resend_email")));
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			mBtnResend.setClickable(false);
			mBtnResend.setText(getText(mMyResources.getString("lib_droi_account_resend_email"))+ "(" + millisUntilFinished/1000 + ")");
		}
		
	}
}
