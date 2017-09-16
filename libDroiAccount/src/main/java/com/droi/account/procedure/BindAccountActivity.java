package com.droi.account.procedure;


import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;

import com.droi.account.DebugUtils;
import com.droi.account.MyResource;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.login.SharedInfo;
import com.droi.account.manager.ShareInfoManager;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;
import com.droi.account.shared.DroiSDKHelper;
import com.droi.account.statis.StaticsCallback;

public class BindAccountActivity extends FragmentActivity implements TextWatcher, IAcitivtyFragment{
	private static final String TAG = "BindAccountActivity";
	
	private static final int DIALOG_ON_PROGRESS = 100;
	private static final int DIALOG_APP_ERROR = DIALOG_ON_PROGRESS + 1;
	
	private static final int GET_SECURITY_CODE = 1;
	private static final int GET_PWD_WHEN_BINDED = GET_SECURITY_CODE + 1;
	private static final int GET_ACCOUNT_NAME = GET_PWD_WHEN_BINDED + 1;
	private static final int SEND_EMAIL = GET_ACCOUNT_NAME + 1;

	
	private ProgressDialog mProgressDialog = null;
	private BackHandledFragment mBackHandedFragment;
	protected MyResource mMyResources = new MyResource(this);
	private int mBintAccountType = 0;
	
	@Override
	protected void onCreate(Bundle arg0) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		Intent intent = getIntent();
		if(intent != null){
			mBintAccountType = intent.getIntExtra(DroiSDKHelper.BIND_ACCOUNT_TYPE, 0);
		}
		setContentView(mMyResources.getLayout("lib_droi_account_layout_bind_account"));
		AccountCheckFragment fragment = new AccountCheckFragment();
		Bundle data = new Bundle();
		data.putInt(DroiSDKHelper.BIND_ACCOUNT_TYPE, mBintAccountType);
		fragment.setArguments(data);
		loadFragmentForResult(fragment, "AccountCheckFragment", GET_ACCOUNT_NAME);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
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
	
