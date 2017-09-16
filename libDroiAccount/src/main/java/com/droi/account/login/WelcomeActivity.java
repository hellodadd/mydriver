package com.droi.account.login;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.droi.account.DebugUtils;
import com.droi.account.Utils;
import com.droi.account.authenticator.AuthenticatorActivity;
import com.droi.account.authenticator.Constants;
import com.droi.account.download.DownloadService;
import com.droi.account.shared.DroiSDKHelper;

public class WelcomeActivity extends BaseActivity {

	private static final String TAG = "WelcomeActivity";
    private QQLoginUtils mQQLogin;
    //private WBLoginUtils mWBLogin;
    private WbAuth3 mWBLogin;
    private ProgressDialog mProgressDialog = null;
    private AccountManager mAccountManager;
    
    private static final int ACCOUNT_REGISTER = 2;
    private static final int ACCOUNT_LOGIN_FREEME = ACCOUNT_REGISTER + 1;
    private static final int ACCOUNT_LOGIN_QQ = ACCOUNT_LOGIN_FREEME + 1;
    private static final int ACCOUNT_LOGIN_WWEIBO = ACCOUNT_LOGIN_QQ + 1;
    private static final int GET_SECURITY_CODE = ACCOUNT_LOGIN_WWEIBO + 1;
    private static final int REGISTER_BY_MOBILE = GET_SECURITY_CODE + 1;
    private static final int REGISTER_BY_MAIL = REGISTER_BY_MOBILE + 1;
    private static final int REGISTER_SET_PSW = REGISTER_BY_MAIL + 1;
    
    //dialog id
	private static final int DIALOG_MSG_BINDED_FOR_QQ_WB = 101;
	private static final int DIALOG_PRECESSING = DIALOG_MSG_BINDED_FOR_QQ_WB + 1;
	private static final int DIALOG_DOWNLOAD = DIALOG_PRECESSING + 1;
    private int mLoginType = 0;	//当第三方登陆的时候，如果用户已经绑定了邮箱或手机，提示用户使用绑定的邮箱或手机登陆
	//第三方登陆的时候，如果有绑定的手机或邮箱，记录他们
	private String mLoginCheckBindPhone;
	private String mLoginCheckBindEmail;
	
