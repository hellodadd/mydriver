package com.droi.account.authenticator;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.droi.account.DebugUtils;
import com.droi.account.MyResource;
import com.droi.account.Utils;
import com.droi.account.download.DownloadService;
import com.droi.account.login.DroiQQAuthLoginUtils;
import com.droi.account.login.DroiWechatAuthLoginUtils;
import com.droi.account.login.FindCodeActivity;
import com.droi.account.login.PasswordSetActivity;
import com.droi.account.login.RegisterActivity;
import com.droi.account.login.SecurityCodeActivity;
import com.droi.account.login.SendActiveEmailActivity;
import com.droi.account.login.SendEmailActivity;
import com.droi.account.login.SharedInfo;
import com.droi.account.login.User;
import com.droi.account.login.UserLoginTask;
import com.droi.account.login.WbAuth3;
import com.droi.account.netutil.MD5Util;
import com.droi.account.shared.DroiSDKHelper;
import com.droi.account.shared.IAccountListener;
import com.droi.account.statis.AccountOperation;
import com.droi.account.statis.StaticsCallback;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

	private static final String TAG = "AuthenticatorActivity";
	private static final boolean DEBUG = Utils.DEBUG;

	private static final int REGISTER_SET_PSW = 1003;
	private static final int GET_SECURITY_CODE = 1004;
	public static final int ERROR_CODE = 1005;
	public static final int ACCOUNT_REGISTER = ERROR_CODE + 1;
	public static final int ACCOUNT_LOGIN = ACCOUNT_REGISTER + 1;
	public static final int ACCOUNT_WELCOME = ACCOUNT_LOGIN + 1;
	public static final int ACCOUNT_DELETE = ACCOUNT_WELCOME + 1;
	public static final int ACCOUNT_FIND_CODE = ACCOUNT_DELETE + 1;
	public static final int RESET_BY_PHONENUMBER = ACCOUNT_FIND_CODE + 1;
	public static final int RESET_BY_EMAIL = RESET_BY_PHONENUMBER + 1;
	public static final int ACCOUNT_LOGIN_QQ = RESET_BY_EMAIL + 1;
	public static final int ACCOUNT_LOGIN_WEIBO = ACCOUNT_LOGIN_QQ + 1;
	private static final int SET_PWD_WHEN_REGISTER_BY_MOBILE = ACCOUNT_LOGIN_WEIBO + 1;
	private static final int SET_PASSWORD_FOR_EMAIL = SET_PWD_WHEN_REGISTER_BY_MOBILE + 1;
	private static final int SEND_ACTIVE_EMAIL = SET_PASSWORD_FOR_EMAIL + 1;
	private static final int RESEND_ACTIVE_MAIL_WHEN_LOGIN = SEND_ACTIVE_EMAIL + 1;
    /** The Intent flag to confirm credentials. */
    public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";

    /** The Intent extra to store password. */
    public static final String PARAM_PASSWORD = "password";

    /** The Intent extra to store username. */
    public static final String PARAM_USERNAME = "username";

    /** The Intent extra to store username. */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

	private AccountManager mAccountManager;
    /** Keep track of the progress dialog so we can dismiss it */
    private ProgressDialog mProgressDialog = null;

    /** Keep track of the login task so can cancel it if requested */
    private UserLoginTask mAuthTask = null;

    private User mUserInfo = SharedInfo.getInstance().getData();//new User();
    //public static AuthenticatorActivity instance;

    private String mPassword;

    private EditText mPasswordEdit;
    protected boolean mRequestNewAccount = true;
    private String mUsername;
    private EditText mUsernameEdit;
	private int mAccountType = -1;

    /**
     * If set we are just checking that the user knows their credentials; this
     * doesn't cause the user's password or authToken to be changed on the
     * device.
     */
    private Boolean mConfirmCredentials = false;
    public static IAccountListener mAccountListener;
    private HomeWatcherReceiver homeKeyReceiver;
    protected MyResource mMyResources = new MyResource(this);


    // welcomeActivity.java

    //private WBLoginUtils mWBLogin;
    private WbAuth3 mWBLogin;
    private DroiQQAuthLoginUtils mDroiQQLogin;
    private DroiWechatAuthLoginUtils mWechatLoginUtils;

    //dialog id
	private static final int DIALOG_MSG_BINDED_FOR_QQ_WB = 101;
	private static final int DIALOG_PRECESSING = DIALOG_MSG_BINDED_FOR_QQ_WB + 1;
	private static final int DIALOG_DOWNLOAD = DIALOG_PRECESSING + 1;
    private int mLoginType = 0;	//当第三方登陆的时候，如果用户已经绑定了邮箱或手机，提示用户使用绑定的邮箱或手机登�?
	//第三方登陆的时候，如果有绑定的手机或邮箱，记录他们
	private String mLoginCheckBindPhone;
	private String mLoginCheckBindEmail;

	public static String LOGIN_THEME = "LOGIN_THEME";
	public static String LOGIN_RequestedOrientation = "LOGIN_RequestedOrientation";

	public static OnBackClickListener onBackClickListener;

	public interface OnBackClickListener{
		void onBackClick();
	}

	 @Override
	 public void onCreate(Bundle icicle){
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
		 final Intent intent = getIntent();
		 // 设置传过来的样式
//		 if(intent.hasExtra(LOGIN_THEME)){
//			 setTheme(intent.getIntExtra(LOGIN_THEME, android.R.style.Theme_Holo_Light));
//		 }
//		 if(intent.hasExtra(LOGIN_RequestedOrientation)){
//			 setRequestedOrientation(intent.getIntExtra(LOGIN_RequestedOrientation, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
//		 }

		 super.onCreate(icicle);
		 //instance = this;
	     if(Utils.SHOW_ACTION_BAR){
	    	 ActionBar actionBar = getActionBar();
	    	 if(actionBar != null){
	    		 actionBar.setDisplayOptions(
		   	                ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
	    	 }
	     }
		 mUsername = intent.getStringExtra(PARAM_USERNAME);
		 if(DebugUtils.DEBUG){
			 DebugUtils.i(TAG, "onCreate");
		 }
		 mAccountManager = AccountManager.get(this.getApplicationContext());
		 Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		 if (accounts != null && accounts.length >= 1){
         	 Utils.showMessage(this, getString(mMyResources.getString("lib_droi_account_has_been_added_account")));
			 finish();
			 return;
		 }

		 mRequestNewAccount = true;//mUsername == null;
		 mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS, false);
		 setTitle(mMyResources.getString("lib_droi_account_login_account_title"));
		 setContentView(mMyResources.getLayout("lib_droi_account_login_activity"));
		 setupViews();
		 if(TextUtils.isEmpty(mUsername)){
			 if(DebugUtils.DEBUG){
				 DebugUtils.i(TAG, "startWelcome in on Create");
			 }
//			startWelcome();
		 }

		 if(DebugUtils.DEBUG){
			 DebugUtils.i(TAG, "taskId = " + getTaskId());
		 }

		 // welcomeActivity
		 View mThirdParty = findViewById(mMyResources.getId("lib_droi_account_layout"));
		 if(DroiSDKHelper.VERSION_FOREIGN){
			 User user = SharedInfo.getInstance().getData();
			 //mQQLogin = new QQLoginUtils(this, user);
			 mDroiQQLogin = new DroiQQAuthLoginUtils(this, mUserInfo, mHandler);
			 //mWBLogin = new WBLoginUtils(this, user);
			 mWBLogin = new WbAuth3(this, user);
			 mWechatLoginUtils = new DroiWechatAuthLoginUtils(this, user);
		 }else{
			 mThirdParty.setVisibility(View.GONE);
		 }

		 //homeKeyReceiver = new HomeWatcherReceiver();
		 //registerReceiver(homeKeyReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));	//监听HOME键
	 }

	 private void setupViews(){
	     mUsernameEdit = (EditText) findViewById(mMyResources.getId("lib_droi_account_username_edit"));
	     if(DroiSDKHelper.PHONE_LOGIN){
				if(DroiSDKHelper.EMAIL_LOGIN){

				}else{
					mUsernameEdit.setInputType(InputType.TYPE_CLASS_PHONE);
					mUsernameEdit.setHint(mMyResources.getString("lib_droi_account_hint_input_phonenumer"));
				}
		 }else if(DroiSDKHelper.EMAIL_LOGIN){
		    	 mUsernameEdit.setHint(mMyResources.getString("lib_droi_account_hint_input_email"));
		 }

	     mPasswordEdit = (EditText) findViewById(mMyResources.getId("lib_droi_account_password_edit"));
		 TextView mForgetPWD = (TextView) findViewById(mMyResources.getId("lib_droi_account_forget_password"));
	     mForgetPWD.setOnClickListener(new View.OnClickListener() {

			 @Override
			 public void onClick(View v) {
				 // TODO Auto-generated method stub
				 if (DebugUtils.DEBUG) {
					 DebugUtils.i(TAG, "click to find password");
				 }
				 Intent intent = new Intent(AuthenticatorActivity.this, FindCodeActivity.class);
				 startActivityForResult(intent, ACCOUNT_FIND_CODE);
			 }
		 });
	    if(!TextUtils.isEmpty(mUsername)){
	    	mUsernameEdit.setText(mUsername);
	    }


		TextView register = (TextView) findViewById(mMyResources.getId("lib_droi_account_register"));
		register.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AuthenticatorActivity.this, RegisterActivity.class);
				startActivityForResult(intent, ACCOUNT_REGISTER);
			}
		});
		/*if(DroiAccount.VERSION_FOREIGN == true){
			Button weiboLogin = (Button)findViewById(mMyResources.getId("lib_droi_account_btn_weibo_login"));
			weiboLogin.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
				}
			});
		}*/
		/*if (DroiAccount.VERSION_FOREIGN == true) {
			Button qqLogin = (Button) findViewById(mMyResources.getId("lib_droi_account_btn_qq_login"));
			qqLogin.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
				}
			});
			
		}*/
	 }

	 public void QQLogin(View view){
		 mLoginType = AuthenticatorActivity.ACCOUNT_LOGIN_QQ;
		 mDroiQQLogin.login();
	 }

	 public void WeChatLogin(View view){
		 mWechatLoginUtils.login();
	 }

	 public void WeiboLogin(View view){
		 mLoginType = AuthenticatorActivity.ACCOUNT_LOGIN_WEIBO;
		 mWBLogin.login();
	 }

	 @Override
	 protected void onResume() {
		 super.onResume();
	 }

	@Override
	 protected Dialog onCreateDialog(int id, Bundle args) {
		 Dialog dialog = null;
		 if(DIALOG_MSG_BINDED_FOR_QQ_WB == id){
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
		 }else if(DIALOG_DOWNLOAD == id){
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 builder.setTitle(mMyResources.getString("lib_droi_account_dialog_title_hint"));
			 builder.setCancelable(false);
			 String msg = getResources().getString(mMyResources.getString("lib_droi_account_download_hint"));
			 builder.setMessage(msg);
			 builder.setPositiveButton(mMyResources.getString("lib_droi_account_btn_download"), new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
			            Intent serviceIntent = new Intent(getApplicationContext(), DownloadService.class);
			            startService(serviceIntent);
					}

				 });
			 builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			 });
			 dialog = builder.create();
		 }else if(DIALOG_PRECESSING == id){
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
		 }else{
	        final ProgressDialog progressDialog = new ProgressDialog(this);
	        progressDialog.setMessage(getText(mMyResources.getString("lib_droi_account_ui_activity_authenticating")));
	        progressDialog.setIndeterminate(true);
	        progressDialog.setCancelable(true);
	        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	                if (mAuthTask != null) {
	                    mAuthTask.cancel(true);
	                }
	            }
	        });
	        // We save off the progress dialog in a field so that we can dismiss
	        // it later. We can't just call dismissDialog(0) because the system
	        // can lose track of our dialog if there's an orientation change.
	        mProgressDialog = progressDialog;
	        progressDialog.setCanceledOnTouchOutside(false);
	        return progressDialog;
		 }
		 return dialog;
	}

