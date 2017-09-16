package com.droi.account.login;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.droi.account.DebugUtils;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;

public class SecurityCodeActivity extends BaseActivity {
	private static final boolean DEBUG = Utils.DEBUG;
	private static final String TAG = "SecurityCodeActivity";
	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	

	private static final int RETRY_TIME = 60;
	private Button mPreviousStep;
	private Button mNextStep;
	private TimeCount mTimeCount;
	private Button mBtnSecurityCode;
	private EditText mSecurityBox;
	private String mPhoneNumber;
    public static String REGEX = "[0-9]{6}";
    public static String TEMP_TOKEN = "";
    public static String UID = "";
    private SmsReceiver mSmsReceiver;
    private UserLoginTask mLoginTask = null;
    private ProgressDialog mProgressDialog = null;
    private CheckBox mCheckbox;
    private String mCodeType = "userreg";
    private String mBindToken;
    private AsyncTask<Void, Void, String> mTask;
    private View mParent;
    private View mButtonParent;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onCreate");
		}
		TEMP_TOKEN = "";
		setTitle(mMyResources.getString("lib_droi_account_security_code_title"));
		setContentView(mMyResources.getLayout("lib_droi_account_security_code_layout"));
		mPreviousStep = (Button)findViewById(mMyResources.getId("lib_droi_account_btn_previous"));
		mNextStep = (Button)findViewById(mMyResources.getId("lib_droi_account_btn_next"));
		mNextStep.setEnabled(false);
		mBtnSecurityCode = (Button)findViewById(mMyResources.getId("lib_droi_account_btn_get_security_code"));
		mSecurityBox = (EditText)findViewById(mMyResources.getId("lib_droi_account_security_code_box"));
		mCheckbox = (CheckBox)findViewById(mMyResources.getId("lib_droi_account_checkBox"));
		Intent intent = getIntent();
		if(intent != null){
			mPhoneNumber = intent.getStringExtra("phonenumber");
			mCodeType = intent.getStringExtra("codetype");
			mBindToken = intent.getStringExtra("bindToken");
		}
		mTimeCount = new TimeCount(60 * 1000, 1000);
		mSmsReceiver = new SmsReceiver();
		registerReceiver(mSmsReceiver, new IntentFilter(SMS_RECEIVED));
		setupViews();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onResume");
		}
		if(Utils.getAvailableNetWorkType(SecurityCodeActivity.this) == -1){
			showMessage(mMyResources.getString("lib_droi_account_network_wrong_text"));
			return;
		}
		//when user return to this activity from PasswordSetActivity, get securityCode again
		/*
		if(mPhoneNumber != null && !TextUtils.isEmpty(mPhoneNumber) && mBtnSecurityCode.isClickable()){
			new GetSecurityCode(mPhoneNumber).execute();
			mTimeCount.start();
			mBtnSecurityCode.setClickable(false);
		}*/
	}
	
	public void setupViews(){
		mParent = findViewById(mMyResources.getId("lib_droi_account_layout_parent"));
		mButtonParent = findViewById(mMyResources.getId("lib_droi_account_button_parent"));
		mSecurityBox.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				String securityCode = mSecurityBox.getText().toString();
				if(TextUtils.isEmpty(securityCode)){
					mNextStep.setEnabled(false);
					mNextStep.setTextColor(getApplicationContext().getResources().getColor(mMyResources.getColor("lib_droi_account_diabled_color")));
				}else{
					mNextStep.setEnabled(true);
					mNextStep.setTextColor(Color.WHITE);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		mNextStep.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				//CheckSecurityCode
				if(Utils.getAvailableNetWorkType(SecurityCodeActivity.this) == -1){
					showMessage(mMyResources.getString("lib_droi_account_network_wrong_text"));
					return;
				}
				String securityCode = mSecurityBox.getText().toString();
				if(TextUtils.isEmpty(securityCode)){
					mSecurityBox.setError(getResources().getText(mMyResources.getString("lib_droi_account_security_code_empty")).toString());
					return;
				}else if(securityCode.length() != 6){
					mSecurityBox.setError(getResources().getString(mMyResources.getString("lib_droi_account_tip_code_wrong")));
					return;
				}
				//user need to get security code first
				if(TextUtils.isEmpty(TEMP_TOKEN)){
					mSecurityBox.setError(getResources().getString(mMyResources.getString("lib_droi_account_tip_code_wrong")));
					return;
				}
				showProgress();
				mTask = new CheckSecurityCode(TEMP_TOKEN, securityCode);
				mTask.execute();

			}
		});
		mPreviousStep.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTimeCount.cancel();
				Intent intent = new Intent();
				intent.putExtra(Constants.CODE_TYPE, mCodeType);
				setResult(RESULT_CANCELED, intent);
				finish();
			}
		});
		mBtnSecurityCode.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(DebugUtils.DEBUG){
					DebugUtils.i(TAG, "get code : " + mPhoneNumber);
				}
				if(Utils.getAvailableNetWorkType(SecurityCodeActivity.this) == -1){
					showMessage(mMyResources.getString("lib_droi_account_network_wrong_text"));
					return;
				}
				if(mPhoneNumber != null && !TextUtils.isEmpty(mPhoneNumber)){
					mTask = new GetSecurityCode(mPhoneNumber);
					mTask.execute();
					mTimeCount.start();
					//mBtnSecurityCode.setText(getText(mMyResources.getString("lib_droi_account_retry_security_code)+ "(" + RETRY_TIME + ")");
					mBtnSecurityCode.setClickable(true);
				}
			}
			
		});
		
		mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mBtnSecurityCode.setText(getText(mMyResources.getString("lib_droi_account_get_security_code_text")) + "(" + RETRY_TIME + ")");
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		setupMargin();
	}
	
	private void setupMargin(){
		final int dimenLeft = 0;
		final int dimenRight = 0;
		if(mParent != null){
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)mParent.getLayoutParams();
			lp.leftMargin = dimenLeft;
			lp.rightMargin = dimenRight;
			mParent.setLayoutParams(lp);
		}
		if(mNextStep != null){
			final int buttonGAP = (int) getResources().getDimension(mMyResources.getDimen("lib_droi_account_security_btn_gap"));
			LinearLayout.LayoutParams btnLp = (LinearLayout.LayoutParams)mNextStep.getLayoutParams();
			btnLp.leftMargin = buttonGAP;
			mNextStep.setLayoutParams(btnLp);
		}
	}
	 @Override
	 protected Dialog onCreateDialog(int id, Bundle args) {
		 final ProgressDialog dialog = new ProgressDialog(this);
		 dialog.setMessage(getString(mMyResources.getString("lib_droi_account_authenticate_login")));
	     dialog.setIndeterminate(true);
	     dialog.setCancelable(true);
	     dialog.setOnCancelListener(new DialogInterface.OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				if(mLoginTask != null){
					mLoginTask.cancel(true);
				}
			}
	    	 
	     });
	     mProgressDialog = dialog;
	     return dialog;
	}
	
	private void showMessage(int stringId){
		Utils.showMessage(SecurityCodeActivity.this, getResources().getText(stringId));
	}
	 
	private void showProgress(){
	        showDialog(0);
	}
	
	private void hideProgress(){
        if (mProgressDialog != null){
            mProgressDialog.dismiss();
        }
	}

	public void onBack(View view){
		onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//setResult(AuthenticatorActivity.ERROR_CODE);
		mTimeCount.cancel();
		Intent intent = new Intent();
		intent.putExtra(Constants.CODE_TYPE, mCodeType);
		setResult(RESULT_CANCELED, intent);
		super.onBackPressed();
	}
	
    @Override
    protected void onDestroy(){
        if (mSmsReceiver != null){
            unregisterReceiver(mSmsReceiver);
        }
        if(mTask != null){
        	mTask.cancel(true);
        	mTask = null;
        }
        if(mLoginTask != null){
        	mLoginTask.cancel(true);
        	mLoginTask = null;
        }
    	super.onDestroy();
    }
    
    public void onAuthenticationResult(){
    	mLoginTask = null;
    	hideProgress();
    }
    
    public void onAuthenticationCancel(){
    	mLoginTask = null;
    	hideProgress();
    }
    
	class GetSecurityCode extends AsyncTask<Void, Void, String>{

		private String mUserName;
		
		public GetSecurityCode(String userName){
			mUserName = userName;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Map<String, String> rawParams = new HashMap<String, String>();
			rawParams.put("uid", mUserName);
			rawParams.put("codetype", mCodeType/*"userreg"*/);
			String signString;
			if(Constants.CODE_TYPE_BIND_MOBILE.equals(mCodeType)){
				rawParams.put("token", mBindToken);
				signString =  MD5Util.md5(mUserName + mCodeType + mBindToken + Constants.SIGNKEY);
			}else{
				signString =  MD5Util.md5(mUserName + mCodeType/*"userreg"*/ + Constants.SIGNKEY);
			}
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "getCode name : " + mUserName + ", type = " + mCodeType);
			}
			rawParams.put("sign", signString);
			String result = null;
			try{
				result = HttpOperation.postRequest(Constants.ACCOUNT_REGISTER, rawParams);
			}catch(Exception e){
				e.printStackTrace();
			}
			return result;
		}
		
        @Override
        protected void onPostExecute(String result){
        	super.onPostExecute(result);
        	JSONObject jsonObject = null;
        	if(DebugUtils.DEBUG){
        		DebugUtils.i(TAG, "GET security result : " + result);
        	}
        	if (!TextUtils.isEmpty(result)){
        		try {
					jsonObject = new JSONObject(result);
					int re = jsonObject.getInt("result");
					if(re == 0){
						TEMP_TOKEN = jsonObject.getString("token");
						UID = jsonObject.getString(Constants.JSON_S_UID);
						//REGEX = (String)jsonObject.get("smspattern");
					}else if(re < 0){
						mTimeCount.onFinish();
						mTimeCount.cancel();
					}
					String desc = jsonObject.getString("desc");
					if(!TextUtils.isEmpty(desc)){
						Utils.showMessage(getBaseContext(), desc.trim());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}else{
        		mTimeCount.onFinish();
        		mTimeCount.cancel();
        		Utils.showMessage(getBaseContext(), mMyResources.getString("lib_droi_account_server_exception"));
        	}
        }
	}
	
	class TimeCount extends CountDownTimer{
		
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        
		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			mBtnSecurityCode.setText(getText(mMyResources.getString("lib_droi_account_get_security_code_text")) + "(" + RETRY_TIME + ")");
			mBtnSecurityCode.setClickable(true);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			mBtnSecurityCode.setClickable(false);
			mBtnSecurityCode.setText(getText(mMyResources.getString("lib_droi_account_retry_security_code"))+ "(" + millisUntilFinished/1000 + ")");
		}
		
	}
	
	private class SmsReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(SMS_RECEIVED.equals(intent.getAction())){
				StringBuilder sb = new StringBuilder();
				Bundle bundle = intent.getExtras();
				if(bundle != null){
					Object[] pdus = (Object[])bundle.get("pdus");
					SmsMessage[] msg = new SmsMessage[pdus.length];
					for(int i = 0; i < pdus.length; i++){
						msg[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
					}
					for(SmsMessage curMsg : msg){
						sb.append(curMsg.getDisplayMessageBody());
					}
					if(!TextUtils.isEmpty(REGEX)){
						Pattern pattern = Pattern.compile(REGEX);
						Matcher matcher = pattern.matcher(sb.toString());
						boolean rs = matcher.find();
						if(DEBUG){
							Log.i(TAG, "found: " + rs + ", count: " + matcher.groupCount());
						}
						if(rs){
							for(int i = 0; i <= matcher.groupCount(); i++){
								if(mCheckbox.isChecked()){
									mSecurityBox.setText(matcher.group(i));
									if(DEBUG){
										Log.i(TAG, "text: " + matcher.group(i));
									}
									mTimeCount.cancel();
									mBtnSecurityCode.setText(getText(mMyResources.getString("lib_droi_account_retry_security_code"))+ "(" + RETRY_TIME + ")");
									mBtnSecurityCode.setClickable(true);
								}
							}
						}
					}
				}
			}
		}
		
	}
	
	 class RegisterUser extends AsyncTask<Void, Object, String>{

		private String mUserName;
		private String mPassword;
		private String mSecurityCode;
		
		public RegisterUser(String userName, String password, String securityCode){
			mUserName = userName;
			mPassword = password;
			mSecurityCode = securityCode;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			if(TextUtils.isEmpty(TEMP_TOKEN)){
				return null;
			}
			
			String signString = MD5Util.md5(TEMP_TOKEN + mPassword + "randreg" +" "+ mSecurityCode + Constants.SIGNKEY);
			Map<String, String> rawParams = new HashMap<String, String>();
			rawParams.put("token", TEMP_TOKEN);
			rawParams.put("passwd", mPassword);
			rawParams.put("regtype", "randreg");
			rawParams.put("randcode", mSecurityCode);
			rawParams.put("sign", signString);
			rawParams.put("devinfo", " ");
			String result = null;
			try{
				result = HttpOperation.postRequest(Constants.ACCOUNT_REGISTER_SIGNUP, rawParams);
			}catch(Exception e){
				e.printStackTrace();
			}
			return result;
		}
		
		@Override
        protected void onPostExecute(String result){
			super.onPostExecute(result);
			dismissProgressbar();
			if (!TextUtils.isEmpty(result)){
				try {
					JSONObject jsonObject = new JSONObject(result);
					int rst = jsonObject.getInt("result");
					if(rst == 0){
						showProgress();
						mLoginTask = new UserLoginTask(mUserName, mPassword);
						mLoginTask.execute();
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_tip_register_wrong"));
                    	Utils.showMessage(SecurityCodeActivity.this, desc);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if(TextUtils.isEmpty(TEMP_TOKEN)){
				Utils.showMessage(getApplicationContext(), getResources().getText(mMyResources.getString("lib_droi_account_tip_code_wrong")));
			}else{
				Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_server_exception"));
			}
		}
		 
	 }
	 
	 
	 public class UserLoginTask extends AsyncTask<Void, Void, String>{

		 private String mUserName;
		 private String mPassword;
		 
		 public UserLoginTask(String username, String password){
			 mUserName = username;
			 mPassword = password;
		 }
		 
		@Override
		protected String doInBackground(Void... params) {
			// TODO Autogenerated method stub
			String passwdMD5 = MD5Util.md5(mPassword);
			final Map<String, String> loginParams = new HashMap<String, String>();
			loginParams.put(Constants.JSON_S_UID, mUserName);
			loginParams.put(Constants.JSON_S_PWD, passwdMD5);
			loginParams.put("utype", "zhuoyou");
			loginParams.put("devinfo", " ");
			loginParams.put("sign", MD5Util.md5(mUserName+passwdMD5 + "zhuoyou" + " "+Constants.SIGNKEY));
			
			String loginResult = null;
			try{
				loginResult = HttpOperation.postRequest(Constants.ACCOUNT_LOGIN, loginParams);
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
						String token = jsonObject.getString("token");
						String openid = jsonObject.getString("openid");
						String gender = jsonObject.getString("gender");
						Intent intent = new Intent();
						intent.putExtra("token", token);
						intent.putExtra("pwd", mPassword);
						intent.putExtra("openid", openid);
						intent.putExtra(Constants.CODE_TYPE, mCodeType);
						intent.putExtra("gender", gender);
						setResult(RESULT_OK, intent);
						finish();
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_login_fail"));
                    	Utils.showMessage(SecurityCodeActivity.this, desc);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}else{
        		Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_login_fail"));
        	}
        }
        
        @Override
        protected void onCancelled(){
        	onAuthenticationCancel();
        }
		 
	 }
	 
	 private class CheckSecurityCode extends AsyncTask<Void, Void, String>{

		private final String token;
		private final String securityCode;
		
		public CheckSecurityCode(String token, String securityCode){
			this.token = token;
			this.securityCode = securityCode;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			final Map<String, String> checkParams = new HashMap<String, String>();
			checkParams.put("token", token);
			checkParams.put("randcode", securityCode);
			checkParams.put("sign", MD5Util.md5(token + securityCode + Constants.SIGNKEY));
			String result = null;
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "CheckCode name : " + securityCode + ", token:" + token);
			}
			try{
				result = HttpOperation.postRequest(Constants.ACCOUNT_CHECK_SECURITY, checkParams);
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
        		JSONObject jsonObject;
				try {
					if(DebugUtils.DEBUG){
						DebugUtils.i(TAG, "checkSecerity : " + result);
					}
					jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == 0){

						mTimeCount.cancel();
						/*
						if(Utils.getAvailableNetWorkType(SecurityCodeActivity.this) == -1){
							showMessage(mMyResources.getString("lib_droi_account_network_wrong_text);
							return;
						}*/
						if(Constants.CODE_TYPE_REG.equals(mCodeType)){
							//showProgressbar(getResources().getText(mMyResources.getString("lib_droi_account_tip_register_now).toString());
							//new RegisterUser(mPhoneNumber, securityCode, securityCode).execute();
							//现在验证码不作为密码，那么仅仅传回token，和验证码即可
							Intent intent = new Intent();
							intent.putExtra("token", TEMP_TOKEN);
							intent.putExtra("securityCode", securityCode);
							intent.putExtra(Constants.CODE_TYPE, Constants.CODE_TYPE_REG);
							intent.putExtra("phonenumber", mPhoneNumber);
							setResult(RESULT_OK, intent);
							finish();
						}else if(Constants.CODE_TYPE_RESET.equals(mCodeType)){
							Intent intent = new Intent();
							intent.putExtra("token", TEMP_TOKEN);
							intent.putExtra("uid", UID);
							intent.putExtra("securityCode", securityCode);
							intent.putExtra(Constants.CODE_TYPE, Constants.CODE_TYPE_RESET);
							setResult(RESULT_OK, intent);
							finish();
						}else if(Constants.CODE_TYPE_BIND_MOBILE.equals(mCodeType)){
							Intent intent = new Intent();
							intent.putExtra("token", TEMP_TOKEN);
							intent.putExtra("uid", UID);
							intent.putExtra("securityCode", securityCode);
							intent.putExtra(Constants.CODE_TYPE, Constants.CODE_TYPE_BIND_MOBILE);
							intent.putExtra("phonenumber", mPhoneNumber);
							setResult(RESULT_OK, intent);
							finish();
						}
					}else{
	                	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
	                		getResources().getString(mMyResources.getString("lib_droi_account_tip_code_wrong"));
	                	//Utils.showMessage(SecurityCodeActivity.this, desc);
	                	mSecurityBox.setError(desc);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

        	}else{
            	Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_tip_code_wrong"));
            	mSecurityBox.setError(getResources().getString(mMyResources.getString("lib_droi_account_tip_code_wrong")));
        	}
        }

	 }
}
