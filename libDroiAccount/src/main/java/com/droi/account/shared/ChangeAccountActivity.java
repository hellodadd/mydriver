package com.droi.account.shared;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.droi.account.Utils;
import com.droi.account.login.BaseActivity;
import com.droi.account.login.CheckPwdListener;
import com.droi.account.login.CheckPwdTask;
import com.droi.account.login.SharedInfo;

public class ChangeAccountActivity extends BaseActivity{

	private TextView mTitle;
	private Button mBtnCancle;
	private Button mBtnOk;
	private EditText mPassword;
	private DroiSDKHelper mDroidAccount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mDroidAccount = DroiSDKHelper.getInstance(this);
		SharedInfo sharedInfo = SharedInfo.getInstance();
		String pwdVal = sharedInfo.getPasswdVal(getApplicationContext());
		if("0".equals(pwdVal) == false){
			mDroidAccount.reLogin();
			finish();
			return;
		}else{
			setContentView(mMyResources.getLayout("lib_droi_account_layout_change_account"));
		}
		
		findViewByIds();
		setupViews();
	}
	
	private void findViewByIds(){
		mTitle = (TextView)findViewById(mMyResources.getId("lib_droi_account_title"));
		mBtnCancle = (Button)findViewById(mMyResources.getId("lib_droi_account_btn_cancle"));
		mBtnOk = (Button)findViewById(mMyResources.getId("lib_droi_account_btn_ok"));
		mPassword = (EditText)findViewById(mMyResources.getId("lib_droi_account_password"));
	}
	
	private void setupViews(){
		String userName = mDroidAccount.getUserName();
		String showtext = getResources().getString(mMyResources.getString("lib_droi_account_change_account_dialog_hint"))+"\n\n" + userName + "\n";
		mTitle.setText(showtext);
		
		mBtnCancle.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		mBtnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SharedInfo sharedInfo = SharedInfo.getInstance();
				final String  password = mPassword.getText().toString().trim();
				if(TextUtils.isEmpty(password)){
					Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_toast_passwd_empty"));
					return;
				}
				showProgressbar(getString(mMyResources.getString("lib_droi_account_msg_on_process")));
				new CheckPwdTask(sharedInfo.getOpenId(getApplicationContext()), 
						sharedInfo.getAccessToken(getApplicationContext()), 
						password, new CheckPwdListener() {
						
					@Override
					public void onResult(boolean result) {
							// TODO Auto-generated method stub
						dismissProgressbar();
						if(result){
							mDroidAccount.reLogin();
							finish();
						}else{
							Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_dialog_error_pwd"));
						}
					}
				}).execute();
			}
			
		});
	}
	
	public void onBack(View view){
		onBackPressed();
	}
}
