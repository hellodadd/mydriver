package com.droi.account.login;




import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class FindCodeEmailActivity extends BaseActivity {

	private TextView mTextView;
	private Button mBtnResend;
	private Button mFinish;
	private String mEmail;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		setTitle(mMyResources.getString("lib_droi_account_send_email_title"));
		if(intent != null){
			mEmail = intent.getStringExtra("email");
		}
		setContentView(mMyResources.getLayout("lib_droi_account_send_email_layout"));
		mTextView = (TextView)findViewById(mMyResources.getId("lib_droi_account_tv_address"));
		if(!TextUtils.isEmpty(mEmail)){
			mTextView.setText(mEmail);
		}
		mBtnResend = (Button)findViewById(mMyResources.getId("lib_droi_account_btn_resend"));
		mFinish = (Button) findViewById(mMyResources.getId("lib_droi_account_btn_finish"));
		setupViews();
	}
	
	private void setupViews(){
		mBtnResend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setResult(RESULT_OK);
				finish();
			}
		});
		
		mFinish.setOnClickListener(new View.OnClickListener() {
			
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
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//setResult(RESULT_CANCELED);
		//super.onBackPressed();
	}
	
	
}
