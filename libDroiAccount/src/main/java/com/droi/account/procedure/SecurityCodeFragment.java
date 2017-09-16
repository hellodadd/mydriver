package com.droi.account.procedure;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.droi.account.DebugUtils;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;

public class SecurityCodeFragment extends BackHandledFragment {
	private static final String TAG = "SecurityCodeFragment";
	
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
    private CheckBox mCheckbox;
    private String mCodeType = "userreg";
    private String mBindToken;
	
    private String mOldTitle;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(mMyResources.getLayout("lib_droi_account_security_code_fragment_layout"), null);
		findViewByIds(view);
		setupViews();
		return view;
	}
	
	protected void findViewByIds(View view){
		mPreviousStep = (Button)view.findViewById(mMyResources.getId("lib_droi_account_btn_previous"));
		mNextStep = (Button)view.findViewById(mMyResources.getId("lib_droi_account_btn_next"));
		mNextStep.setEnabled(false);
		mBtnSecurityCode = (Button)view.findViewById(mMyResources.getId("lib_droi_account_btn_get_security_code"));
		mSecurityBox = (EditText)view.findViewById(mMyResources.getId("lib_droi_account_security_code_box"));
		mCheckbox = (CheckBox)view.findViewById(mMyResources.getId("lib_droi_account_checkBox"));
	}
	
	protected void setupViews(){
		mSecurityBox.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				String securityCode = mSecurityBox.getText().toString();
				if(TextUtils.isEmpty(securityCode)){
					mNextStep.setEnabled(false);
					mNextStep.setTextColor(getResources().getColor(mMyResources.getColor("lib_droi_account_diabled_color")));
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
				
				
				if(Utils.getAvailableNetWorkType(getActivity()) == -1){
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
				showDialog(DIALOG_ON_PROGRESS, null);
				new CheckSecurityCode(TEMP_TOKEN, securityCode).execute();
				/*
				//test Code
				Intent intent = new Intent();
				intent.putExtra("token", TEMP_TOKEN);
				intent.putExtra("uid", UID);
				intent.putExtra("securityCode", "123455");
				intent.putExtra(Constants.CODE_TYPE, Constants.CODE_TYPE_BIND_MOBILE);
				intent.putExtra("phonenumber", mPhoneNumber);
				setResult(Activity.RESULT_OK, intent);
				//finish();*/

			}
		});
		mPreviousStep.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTimeCount.cancel();
				
				Intent intent = new Intent();
				intent.putExtra(Constants.CODE_TYPE, mCodeType);
				setResult(Activity.RESULT_CANCELED, intent);
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
				if(Utils.getAvailableNetWorkType(getActivity()) == -1){
					showMessage(mMyResources.getString("lib_droi_account_network_wrong_text"));
					return;
				}
				if(mPhoneNumber != null && !TextUtils.isEmpty(mPhoneNumber)){
					new GetSecurityCode(mPhoneNumber).execute();
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
	
	private void showMessage(int stringId){
		Utils.showMessage(getActivity(), getResources().getText(stringId));
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		final Activity activity = getActivity();
		//activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		mOldTitle = activity.getTitle().toString();
		activity.setTitle(mMyResources.getString("lib_droi_account_security_code_title"));
		Bundle bundle = getArguments();
		if(bundle != null){
			mPhoneNumber = bundle.getString("phonenumber");
			mCodeType = bundle.getString("codetype");
			mBindToken = bundle.getString("bindToken");
		}
		mTimeCount = new TimeCount(60 * 1000, 1000);
		mSmsReceiver = new SmsReceiver();
		activity.registerReceiver(mSmsReceiver, new IntentFilter(SMS_RECEIVED));
	}
	
	
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		final Activity activity = getActivity();
		//activity.getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
		//activity.getActionBar().setCustomView(null);
		if(!TextUtils.isEmpty(mOldTitle)){
			activity.setTitle(mOldTitle);
		}
	}
	
	class GetSecurityCode extends AsyncTask<Object, Object, String>{

		private String mUserName;
		
		public GetSecurityCode(String userName){
			mUserName = userName;
		}
		
		@Override
		protected String doInBackground(Object... arg0) {
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
						Utils.showMessage(getActivity(), desc.trim());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}else{
        		mTimeCount.onFinish();
        		mTimeCount.cancel();
        		Utils.showMessage(getActivity(), mMyResources.getString("lib_droi_account_server_exception"));
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
						if(DebugUtils.DEBUG){
							DebugUtils.i(TAG, "found: " + rs + ", count: " + matcher.groupCount());
						}
						if(rs){
							for(int i = 0; i <= matcher.groupCount(); i++){
								if(mCheckbox.isChecked()){
									mSecurityBox.setText(matcher.group(i));
									if(DebugUtils.DEBUG){
										DebugUtils.i(TAG, "text: " + matcher.group(i));
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
						if(Constants.CODE_TYPE_REG.equals(mCodeType)){
							//showProgressbar(getResources().getText(mMyResources.getString("lib_droi_account_tip_register_now).toString());
							//new RegisterUser(mPhoneNumber, securityCode, securityCode).execute();
							//现在验证码不作为密码，那么仅仅传回token，和验证码即可
							Intent intent = new Intent();
							intent.putExtra("token", TEMP_TOKEN);
							intent.putExtra("securityCode", securityCode);
							intent.putExtra(Constants.CODE_TYPE, Constants.CODE_TYPE_REG);
							intent.putExtra("phonenumber", mPhoneNumber);
							setResult(Activity.RESULT_OK, intent);
							//finish();
						}else if(Constants.CODE_TYPE_RESET.equals(mCodeType)){
							Intent intent = new Intent();
							intent.putExtra("token", TEMP_TOKEN);
							intent.putExtra("uid", UID);
							intent.putExtra("securityCode", securityCode);
							intent.putExtra(Constants.CODE_TYPE, Constants.CODE_TYPE_RESET);
							setResult(Activity.RESULT_OK, intent);
							//finish();
						}else if(Constants.CODE_TYPE_BIND_MOBILE.equals(mCodeType)){
							Intent intent = new Intent();
							intent.putExtra("token", TEMP_TOKEN);
							intent.putExtra("uid", UID);
							intent.putExtra("securityCode", securityCode);
							intent.putExtra(Constants.CODE_TYPE, Constants.CODE_TYPE_BIND_MOBILE);
							intent.putExtra("phonenumber", mPhoneNumber);
							setResult(Activity.RESULT_OK, intent);
							//finish();
						}
					}else{
	                	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
	                		getResources().getString(mMyResources.getString("lib_droi_account_tip_code_wrong"));
	                	mSecurityBox.setError(desc);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

       	}else{
           	showMessage(mMyResources.getString("lib_droi_account_tip_code_wrong"));
           	mSecurityBox.setError(getResources().getString(mMyResources.getString("lib_droi_account_tip_code_wrong")));
       	}
       }

	 }
	
}
