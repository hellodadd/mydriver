package com.droi.account.login;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.droi.account.Utils;
import com.droi.account.authenticator.AuthenticatorActivity;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;
import com.droi.account.shared.DroiSDKHelper;
import com.droi.account.view.AccountAutoCompleteTextView;


public class FindCodeActivity extends AccountCheckBaseActivity {

	private Button mOkBtn;
	private AccountAutoCompleteTextView mAcccountView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setTitle(mMyResources.getString("lib_droi_account_find_code_title"));
		setContentView(mMyResources.getLayout("lib_droi_account_find_code_layout"));
		mOkBtn = (Button) findViewById(mMyResources.getId("lib_droi_account_ok"));
		setupViews();
	}

	private void setupViews(){
		mAcccountView = (AccountAutoCompleteTextView)findViewById(mMyResources.getId("lib_droi_account_account_name"));
		if(DroiSDKHelper.PHONE_LOGIN){
			if(DroiSDKHelper.EMAIL_LOGIN){

			}else{
				mAcccountView.setInputType(InputType.TYPE_CLASS_PHONE);
				mAcccountView.setHint(getResources().getText(mMyResources.getString("lib_droi_account_find_code_phone_hint")).toString());
			}
		}else if(DroiSDKHelper.EMAIL_LOGIN){
			mAcccountView.setHint(getResources().getText(mMyResources.getString("lib_droi_account_find_code_email_hint")).toString());
		}
		mOkBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//start different activity depends on the input text
				 if(Utils.getAvailableNetWorkType(FindCodeActivity.this) == -1){
					  showDialog(DIALOG_NETWORK_ERROR);
					  return;
				 }

				String accountName = mAcccountView.getText().toString().trim();
				int accountType = Utils.getAccountType(accountName);
				if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER && DroiSDKHelper.PHONE_LOGIN){
					checkAccount(accountName);
				}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL && DroiSDKHelper.EMAIL_LOGIN){
					checkAccount(accountName);
				}else{
					mAcccountView.requestFocus();
					if(DroiSDKHelper.PHONE_LOGIN){
						if(DroiSDKHelper.EMAIL_LOGIN){
							mAcccountView.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_accountname")).toString());
						}else{
							mAcccountView.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_phonenumer")).toString());
						}
					}else if(DroiSDKHelper.EMAIL_LOGIN){
						mAcccountView.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_email")).toString());
					}
				}
			}

		});

	}

	private int getAccountType(String accountName){
		if(accountName == null){
			return -1;
		}

		if(isNumber(accountName)){
			if(accountName.length() == 11 && accountName.startsWith("1")){
				return AuthenticatorActivity.RESET_BY_PHONENUMBER;
			}
		}else if(isEmailAddress(accountName)){
			return AuthenticatorActivity.RESET_BY_EMAIL;
		}
		return -1;
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

	@Override
	protected void onCheckResult(boolean result, String msg) {
		// TODO Auto-generated method stub
		if(result){
			int accountType = getAccountType(mAcccountView.getText().toString().trim());
			Intent intent = new Intent();
			intent.putExtra("accountname", mAcccountView.getText().toString().trim());
			intent.putExtra("accountType", accountType);
			setResult(RESULT_OK, intent);
			finish();
		}else{
			if(mAcccountView != null && !TextUtils.isEmpty(msg)){
				mAcccountView.requestFocus();
				mAcccountView.setError(msg);
			}
		}
	}


	//check whether the account is locked.
	private class CheckAccountLocked extends AsyncTask<Void, Void, String> {
		private final String mAccountName;
		private String mUtype;
		public CheckAccountLocked(String accountName){
			mAccountName = accountName;
			int accountType = Utils.getAccountType(accountName);
			if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
				mUtype = "zhuoyou";
			}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
				mUtype = "mail";
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			final Map<String, String> checkParams = new HashMap<String, String>();
			checkParams.put("uid", mAccountName);
			checkParams.put("utype", mUtype);
			checkParams.put("sign", MD5Util.md5(mAccountName +mUtype+ Constants.SIGNKEY));

			String result = null;
			try{
				result = HttpOperation.postRequest(Constants.ACCOUNT_CHECK_LOCKED, checkParams);
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
					jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == 0){
						int accountType = getAccountType(mAcccountView.getText().toString().trim());
						Intent intent = new Intent();
						intent.putExtra("accountname", mAcccountView.getText().toString().trim());
						intent.putExtra("accountType", accountType);
						setResult(RESULT_OK, intent);
						finish();
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") :
                    		getResources().getString(mMyResources.getString("lib_droi_account_toast_account_locked"));
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
        		Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_toast_account_locked"));
        	}
	     }
	}
}