	@Override
	public void onFragmentResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "requestCode = " + requestCode + ", resultCode = " + resultCode);
		}
		switch(requestCode){
			case GET_SECURITY_CODE:{
				//绑定手机，获取的验证码
				SharedInfo sharedInfo = SharedInfo.getInstance();
				String passwdVal = sharedInfo.getPasswdVal(this);
				if(resultCode == RESULT_OK){
					String uid = data.getStringExtra("uid");
					String token = data.getStringExtra("token");
					String securityCode = data.getStringExtra("securityCode");
					String phonenumber = data.getStringExtra("phonenumber");
					if("0".equals(passwdVal) == false){
						if(DebugUtils.DEBUG){
							DebugUtils.i(TAG, "goto GetPwdFragment");
						}
						//没有设置密码的账户，需要用户首先输入密码
						GetPwdFragment fragment = new GetPwdFragment();
						Bundle arguments = new Bundle();
						arguments.putString("uid", uid);
						arguments.putString("token", token);
						arguments.putString("securityCode", securityCode);
						arguments.putString("accountName", phonenumber);
						arguments.putInt("accountType", Constants.ACCOUNT_TYPE_PHONENUMBER);
						fragment.setArguments(arguments);
						loadFragmentForResult(fragment, "GetPwdFragment", GET_PWD_WHEN_BINDED);
					}else{
						doBindMobile(uid, securityCode, token, null);
					}
				}else if(resultCode == RESULT_CANCELED){

				}
			}
			break;
			case GET_ACCOUNT_NAME:{
				if(resultCode == RESULT_OK){
					String accountName = data.getStringExtra("accountname");
					int accountType =  data.getIntExtra("accountType", -1);
					bindAccount(accountName, accountType, null);
				}else if(resultCode == RESULT_CANCELED){
					//remove all fragment
					setResult(RESULT_CANCELED);
					finish();
				}
			}
			break;
			case GET_PWD_WHEN_BINDED:{
				if(resultCode == RESULT_OK){
					String accountName = data.getStringExtra("accountName");
					int accountType =  data.getIntExtra("accountType", -1);
					String password = data.getStringExtra(Constants.JSON_S_PWD);
					if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
						String uid = data.getStringExtra("uid");
						String securityCode = data.getStringExtra("securityCode");
						String token = data.getStringExtra("token");
						doBindMobile(uid, securityCode, token, password);
					}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
						doBindEmail(accountName, password);
					}
				}
			}
			break;
			case SEND_EMAIL:{
				if(resultCode == RESULT_OK){
					setResult(RESULT_OK);
					finish();
				}
			}
			break;
		
		}
	}
	
	private void bindAccount(String accountName, int accountType, String password){
		SharedInfo sharedInfo = SharedInfo.getInstance();
		String passwdVal = sharedInfo.getPasswdVal(this);
		if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
			SecurityCodeFragment fragment = new SecurityCodeFragment();
			Bundle data = new Bundle();
			data.putString("phonenumber", accountName);
			data.putString("codetype", Constants.CODE_TYPE_BIND_MOBILE);
			//此处为第三方登陆后，服务器返回的token
			data.putString("bindToken", sharedInfo.getAccessToken(getApplicationContext()));
			data.putString(Constants.ACCOUNT_PWD_VAL, passwdVal);
			fragment.setArguments(data);
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "bind phone");
			}
			loadFragmentForResult(fragment, "SecurityCodeFragment", GET_SECURITY_CODE);
		}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
			if("0".equals(passwdVal) == false){
				//没有设置密码的账户，需要用户首先输入密码
				GetPwdFragment fragment = new GetPwdFragment();
				Bundle arguments = new Bundle();
				arguments.putString("accountName", accountName);
				arguments.putInt("accountType", accountType);
				fragment.setArguments(arguments);
				loadFragmentForResult(fragment, "GetPwdFragment", GET_PWD_WHEN_BINDED);
			}else{
				doBindEmail(accountName, null);
			}
		}
	}
	private void showProgress(){
		   showDialog(DIALOG_ON_PROGRESS);
	}
		
	private void hideProgress(){
	    if (mProgressDialog != null){
	        mProgressDialog.dismiss();
	    }
	}
	
	@Override
	public void setSelectedFragment(BackHandledFragment selectedFragment) {
		// TODO Auto-generated method stub
		mBackHandedFragment = selectedFragment;
	}
	
	private void doBindMobile(String uid, String securityCode, String token, String password){
		showProgress();
		//绑定手机，如果之前设置过密码，那么直接绑定即可，如果没有，那么需要设置密码。
		new BindMobileTask(uid, securityCode, token, password).execute();
	}
	
	private void doBindEmail(String accountName, String password){
		SharedInfo sharedInfo = SharedInfo.getInstance();
		String passwdVal = sharedInfo.getPasswdVal(this);
		SendEmailFragment fragment = new SendEmailFragment();
		Bundle data = new Bundle();
		data.putString("title", getResources().getString(mMyResources.getString("lib_droi_account_send_email_title")));
		data.putString("email", accountName);
		data.putString("openid", sharedInfo.getOpenId(getApplicationContext()));
		data.putString("what", "bindmail");
		data.putString("token", sharedInfo.getAccessToken(getApplicationContext()));
		if("0".equals(passwdVal) == false && !TextUtils.isEmpty(password)){
			data.putString(Constants.JSON_S_PWD, password);
		}
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "doBindEmail passwdVal = " + passwdVal);
		}
		data.putString(Constants.ACCOUNT_PWD_VAL, passwdVal);
		fragment.setArguments(data);
		loadFragmentForResult(fragment, "SendEmailFragment", SEND_EMAIL);
	}
	
	private class BindMobileTask extends AsyncTask<Void, Void, String>{
		
		private final String mUid;
		private final String mRandCode;
		private final String mToken;
		private final String mPassword;
		
		public BindMobileTask(String uid, String randcode, String token, String password){
			mUid = uid;
			mRandCode = randcode;
			mToken = token;
			mPassword = password;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			final Map<String, String> bindParams = new HashMap<String, String>();
			bindParams.put(Constants.JSON_S_UID, mUid);
			bindParams.put("randcode", mRandCode);
			bindParams.put(Constants.JSON_S_TOKEN, mToken);
			if(!TextUtils.isEmpty(mPassword)){
				bindParams.put("passwd", mPassword);
			}
			bindParams.put("sign", MD5Util.md5(mUid+ mRandCode + mToken + Constants.SIGNKEY));
			StaticsCallback staticsCallback = StaticsCallback.getInstance(getApplicationContext());
			String loginfo = staticsCallback.encryptBindingLoginfo(Constants.BIND_TO_PHONE);
			bindParams.put("loginfo", loginfo);
			String bindResult = null;
			try{
				bindResult = HttpOperation.postRequest(Constants.ACCOUNT_BIND_MOBILE, bindParams);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return bindResult;
		}
		
        @Override
        protected void onPostExecute(String result){
        	super.onPostExecute(result);
        	hideProgress();
        	if(DebugUtils.DEBUG){
        		DebugUtils.i(TAG, "bind phone : " + result);
        	}
        	if (!TextUtils.isEmpty(result)){
        		try {
					JSONObject jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == 0){
						StaticsCallback staticsCallback = StaticsCallback.getInstance(getApplicationContext());
						staticsCallback.onBindResult(Constants.BIND_TO_PHONE);
						int passwdVal = jsonObject.has("passwdval")?jsonObject.getInt("passwdval"): -1;
						JSONObject updateBindPhone = new JSONObject();
						updateBindPhone.put(ShareInfoManager.SHARED_BINDPHONE, mUid);
						if(!TextUtils.isEmpty(mPassword)){
							updateBindPhone.put(ShareInfoManager.SHARED_PWDVAL, passwdVal);
						}
						SharedInfo sharedInfo = SharedInfo.getInstance();
						sharedInfo.updateLocalUserInfo(getApplicationContext(), updateBindPhone);
						DroiSDKHelper droiAccount = DroiSDKHelper.getInstance(getApplicationContext());
						droiAccount.onAccountBinded("phone", mUid);
						setResult(RESULT_OK);
						finish();
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") :
                    		getResources().getString(mMyResources.getString("lib_droi_account_toast_bind_fail"));
                    	Utils.showMessage(getApplicationContext(), desc);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}else{
        		Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_toast_bind_fail"));
        	}
        }
        
        @Override
        protected void onCancelled(){
        	hideProgress();
        }
		
	}
	
	private void loadFragmentForResult(BackHandledFragment fragment, String fragmentTag, int requestCode){
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		fragment.setTargetFragment(null, requestCode);
		ft.replace(mMyResources.getId("lib_droi_account_fragment_replaced"), fragment, fragmentTag);
		ft.addToBackStack(fragmentTag);
		ft.commit();
	}
	
	private void loadFragment(BackHandledFragment fragment, String fragmentTag){
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(mMyResources.getId("lib_droi_account_fragment_replaced"), fragment, fragmentTag);
		ft.commit();
	}
	
	private void loadFragment(FragmentManager fm, BackHandledFragment fragment, String fragmentTag){
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(mMyResources.getId("lib_droi_account_fragment_replaced"), fragment, fragmentTag);
		ft.commit();
	}
	
	private void loadFragmentToBackStack(FragmentManager fm, BackHandledFragment fragment, String fragmentTag){
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(mMyResources.getId("lib_droi_account_fragment_replaced"), fragment, fragmentTag);
		ft.addToBackStack(fragmentTag);
		ft.commit();
	}
	
	public void onBack(View view){
		onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "ACTIVITY onBackPressed");
		}
		if(mBackHandedFragment != null){
			mBackHandedFragment.onBackPressed();
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
		
	}

}
