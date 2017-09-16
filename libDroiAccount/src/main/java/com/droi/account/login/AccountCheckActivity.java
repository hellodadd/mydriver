package com.droi.account.login;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;


import com.droi.account.MyResource;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;
import com.droi.account.view.AccountAutoCompleteTextView;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class AccountCheckActivity extends Activity implements TextWatcher{
	private static final boolean DEBUG = Utils.DEBUG;
	private static final String TAG = "RegisterActivity";
	private static final int CHECK_PRIVACY_POLICY = 1;
	
	private static final int DIALOG_ON_PROGRESS = 100;
	private static final int DIALOG_APP_ERROR = DIALOG_ON_PROGRESS + 1;
	private Button mOkBtn;
	private AccountAutoCompleteTextView mAcccountView;
	private TextView mPrivacyPolicy;
	private String mHint = "";
	private int mType = 0;
	private CheckBox mCheckBox;
	private MyResource mMyResources = new MyResource(this);
	private ProgressDialog mProgressDialog = null;
	private CheckAccountExist mCheckTask;
	
	private TextView mAccountDes;
	private AsyncTask<Void, Void, String> mTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
	    if(Utils.SHOW_ACTION_BAR){
	    	 ActionBar actionBar = getActionBar();
	    	 if(actionBar != null){
	    		 actionBar.setDisplayOptions(
		   	                ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
	    	 }
	    }
		setTitle(mMyResources.getString("lib_droi_account_register_title"));
		Intent intent = getIntent();
		if(intent != null){
			String title = intent.getStringExtra("title");
			mHint = intent.getStringExtra("hint");
			mType = intent.getIntExtra("accountType", 0);
			if(!TextUtils.isEmpty(title)){
				setTitle(title);
			}
		}
		setContentView(mMyResources.getLayout("lib_droi_account_check_layout"));
		mOkBtn = (Button) findViewById(mMyResources.getId("lib_droi_account_ok"));
		mPrivacyPolicy = (TextView)findViewById(mMyResources.getId("lib_droi_account_privacy_policy"));
		mCheckBox = (CheckBox)findViewById(mMyResources.getId("lib_droi_account_user_contract"));
		mAccountDes = (TextView)findViewById(mMyResources.getId("lib_droi_account_account_description"));
		setupViews();
		initViewsState(intent);
	}
	
	private void initViewsState(Intent intent){
		if(mAccountDes != null && intent != null){
			if(mType == Constants.ACCOUNT_TYPE_PHONENUMBER){
				String binded_phone = intent.getStringExtra("binded_phone");
				if(!TextUtils.isEmpty(binded_phone)){
					String desc = String.format(getString(mMyResources.getString("lib_droi_account_phone_check_desc")), binded_phone);
					mAccountDes.setText(desc);
					mAccountDes.setVisibility(View.VISIBLE);
				}else{
					mAccountDes.setVisibility(View.GONE);
				}
			}else if(mType == Constants.ACCOUNT_TYPE_EMAIL){
				String binded_email = intent.getStringExtra("binded_email");
				if(!TextUtils.isEmpty(binded_email)){
					String desc = String.format(getString(mMyResources.getString("lib_droi_account_email_check_desc")), binded_email);
					mAccountDes.setText(desc);
					mAccountDes.setVisibility(View.VISIBLE);
				}else{
					mAccountDes.setVisibility(View.GONE);
				}
			}else{
				mAccountDes.setVisibility(View.GONE);
			}
		}
	}
	
	@Override
	 protected Dialog onCreateDialog(int id, Bundle args) {
		 Dialog dialog = null;
		 if(DIALOG_ON_PROGRESS == id){
			 final ProgressDialog progressDialog = new ProgressDialog(this);
			 progressDialog.setMessage(getString(mMyResources.getString("lib_droi_account_msg_on_process")));
			 progressDialog.setIndeterminate(true);
			 progressDialog.setCancelable(true);
			 progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
	
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					if(mCheckTask != null){
					    mCheckTask.cancel(true);
					}
				}
		    	 
		     });
		     mProgressDialog = progressDialog;
		     dialog = progressDialog;
		 }else if(DIALOG_APP_ERROR == id){
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 builder.setTitle(mMyResources.getString("lib_droi_account_dialog_title_hint"));
			 builder.setCancelable(false);
			 builder.setMessage(mMyResources.getString("lib_droi_account_network_wrong_text"));
			 builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						//finish();
					}
					 
				 });
			 dialog = builder.create();
		 }
		 
		 return dialog;
	}
	
	private void showProgress(){
		   showDialog(DIALOG_ON_PROGRESS);
	}
		
	private void hideProgress(){
	    if (mProgressDialog != null){
	        mProgressDialog.dismiss();
	    }
	}
	
	private void setupViews(){
		mOkBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//start different activity depends on the input text
				String accountName = mAcccountView.getText().toString().trim();
				int accountType = Utils.getAccountType(accountName);
				/*if(accountType != -1){
					
					Intent intent = new Intent();
					intent.putExtra("accountname", mAcccountView.getText().toString().trim());
					intent.putExtra("accountType", accountType);
					setResult(RESULT_OK, intent);
					finish();

				}else */
				 if(Utils.getAvailableNetWorkType(AccountCheckActivity.this) == -1){
					  showDialog(DIALOG_APP_ERROR);
					  return;
				 }
				 
				if(!mCheckBox.isChecked()){
					Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_need_to_agree_plicy_contrat"));
					return;
				}

				if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
					showProgress();
					mTask = new CheckAccountExist(accountName, "mobile");
					mTask.execute();
				}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
					showProgress();
					mTask = new CheckAccountExist(accountName, "mail");
					mTask.execute();
				}else{
					mAcccountView.requestFocus();
					if(mType == Constants.ACCOUNT_TYPE_PHONENUMBER){
						mAcccountView.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_phonenumer")).toString());
					}else if(mType == Constants.ACCOUNT_TYPE_EMAIL){
						mAcccountView.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_email")).toString());
					}else{
						mAcccountView.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_accountname")).toString());
					}
				}
				
			}
			
		});
		
		mAcccountView = (AccountAutoCompleteTextView)findViewById(mMyResources.getId("lib_droi_account_account_name"));
		if(mType == Constants.ACCOUNT_TYPE_PHONENUMBER){
			mAcccountView.setInputType(InputType.TYPE_CLASS_NUMBER);
		}
		if(!TextUtils.isEmpty(mHint)){
			mAcccountView.setHint(mHint);
		}
		mAcccountView.addTextChangedListener(this);
		if(mPrivacyPolicy != null){
			mPrivacyPolicy.setClickable(true);
			mPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent();
					intent.setClass(AccountCheckActivity.this, PrivacyPolicy.class);
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
	
	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		if(DEBUG){
			Log.i(TAG, "s = " + s);
		}
	}
	
	public void onBack(View view) {
		onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		String accountName = mAcccountView.getText().toString().trim();
		int accountType = Utils.getAccountType(accountName);
		Intent intent = new Intent();
		intent.putExtra("accountname", accountName);
		intent.putExtra("accountType", accountType);
		setResult(RESULT_CANCELED, intent);
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(mTask != null){
			mTask.cancel(true);
			mTask = null;
		}
		super.onDestroy();
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
        	hideProgress();
        	if (!TextUtils.isEmpty(result)){
        		JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					//-2002 account exits, 0 account doesnot exit
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
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return true;
	}
}
