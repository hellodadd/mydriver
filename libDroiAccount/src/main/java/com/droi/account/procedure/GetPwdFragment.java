package com.droi.account.procedure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.droi.account.DebugUtils;

import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;

public class GetPwdFragment extends BackHandledFragment {
	private static final String TAG = "GetPwdFragment";
	
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		final Activity activity = getActivity();
		activity.setTitle(mMyResources.getString("lib_droi_account_create_psw_title"));
		Bundle bundle = getArguments();
		if(bundle != null){
			mAccountName = bundle.getString("accountName");
			mAccountType = bundle.getInt("accountType", -1);
			mUid = bundle.getString("uid");
			mSecurityCode = bundle.getString("securityCode");
			mToken = bundle.getString("token");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(mMyResources.getLayout("lib_droi_account_set_password_layout"), null);
		findViewByIds(view);
		setupViews();
		return view;
	}
	
	
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void findViewByIds(View view) {
		// TODO Auto-generated method stub
		mFinish = (Button) view.findViewById(mMyResources.getId("lib_droi_account_finish"));
		mNewPwd = (EditText)view.findViewById(mMyResources.getId("lib_droi_account_new_pwd"));
		mConfirmPwd = (EditText)view.findViewById(mMyResources.getId("lib_droi_account_confirm_pwd"));
	}

	@Override
	protected void setupViews() {
		// TODO Auto-generated method stub
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
						setResult(Activity.RESULT_OK, intent);
					}
				}
				
			});
		}

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
		
		return true;
	}
	
	
	@Override
	protected boolean onBackPressed(){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onBackPressed");
		}
		Intent intent = new Intent();
		if(mAccountType == Constants.ACCOUNT_TYPE_EMAIL){
			intent.putExtra("accountName", mAccountName);
		}else if(mAccountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
			intent.putExtra("uid", mUid);
			intent.putExtra("accountName", mAccountName);
		}
		intent.putExtra("accountType", mAccountType);
		setResult(Activity.RESULT_CANCELED, intent);
		getFragmentManager().popBackStack();
		return false;
	}
}
