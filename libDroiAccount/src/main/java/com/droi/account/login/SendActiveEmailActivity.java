package com.droi.account.login;


/*
 * used for register by email for bind by email, send active email
 * */
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.droi.account.DebugUtils;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;
import com.droi.account.statis.AccountOperation;
import com.droi.account.statis.StaticsCallback;

public class SendActiveEmailActivity extends BaseActivity {

	private static final String TAG = "SendActiveEmailActivity";
	private static final String STATE_SAVED_KEY_INIT = "droiaccount:init";
	private static final String STATE_SAVED_KEY_NEW_EMAIL = "new_email";
	//Operation
	public static final int EMAIL_REGISTER = 1;
	public static final int EMAIL_BIND = EMAIL_REGISTER + 1;
	public static final int EMAIL_REBIND = EMAIL_BIND + 1;
	public static final int EMAIL_RESEND = EMAIL_REBIND + 1;
	//dialog id
	private static final int DIALOG_ON_PROGRESS = 100;
	private static final int DIALOG_CONFIRM_PASSWORD = DIALOG_ON_PROGRESS + 1;
	
	//requestCode being
	private static final int MODIFY_EMAIL = 2;
	//requestCode end
	//control the display of the bottom widget
	private boolean mRightToModifyAddress = false;
	private String mEmail;
	private String mPassword;
	
	private TextView mTextView;
	private Button mBtnResend;
	private Button mFinish;
	
	private boolean mResentYet = false;
	private TimeCounter mTimeCounter;
	private AsyncTask<Void, Void, String> mTask;
	
	private ProgressDialog mProgressDialog = null;
	
	private String mPasswdVal = "-1";
	private String mOpenId;
	private String mToken;
	private int mWhat = -1;
	
	private TextView mModifyAddress;
	private EditText mPasswdEditText;
	