//	private void startWelcome(){
//	    Intent intent = new Intent(AuthenticatorActivity.this, WelcomeActivity.class);
//	    startActivityForResult(intent, ACCOUNT_WELCOME);
//	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onNewIntent");
		}
		 mAccountManager = AccountManager.get(this);
		 Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		 if (accounts != null && accounts.length >= 1){
			 finish();
			 return;
		 }
		 if(DebugUtils.DEBUG){
			 DebugUtils.i(TAG, "startWelcome in onNewIntent");
		 }
//		 startWelcome();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onActivityResult requestCode = " + requestCode + ", resultCode = " + resultCode);
		}

		switch (requestCode) {
		case REGISTER_SET_PSW:
			if (resultCode == RESULT_OK) {
				String password = data.getStringExtra("password");
				boolean findcode = data.getBooleanExtra("findcode", false);
				if (findcode) {
					// 找回密码之后，由用户自己登陆
					String userName = data.getStringExtra("userName");
					if (!TextUtils.isEmpty(userName)) {
						mUsernameEdit.setText(userName);
					}
					return;
				}
				if (!TextUtils.isEmpty(password)) {
					mPassword = password;
					addAccountAfterRegister(mUsername, password);

				}
				finish();
			} else if (resultCode == RESULT_CANCELED) {
				if (mAccountType == Constants.ACCOUNT_TYPE_PHONENUMBER) {
					mHandler.removeMessages(Constants.ACCOUNT_TYPE_PHONENUMBER);
					mHandler.sendEmptyMessage(Constants.ACCOUNT_TYPE_PHONENUMBER);
				} else if (mAccountType == Constants.ACCOUNT_TYPE_EMAIL) {
					mHandler.removeMessages(ACCOUNT_REGISTER);
					mHandler.sendEmptyMessage(ACCOUNT_REGISTER);
				} else if (mAccountType == RESET_BY_PHONENUMBER) {
					mHandler.removeMessages(RESET_BY_PHONENUMBER);
					mHandler.sendEmptyMessage(RESET_BY_PHONENUMBER);
				} else if (mAccountType == RESET_BY_EMAIL) {
					Intent intent = new Intent(AuthenticatorActivity.this,
							FindCodeActivity.class);
					startActivityForResult(intent, ACCOUNT_FIND_CODE);
				} else {

				}
			}

			break;
		case GET_SECURITY_CODE:
			if (data == null) {
				// if user press home key, and Re-entry
				break;
			}
			String codeType = data.getStringExtra(Constants.CODE_TYPE);
			if (resultCode == RESULT_OK) {
				String token = data.getStringExtra("token");
				Bundle bundle = new Bundle();
				bundle.putString("token", token);
				if (Constants.CODE_TYPE_REG.equals(codeType)) {
					// String password = data.getStringExtra("pwd");
					// bundle.putString("pwd", password);
					bundle.putString(Constants.REQUEST_TYPE,Constants.PASSWD_MODIFY);
					String securityCode = data.getStringExtra("securityCode");
					String phoneNumber = data.getStringExtra("phonenumber");
					Intent intent = new Intent(AuthenticatorActivity.this,PasswordSetActivity.class);
					intent.putExtra(Constants.REQUEST_TYPE,Constants.PASSWD_REGISTER_BY_MOBILE);
					intent.putExtra("title",getResources().getString(mMyResources.getString("lib_droi_account_create_psw_title")));
					intent.putExtra("securityCode", securityCode);
					intent.putExtra("token", token);
					intent.putExtra("username", phoneNumber);
					startActivityForResult(intent, REGISTER_SET_PSW);
					return;
				} else if (Constants.CODE_TYPE_RESET.equals(codeType)) {
					bundle.putString(Constants.REQUEST_TYPE,Constants.PASSWD_RESET);
					String securityCode = data.getStringExtra("securityCode");
					bundle.putString("securityCode", securityCode);
				}
				Message msg = mHandler.obtainMessage();
				msg.what = REGISTER_SET_PSW;
				msg.setData(bundle);
				mHandler.removeMessages(REGISTER_SET_PSW);
				mHandler.sendMessage(msg);
			} else if (resultCode == RESULT_CANCELED) {
				if (Constants.CODE_TYPE_REG.equals(codeType)) {
					mHandler.removeMessages(ACCOUNT_REGISTER);
					mHandler.sendEmptyMessage(ACCOUNT_REGISTER);
				} else if (Constants.CODE_TYPE_RESET.equals(codeType)) {
					Intent intent = new Intent(AuthenticatorActivity.this,FindCodeActivity.class);
					startActivityForResult(intent, ACCOUNT_FIND_CODE);
				}
			}
			break;
		case SET_PWD_WHEN_REGISTER_BY_MOBILE:

			break;
		case ACCOUNT_REGISTER:
			if (resultCode == RESULT_OK) {
				String accountName = data.getStringExtra("accountname");
				mUsername = accountName;
				int accountType = data.getIntExtra("accountType", -1);
				mAccountType = accountType;
				if (accountType != -1) {
					mHandler.removeMessages(accountType);
					mHandler.sendEmptyMessage(accountType);
				}
			} else if (resultCode == RESULT_CANCELED) {
				// mHandler.removeMessages(ACCOUNT_WELCOME);
				// mHandler.sendEmptyMessage(ACCOUNT_WELCOME);
			}
			break;
		case ACCOUNT_LOGIN:

			break;
		case ACCOUNT_FIND_CODE:
			// 找回密码返回需要找回的用户手机或邮�?
			if (resultCode == RESULT_OK) {
				String accountName = data.getStringExtra("accountname");
				mUsername = accountName;
				int accountType = data.getIntExtra("accountType", -1);
				mAccountType = accountType;
				if (accountType == RESET_BY_PHONENUMBER) {
					mHandler.removeMessages(RESET_BY_PHONENUMBER);
					mHandler.sendEmptyMessage(RESET_BY_PHONENUMBER);
				} else if (accountType == RESET_BY_EMAIL) {
					mHandler.removeMessages(RESET_BY_EMAIL);
					mHandler.sendEmptyMessage(RESET_BY_EMAIL);
				}
			} else if (resultCode == RESULT_CANCELED) {
				// go to login in
				// finish();
			}
			break;
		case RESET_BY_EMAIL:
			if (resultCode == RESULT_OK) {
				if (data != null) {
					String email = data.getStringExtra("email");
					if (!TextUtils.isEmpty(email)) {
						mUsernameEdit.setText(email);
						return;
					}
					finish();
				}
			} else if (resultCode == RESULT_CANCELED) {
				Intent intent = new Intent(AuthenticatorActivity.this,FindCodeActivity.class);
				startActivityForResult(intent, ACCOUNT_FIND_CODE);
			}
			break;
		case ACCOUNT_WELCOME:
			if (resultCode == RESULT_OK) {
				int type = data.getIntExtra("type", -1);
				if (type == ACCOUNT_REGISTER) {
					mHandler.removeMessages(ACCOUNT_REGISTER);
					mHandler.sendEmptyMessage(ACCOUNT_REGISTER);
				} else if (type == ACCOUNT_LOGIN) {

				} else if (type == ACCOUNT_LOGIN_QQ) {
					finish();
				} else if (type == ACCOUNT_LOGIN_WEIBO) {
					finish();
				} else {
					String delete = data.getStringExtra("type");
					if (delete.equals("delete")) {
						Intent intent = new Intent();
						intent.setClass(this, PasswordSetActivity.class);// for
																			// test
						startActivityForResult(intent, ACCOUNT_DELETE);
					}
				}
			} else if (resultCode == RESULT_CANCELED) {
				// what about if user pressed the HOME key, and re-entry this
				// app
				if (data != null) {
					boolean exitOnBack = data.getBooleanExtra("finish_on_back",false);
					if (exitOnBack) {
						// cancled from welcome activity
						DroiSDKHelper droiAccount = DroiSDKHelper.getInstance(this);
						droiAccount.onAccountCancelled();
						finish();
					}
				}
				// finish();
			}
			break;
		case ACCOUNT_DELETE:
			finish();
			break;
		case SET_PASSWORD_FOR_EMAIL:
			if (resultCode == RESULT_OK) {
				String email = data.getStringExtra("email");
				String pwd = data.getStringExtra("pwd");
				Intent intent = new Intent(this, SendActiveEmailActivity.class);
				intent.putExtra("email", email);
				intent.putExtra("pwd", pwd);
				intent.putExtra("what", SendActiveEmailActivity.EMAIL_REGISTER);
				startActivityForResult(intent, SEND_ACTIVE_EMAIL);

			} else if (resultCode == RESULT_CANCELED) {
				mHandler.removeMessages(ACCOUNT_REGISTER);
				mHandler.sendEmptyMessage(ACCOUNT_REGISTER);
			}
			break;
		case SEND_ACTIVE_EMAIL:
			if (resultCode == RESULT_OK) {
				String email = data.getStringExtra("email");
				if (!TextUtils.isEmpty(email)) {
					mUsernameEdit.setText(email);
				}
			} else if (resultCode == RESULT_CANCELED) {
				Intent intent = new Intent(AuthenticatorActivity.this,PasswordSetActivity.class);
				intent.putExtra("title",getResources().getString(mMyResources.getString("lib_droi_account_create_psw_title")));
				intent.putExtra(Constants.REQUEST_TYPE,Constants.PASSWD_REGISTER_BY_EMAIL);
				intent.putExtra("username", mUsername);
				startActivityForResult(intent, SET_PASSWORD_FOR_EMAIL);
			}
			break;

		case RESEND_ACTIVE_MAIL_WHEN_LOGIN:
			break;
		}
	}

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
			case Constants.ACCOUNT_TYPE_PHONENUMBER:{
				Intent intent = new Intent(AuthenticatorActivity.this, SecurityCodeActivity.class);
				intent.putExtra("phonenumber", mUsername);
				intent.putExtra("codetype", Constants.CODE_TYPE_REG);
				startActivityForResult(intent, GET_SECURITY_CODE);
			}
			break;
			case REGISTER_SET_PSW:
				startPwdActivity(msg);
				break;
			case Constants.ACCOUNT_TYPE_EMAIL:{
				//register by email
				//startPwdActivity(msg);
				Intent intent = new Intent(AuthenticatorActivity.this, PasswordSetActivity.class);
				intent.putExtra("title", getResources().getString(mMyResources.getString("lib_droi_account_create_psw_title")));
				intent.putExtra(Constants.REQUEST_TYPE, Constants.PASSWD_REGISTER_BY_EMAIL);
				intent.putExtra("username", mUsername);
				startActivityForResult(intent, SET_PASSWORD_FOR_EMAIL);
			}
				break;
			case ACCOUNT_REGISTER:{
				Intent intent = new Intent(AuthenticatorActivity.this, RegisterActivity.class);
				startActivityForResult(intent, ACCOUNT_REGISTER);
			}
			break;
			case ACCOUNT_LOGIN:
				break;
			case ACCOUNT_WELCOME:{
				if(DebugUtils.DEBUG){
					DebugUtils.i(TAG, "startWelcome in mHandler ACCOUNT_WELCOME");
				}
//				startWelcome();
			}
				break;
			case RESET_BY_PHONENUMBER:{
				Intent intent = new Intent(AuthenticatorActivity.this, SecurityCodeActivity.class);
				intent.putExtra("phonenumber", mUsername);
				intent.putExtra("codetype", Constants.CODE_TYPE_RESET);
				startActivityForResult(intent, GET_SECURITY_CODE);
			}
			break;
			case RESET_BY_EMAIL:{
				Intent intent = new Intent(AuthenticatorActivity.this, SendEmailActivity.class);
				intent.putExtra("title", getResources().getText(mMyResources.getString("lib_droi_account_reset_pwd_by_email_title")));
				intent.putExtra("email", mUsername);
				intent.putExtra("what", "findbackpwdbyemail");
				startActivityForResult(intent, RESET_BY_EMAIL);
			}
			break;
			}
		}
	};

	private void startPwdActivity(Message message){
		Bundle data = message.getData();
		String token = data.getString("token");
		String password = data.getString("pwd");
		String requestType = data.getString(Constants.REQUEST_TYPE);
		String securityCode = data.getString("securityCode");
		Intent intent = new Intent(AuthenticatorActivity.this, PasswordSetActivity.class);
		intent.putExtra("title", getResources().getString(mMyResources.getString("lib_droi_account_create_psw_title")));
		if(mAccountType == Constants.ACCOUNT_TYPE_EMAIL){
			//如果为当前为邮箱注册
			intent.putExtra(Constants.REQUEST_TYPE, Constants.PASSWD_REGISTER_BY_EMAIL);
		}else{
			intent.putExtra(Constants.REQUEST_TYPE, requestType);
		}
		intent.putExtra("securityCode", securityCode);
		intent.putExtra("token", token);
		intent.putExtra("username", mUsername);
		intent.putExtra("pwd", password);
		startActivityForResult(intent, REGISTER_SET_PSW);
	}

	 public void handleLogin(View view){
	     mUsername = mUsernameEdit.getText().toString().trim();
	     mPassword = mPasswordEdit.getText().toString().trim();
	     if (TextUtils.isEmpty(mUsername)){
	         mUsernameEdit.requestFocus();
	         mUsernameEdit.setError(getResources().getText(mMyResources.getString("lib_droi_account_tip_username_none")).toString());
	         return;
	     }
	     if (TextUtils.isEmpty(mPassword)){
	         mPasswordEdit.requestFocus();
	         mPasswordEdit.setError(getResources().getText(mMyResources.getString("lib_droi_account_tip_password_none")).toString());
	         return;
	     }
		 if(Utils.getAvailableNetWorkType(AuthenticatorActivity.this) == -1){
				showMessage(mMyResources.getString("lib_droi_account_network_wrong_text"));
				return;
		 }
		 final int accountType = Utils.getAccountType(mUsername);
		 //账户登陆只有两种账户类型, 手机号和邮箱

		 if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER && DroiSDKHelper.PHONE_LOGIN){

		 }else if(accountType == Constants.ACCOUNT_TYPE_EMAIL && DroiSDKHelper.EMAIL_LOGIN){

		 }else{
			 mUsernameEdit.requestFocus();
			 if(DroiSDKHelper.PHONE_LOGIN){
				 if(DroiSDKHelper.EMAIL_LOGIN){
						mUsernameEdit.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_accountname")).toString());
					}else{
						mUsernameEdit.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_phonenumer")).toString());
					}

			 }else if(DroiSDKHelper.EMAIL_LOGIN){
				 mUsernameEdit.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_email")).toString());
			 }
			 return;
		 }
		 if(mAuthTask != null){
			 mAuthTask.cancel(true);
		 }
	     showProgress();

         mAuthTask = new UserLoginTask(this, mUsername, mPassword, mUserInfo, new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				hideProgress();
				if(mUserInfo.getResult() == 0){

					//String active_mail = mUserInfo.getActiveMail();
					//if(!TextUtils.isEmpty(active_mail)){

					//}else{
					StaticsCallback staticsCallback = StaticsCallback.getInstance(getApplicationContext());
					if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
						staticsCallback.onLoginResult(mUsername, mUserInfo, AccountOperation.ACCOUNT_LOGIN_TYPE_PHONE);
					}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
						staticsCallback.onLoginResult(mUsername, mUserInfo, AccountOperation.ACCOUNT_LOGIN_TYPE_MAIL);
					}
					onAuthenticationResult(mUserInfo.getToken());
					//}
				}else if(mUserInfo.getResult() == -2012){
					//not active account when login, send active mail again
					String username = mUsernameEdit.getText().toString().trim();
					if(Utils.isValidEmailAddress(username)){
						Intent intent = new Intent(AuthenticatorActivity.this, SendActiveEmailActivity.class);
						intent.putExtra("email", username);
						intent.putExtra("what", SendActiveEmailActivity.EMAIL_RESEND);
						startActivityForResult(intent, RESEND_ACTIVE_MAIL_WHEN_LOGIN);
					}
				}
			}

         });

         mAuthTask.execute();
	 }

	 public void onAuthenticationResult(String authToken) {
		 boolean success = ((authToken != null) && (authToken.length() > 0));
		 mAuthTask = null;
		 if (success){
			 if (!mConfirmCredentials) {
	               finishLogin(authToken);
	               finish();
	            } else{
	               finishConfirmCredentials(success);
	            }
		 }else{
	          if (mRequestNewAccount) {
	        	  // "Please enter a valid username/password.
	        	  Utils.showMessage(getApplicationContext(), "info from server");
	          } else {
	              // "Please enter a valid password." (Used when the
	              // account is already in the database but the password
	              // doesn't work.)
	        	  Utils.showMessage(getApplicationContext(), "info from server");
	          }
	     }
	 }

	 public void onAuthenticationResult(User user){
	     String authToken = null;
	     if (null != user){
	        authToken = user.getToken();
	     }
	     boolean success = ((authToken != null) && (authToken.length() > 0));
	     if(success){
	    	 if (!mConfirmCredentials){
	            finishLogin(authToken);
	            finish();
	         } else{
	            finishConfirmCredentials(success);
	         }
	     }
	 }

	 private void showMessage(int stringId){
			Utils.showMessage(AuthenticatorActivity.this, getResources().getText(stringId));
	 }

	 private void finishLogin(String authToken) {
		 if(DEBUG){
			 DebugUtils.i(TAG, "finishLogin begin... authToken : " + authToken);
		 }
		 SharedInfo sharedInfo = SharedInfo.getInstance();
		 String accountName = sharedInfo.getData().getName();
		 if(!TextUtils.isEmpty(accountName)){
			 mUsername = accountName;
		 }
		 final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
		 if (mRequestNewAccount) {
	          mAccountManager.addAccountExplicitly(account, authToken, null);
	     } else {
	          mAccountManager.setPassword(account, authToken);
	     }

		 if(DebugUtils.DEBUG){
			 Account[] accounts = AccountManager.get(this).getAccountsByType(Constants.ACCOUNT_TYPE);
			 DebugUtils.i(TAG, "finishLogin accounts : " + accounts);
			 if(accounts != null){
				 DebugUtils.i(TAG, "finishLogin length = " + accounts.length);
			 }
		 }

		 mUserInfo.setName(mUsername);

		 sharedInfo.getData().setRegtype("");
		 sharedInfo.saveDataToAccount(this, account);
	     final Intent intent = new Intent();
	     intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
	     intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
	     setAccountAuthenticatorResult(intent.getExtras());

	     //force to onAccountsUpdated

	     Intent boradToUpdate = new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
	     sendBroadcast(boradToUpdate);

	     //
	     setResult(RESULT_OK, intent);
	     hideProgress();
		 if(DebugUtils.DEBUG){
			 DebugUtils.i(TAG, "finishLogin end...");
		 }
	 }

	 private void addAccountAfterRegister(String userName, String password){
		 if(DebugUtils.DEBUG){
			 DebugUtils.i(TAG, "addAccountAfterRegister begin...");
		 }
		 final Account account = new Account(userName, Constants.ACCOUNT_TYPE);
		 String passwordMD5 = MD5Util.md5(password);
		 mUserInfo.setName(mUsername);
		 mUserInfo.setPassword(passwordMD5);
		 mAccountManager.addAccountExplicitly(account, passwordMD5, null);
	     final Intent intent = new Intent();
	     intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, userName);
	     intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
		 SharedInfo sharedInfo = SharedInfo.getInstance();
		 sharedInfo.getData().setRegtype("");
		 sharedInfo.saveDataToAccount(this, account);
	     setAccountAuthenticatorResult(intent.getExtras());
	     //force to onAccountsUpdated

	     Intent boradToUpdate = new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
	     sendBroadcast(boradToUpdate);

	     //
	     setResult(RESULT_OK, intent);
		 if(DebugUtils.DEBUG){
			 DebugUtils.i(TAG, "addAccountAfterRegister end...");
		 }

	 }

	 private void finishConfirmCredentials(boolean result){
	     final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
	     mAccountManager.setPassword(account, mPassword);
	     final Intent intent = new Intent();
	     intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
	     setAccountAuthenticatorResult(intent.getExtras());
	     setResult(RESULT_OK, intent);
	     finish();
	 }

	 public void onAuthenticationCancel() {
		 mAuthTask = null;
		 hideProgress();
	 }

	 public void showProgress() {
	     showDialog(0);
	 }

	 public void hideProgress() {
	     if (mProgressDialog != null) {
	         mProgressDialog.dismiss();
	     }
	 }

	 public void onThirdPartyLoginFinish(){
		 if(DebugUtils.DEBUG){
	         DebugUtils.i(TAG, "WelcomeActivity otherLoginFinish");
		 }
		 Constants.ACCOUNT_TYPE = DroiSDKHelper.ACCOUNT_TYPE = getPackageName();
		 DroiSDKHelper droiAccount = DroiSDKHelper.getInstance(this);
		 if(droiAccount.checkAccount() == false){
			 User user = SharedInfo.getInstance().getData();
			 mLoginCheckBindPhone = user.getBindPhone();
			 mLoginCheckBindEmail = user.getBindEmail();
			 SharedInfo sharedInfo = SharedInfo.getInstance();
			 //Utils.showMessage(this, mMyResources.getString("lib_droi_account_login_success"));
			 String userName = sharedInfo.getData().getName();
			 String token = sharedInfo.getData().getToken();
			 if(DebugUtils.DEBUG){
				 DebugUtils.i(TAG, "type,name,token:" + Constants.ACCOUNT_TYPE + " , " + userName + " , " + token);
			 }
			 if(TextUtils.isEmpty(userName) || TextUtils.isEmpty(token)){
				 Utils.showMessage(this, getString(mMyResources.getString("lib_droi_account_login_fail")));
			 }else{
				 final Account account = new Account(userName,  Constants.ACCOUNT_TYPE);
				 mAccountManager.addAccountExplicitly(account, token, null);
				 sharedInfo.saveDataToAccount(this, account);
			     Intent intent = new Intent();
			     intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, userName);
			     intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
			     intent.putExtra("type", mLoginType);
			     //setAccountAuthenticatorResult(intent.getExtras());
			     //force to onAccountsUpdated
			     Intent boradToUpdate = new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
			     sendBroadcast(boradToUpdate);
			     //
			     setResult(RESULT_OK, intent);
			 }
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
	protected void onDestroy() {
			// TODO Auto-generated method stub
		super.onDestroy();
		hideProgress();
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onDestroy");
		}
		if(mAccountListener != null){
			DroiSDKHelper droiAccount = DroiSDKHelper.getInstance(this);
			if(droiAccount.checkAccount()){
				mAccountListener.onSuccess(null);
			}else{
				mAccountListener.onCancel();
			}
			mAccountListener = null;
		}
		onBackClickListener = null;
		//unregisterReceiver(homeKeyReceiver);
	}

	 @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if (keyCode == KeyEvent.KEYCODE_HOME) {	// HOME键不一定能监听到
			 onBackPressed();
			 return true;
		 }
		 return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onBackPressed");
		}
		if(onBackClickListener != null) {
			onBackClickListener.onBackClick();
		}
		super.onBackPressed();
	}

	class HomeWatcherReceiver extends BroadcastReceiver {
		private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
		private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
		private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
		private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
		private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
					// 短按Home键
					finish();
				} else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
					// 长按Home键 或者 activity切换键
					finish();
				} else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
					// 锁屏
					finish();
				} else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
					// samsung 长按Home键
					finish();
				}
			}
		}
	}



}
