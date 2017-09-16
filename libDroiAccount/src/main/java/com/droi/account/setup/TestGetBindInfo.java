package com.droi.account.setup;

import com.droi.account.MyResource;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;



public class TestGetBindInfo extends Activity {
	private Button mFinish;
	private EditText mUid;
	private EditText mSecurityCode;
	private EditText mToken;
	protected MyResource mMyResources = new MyResource(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(mMyResources.getLayout("lib_droi_account_test_layout_get_bind_info"));
		setupViews();
	}
	
	private void setupViews(){
		mFinish = (Button) findViewById(mMyResources.getId("lib_droi_account_finish"));
		if(mFinish != null){
			mFinish.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String uid = mUid.getText().toString().trim();
					String securityCode = mSecurityCode.getText().toString().trim();
					String token = mToken.getText().toString().trim();
					Intent intent = new Intent();
					intent.putExtra("uid", uid);
					intent.putExtra("securityCode", securityCode);
					intent.putExtra("token", token);
					setResult(RESULT_OK, intent);
					finish();
				}
				
			});
		}
		mUid = (EditText)findViewById(mMyResources.getId("lib_droi_account_uid"));
		mSecurityCode = (EditText)findViewById(mMyResources.getId("lib_droi_account_securityCode"));
		mToken = (EditText)findViewById(mMyResources.getId("lib_droi_account_token"));
	}
	

	public void onBack(View view){
		onBackPressed();
	}
	
}
