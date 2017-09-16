package com.droi.account.login;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.droi.account.DebugUtils;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;
import com.droi.account.statis.StaticsCallback;

public class SendEmailActivity extends BaseActivity {
	private static final boolean DEBUG = Utils.DEBUG;
	private static final String TAG = "SendEmailActivity";
	private static final String STATE_SAVED_KEY_INIT = "droiaccount:init";
	private static final int DIALOG_ON_PROGRESS = 100;
	private TextView mTextView;
	private Button mBtnResend;
	private Button mFinish;
	private String EMAIL;
	private String mWhat;
	//
	private String OPEN_ID;
	private String TOKEN;
	
	private ProgressDialog mProgressDialog = null;
	
	//mark if user press the reset button, if user resent success
	private boolean mResentYet = false;
	private AsyncTask<Void, Void, String> mTask;
	private TimeCounter mTimeCounter;
	private String mPasswdVal = "-1";
	private String mPassword;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if(intent != null){
			String title = intent.getStringExtra("title");
			if(!TextUtils.isEmpty(title)){
				setTitle(title);
			}
			EMAIL = intent.getStringExtra("email");
			mWhat = intent.getStringExtra("what");
			OPEN_ID = intent.getStringExtra("openid");
			TOKEN = intent.getStringExtra("token");
			mPasswdVal = intent.getStringExtra(Constants.ACCOUNT_PWD_VAL);
			mPassword = intent.getStringExtra(Constants.JSON_S_PWD);
		}

		setContentView(mMyResources.getLayout("lib_droi_account_send_email_layout"));
		mTextView = (TextView)findViewById(mMyResources.getId("lib_droi_account_tv_address"));
		if(!TextUtils.isEmpty(EMAIL)){
			mTextView.setText(EMAIL);
		}
		mBtnResend = (Button)findViewById(mMyResources.getId("lib_droi_account_btn_resend"));
		mFinish = (Button) findViewById(mMyResources.getId("lib_droi_account_btn_finish"));
		setupViews();
		 if(Utils.getAvailableNetWorkType(SendEmailActivity.this) == -1){
			  Utils.showMessage(SendEmailActivity.this, mMyResources.getString("lib_droi_account_network_wrong_text"));
			  return;
		 }
		 mTimeCounter = new TimeCounter(60*1000, 1000);
		 if(savedInstanceState != null){
			 boolean inited = savedInstanceState.getBoolean(STATE_SAVED_KEY_INIT, false);
			 if(inited == false){
				 sendRequest(mWhat);
			 }
		 }else{
			 sendRequest(mWhat);
		 }
	}
	
	private void sendRequest(String what){
		if("bindmail".equals(what)){
			showProgress();
			mTask = new BindMailTask(OPEN_ID, TOKEN, EMAIL, mPasswdVal, mPassword);
			mTask.execute();
		}else if("findbackpwdbyemail".equals(what)){
			showProgress();
			mTask = new FindPwdByEmailTask(EMAIL);
			mTask.execute();
		}else if("setpwdforbindmail".equals(what)){
			showProgress();
			mTask = new SetPwdForBindEmailTask(EMAIL);
			mTask.execute();
		}
	}
	
	 @Override
	 protected Dialog onCreateDialog(int id, Bundle args) {
		 Dialog dialog = null;
		 if(DIALOG_ON_PROGRESS == id){
			 final ProgressDialog progressDialog = new ProgressDialog(this);
			 progressDialog.setMessage(getString(mMyResources.getString("lib_droi_account_msg_on_process")));
			 progressDialog.setIndeterminate(true);
			 progressDialog.setCancelable(true);
			 progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
	
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					if(mTask != null){
						mTask.cancel(true);
					}
				}
		    	 
		     });
		     mProgressDialog = progressDialog;
		     dialog = progressDialog;
		 }
	     return dialog;
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_SAVED_KEY_INIT, true);
		
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}
	private void setupViews(){
		mBtnResend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//setResult(RESULT_OK);
				//finish();
				 if(Utils.getAvailableNetWorkType(SendEmailActivity.this) == -1){
					  Utils.showMessage(SendEmailActivity.this, mMyResources.getString("lib_droi_account_network_wrong_text"));
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
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
	
	private void showProgress(){
		   showDialog(DIALOG_ON_PROGRESS);
	}
		
	private void hideProgress(){
	   if (mProgressDialog != null){
	       mProgressDialog.dismiss();
	   }
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(mTask != null){
			mTask.cancel(true);
			mTask = null;
		}
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.putExtra("email", EMAIL);
		setResult(RESULT_CANCELED, intent);
		super.onBackPressed();
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
			StaticsCallback staticsCallback = StaticsCallback.getInstance(getApplicationContext());
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
						StaticsCallback staticsCallback = StaticsCallback.getInstance(getApplicationContext());
						staticsCallback.onBindResult(Constants.BIND_TO_MAIL);
						if("0".equals(mPasswdVal) == false){
							//对于没有设置密码的用户，需要更新用户的信息
							//SharedInfo sharedInfo = SharedInfo.getInstance();
							//User userData = sharedInfo.getData();
							//userData.setToken(jsonObject.getString("token"));
							//userData.setUID(jsonObject.has("uid")?jsonObject.getString("uid"): userData.getName());
							//userData.setOpenId(jsonObject.getString("openid"));
							//userData.setGender(jsonObject.getString("gender"));
							//userData.setPasswdVal(0);
							//sharedInfo.saveDataToAccount(getApplicationContext());
						}
				      	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
				      		getResources().getString(mMyResources.getString("lib_droi_account_toast_bind_fail"));
                    	Utils.showMessage(getApplicationContext(), desc);
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_toast_bind_fail"));
                    	Utils.showMessage(getApplicationContext(), desc);
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
        		Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_toast_bind_fail"));
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
                    	Utils.showMessage(getApplicationContext(), desc);
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_msg_find_pwd_by_email_fail"));
                    	Utils.showMessage(getApplicationContext(), desc);
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
        		Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_msg_find_pwd_by_email_fail"));
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
                    	Utils.showMessage(getApplicationContext(), desc);
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_set_pwd_fail"));
                    	Utils.showMessage(getApplicationContext(), desc);
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
        		Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_set_pwd_fail"));
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

	public void onBack(View view){
		onBackPressed();
	}
}
