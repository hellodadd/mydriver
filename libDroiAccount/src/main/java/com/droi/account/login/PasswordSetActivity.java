package com.droi.account.login;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.droi.account.DebugUtils;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;
import com.droi.account.statis.AccountOperation;
import com.droi.account.statis.StaticsCallback;

public class PasswordSetActivity extends BaseActivity {
	private static final String TAG = "PasswordSetActivity";
	
	private Button mFinish;
	private EditText mNewPwd;
	private EditText mConfirmPwd;
	private String mOldPwd = "";
	private String mToken = "";
	private String mRequestType = "";
	private String mSecurityCode = "";
    private UserLoginTask mAuthTask = null;
    private User mUserInfo = SharedInfo.getInstance().getData();;
    private String mUserName;
    private int mPasswdLength = Utils.PWSSWD_LENGTH_MAX;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if(intent != null){
			unpackingIntent(intent);
		}else{
			setResult(RESULT_CANCELED);
			finish();
		}
		setContentView(mMyResources.getLayout("lib_droi_account_set_password_layout"));
		mPasswdLength = getResources().getInteger(mMyResources.getInteger("lib_droi_account_default_passwd_length"));
		mFinish = (Button) findViewById(mMyResources.getId("lib_droi_account_finish"));
		setupViews();
		 if(DebugUtils.DEBUG){
			 DebugUtils.i(TAG, "taskId = " + getTaskId());
		 }
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(mNewPwd != null){
			mNewPwd.requestFocus();
		}
	}
	
	private void unpackingIntent(Intent intent){
		String title = intent.getStringExtra("title");
		if(!TextUtils.isEmpty(title)){
			setTitle(title);
		}
		mUserName = intent.getStringExtra("username");
		mRequestType = intent.getStringExtra(Constants.REQUEST_TYPE);
		if(Constants.PASSWD_RESET.equals(mRequestType)){
			mSecurityCode = intent.getStringExtra("securityCode");
			mToken = intent.getStringExtra("token");
		}else if(Constants.PASSWD_MODIFY.equals(mRequestType)){
			mOldPwd = intent.getStringExtra("pwd");
			mToken = intent.getStringExtra("token");
		}else if(Constants.PASSWD_REGISTER_BY_EMAIL.equals(mRequestType)){
			
		}else if(Constants.PASSWD_REGISTER_BY_MOBILE.equals(mRequestType)){
			mSecurityCode = intent.getStringExtra("securityCode");
			mToken = intent.getStringExtra("token");
		}else{
			setResult(RESULT_CANCELED);
			finish();
		}
	}
	
	private void setupViews(){
		if(mFinish != null){
			mFinish.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(checkPassword()){
						String password_raw = mNewPwd.getText().toString().trim();
						String password = password_raw;//password_raw.toLowerCase();
						if(Utils.getAvailableNetWorkType(PasswordSetActivity.this) == -1){
							showMessage(mMyResources.getString("lib_droi_account_network_wrong_text"));
							return;
						}
						
						if(Constants.PASSWD_RESET.equals(mRequestType)){
							showProgressbar(getResources().getText(mMyResources.getString("lib_droi_account_msg_on_process")).toString());
							new ResetPwdTask(mToken, password, mSecurityCode).execute();
						}else if(Constants.PASSWD_MODIFY.equals(mRequestType)){
							String md5Old = MD5Util.md5(mOldPwd);
							String md5New = MD5Util.md5(password);
							if(md5New.equals(md5Old)){
								mNewPwd.requestFocus();
								mNewPwd.setError(getString(mMyResources.getString("lib_droi_account_reinput_passwd_text")));
								return;
							}
							showProgressbar(getResources().getText(mMyResources.getString("lib_droi_account_msg_on_process")).toString());
							new ModifyPwdTask(mToken, mOldPwd, password).execute();
						}else if(Constants.PASSWD_REGISTER_BY_EMAIL.equals(mRequestType)){
							Intent intent = new Intent();
							intent.putExtra("email", mUserName);
							intent.putExtra("pwd", password);
							setResult(RESULT_OK, intent);
							finish();
							/*
							showProgressbar(getResources().getText(mMyResources.getString("lib_droi_account_msg_on_process).toString());
							//使用邮箱注册账号
							new RegisterByEmail(mUserName, password, "userreg").execute();
							*/
						}else if(Constants.PASSWD_REGISTER_BY_MOBILE.equals(mRequestType)){
							showProgressbar(getResources().getText(mMyResources.getString("lib_droi_account_msg_on_process")).toString());
							new RegisterByMobile(mUserName, password, mSecurityCode, mToken).execute();
						}
					}
				}
			});
		}
		
		mNewPwd = (EditText)findViewById(mMyResources.getId("lib_droi_account_new_pwd"));
		mNewPwd.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
			
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		mNewPwd.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.length() >= mPasswdLength){
					Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_max_length_reached"));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mConfirmPwd = (EditText)findViewById(mMyResources.getId("lib_droi_account_confirm_pwd"));
		mConfirmPwd.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
			
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		mConfirmPwd.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.length() >= mPasswdLength){
					Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_max_length_reached"));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
	}
	
	private void showMessage(int stringId){
		Utils.showMessage(PasswordSetActivity.this, getResources().getText(stringId));
	}
	
	private boolean checkPassword(){
		String pwd = mNewPwd.getText().toString().trim();
		String confirm_pwd = mConfirmPwd.getText().toString().trim();
		if(TextUtils.isEmpty(pwd)){
			mNewPwd.requestFocus();
			mNewPwd.setError(getResources().getText(mMyResources.getString("lib_droi_account_password_empty")).toString());
			return false;
		}
		
		if(TextUtils.isEmpty(confirm_pwd)){
			mConfirmPwd.requestFocus();
			mConfirmPwd.setError(getResources().getText(mMyResources.getString("lib_droi_account_password_empty")).toString());
			return false;
		}
		
		if(!Utils.isValidPassword(pwd)){
			mNewPwd.requestFocus();
			mNewPwd.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_password_rule")).toString());
			return false;
		}
		
		if(!Utils.isValidPassword(confirm_pwd)){
			mConfirmPwd.requestFocus();
			mConfirmPwd.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_password_rule")).toString());
			return false;
		}
		
		if(!pwd.equals(confirm_pwd)){
			mConfirmPwd.requestFocus();
			mConfirmPwd.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_pwd_not_same")).toString());
			return false;
		}
		if(Utils.onlyNumber(pwd)){
			mNewPwd.requestFocus();
			mNewPwd.setError(getResources().getText(mMyResources.getString("lib_droi_account_password_requires_letters")).toString());
			return false;
		}
		return true;
	}

	public void onBack(View view){
		onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//setResult(AuthenticatorActivity.ERROR_CODE);
		super.onBackPressed();
	}
	/*
	@Override    
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
		if(keyCode == KeyEvent.KEYCODE_BACK){      
			return  true;
		}  
		return  super.onKeyDown(keyCode, event);  
	}*/
	
	class ModifyPwdTask extends AsyncTask<Void, Void, String>{

		private String mToken;
		private String mOldPwd;
		private String mNewPwd;
		
		public ModifyPwdTask(String token, String oldPassword, String newPassword){
			mToken = token;
			mOldPwd = oldPassword;
			mNewPwd = newPassword;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Map<String, String> modifyPwdParams = new HashMap<String, String>();
			
			String passwdMD5 = MD5Util.md5(mOldPwd);
			String signString = MD5Util.md5(mToken + passwdMD5 + mNewPwd + Constants.SIGNKEY);
			modifyPwdParams.put(Constants.JSON_S_TOKEN, mToken);
			modifyPwdParams.put("oldpasswd", passwdMD5);
			modifyPwdParams.put("newpasswd", mNewPwd);
			modifyPwdParams.put("sign", signString);
			String result = null;
			try{
				result = HttpOperation.postRequest(Constants.ACCOUNT_MODIFY_PWD, modifyPwdParams);
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
					int rs = jsonObject.getInt("result");
					if(rs == 0){
						//修改密码之后，要求用户重新登陆，因为我们无法帮用户登陆，因为不知道用户名，密码
						//showProgressbar(getResources().getText(mMyResources.getString("lib_droi_account_ui_activity_authenticating).toString());
						//handleLogin(mUserName, mNewPwd);
						Intent intent = new Intent();
						setResult(RESULT_OK, intent);
						finish();
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_modify_pwd_error"));
                    	Utils.showMessage(PasswordSetActivity.this, desc);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_server_exception"));
			}
		}
		
	}
	
	class ResetPwdTask extends AsyncTask<Void, Void, String>{
		private final String mToken;
		private final String mPassword;
		private final String mSecurityCode;
		
		public ResetPwdTask (String token, String password, String securityCode){
			mToken = token;
			mPassword = password;
			mSecurityCode = securityCode;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
            if(TextUtils.isEmpty(mToken)){
            	return null;
            }
			Map<String, String> resetPwdParams = new HashMap<String, String>();
			String signString = MD5Util.md5(mToken + mPassword + mSecurityCode + Constants.SIGNKEY); 
			resetPwdParams.put(Constants.JSON_S_TOKEN, mToken);
			resetPwdParams.put(Constants.JSON_S_PWD, mPassword);
			resetPwdParams.put("randcode", mSecurityCode);
			resetPwdParams.put("sign", signString);
			String result = null;
			
            try  {
                result = HttpOperation.postRequest(Constants.ACCOUNT_RESET_PWD, resetPwdParams);
            } catch (Exception e) {
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
					int rs = jsonObject.getInt("result");
					if(rs == 0){
						//找回密码之后由用户自己登陆
						Intent intent = new Intent();
						intent.putExtra("userName", mUserName);
						//if the user is locked, login failed, so set password null
						intent.putExtra("password", "");
						intent.putExtra("findcode", true);
						setResult(RESULT_OK, intent);
						finish();
						//showProgressbar(getResources().getText(mMyResources.getString("lib_droi_account_ui_activity_authenticating).toString());
						//handleLogin(mUserName, mPassword);
					}else{
						String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
							getResources().getString(mMyResources.getString("lib_droi_account_tip_reset_password_failed"));
                		Utils.showMessage(getApplicationContext(), desc);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if(TextUtils.isEmpty(mToken)){
				Utils.showMessage(getApplicationContext(), 
						getResources().getText(mMyResources.getString("lib_droi_account_tip_code_wrong")));
			}else{
				Utils.showMessage(PasswordSetActivity.this, 
						getResources().getText(mMyResources.getString("lib_droi_account_server_exception")));
			}
		}
		
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
			dismissProgressbar();
			if (!TextUtils.isEmpty(result)){
				try {
					if(DebugUtils.DEBUG){
						DebugUtils.i("PassSetA", "Register by Email: result = " + result);
					} 
					JSONObject jsonObject = new JSONObject(result);
					int rst = jsonObject.getInt("result");
					if(rst == 0){
						showProgressbar(getResources().getText(mMyResources.getString("lib_droi_account_ui_activity_authenticating")).toString());
						handleLogin(mEmail, mPassword);
						//邮箱注册之后，用户不需要在邮箱里面确认，即可注册成功。
						//去 finish AuthenticatorActivity
						//Intent intent = new Intent();
						//intent.putExtra("password", mPassword);
						//setResult(RESULT_OK, intent);
						//finish();
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_tip_register_wrong"));
                    	Utils.showMessage(getApplicationContext(), desc);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_server_exception"));
			}
		}
		
	}
	
	class RegisterByMobile extends AsyncTask<Void, Void, String>{
		private String mUserName;
		private String mPassword;
		private String mSecurityCode;
		private String mRegisterToken;
		
		public RegisterByMobile(String userName, String password, String securityCode, String token){
			mUserName = userName;
			mPassword = password;
			mSecurityCode = securityCode;
			mRegisterToken = token;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			String signString = MD5Util.md5(mRegisterToken + mPassword + "randreg" +" "+ mSecurityCode + Constants.SIGNKEY);
			Map<String, String> rawParams = new HashMap<String, String>();
			rawParams.put("token", mRegisterToken);
			rawParams.put("passwd", mPassword);
			rawParams.put("regtype", "randreg");
			rawParams.put("randcode", mSecurityCode);
			rawParams.put("sign", signString);
			rawParams.put("devinfo", " ");
			StaticsCallback staticsCallback = StaticsCallback.getInstance(getApplicationContext());
			String loginfo = staticsCallback.encryptRegisteringLogInfo(mUserName, AccountOperation.ACCOUNT_REGISTER_TYPE_PHONE);
			rawParams.put("loginfo", loginfo);
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
						showProgressbar(getResources().getText(mMyResources.getString("lib_droi_account_ui_activity_authenticating")).toString());
						StaticsCallback staticsCallback = StaticsCallback.getInstance(getApplicationContext());
						staticsCallback.onRegisterResult(mUserName, AccountOperation.ACCOUNT_REGISTER_TYPE_PHONE, 
								jsonObject.has("openid") ? jsonObject.getString("openid") : "", true);
						
						handleLogin(mUserName, mPassword);
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_tip_register_wrong"));
                    	Utils.showMessage(PasswordSetActivity.this, desc);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if(TextUtils.isEmpty(mRegisterToken)){
				Utils.showMessage(getApplicationContext(), getResources().getText(mMyResources.getString("lib_droi_account_tip_code_wrong")));
			}else{
				Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_server_exception"));
			}
		}
		
	}
	
	private void handleLogin(final String userName, final String password){
		mAuthTask = new UserLoginTask(this, userName, password, mUserInfo, new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				dismissProgressbar();
				Intent intent = new Intent();
				intent.putExtra("userName", userName);
				//if the user is locked, login failed, so set password null
				if(mUserInfo.getResult() == 0){
					intent.putExtra("password", password);
				}
				setResult(RESULT_OK, intent);
				finish();
			}
        	 
         });
		mAuthTask.execute();
	}
	
}
