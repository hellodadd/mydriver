package com.droi.account.login;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.droi.account.DebugUtils;
import com.droi.account.MyResource;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;

abstract class AccountCheckBaseActivity extends Activity {
	private static final boolean DEBUG = Utils.DEBUG;
	private static final String TAG = "AccountCheckBaseActivity";
	
	private static final int DIALOG_ON_PROGRESS = 100;
	protected static final int DIALOG_NETWORK_ERROR = DIALOG_ON_PROGRESS + 1;

	private ProgressDialog mProgressDialog = null;
	private CheckAccountExist mCheckTask;
	protected MyResource mMyResources = new MyResource(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    if(Utils.SHOW_ACTION_BAR){
	    	 ActionBar actionBar = getActionBar();
	    	 if(actionBar != null){
	    		 actionBar.setDisplayOptions(
		   	                ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
	    	 }
	    }
		Intent intent = getIntent();
		if(intent != null){
			String title = intent.getStringExtra("title");
			if(!TextUtils.isEmpty(title)){
				setTitle(title);
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
		 }else if(DIALOG_NETWORK_ERROR == id){
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
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return true;
	}
	
	protected void showProgress(){
		   showDialog(DIALOG_ON_PROGRESS);
	}
		
	protected void hideProgress(){
	    if (mProgressDialog != null){
	        mProgressDialog.dismiss();
	    }
	}
	

	protected abstract void onCheckResult(boolean result, String msg);
	
	protected void checkAccount(String accountName){
		showProgress();
		new CheckAccountExist(accountName).execute();
	}
	
	protected boolean isNumber(String accountName){
		if(accountName == null){
			return false;
		}
		return Utils.isNumber(accountName);
	}
	
	protected boolean isEmailAddress(String accountName){
		if(accountName == null){
			return false;
		}
		return Utils.isValidEmailAddress(accountName);

	}
	
    private class CheckAccountExist extends AsyncTask<Void, Void, String>{

		private final String mAccountName;
		private String mUserType;
		
		public CheckAccountExist(String accountName){
			mAccountName = accountName;
			if(isEmailAddress(accountName)){
				mUserType = "mail";
			}else if(isNumber(accountName)){
				mUserType = "mobile";
			}else{
				mUserType = null;
			}
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			if(mUserType == null){
				return null;
			}
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
        	if(DebugUtils.DEBUG){
        		DebugUtils.d(TAG, result);
        	}
        	hideProgress();
        	if (!TextUtils.isEmpty(result)){
        		JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == -2002){
						/*
						int accountType = getAccountType(mAccountName);
						Intent intent = new Intent();
						intent.putExtra("accountname", mAccountName);
						intent.putExtra("accountType", accountType);
						setResult(RESULT_OK, intent);
						finish();*/
						onCheckResult(true, null);
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_no_account"));
                    	//Utils.showMessage(getApplicationContext(), desc);
                    	if(!TextUtils.isEmpty(desc)){
                    		onCheckResult(false, desc);
                    	}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					onCheckResult(false, null);
					e.printStackTrace();
				}

        	}else{
        		Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_no_account"));
        		onCheckResult(false, null);
        	}
        }
	}
}