	private View mThirdParty = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//hideNavigationBar();
		super.onCreate(savedInstanceState);
		//customActionBar();
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onCreate");
		}
		mAccountManager = AccountManager.get(this.getApplicationContext());
		setContentView(mMyResources.getLayout("lib_droi_account_welcome_layout"));
		mThirdParty = findViewById(mMyResources.getId("lib_droi_account_layout"));
		
		if(DroiSDKHelper.VERSION_FOREIGN == true){
		User user = SharedInfo.getInstance().getData();
		//mQQLogin = new QQLoginUtils(this, user);//修改了QQLoginUtils.java
		//mWBLogin = new WBLoginUtils(this, user);
		//mWBLogin = new WbAuth3(this, user);//修改了WBLoginUtils.java
		}else{
			mThirdParty.setVisibility(View.GONE);
		}
		
		setupView();
		 if(DebugUtils.DEBUG){
			 DebugUtils.i(TAG, "taskId = " + getTaskId());
		 }
	}
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case ACCOUNT_REGISTER:{		//register
					Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
					startActivityForResult(intent, ACCOUNT_REGISTER);
				}
				break;
				case REGISTER_BY_MOBILE:{	// register by mobile
					//register by mobile
					Bundle data = msg.getData();
					String accountName = data.getString("accountName");
					Intent intent = new Intent(WelcomeActivity.this, SecurityCodeActivity.class);
					intent.putExtra("phonenumber", accountName);
					intent.putExtra("codetype", Constants.CODE_TYPE_REG);
					startActivityForResult(intent, GET_SECURITY_CODE);
				}
				break;
				
				case REGISTER_BY_MAIL:{		// register by email
					//register by email
				}
				break;
				
			}
		}
	};
	
	 @Override
	 protected Dialog onCreateDialog(int id, Bundle args) {
		 Dialog dialog = null;
		 if(DIALOG_MSG_BINDED_FOR_QQ_WB == id){		// dialog msg binded for qq wb
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 builder.setTitle(mMyResources.getString("lib_droi_account_dialog_title_hint"));
			 builder.setCancelable(false);
			 String msg = getResources().getString(mMyResources.getString("lib_droi_account_login_by_phone_or_email"));
			 if(!TextUtils.isEmpty(mLoginCheckBindPhone)){
				 msg += "\n";
				 msg += mLoginCheckBindPhone;
			 }
			 if(!TextUtils.isEmpty(mLoginCheckBindEmail)){
				 msg += "\n";
				 msg += mLoginCheckBindEmail;
			 }
			 builder.setMessage(msg);
			 builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
					 
				 });
			 dialog = builder.create();
		 }else if(DIALOG_DOWNLOAD == id){		// dialog download
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 builder.setTitle(mMyResources.getString("lib_droi_account_dialog_title_hint"));
			 builder.setCancelable(false);
			 String msg = getResources().getString(mMyResources.getString("lib_droi_account_download_hint"));
			 builder.setMessage(msg);
			 builder.setPositiveButton(mMyResources.getString("lib_droi_account_btn_download"), new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
			            Intent serviceIntent = new Intent(WelcomeActivity.this, DownloadService.class);
			            startService(serviceIntent);
			            finish();
					}
					 
				 });
			 builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
				 
			 });
			 dialog = builder.create();
		 }else if(DIALOG_PRECESSING == id){  	// dialog precessing
	        final ProgressDialog progressDialog = new ProgressDialog(this);
	        progressDialog.setMessage(getText(mMyResources.getString("lib_droi_account_ui_activity_authenticating")));
	        progressDialog.setIndeterminate(true);
	        //progressDialog.setCancelable(true);
	        progressDialog.setCanceledOnTouchOutside(false);
	        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	            }
	        });
	        // We save off the progress dialog in a field so that we can dismiss
	        // it later. We can't just call dismissDialog(0) because the system
	        // can lose track of our dialog if there's an orientation change.
	        mProgressDialog = progressDialog;
	        dialog = progressDialog;
		 }
		 
	     return dialog;	    	
	}	
	
	 public void showProgress() {
	     showDialog(DIALOG_PRECESSING);
	 }
	 
	 public void hideProgress() {
	     if (mProgressDialog != null) {
	         mProgressDialog.dismiss();
	     }
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
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if(DroiSDKHelper.VERSION_FOREIGN == true){
		Button qqLogin = (Button)findViewById(mMyResources.getId("lib_droi_account_btn_qq_login"));
		if(Utils.isAppInstalled(this, "com.droi.account") == false){
			qqLogin.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					showDialog(DIALOG_DOWNLOAD);
				}
			});
		}else{
			qqLogin.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mLoginType = AuthenticatorActivity.ACCOUNT_LOGIN_QQ;
					mQQLogin.login();
				}
			});
			}
		}
		
	}

	public void onBack(View view){
		onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.putExtra("finish_on_back", true);
		setResult(RESULT_CANCELED, intent);
		super.onBackPressed();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
			case REGISTER_SET_PSW:{	// register set psw
			}
			break;
			case ACCOUNT_REGISTER:{
				if(resultCode == RESULT_OK){
					String accountName = data.getStringExtra("accountname");
					int accountType = data.getIntExtra("accountType", -1);
					if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
						mHandler.removeMessages(REGISTER_BY_MOBILE);
						Bundle bundle = new Bundle();
						bundle.putString("accountName", accountName);
						Message msg = mHandler.obtainMessage();
						msg.what = REGISTER_BY_MOBILE;
						msg.setData(bundle);
						mHandler.sendMessage(msg);
					}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
						mHandler.removeMessages(REGISTER_BY_MAIL);
						Bundle bundle = new Bundle();
						bundle.putString("accountName", accountName);
						Message msg = mHandler.obtainMessage();
						msg.what = REGISTER_BY_MAIL;
						msg.setData(bundle);
						mHandler.sendMessage(msg);
					}
				}else if(resultCode == RESULT_CANCELED){
				}
			}
			break;
			case GET_SECURITY_CODE:{		// get security code
				String codeType = data.getStringExtra(Constants.CODE_TYPE);
				if(resultCode == RESULT_OK){
					String token = data.getStringExtra("token");
					Bundle bundle = new Bundle();
					bundle.putString("token", token);
					if(Constants.CODE_TYPE_REG.equals(codeType)){
						//String password = data.getStringExtra("pwd");
						//bundle.putString("pwd", password);
						bundle.putString(Constants.REQUEST_TYPE, Constants.PASSWD_MODIFY);
						String securityCode = data.getStringExtra("securityCode");
						String phoneNumber = data.getStringExtra("phonenumber");
						Intent intent = new Intent(WelcomeActivity.this, PasswordSetActivity.class);
						intent.putExtra(Constants.REQUEST_TYPE, Constants.PASSWD_REGISTER_BY_MOBILE);
						intent.putExtra("title", getResources().getString(mMyResources.getString("lib_droi_account_create_psw_title")));
						intent.putExtra("securityCode", securityCode);
						intent.putExtra("token", token);
						intent.putExtra("username", phoneNumber);
						startActivityForResult(intent, REGISTER_SET_PSW);
						return;
					}else if(Constants.CODE_TYPE_RESET.equals(codeType)){
						bundle.putString(Constants.REQUEST_TYPE, Constants.PASSWD_RESET);
						String securityCode = data.getStringExtra("securityCode");
						bundle.putString("securityCode", securityCode);
					}
					Message msg = mHandler.obtainMessage();
					msg.what = REGISTER_SET_PSW;
					msg.setData(bundle);
					mHandler.removeMessages(REGISTER_SET_PSW);
					mHandler.sendMessage(msg);
				}else if(resultCode == RESULT_CANCELED){
					
				}
			}
			break;
		}
	
		
	}
	
	
	private void setupView(){
		Button register = (Button) findViewById(mMyResources.getId("lib_droi_account_btn_register"));
		register.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("type", AuthenticatorActivity.ACCOUNT_REGISTER);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		Button login = (Button) findViewById(mMyResources.getId("lib_droi_account_btn_login"));
		login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("type", AuthenticatorActivity.ACCOUNT_LOGIN);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		
		Button delete = (Button)findViewById(mMyResources.getId("lib_droi_account_btn_delete"));
		if(delete != null){
			delete.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent();
					intent.putExtra("type", "delete");
					setResult(RESULT_OK, intent);
					finish();
				}
			});
		}
		
		if(DroiSDKHelper.VERSION_FOREIGN == true){
		Button weiboLogin = (Button)findViewById(mMyResources.getId("lib_droi_account_btn_weibo_login"));
		weiboLogin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mLoginType = AuthenticatorActivity.ACCOUNT_LOGIN_WEIBO;
				mWBLogin.login();
			}
		});
		}
		
	}
	
	 public void otherLoginFinish(){
		 if(DebugUtils.DEBUG){
	         DebugUtils.i(TAG, "WelcomeActivity otherLoginFinish");
		 }
		 DroiSDKHelper droiAccount = DroiSDKHelper.getInstance(this);
		 if(droiAccount.checkAccount() == false){
			 User user = SharedInfo.getInstance().getData();
			 mLoginCheckBindPhone = user.getBindPhone();
			 mLoginCheckBindEmail = user.getBindEmail();
			 SharedInfo sharedInfo = SharedInfo.getInstance();
			 Utils.showMessage(this, mMyResources.getString("lib_droi_account_login_success"));
			 String userName = sharedInfo.getData().getName();
			 String token = sharedInfo.getData().getToken();
	
			 final Account account = new Account(userName, Constants.ACCOUNT_TYPE);
			 mAccountManager.addAccountExplicitly(account, token, null);
			 sharedInfo.saveDataToAccount(this, account);
		     Intent intent = new Intent();
		     intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, userName);
		     intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
		     intent.putExtra("type", mLoginType);
		     //setAccountAuthenticatorResult(intent.getExtras());
		     //force to onAccountsUpdated
		     /*
		     Intent boradToUpdate = new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
		     sendBroadcast(boradToUpdate);
		     */
		     //
		     setResult(RESULT_OK, intent);
		 }else{
			 Intent intent = new Intent();
			 setResult(RESULT_OK, intent);
		 }
		 if(DebugUtils.DEBUG){
	         DebugUtils.i(TAG, "WelcomeActivity otherLoginFinish end");
		 }
	     finish();
	 }
	 
	 @Override
	 public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
	 }
	 
	 @Override
	 public void onDestroy(){
		super.onDestroy();
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onDestroy");
		}
	 }
	 
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onNewIntent");
		}
	}
}
