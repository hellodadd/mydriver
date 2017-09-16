package com.droi.account.login;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;


import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;
import com.droi.account.view.AccountAutoCompleteTextView;

public class EmailInputActivity extends BaseActivity {
	private static final int CHECK_PRIVACY_POLICY = 1;
	private Button mOkBtn;
	private AccountAutoCompleteTextView mAcccountView;
	private CheckBox mCheckbox;
	private TextView mPrivacyPolicy;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTitle(mMyResources.getString("lib_droi_account_modify_active_email"));
		Intent intent = getIntent();
		if(intent != null){
			String title = intent.getStringExtra("title");
			if(!TextUtils.isEmpty(title)){
				setTitle(title);
			}
		}
		setContentView(mMyResources.getLayout("lib_droi_account_layout_input_email"));
		mOkBtn = (Button) findViewById(mMyResources.getId("lib_droi_account_ok"));
		mPrivacyPolicy = (TextView)findViewById(mMyResources.getId("lib_droi_account_privacy_policy"));
		setupViews();
	}
	
	private void setupViews(){
		mOkBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//start different activity depends on the input text
				 if(Utils.getAvailableNetWorkType(EmailInputActivity.this) == -1){
					  showNetWorkError();
					  return;
				 }	
				 
				if(!mCheckbox.isChecked()){
					Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_need_to_agree_plicy_contrat"));
					return;
				}
				String accountName = mAcccountView.getText().toString().trim();
				int accountType = Utils.getAccountType(accountName);
				/*if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
					showProgressbar(getString(mMyResources.getString("lib_droi_account_msg_on_process));
					new CheckAccountExist(accountName, "mobile").execute();
				}else */
				if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
					showProgressbar(getString(mMyResources.getString("lib_droi_account_msg_on_process")));
					new CheckAccountExist(accountName, "mail").execute();
				}else{
					mAcccountView.requestFocus();
					mAcccountView.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_email")).toString());
				}
			}
			
		});
		mCheckbox = (CheckBox)findViewById(mMyResources.getId("lib_droi_account_user_contract"));
		mAcccountView = (AccountAutoCompleteTextView)findViewById(mMyResources.getId("lib_droi_account_account_name"));
		if(mPrivacyPolicy != null){
			mPrivacyPolicy.setClickable(true);
			mPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent();
					intent.setClass(EmailInputActivity.this, PrivacyPolicy.class);
					startActivityForResult(intent, CHECK_PRIVACY_POLICY);
				}
			});
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == CHECK_PRIVACY_POLICY){
			
			return ;
		}
	}
	
	public void onBack(View view){
		onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		setResult(RESULT_CANCELED);
		super.onBackPressed();
	}
	
	private class CheckAccountExist extends AsyncTask<Void, Void, String>{

		private final String mAccountName;
		private final String mUserType;
		
		public CheckAccountExist(String accountName, String usertype){
			mAccountName = accountName;
			mUserType = usertype;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			final Map<String, String> checkParams = new HashMap<String, String>();
			checkParams.put("uid", mAccountName);
			checkParams.put("sign", MD5Util.md5(mAccountName + Constants.SIGNKEY));
			checkParams.put("usertype", mUserType);
			String result = null;
			try{
				result = HttpOperation.postRequest(Constants.ACCOUNT_CHECK_EXIST, checkParams);
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
        		JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					//0 : account doesnot exits
					if(rs == 0){
						int accountType = Utils.getAccountType(mAccountName);
						Intent intent = new Intent();
						intent.putExtra("accountname", mAccountName);
						intent.putExtra("accountType", accountType);
						setResult(RESULT_OK, intent);
						finish();
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_account_already_exits"));
                    	//Utils.showMessage(getApplicationContext(), desc);
                    	if(mAcccountView != null && !TextUtils.isEmpty(desc)){
                    		mAcccountView.requestFocus();
    						mAcccountView.setError(desc);
                    	}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

        	}else{
        		Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_account_already_exits"));
        	}
        }
	}
}
