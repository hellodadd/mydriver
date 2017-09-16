package com.droi.account.setup;


import com.droi.account.MyResource;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SetPwdWhenBinded extends Activity {
	
	private EditText mNewPwd;
	private EditText mConfirmPwd;
	private Button mFinish;
	private String mAccountName;
	private int mAccountType;
	
	//for bind mobile begin ...
	private String mSecurityCode;
	private String mUid;
	private String mToken;
	//for bind mobile end ...
	protected MyResource mMyResources = new MyResource(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if(intent != null){
			mAccountName = intent.getStringExtra("accountName");
			mAccountType = intent.getIntExtra("accountType", -1);
			mUid = intent.getStringExtra("uid");
			mSecurityCode = intent.getStringExtra("securityCode");
			mToken = intent.getStringExtra("token");
		}
		setTitle(mMyResources.getString("lib_droi_account_create_psw_title"));
		setContentView(mMyResources.getLayout("lib_droi_account_set_password_layout"));
		mFinish = (Button) findViewById(mMyResources.getId("lib_droi_account_finish"));
		setupViews();
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
						Intent intent = new Intent();
						if(mAccountType == Constants.ACCOUNT_TYPE_EMAIL){
							intent.putExtra("accountName", mAccountName);
						}else if(mAccountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
							intent.putExtra("uid", mUid);
							intent.putExtra("securityCode", mSecurityCode);
							intent.putExtra("token", mToken);
						}
						intent.putExtra(Constants.JSON_S_PWD, password);
						intent.putExtra("accountType", mAccountType);
						setResult(RESULT_OK, intent);
						finish();
					}
				}
				
			});
		}
		mNewPwd = (EditText)findViewById(mMyResources.getId("lib_droi_account_new_pwd"));
		mConfirmPwd = (EditText)findViewById(mMyResources.getId("lib_droi_account_confirm_pwd"));
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
		Intent intent = new Intent();
		if(mAccountType == Constants.ACCOUNT_TYPE_EMAIL){
			intent.putExtra("accountName", mAccountName);
		}else if(mAccountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
			intent.putExtra("uid", mUid);
			intent.putExtra("accountName", mAccountName);
			//intent.putExtra("securityCode", mSecurityCode);
			//intent.putExtra("token", mToken);
		}
		intent.putExtra("accountType", mAccountType);
		setResult(RESULT_CANCELED, intent);
		super.onBackPressed();
	}
}