	private String mUserName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onCreate");
		}
		if(intent != null){
			mRightToModifyAddress = intent.getBooleanExtra("modifyAddress", false);
			mEmail = intent.getStringExtra("email");
			mPassword = intent.getStringExtra("pwd");
			mWhat = intent.getIntExtra("what", -1);
			
			mPasswdVal = intent.getStringExtra(Constants.ACCOUNT_PWD_VAL);
			mOpenId = intent.getStringExtra("openid");
			mToken = intent.getStringExtra("token");
			mUserName = intent.getStringExtra("account");
		}
		if(savedInstanceState != null){
			String newEmail = savedInstanceState.getString(STATE_SAVED_KEY_NEW_EMAIL);
			if(!TextUtils.isEmpty(newEmail)){
				mEmail = newEmail;
			}
		}
		setContentView(mMyResources.getLayout("lib_droi_account_layout_send_active_email"));
		
		View modifyAccountView = findViewById(mMyResources.getId("lib_droi_account_modify_layout"));
		if(modifyAccountView != null){
			if(TextUtils.isEmpty(mOpenId) && TextUtils.isEmpty(mToken)){
				modifyAccountView.setVisibility(View.GONE);
			}else{
				modifyAccountView.setVisibility(View.VISIBLE);
			}
			
		}
		
		mTextView = (TextView)findViewById(mMyResources.getId("lib_droi_account_tv_address"));
		if(!TextUtils.isEmpty(mEmail)){
			mTextView.setText(mEmail);
		}
		mBtnResend = (Button)findViewById(mMyResources.getId("lib_droi_account_btn_resend"));
		mFinish = (Button) findViewById(mMyResources.getId("lib_droi_account_btn_finish"));
		setupViews();
		 if(Utils.getAvailableNetWorkType(SendActiveEmailActivity.this) == -1){
			  Utils.showMessage(SendActiveEmailActivity.this, mMyResources.getString("lib_droi_account_network_wrong_text"));
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
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_SAVED_KEY_INIT, true);
		outState.putString(STATE_SAVED_KEY_NEW_EMAIL, mEmail);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}
	private void setupViews(){
		mModifyAddress = (TextView)findViewById(mMyResources.getId("lib_droi_account_modify_my_address"));
		mModifyAddress.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//
				if(mWhat == EMAIL_BIND){
					//如果是绑定邮箱，需要用户输入密码验证
					//if no password is setted, skip this
					SharedInfo sharedInfo = SharedInfo.getInstance();
					String pwdVal = sharedInfo.getPasswdVal(getApplicationContext());
					if("0".equals(pwdVal) == false){
						Intent intent = new Intent(SendActiveEmailActivity.this, EmailInputActivity.class);
						startActivityForResult(intent, MODIFY_EMAIL);
					}else{
						showDialog(DIALOG_CONFIRM_PASSWORD);
					}
				}else{
					Intent intent = new Intent(SendActiveEmailActivity.this, EmailInputActivity.class);
					startActivityForResult(intent, MODIFY_EMAIL);
				}
			}
		});
		mBtnResend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//setResult(RESULT_OK);
				//finish();
				 if(Utils.getAvailableNetWorkType(SendActiveEmailActivity.this) == -1){
					  Utils.showMessage(SendActiveEmailActivity.this, mMyResources.getString("lib_droi_account_network_wrong_text"));
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
				intent.putExtra("email", mEmail);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
	
	 @Override
	 protected Dialog onCreateDialog(int id, Bundle args) {
		 Dialog dialog = null;
		 if(DIALOG_ON_PROGRESS == id  && mProgressDialog == null){
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
		 }else if(DIALOG_CONFIRM_PASSWORD == id){
			 View view = getLayoutInflater().inflate(mMyResources.getLayout("lib_droi_account_password_input_layout"), null);
			 mPasswdEditText = (EditText)view.findViewById(mMyResources.getId("lib_droi_account_password"));
			 mPasswdEditText.setHint(mMyResources.getString("lib_droi_account_input_original_passwd_text"));
			 dialog = new AlertDialog.Builder(this)
			 	 .setTitle(getString(mMyResources.getString("lib_droi_account_current_account_title")) + mUserName)
			 	 .setView(view)
			 	 .setNegativeButton(android.R.string.cancel, null)
			 	 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if(mPasswdEditText != null){
							final String  password = mPasswdEditText.getText().toString().trim();
							if(TextUtils.isEmpty(password)){
								Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_toast_passwd_empty"));
								return;
							}
							SharedInfo sharedInfo = SharedInfo.getInstance();
							showProgress();
							new CheckPwdTask(sharedInfo.getOpenId(getApplicationContext()), 
									sharedInfo.getAccessToken(getApplicationContext()), 
									password, new CheckPwdListener() {
								
								@Override
								public void onResult(boolean result) {
									// TODO Auto-generated method stub
									if(DebugUtils.DEBUG){
										DebugUtils.i(TAG, "check result : " + result);
									}
									hideProgress();
									if(result){
										Intent intent = new Intent(SendActiveEmailActivity.this, EmailInputActivity.class);
										startActivityForResult(intent, MODIFY_EMAIL);
									}else{
										Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_dialog_error_pwd"));
									}
								}
							}).execute();
							
						}
					}
			 		 
			 	 })

			     .create();
		 }
	     return dialog;
	}
	 
	@Override
	@Deprecated
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		// TODO Auto-generated method stub
		super.onPrepareDialog(id, dialog, args);
		if(id == DIALOG_CONFIRM_PASSWORD){
			if(mPasswdEditText != null){
				mPasswdEditText.setText("");
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
			case MODIFY_EMAIL:{
				if(resultCode == RESULT_OK){
					String email = data.getStringExtra("accountname");
					if(!TextUtils.isEmpty(email)){
						mEmail = email;
						mTextView.setText(email);
						sendRequest(EMAIL_BIND);
					}
				}
			}
			break;
		}
		
	}
	
	private void showProgress(){
		   showDialog(DIALOG_ON_PROGRESS);
	}
		
	private void hideProgress(){
	   if (mProgressDialog != null){
	       mProgressDialog.dismiss();
	   }
	}
	
	private void sendRequest(int what){
		if(what == EMAIL_REGISTER){
			showProgress();
			mTask = new RegisterByEmail(mEmail, mPassword, "userreg");
			mTask.execute();
		}else if(what == EMAIL_BIND){
			showProgress();
			mTask = new BindMailTask(mOpenId, mToken, mEmail, mPasswdVal, mPassword);
			mTask.execute();
		}else if(what == EMAIL_RESEND){
			showProgress();
			mTask = new ResendEmailTask(mEmail);
			mTask.execute();
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
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mTask != null) {
			mTask.cancel(true);
			mTask = null;
		}
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			removeDialog(DIALOG_ON_PROGRESS);
			mProgressDialog = null;
		}
		super.onDestroy();
	}
	
	class RegisterByEmail extends AsyncTask<Void, Void, String>{
		private final String mEmail;
		private final String mPassword;
		private final String mCodeType;
		
		public RegisterByEmail(String email, String password, String codeType){
			mEmail = email;
			mPassword = password;
			mCodeType = codeType;
		}
		
		@Override
		protected String doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			Map<String, String> registerParams = new HashMap<String, String>();
			String signString = MD5Util.md5(mEmail + mCodeType + " "+ Constants.SIGNKEY);
			registerParams.put("mail", mEmail);
			registerParams.put("codetype", mCodeType);
			registerParams.put("passwd", mPassword);
			registerParams.put("devinfo", " ");
			registerParams.put("sign", signString);
			StaticsCallback staticsCallback = StaticsCallback.getInstance(getApplicationContext());
			String loginfo = staticsCallback.encryptRegisteringLogInfo(mUserName, AccountOperation.ACCOUNT_REGISTER_TYPE_MAIL);
			registerParams.put("loginfo", loginfo);
			String result = null;
			try{
				result = HttpOperation.postRequest(Constants.ACCOUNT_REGISTER_BY_EMAIL, registerParams);
			}catch(Exception e){
				e.printStackTrace();
			}
			return result;
		}
		
		@Override
        protected void onPostExecute(String result){
			super.onPostExecute(result);
			hideProgress();
			if (!TextUtils.isEmpty(result)){
				try {
					if(DebugUtils.DEBUG){
						DebugUtils.i("RegisterByEmail", "Register by Email: result = " + result);
					} 
					JSONObject jsonObject = new JSONObject(result);
					int rst = jsonObject.getInt("result");
					if(rst == 0){
						//注册之后，需要用户邮箱激活才能登陆
						StaticsCallback staticsCallback = StaticsCallback.getInstance(getApplicationContext());
						staticsCallback.onRegisterResult(mUserName, AccountOperation.ACCOUNT_REGISTER_TYPE_MAIL, 
								jsonObject.has("openid") ? jsonObject.getString("openid") : "", true);
					
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_tip_register_wrong"));
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
				Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_tip_register_wrong"));
			}
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
	
	class ResendEmailTask extends AsyncTask<Void, Void, String>{
		private final String mEmail;
		private final String mCodeType = "resend";
		
		public ResendEmailTask(String email){
			mEmail = email;
		}
		
		@Override
		protected String doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			Map<String, String> params = new HashMap<String, String>();
			String signString = MD5Util.md5(mEmail + mCodeType + " "+ Constants.SIGNKEY);
			params.put("mail", mEmail);
			params.put("codetype", mCodeType);
			params.put("devinfo", " ");
			params.put("sign", signString);
			String result = null;
			
			try{
				result = HttpOperation.postRequest(Constants.ACCOUNT_RESEND_MAIL, params);
			}catch(Exception e){
				e.printStackTrace();
			}
			return result;
		}
		
		@Override
        protected void onPostExecute(String result){
			super.onPostExecute(result);
			hideProgress();
			if (!TextUtils.isEmpty(result)){
				try {
					if(DebugUtils.DEBUG){
						DebugUtils.i("Resend email", "result = " + result);
					} 
					JSONObject jsonObject = new JSONObject(result);
					int rst = jsonObject.getInt("result");
					if(rst == 0){
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : "";
                    	if(!TextUtils.isEmpty(desc)){
                    		Utils.showMessage(getApplicationContext(), desc);
                    	}
						
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_resend_mail_failed"));
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
				Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_resend_mail_failed"));
			}
		}
	}

	public void onBack(View view){
		onBackPressed();
	}
	
}