package com.droi.account.setup;

import com.droi.account.MyResource;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class UserInfoUpdated extends Activity {

	protected MyResource mMyResources = new MyResource(this);
	private Button mBtnOk;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(mMyResources.getString("lib_droi_account_app_name"));
		setContentView(mMyResources.getLayout("lib_droi_account_layout_account_updated"));
		mBtnOk = (Button)findViewById(mMyResources.getId("lib_droi_account_btn_ok"));
		mBtnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setResult(RESULT_OK);
				finish();
			}
		});
	}
	

	public void onBack(View view){
		onBackPressed();
	}
	
}
