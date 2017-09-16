package com.droi.account;


import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.droi.account.login.BaseActivity;
import com.droi.account.login.PasswordSetActivity;

public class MainActivity extends BaseActivity implements View.OnClickListener{

	private Button mPwdSet;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//customActionBar();
		//ActionBar actionBar = getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(false);
		//actionBar.setDisplayShowHomeEnabled(false);
		//actionBar.set
		setContentView(mMyResources.getLayout("lib_droi_account_activity_main"));
		mPwdSet = (Button)findViewById(mMyResources.getId("lib_droi_account_pwd_set"));
		mPwdSet.setOnClickListener(this);
		String str = "sss";
		Log.i("xuyongfeng", "" + str.getBytes().length);

		//setTitle("aasaaa");
	}


	private void customActionBar(){
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setDisplayShowCustomEnabled(true);
		View view = getLayoutInflater().inflate(mMyResources.getLayout("lib_droi_account_actionbar_layout"), null);
		actionBar.setCustomView(view, new ActionBar.LayoutParams(
				ActionBar.LayoutParams.WRAP_CONTENT, 
				ActionBar.LayoutParams.WRAP_CONTENT, 
				Gravity.CENTER));
		TextView textView = (TextView)view.findViewById(mMyResources.getId("lib_droi_account_title"));
		if(textView != null){
			textView.setText(mMyResources.getString("lib_droi_account_login_account_title"));
		}
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if(DebugUtils.DEBUG){
			DebugUtils.i("test", "click ");
		}
		if(mMyResources.getId("lib_droi_account_pwd_set") == v.getId()){
			intent.setClass(this, PasswordSetActivity.class);
		}
		startActivity(intent);
	}
}
