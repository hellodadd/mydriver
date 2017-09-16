package com.droi.account.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droi.account.DebugUtils;
import com.droi.account.MyResource;
import com.droi.account.Utils;
import com.droi.account.authenticator.AuthenticatorActivity;
import com.droi.account.authenticator.Constants;
import com.droi.account.login.AccountCheckActivity;
import com.droi.account.login.AvatarUtils;
import com.droi.account.login.CheckPwdListener;
import com.droi.account.login.CheckPwdTask;
import com.droi.account.login.PasswordSetActivity;
import com.droi.account.login.SecurityCodeActivity;
import com.droi.account.login.SendActiveEmailActivity;
import com.droi.account.login.SharedInfo;
import com.droi.account.login.User;
import com.droi.account.manager.ShareInfoManager;
import com.droi.account.netutil.FormFile;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;
import com.droi.account.netutil.SocketHttpRequester;
import com.droi.account.shared.DroiSDKHelper;
import com.droi.account.shared.IAccountSettingListener;
import com.droi.account.statis.StaticsCallback;

public class ActivitySettings extends Activity {
	
	//key begin
	private static final String TAG = "AccountSettings";
	public static final String KEY_ACCOUNT_NAME = "account_description";
	public static final String KEY_ACCOUNT_GENDER = "user_gender";
	public static final String KEY_ACCOUNT_NICKNAME = "user_nickname";
	public static final String KEY_ACCOUNT_BIND_PHONE = "user_bind_phonenumber";
	public static final String KEY_ACCOUNT_BIND_EMAIL = "user_bind_email";
	public static final String KEY_CUSTOM_PREFERNECE = "button_custom";
	public static final String KEY_MODIFY_PWD = "user_modify_pwd";
	public static final String KEY_AVATAR = "account_avatar";
	public static final String KEY_MY_ADDRESS = "my_address";
	private static final String KEY_CATEGORY_DETAILED = "key_info_detailed";
	//key end
	
	//info from apps
	public static final String CALLED_FROM_APPS = "account_settings";
	public static final String CALLED_APPS_TITLE = "app_title";
	
	//Dialog id begin
	private static final int DIALOG_ON_PROGRESS = 100;
	public static final int DIALOG_MODIFY_PWD_CONFIRM = DIALOG_ON_PROGRESS + 1;
	private static final int DIALOG_MODIFY_PWD_QQ_OR_WEIBO = DIALOG_MODIFY_PWD_CONFIRM + 1;
	private static final int DIALOG_SET_AVATER = DIALOG_MODIFY_PWD_QQ_OR_WEIBO + 1;
	private static int DIALOG_CONFIRM = DIALOG_SET_AVATER + 1;
	//进入设置界面，提示错误信息
	private static final int DIALOG_ENTRY_APP_ERROR = DIALOG_CONFIRM + 1;
	//当第三方登陆的时候，如果用户已经绑定了邮箱或手机，提示用户使用绑定的邮箱或手机登陆
	private static final int DIALOG_MSG_BINDED_FOR_QQ_WB = DIALOG_ENTRY_APP_ERROR + 1;
	private static final int DIALOG_NO_ACCOUNT_EXITS = DIALOG_MSG_BINDED_FOR_QQ_WB + 1;

	//Dialog id end
	private static final int REQUEST_CODE_MODIFY_PWD = 1;
	private static final int BIND_MOBILE = REQUEST_CODE_MODIFY_PWD + 1;
	private static final int BIND_EMAIL = BIND_MOBILE + 1;
	private static final int GET_SECURITY_CODE = BIND_EMAIL + 1;
	private static final int GET_SECURITY_CODE_MODIFY_PWD = GET_SECURITY_CODE + 1;
	private static final int SET_PWD_WHEN_BINDED_MODIFY_PWD = GET_SECURITY_CODE_MODIFY_PWD + 1;
	private static final int SEND_EMAIL = SET_PWD_WHEN_BINDED_MODIFY_PWD + 1;
	private static final int BIND_ACCOUNT = SEND_EMAIL + 1;
	private static final int RESET_PWD_GET_SECURITY = BIND_ACCOUNT + 1;
	private static final int RESET_PWD_FOR_BIND_PHONE = RESET_PWD_GET_SECURITY + 1;
	//当第三方用户只绑定邮箱时，用户点击修改密码，为该邮箱设置密码，向服务器发送请求
	private static final int SET_PWD_FOR_BIND_MAIL = RESET_PWD_FOR_BIND_PHONE + 1;
	//第三方登陆，没有绑定手机或邮箱，如果修改密码，需要完善资料
	private static final int BIND_BEFORE_MODIFY_PWD = SET_PWD_FOR_BIND_MAIL + 1;
	private static final int REQUEST_CODE_CAMERA_WITH_DATA = BIND_BEFORE_MODIFY_PWD + 1;
	private static final int REQUEST_CODE_PHOTO_PICKED_WITH_DATA = REQUEST_CODE_CAMERA_WITH_DATA + 1;
	private static final int REQUEST_CROP_PHOTO = REQUEST_CODE_PHOTO_PICKED_WITH_DATA + 1;
	//这种情况只出现在QQ或WB的第一次绑定，需要设置密码
	private static final int SET_PWD_WHEN_BINDED = REQUEST_CROP_PHOTO + 1;
	private static final int ACCOUNT_INFO_UPDATED = SET_PWD_WHEN_BINDED + 1;
	private static final int TEST_GET_BIND_INFO = ACCOUNT_INFO_UPDATED + 1;
	private static final int MY_ADDRESS = TEST_GET_BIND_INFO + 1;
	private static final int SEND_ACTIVE_EMAIL = MY_ADDRESS + 1;
	//
	
	private static final int CROP_PHOTO_FROM_GALLERY = 1;
	private static final int CROP_PHOTO_FROM_CAMERA = 2;
	private static String mUserName;
	private String appName;
	private String mPwdMD5;
	private ProgressDialog mProgressDialog = null;
	private LoginTask mLoginTask;
	private AsyncTask mGetInfoTask;
	private LayoutInflater mInflater;
	private EditText mPasswdEditText;
	private String TEMP_TOKEN;
	private boolean mStartedByApps = false;
	private int mAppsAction = 0;
	
	private String mRegType = "";
	//
	private final int mPhotoPickSize = 96;
	private String mLogGetOpenId;
	private Uri mTempPhotoUri;
	private Uri mCroppedPhotoUri;
	//
	private String mErrorMsg = "";
	private static final int ITEM_MODIFY_PWD = 2;
	public static final int ITEM_BIND_PHONE = 3;
	public static final int ITEM_BIND__EMAIL = 4;
	private int mClickItem = 0;
	//第三方登陆的时候，如果有绑定的手机或邮箱，记录他们
	private String mLoginCheckBindPhone;
	private String mLoginCheckBindEmail;
	
	//记录当前登陆用户，设置设置了密码，如果是QQ登陆，且绑定账户设置了密码，应该使用绑定的账户登陆
	private boolean mPasswdSeted = false;
	public static IAccountSettingListener mAccountSettingListener;
	public String mExitSettingState;
	protected MyResource mMyResources = new MyResource(this);
//	private static long mClickTime = 0;
	private static final long DELAYED = 500 * 1000000;
	
	private ImageView userImage;
	
	private TextView userAccount;
	private TextView userHead;
	private TextView userGender;
	private TextView userName;
	private TextView signalGender;
	private TextView signalNickName;
	private TextView userBindPhone;
	private TextView userBindEmail;
	private TextView userAddress;
	private RelativeLayout userGenderLayout;
	private RelativeLayout userNameLayout;
	private RelativeLayout userHeaderLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
		 super.onCreate(savedInstanceState);
    	 ActionBar actionBar = getActionBar();
    	 if(actionBar != null){
    		 actionBar.setDisplayOptions(
	   	                ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
    	 }
    	 if(DebugUtils.DEBUG){
    		 DebugUtils.i(TAG, "onCreate taskId = " + getTaskId());
    	 }
		 Intent intent = getIntent();
		 if(intent != null){
			 appName = intent.getStringExtra(CALLED_FROM_APPS);
			 if(TextUtils.isEmpty(appName)){
				 mStartedByApps = false;
			 }else{
				 mStartedByApps = true;//intent.getBooleanExtra(CALLED_FROM_APPS, false);
			 }
			 mAppsAction = intent.getIntExtra(DroiSDKHelper.BIND_ACCOUNT_TYPE, 0);
		 }
		 
		setContentView(mMyResources.getLayout("lib_droi_account_layout_settings"));
		findViewId();
		if (DroiSDKHelper.PHONE_LOGIN == false) {
			findViewById(mMyResources.getId("lib_droi_account_setting_phone")).setVisibility(View.GONE);
		}
		if (DroiSDKHelper.EMAIL_LOGIN == false) {
			findViewById(mMyResources.getId("lib_droi_account_setting_email")).setVisibility(View.GONE);
		}
		
		 if(Utils.getAvailableNetWorkType(this) == -1){
			 mErrorMsg = getResources().getText(mMyResources.getString("lib_droi_account_network_wrong_text")).toString();
			  setResult(RESULT_CANCELED);
			  finish();
			  showDialog(DIALOG_ENTRY_APP_ERROR);
			  return;
		 }
		 SharedInfo sharedInfo = SharedInfo.getInstance();
		 if(!sharedInfo.accountExits(this)){
			 showDialog(DIALOG_NO_ACCOUNT_EXITS);
			 return;
		 }
		 mInflater = LayoutInflater.from(this);
		 getUserInfo();
	}
	
	private void findViewId() {
		userImage= (ImageView)findViewById(mMyResources.getId("lib_droi_account_head_image"));
		userAccount = (TextView)findViewById(mMyResources.getId("lib_droi_account_useraccount"));
		userHead = (TextView)findViewById(mMyResources.getId("lib_droi_account_user_image_edit"));
		userGender = (TextView)findViewById(mMyResources.getId("lib_droi_account_user_gender"));
		userName = (TextView)findViewById(mMyResources.getId("lib_droi_account_user_username"));
		signalGender = (TextView)findViewById(mMyResources.getId("lib_droi_account_setting_signal_gender"));
		signalNickName = (TextView)findViewById(mMyResources.getId("lib_droi_account_setting_signal_nickname"));
		userBindPhone = (TextView)findViewById(mMyResources.getId("lib_droi_account_user_userbindphone"));
		userBindEmail = (TextView)findViewById(mMyResources.getId("lib_droi_account_user_userbindemail"));
		userAddress = (TextView)findViewById(mMyResources.getId("lib_droi_account_user_editaddress"));
		
		userGenderLayout = (RelativeLayout)findViewById(mMyResources.getId("lib_droi_account_setting_gender"));
		userNameLayout = (RelativeLayout)findViewById(mMyResources.getId("lib_droi_account_setting_nickname"));
		userHeaderLayout = (RelativeLayout)findViewById(mMyResources.getId("lib_droi_account_setting_header"));
	}

	private void getUserInfo(){
		SharedInfo sharedInfo = SharedInfo.getInstance();
		String token = sharedInfo.getAccessToken(getApplicationContext());
		String openId = sharedInfo.getOpenId(getApplicationContext());
		mLogGetOpenId = openId;
		TEMP_TOKEN = token;
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "GET user info : openid = " + openId);
		}
		showProgress();
		mGetInfoTask = new GetUserInfo(openId, token).execute();
	} 
	
	public void onClickEditHead(View view){
		showDialog(DIALOG_SET_AVATER);
	}
	
	public void onClickGender(View view){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View dialogView = mInflater.inflate(mMyResources.getLayout("lib_droi_account_dialog_gender_select"), null);
		final Dialog dialog = builder.setView(dialogView).create();
		
		final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(mMyResources.getId("radiogroup"));
		RadioButton man = (RadioButton) radioGroup.findViewById(mMyResources.getId("man"));
		RadioButton women = (RadioButton) radioGroup.findViewById(mMyResources.getId("woman"));
		RadioButton secret = (RadioButton) radioGroup.findViewById(mMyResources.getId("secret"));
		String gender = userGender.getText().toString();
		if(gender.equals(man.getText().toString())){
			man.setChecked(true);
		}else if(gender.equals(women.getText().toString())){
			women.setChecked(true);
		}else{
			secret.setChecked(true);
		}
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				String gender = ((RadioButton)(group.findViewById(checkedId))).getText().toString();
				userGender.setText(gender);
				new UploadUserInfo(mLogGetOpenId, TEMP_TOKEN, null, gender, null).execute();
				dialog.cancel();
			}
		});
		TextView cancel = (TextView) dialogView.findViewById(mMyResources.getId("lib_droi_account_dialog_btn_cancel"));
		cancel.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		dialog.show();
	}
	public void onClickNickName(View view){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final View dialogView = mInflater.inflate(mMyResources.getLayout("lib_droi_account_dialog_nickname_edit"), null);
		
		final EditText nickNameEdit = (EditText) dialogView.findViewById(mMyResources.getId("dialog_nickname_select"));
		nickNameEdit.setFilters(new InputFilter[] { new InputFilter.LengthFilter(25) });
		nickNameEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() >= 25) {
					Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_max_length_reached"));
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {
			}

		});
		
		
		final Dialog dialog = builder.setView(dialogView).create();
		TextView cancel = (TextView) dialogView.findViewById(mMyResources.getId("lib_droi_account_dialog_btn_cancel"));
		TextView oknext = (TextView) dialogView.findViewById(mMyResources.getId("lib_droi_account_dialog_btn_ok"));
		cancel.setOnClickListener(new TextView.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		oknext.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				String nickName = nickNameEdit.getText().toString();
				if(TextUtils.isEmpty(nickName) || nickName != null && (nickName.length()< 2 || nickName.length() > 25)){
					Utils.showMessage(ActivitySettings.this, mMyResources.getString("lib_droi_account_nick_name_length_limited"));
					return;
				}
				showProgress();
				userName.setText(nickName);
				new UploadUserInfo(mLogGetOpenId, TEMP_TOKEN, nickName, null, null).execute();
				dialog.cancel();
			}
		});
				
		dialog.show();
	}
	public void onClickBindPhone(View view){
		mClickItem = ITEM_BIND_PHONE;
		SharedInfo sharedInfo = SharedInfo.getInstance();
		String passwdVal = sharedInfo.getPasswdVal(this);
		if("0".equals(passwdVal) == false){
			onClickBindPhone();
		}else{
			showDialog(DIALOG_MODIFY_PWD_CONFIRM);
		}
	}
	public void onClickBindEmail(View view){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onPreferenceTreeClick BIND Email");
		}
//		if(mClickTime == 0){
//			mClickTime = System.nanoTime();
			mClickItem = ITEM_BIND__EMAIL;
			SharedInfo sharedInfo = SharedInfo.getInstance();
			//String pwdVal = sharedInfo.getPasswdVal(getApplicationContext());
			
			String mail = userBindEmail.getText().toString();
			int bindMailTag = (Integer) userBindEmail.getTag();
			if(bindMailTag == 0){
				//未激活邮箱
				Intent intent = new Intent(ActivitySettings.this, SendActiveEmailActivity.class);
				String passwdVal = sharedInfo.getPasswdVal(this);
				intent.putExtra("email", mail);
				intent.putExtra("what", SendActiveEmailActivity.EMAIL_RESEND);
				intent.putExtra("account", mUserName);
				intent.putExtra(Constants.ACCOUNT_PWD_VAL, passwdVal);
				intent.putExtra("openid", sharedInfo.getOpenId(getApplicationContext()));
				intent.putExtra("token", sharedInfo.getAccessToken(getApplicationContext()));
				
				startActivityForResult(intent, SEND_ACTIVE_EMAIL);
			}else if(bindMailTag == 1){
				//改绑需要输入密码
				mClickItem = ITEM_BIND__EMAIL;
				showDialog(DIALOG_MODIFY_PWD_CONFIRM);
			}else if(bindMailTag == 2){
				//尚未绑定邮箱的状态
				onClickBindEmail();
			}
//		}else if(System.nanoTime() - mClickTime  >= DELAYED){
//			mClickTime = 0;
//		}
	}

	public void onChangeBindPhone(View view) {
		mClickItem = ITEM_BIND_PHONE;
		showDialog(DIALOG_MODIFY_PWD_CONFIRM);
	}

	public void onChangeBindEmail(View view) {
		mClickItem = ITEM_BIND__EMAIL;
		showDialog(DIALOG_MODIFY_PWD_CONFIRM);
	}

	public void onClickEditAddress(View view) {
		Intent intent = new Intent(ActivitySettings.this,AddressListActivity.class);
		startActivityForResult(intent, MY_ADDRESS);
	}
	
	public void onClickChangePassword(View view){
		mClickItem = ITEM_MODIFY_PWD;
		SharedInfo sharedInfo = SharedInfo.getInstance();
		String pwdVal = sharedInfo.getPasswdVal(getApplicationContext());
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "change pwd " + pwdVal);
		}
		if(/*("openqq".equals(regType) || "openweibo".equals(regType)) &&*/ "0".equals(pwdVal) == false){
			//没有绑定手机和邮箱，提示用户完善资料，并设置密码
			showDialog(DIALOG_MODIFY_PWD_QQ_OR_WEIBO);
		}else{
			showDialog(DIALOG_MODIFY_PWD_CONFIRM);
		}
	}
	public void onClickExitAccount(View view){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(mMyResources.getString("lib_droi_account_dialog_title_hint"))
		.setMessage(mMyResources.getString("lib_droi_account_dialog_msg_body_hint"))
		.setNegativeButton(android.R.string.cancel, null)
		.setPositiveButton(android.R.string.ok,new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedInfo sharedInfo = SharedInfo.getInstance();
				//String regType = sharedInfo.getRegType(getContext());
				// 针对第三方登陆如果需要删除账户的话，直接删除，不需要输入密码
    			//如果用户设置了密码，那么删除之前需要设置密码

				String passwdVal = sharedInfo.getPasswdVal(ActivitySettings.this);
    			if(!"0".equals(passwdVal)){
    				deleteAccount();
    			}else{
    				showDialog(DIALOG_CONFIRM);
				}
			}
		});
		builder.create().show();
	}
	
	 private void deleteAccount(){
		 AccountManager mAccountManager = AccountManager.get(ActivitySettings.this);
		 Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		 Account mAccount = null;
		 if (accounts != null && accounts.length >= 1){
			 mAccount = accounts[0];
		 }
		
		 if(mAccountManager != null && mAccount != null){
			 mAccountManager.removeAccount(mAccount, new AccountManagerCallback<Boolean>(){

				@Override
				public void run(AccountManagerFuture<Boolean> future) {
					boolean failed = true;
					try {
                        if (future.getResult() == true) {
                            failed = false;
                        }
                    } catch (OperationCanceledException e) {
                    } catch (IOException e) {
                    } catch (AuthenticatorException e) {
                    }
					if(failed){
						finish();
					} else {
						String accountName = userAccount.getText().toString();
						if (!TextUtils.isEmpty(accountName)) {
							String toastMsg = getApplicationContext().getResources().getString(mMyResources.getString("lib_droi_account_delete_account_toast"),accountName);
							//Utils.showMessage(ActivitySettings.this, toastMsg);
							mExitSettingState = "DeleteAccount";
							Intent intent = new Intent();
							intent.putExtra("DeleteAccount", true);
							setResult(Activity.RESULT_OK,intent);
							finish();
						}
					}
				}
				
			}, null);
		}
    }
	
	public void onClickDeleteAccount(View view){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String msg = String.format(getResources().getText(mMyResources.getString("lib_droi_account_exit_account_hint")).toString(), appName);
		builder.setTitle(mMyResources.getString("lib_droi_account_dialog_title_hint"))
		.setMessage(msg)
		.setNegativeButton(android.R.string.cancel, null)
		.setPositiveButton(android.R.string.ok,new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		builder.create().show();
	}
	
	
	private void onClickBindPhone(){
		mClickItem = ITEM_BIND_PHONE;
		Intent intent = new Intent();
		intent.setClass(ActivitySettings.this, AccountCheckActivity.class);
		SharedInfo sharedInfo = SharedInfo.getInstance();
		String bindPhone = sharedInfo.getBindPhone(getApplicationContext());
		if(!TextUtils.isEmpty(bindPhone) && Utils.onlyNumber(bindPhone)){
			intent.putExtra("binded_phone", userBindPhone.getText().toString());
		}
		intent.putExtra("title", getResources().getString(mMyResources.getString("lib_droi_account_user_bind_phonenumber")));
		intent.putExtra("hint", getResources().getString(mMyResources.getString("lib_droi_account_hint_input_phonenumer")));
		intent.putExtra("accountType", Constants.ACCOUNT_TYPE_PHONENUMBER);
		startActivityForResult(intent, BIND_ACCOUNT);
	}
	
	private void onClickBindEmail(){
		mClickItem = ITEM_BIND__EMAIL;
		Intent intent = new Intent();
		intent.setClass(ActivitySettings.this, AccountCheckActivity.class);
		intent.putExtra("title", getResources().getString(mMyResources.getString("lib_droi_account_user_bind_email")));
		intent.putExtra("hint", getResources().getString(mMyResources.getString("lib_droi_account_hint_input_email")));
		intent.putExtra("accountType", Constants.ACCOUNT_TYPE_EMAIL);
		startActivityForResult(intent, BIND_ACCOUNT);
	}
	
	 @Override
	 protected Dialog onCreateDialog(int id, Bundle args) {
		 Dialog dialog = null;
		 if(DIALOG_ON_PROGRESS == id){
			 if(mProgressDialog == null){
				 mProgressDialog = new ProgressDialog(this);
			 }
			 mProgressDialog.setMessage(getString(mMyResources.getString("lib_droi_account_msg_on_process")));
			 mProgressDialog.setIndeterminate(true);
			 mProgressDialog.setCancelable(true);
			 mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
	
				@Override
				public void onCancel(DialogInterface dialog) {
					if(mLoginTask != null){
						mLoginTask.cancel(true);
					}
					
					if(mGetInfoTask != null){
						mGetInfoTask.cancel(true);
						mGetInfoTask = null;
						finish();
					}
				}
		    	 
		     });
		     dialog = mProgressDialog;
		 }else if(id == DIALOG_MODIFY_PWD_CONFIRM){		//更换密码
			 View view = mInflater.inflate(mMyResources.getLayout("lib_droi_account_password_input_layout"), null);
			 mPasswdEditText = (EditText)view.findViewById(mMyResources.getId("lib_droi_account_password"));
			 mPasswdEditText.setHint(mMyResources.getString("lib_droi_account_input_original_passwd_text"));
			 dialog = new AlertDialog.Builder(this)
			 	 .setTitle(getString(mMyResources.getString("lib_droi_account_current_account_title")) + mUserName)
			 	 .setView(view)
			 	 .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mPasswdEditText.setText("");
					}
			 		 
			 	 })
			 	 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(mPasswdEditText != null){
							final String  password = mPasswdEditText.getText().toString().trim();
							if(TextUtils.isEmpty(password)){
								Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_toast_passwd_empty"));
								return;
							}
							SharedInfo sharedInfo = SharedInfo.getInstance();
							if(DebugUtils.DEBUG){
								DebugUtils.i(TAG, "check pwd : " + password);
							}
							if(Utils.getAvailableNetWorkType(ActivitySettings.this) == -1){
		    					Utils.showMessage(ActivitySettings.this,mMyResources.getString("lib_droi_account_network_wrong_text"));
		    				 }else{
		    					showProgress();
								new CheckPwdTask(sharedInfo.getOpenId(getApplicationContext()), 
										sharedInfo.getAccessToken(getApplicationContext()), 
										password, new CheckPwdListener() {
									
									@Override
									public void onResult(boolean result) {
										if(DebugUtils.DEBUG){
											DebugUtils.i(TAG, "check result : " + result);
										}
										hideProgress();
										if(result){
											//setPassword(password);
											if(mClickItem == ITEM_BIND__EMAIL){
												//判断是否为未激活
												Intent intent = new Intent();
												intent.setClass(ActivitySettings.this, AccountCheckActivity.class);
												intent.putExtra("binded_email", userBindEmail.getText().toString());
												intent.putExtra("title", getResources().getString(mMyResources.getString("lib_droi_account_user_bind_email")));
												intent.putExtra("hint", getResources().getString(mMyResources.getString("lib_droi_account_hint_input_email")));
												intent.putExtra("accountType", Constants.ACCOUNT_TYPE_EMAIL);
												startActivityForResult(intent, BIND_ACCOUNT);
											}else if(ITEM_BIND_PHONE == mClickItem){
												Intent intent = new Intent();
												intent.setClass(ActivitySettings.this, AccountCheckActivity.class);
												intent.putExtra("binded_phone", userBindPhone.getText().toString());
												intent.putExtra("title", getResources().getString(mMyResources.getString("lib_droi_account_user_bind_phonenumber")));
												intent.putExtra("hint", getResources().getString(mMyResources.getString("lib_droi_account_hint_input_phonenumer")));
												intent.putExtra("accountType", Constants.ACCOUNT_TYPE_PHONENUMBER);
												startActivityForResult(intent, BIND_ACCOUNT);
											}else{
												modifyPassword(password);
											}
										}else{
											Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_dialog_error_pwd"));
										}
									}
								}).execute();
		    				 }
							mPasswdEditText.setText("");
						}
					}
			 		 
			 	 }).create();
		 }else if(id == DIALOG_MODIFY_PWD_QQ_OR_WEIBO){
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 builder.setTitle(mMyResources.getString("lib_droi_account_dialog_title_hint"));
			 builder.setMessage(mMyResources.getString("lib_droi_account_dialog_msg_bind_info"));
			 //builder.setMessage(mMyResources.getString("lib_droi_account_msg_bind_phone_or_email);
			 builder.setNegativeButton(android.R.string.cancel, null);
			 builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					//register_account_hint
					changePwdsetAccount();
				}
			 });
			 dialog = builder.create();
		 }else if(DIALOG_SET_AVATER == id){
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 builder.setTitle(mMyResources.getString("lib_droi_account_avatar_dialog_title"));
			 
			 CharSequence[] values = getResources().getStringArray(mMyResources.getArrray("lib_droi_account_avatar_sources_entries"));
			 builder.setItems(values, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(which == 0){
						//take photo
						chooseTakePhoto();
					}else if(which == 1){
						//pick photo
						pickFromGallery();
					}
				}
				 
			 });
			 dialog = builder.create();
		 }else if(DIALOG_MSG_BINDED_FOR_QQ_WB == id){
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
						SharedInfo sharedInfo = SharedInfo.getInstance();
						sharedInfo.deleteAccount(ActivitySettings.this, mUserName, true);
					}
					 
				 });
			 dialog = builder.create();
		 }else if(DIALOG_ENTRY_APP_ERROR == id){
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 builder.setTitle(mMyResources.getString("lib_droi_account_dialog_title_hint"));
			 builder.setCancelable(false);
			 builder.setMessage(mErrorMsg);
			 builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
					 
				 });
			 dialog = builder.create();
		 }else if(DIALOG_NO_ACCOUNT_EXITS == id){
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 builder.setTitle(mMyResources.getString("lib_droi_account_dialog_title_hint"));
			 builder.setCancelable(false);
			 builder.setMessage(mMyResources.getString("lib_droi_account_no_account"));
			 builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent intent = new Intent();
						setResult(RESULT_OK, intent);
						finish();
					}
					 
				 });
			 dialog = builder.create();
		 }else if(DIALOG_CONFIRM == id){
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 View view = mInflater.inflate(mMyResources.getLayout("lib_droi_account_password_input_layout"),null);
			 final EditText mEditText = (EditText)view.findViewById(mMyResources.getId("lib_droi_account_password"));
			 builder.setTitle(getString(mMyResources.getString("lib_droi_account_current_account_title")) + mUserName)
			 .setView(view)
			 .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mEditText.setText("");
				}
			}).setPositiveButton(android.R.string.ok,	new Dialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

	    			if(mEditText != null){
	    				String password = mEditText.getText().toString().trim();
	    				 SharedInfo sharedInfo = SharedInfo.getInstance();
	    				 if(TextUtils.isEmpty(password)){
	    					 Utils.showMessage(ActivitySettings.this, mMyResources.getString("lib_droi_account_toast_passwd_empty"));
	    					 return;
	    				 }
	    				 if(Utils.getAvailableNetWorkType(ActivitySettings.this) == -1){
	    					Utils.showMessage(ActivitySettings.this,mMyResources.getString("lib_droi_account_network_wrong_text"));
	    				 }else{
		    				 new CheckPwdTask(sharedInfo.getOpenId(ActivitySettings.this),sharedInfo.getAccessToken(ActivitySettings.this),password, new CheckPwdListener() {
									@Override
									public void onResult(boolean result) {
										if(result){
											deleteAccount();
										}else{
											Utils.showMessage(ActivitySettings.this, mMyResources.getString("lib_droi_account_dialog_error_pwd"));
										}
									}
								}).execute();
	    				 }
	    			}
	    			mEditText.setText("");
				}
			 });
			 dialog = builder.create();
		 }
		 
	     return dialog;
	}
	
	private void modifyPassword(final String oldPassword){
		//针对平台用户，登陆之后才能修改密码, 登陆之后应该有token, openID
		Intent intent = new Intent(ActivitySettings.this, PasswordSetActivity.class);
		intent.putExtra("title", getResources().getString(mMyResources.getString("lib_droi_account_user_mofify_pwd")));
		//intent.putExtra("username", mUserName);
		SharedInfo sharedInfo = SharedInfo.getInstance();
		intent.putExtra("token", sharedInfo.getAccessToken(getApplicationContext()));
		intent.putExtra(Constants.REQUEST_TYPE, Constants.PASSWD_MODIFY);
		intent.putExtra("pwd", oldPassword);
		startActivityForResult(intent, REQUEST_CODE_MODIFY_PWD);
	}
	
	private void chooseTakePhoto(){
		//
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		mTempPhotoUri = PhotoUtils.generateTempImageUri();
		mCroppedPhotoUri = PhotoUtils.generateTempCroppedImageUri();
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "chooseTakePhoto mCroppedPhotoUri : " + mCroppedPhotoUri);
		}
		//where to store the image after take photo
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri);
		startActivityForResult(intent, REQUEST_CODE_CAMERA_WITH_DATA);
	}
	
	private void pickFromGallery(){
		
		//mTempPhotoUri = PhotoUtils.generateTempImageUri();
		mCroppedPhotoUri = PhotoUtils.generateTempCroppedImageUri();
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "pickFromGallery mCroppedPhotoUri : " + mCroppedPhotoUri);
		}
		Intent intent = getPhotoPickIntent(null);
		startActivityForResult(intent, REQUEST_CODE_PHOTO_PICKED_WITH_DATA);
	}
	
	private Intent getPhotoPickIntent(Uri outputUri) {
		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
	    intent.setType("image/*");
	    //PhotoUtils.addPhotoPickerExtras(intent, outputUri);
	    return intent;
	}
	
	private void changePwdsetAccount(){
		Intent intent = new Intent();
		intent.setClass(ActivitySettings.this, AccountCheckActivity.class);
		if(DroiSDKHelper.PHONE_LOGIN ==true && DroiSDKHelper.EMAIL_LOGIN == false){
			intent.putExtra("title", getResources().getString(mMyResources.getString("lib_droi_account_user_bind_phonenumber")));
			intent.putExtra("hint", getResources().getString(mMyResources.getString("lib_droi_account_hint_input_phonenumer")));
			intent.putExtra("accountType", Constants.ACCOUNT_TYPE_PHONENUMBER);
		}else if(DroiSDKHelper.PHONE_LOGIN ==false && DroiSDKHelper.EMAIL_LOGIN == true){
			intent.putExtra("title", getResources().getString(mMyResources.getString("lib_droi_account_user_bind_email")));
			intent.putExtra("hint", getResources().getString(mMyResources.getString("lib_droi_account_hint_input_email")));
			intent.putExtra("accountType", Constants.ACCOUNT_TYPE_EMAIL);
		}else{
			intent.putExtra("title", getResources().getText(mMyResources.getString("lib_droi_account_bind_info_title")));
			intent.putExtra("hint", getResources().getText(mMyResources.getString("lib_droi_account_register_account_hint")));
		}
		startActivityForResult(intent, BIND_BEFORE_MODIFY_PWD);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	

	public void onBack(View view){
		onBackPressed();
	}
	
	private void showProgress(){
		showDialog(DIALOG_ON_PROGRESS);
	}
		
	private void hideProgress(){
        if (mProgressDialog != null){
            mProgressDialog.dismiss();
        }
	}
	
	private void handleLogin(String userName, String pwdMD5){
		showProgress();
		mLoginTask = new LoginTask(userName, pwdMD5);
		mLoginTask.execute();
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "finish");
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onActivityResult requestCode : " + requestCode + ", resultCode = " + resultCode);
		}
		switch(requestCode){
		case REQUEST_CODE_MODIFY_PWD:{
				if(resultCode == RESULT_OK){
				//修改密码之后，让用户重新登陆。对邮箱来说，在app端也可以修改密码,不必通过邮箱
					SharedInfo sharedInfo = SharedInfo.getInstance();
					if(mLoginTask != null){
						mLoginTask.cancel(true);
						mLoginTask = null;
					}
					
					//goto welcom WELCOME
					sharedInfo.deleteAccount(this, mUserName, false);
					Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_toast_passwd_modify_success"));
					//handleLogin(mUserName, mPwdMD5);
					finish();
			}else if(resultCode == RESULT_CANCELED){
				
			}
			
		}
		break;

		case BIND_ACCOUNT:{
			if(resultCode == RESULT_OK){
				//对于绑定要区分是QQ绑定，还是卓易账号绑定
				//QQ第一次绑定是注册，当要携带QQ相关的信息，
				//要先确定用户是否已经设置了密码，获取passwdval字段
				
				String accountName = data.getStringExtra("accountname");
				int accountType =  data.getIntExtra("accountType", -1);
				bindAccount(accountName, accountType, null);
				/*
				if(accountType == AuthenticatorActivity.REGISTER_BY_PHONENUMBER){
					Intent intent = new Intent();
					intent.setClass(this, SecurityCodeActivity.class);
					intent.putExtra("phonenumber", accountName);
					intent.putExtra("codetype", Constants.CODE_TYPE_BIND_MOBILE);
					//此处为第三方登陆后，服务器返回的token
					intent.putExtra("bindToken", TEMP_TOKEN);
					startActivityForResult(intent, GET_SECURITY_CODE);
				}else if(accountType == AuthenticatorActivity.REGISTER_BY_EMAIL){
					//showProgress();
					//new BindMailTask(mLogGetOpenId, TEMP_TOKEN, accountName).execute();
					//绑定邮箱之后需要设置密码
					Intent intent = new Intent();
					intent.setClass(AccountSettings.this, SendEmailActivity.class);
					intent.putExtra("title", getResources().getText(mMyResources.getString("lib_droi_account_send_email_title));
					intent.putExtra("email", accountName);
					intent.putExtra("openid", mLogGetOpenId);
					intent.putExtra("what", "bindmail");
					intent.putExtra("token", TEMP_TOKEN);
					startActivityForResult(intent, SEND_EMAIL);
				}*/
				
			}else if(resultCode == RESULT_CANCELED){
				if(mAppsAction != 0){
					setResult(RESULT_CANCELED);
					finish();
				}
				/*
				if(data != null){
					int accountType =  data.getIntExtra("accountType", -1);
					if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
						
					}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
							
					}
				}*/
			}
		}
			break;
		case SET_PWD_WHEN_BINDED:{
			if(resultCode == RESULT_OK){
				String accountName = data.getStringExtra("accountName");
				int accountType =  data.getIntExtra("accountType", -1);
				String password = data.getStringExtra(Constants.JSON_S_PWD);
				if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
					//Intent intent = new Intent();
					//intent.setClass(AccountSettings.this, TestGetBindInfo.class);
					//startActivityForResult(intent, TEST_GET_BIND_INFO);
					String uid = data.getStringExtra("uid");
					String securityCode = data.getStringExtra("securityCode");
					String token = data.getStringExtra("token");
					doBindMobile(uid, securityCode, token, password);
				}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
					doBindEmail(accountName, password);
				}
			}else if(resultCode == RESULT_CANCELED){
				if(data != null){
					int accountType =  data.getIntExtra("accountType", -1);
					if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
						SharedInfo sharedInfo = SharedInfo.getInstance();
						String passwdVal = sharedInfo.getPasswdVal(this);
						String phonenumber = data.getStringExtra("accountName");
						Intent intent = new Intent();
						intent.setClass(this, SecurityCodeActivity.class);
						intent.putExtra("phonenumber", phonenumber);
						intent.putExtra("codetype", Constants.CODE_TYPE_BIND_MOBILE);
						//此处为第三方登陆后，服务器返回的token
						intent.putExtra("bindToken", sharedInfo.getAccessToken(getApplicationContext()));
						intent.putExtra(Constants.ACCOUNT_PWD_VAL, passwdVal);
						startActivityForResult(intent, GET_SECURITY_CODE);
					}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
						Intent intent = new Intent();
						intent.setClass(ActivitySettings.this, AccountCheckActivity.class);
						intent.putExtra("title", getResources().getString(mMyResources.getString("lib_droi_account_user_bind_email")));
						intent.putExtra("hint", getResources().getString(mMyResources.getString("lib_droi_account_hint_input_email")));
						intent.putExtra("accountType", Constants.ACCOUNT_TYPE_EMAIL);
						startActivityForResult(intent, BIND_ACCOUNT);
					}
					
				}
			}
			
		}
			break;
		case ACCOUNT_INFO_UPDATED:{
			if(resultCode == RESULT_OK){
				//user the token update user info ,preference update
				if(mAppsAction != 0){
					setResult(RESULT_OK);
					finish();
				}else{
					SharedInfo sharedInfo = SharedInfo.getInstance();
					String token = sharedInfo.getAccessToken(getApplicationContext());
					String openId = sharedInfo.getOpenId(getApplicationContext());
					showProgress();
					new GetUserInfo(openId, token).execute();
				}
			}else{
				if(mAppsAction != 0){
					setResult(RESULT_CANCELED);
					finish();
				}
			}
		}
			break;
		case TEST_GET_BIND_INFO:{
			if(resultCode == RESULT_OK){
				String uid = data.getStringExtra("uid");
				String securityCode = data.getStringExtra("securityCode");
				String token = data.getStringExtra("token");
				doBindMobile(uid, securityCode, token, "123456");
			}
		}
			break;
		case BIND_BEFORE_MODIFY_PWD:
			if(resultCode == RESULT_OK){
				String accountName = data.getStringExtra("accountname");
				int accountType =  data.getIntExtra("accountType", -1);
				
				/**	bindAccount(accountName, accountType, null);
				if(accountType == AuthenticatorActivity.REGISTER_BY_PHONENUMBER){
					//register by phonenumber, then set pwd and login
					bindAccount(accountName, AuthenticatorActivity.REGISTER_BY_PHONENUMBER);
				}else if(accountType == AuthenticatorActivity.REGISTER_BY_EMAIL){
					//register by email , then set pwd and login
					bindAccount(accountName, AuthenticatorActivity.REGISTER_BY_EMAIL);
				} */
				
				SharedInfo sharedInfo = SharedInfo.getInstance();
				String passwdVal = sharedInfo.getPasswdVal(this);
				if("0".equals(passwdVal) == false){
					mPasswdSeted = false;
				}else{
					mPasswdSeted = true;
				}
				
				if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
					Intent intent = new Intent();
					intent.setClass(this, SecurityCodeActivity.class);
					intent.putExtra("phonenumber", accountName);
					intent.putExtra("codetype", Constants.CODE_TYPE_BIND_MOBILE);
					//此处为第三方登陆后，服务器返回的token
					intent.putExtra("bindToken", sharedInfo.getAccessToken(getApplicationContext()));
					intent.putExtra(Constants.ACCOUNT_PWD_VAL, passwdVal);
					startActivityForResult(intent, GET_SECURITY_CODE_MODIFY_PWD);
				}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
					//showProgress();
					//new BindMailTask(mLogGetOpenId, TEMP_TOKEN, accountName).execute();
					if("0".equals(passwdVal) == false){
						//没有设置密码的账户，需要用户首先输入密码
						Intent intent = new Intent();
						intent.setClass(ActivitySettings.this, SetPwdWhenBinded.class);
						intent.putExtra("accountName", accountName);
						intent.putExtra("accountType", accountType);
						startActivityForResult(intent, SET_PWD_WHEN_BINDED_MODIFY_PWD);
					}else{
						doBindEmail(accountName, null);
					}
				}
			}
			break;
			
		case GET_SECURITY_CODE_MODIFY_PWD:{
			//绑定手机，获取的验证码
			SharedInfo sharedInfo = SharedInfo.getInstance();
			String passwdVal = sharedInfo.getPasswdVal(this);
			if(resultCode == RESULT_OK){
				String uid = data.getStringExtra("uid");
				String token = data.getStringExtra("token");
				String securityCode = data.getStringExtra("securityCode");
				String phonenumber = data.getStringExtra("phonenumber");
				if("0".equals(passwdVal) == false){
					//没有设置密码的账户，需要用户首先输入密码
					Intent intent = new Intent();
					intent.setClass(ActivitySettings.this, SetPwdWhenBinded.class);
					intent.putExtra("uid", uid);
					intent.putExtra("token", token);
					intent.putExtra("securityCode", securityCode);
					intent.putExtra("accountName", phonenumber);
					intent.putExtra("accountType", Constants.ACCOUNT_TYPE_PHONENUMBER);
					startActivityForResult(intent, SET_PWD_WHEN_BINDED);
				}else{
					doBindMobile(uid, securityCode, token, null);
				}
			}else if(resultCode == RESULT_CANCELED){
				changePwdsetAccount();
			}
		}
			break;
		case SET_PWD_WHEN_BINDED_MODIFY_PWD:{
			if(resultCode == RESULT_OK){
				String accountName = data.getStringExtra("accountName");
				int accountType =  data.getIntExtra("accountType", -1);
				String password = data.getStringExtra(Constants.JSON_S_PWD);
				if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
					//Intent intent = new Intent();
					//intent.setClass(AccountSettings.this, TestGetBindInfo.class);
					//startActivityForResult(intent, TEST_GET_BIND_INFO);
					String uid = data.getStringExtra("uid");
					String securityCode = data.getStringExtra("securityCode");
					String token = data.getStringExtra("token");
					doBindMobile(uid, securityCode, token, password);
				}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
					doBindEmail(accountName, password);
				}
			}else if(resultCode == RESULT_CANCELED){
				changePwdsetAccount();
			}
		}
			break;
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
					//没有设置密码的账户，需要用户首先输入密码
					Intent intent = new Intent();
					intent.setClass(ActivitySettings.this, SetPwdWhenBinded.class);
					intent.putExtra("uid", uid);
					intent.putExtra("token", token);
					intent.putExtra("securityCode", securityCode);
					intent.putExtra("accountName", phonenumber);
					intent.putExtra("accountType", Constants.ACCOUNT_TYPE_PHONENUMBER);
					startActivityForResult(intent, SET_PWD_WHEN_BINDED);
				}else{
					doBindMobile(uid, securityCode, token, null);
				}
			}else if(resultCode == RESULT_CANCELED){
				Intent intent = new Intent();
				intent.setClass(ActivitySettings.this, AccountCheckActivity.class);
				String bindPhone = sharedInfo.getBindPhone(getApplicationContext());
				if(!TextUtils.isEmpty(bindPhone) && Utils.onlyNumber(bindPhone)){
					intent.putExtra("binded_phone", userBindPhone.getText().toString());
				}
				intent.putExtra("title", getResources().getString(mMyResources.getString("lib_droi_account_user_bind_phonenumber")));
				intent.putExtra("hint", getResources().getString(mMyResources.getString("lib_droi_account_hint_input_phonenumer")));
				intent.putExtra("accountType", Constants.ACCOUNT_TYPE_PHONENUMBER);
				startActivityForResult(intent, BIND_ACCOUNT);
			}
		}
			break;
		case SEND_EMAIL:{
			//如果是修改邮箱密码，那么修改之后，删除账户
			SharedInfo sharedInfo = SharedInfo.getInstance();
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "onActivityResult SEND_MAIL mClickItem = " + mClickItem);
			}
			if(mClickItem == ITEM_MODIFY_PWD){
				//goto welcom WELCOME
				//因此第一次绑定手机的时候
				if(mPasswdSeted){
					sharedInfo.deleteAccount(this, mUserName, true);
				}
			}else if(mClickItem == ITEM_BIND_PHONE){
				//rebind or not, if rebind ,delete account
			}else if(mClickItem == ITEM_BIND__EMAIL){
				//rebind or not
			}else{
				
			}
			showUpdatedMsg();
			/*
			if(mPasswdSeted == false){
				//如果是绑定邮箱的话，且是第一次绑定的话，那么需要更新用户的token
				//sharedInfo.saveDataToAccount(getApplicationContext());
				String passwdVal = sharedInfo.getPasswdVal(this);
				if(DebugUtils.DEBUG){
					DebugUtils.i(TAG, "passwdVal : " + passwdVal);
				}
				mPasswdSeted = true;
				Intent intent = new Intent();
				intent.setClass(AccountSettings.this, UserInfoUpdated.class);
				startActivityForResult(intent, ACCOUNT_INFO_UPDATED);
			}*/
		}
			break;
		case SEND_ACTIVE_EMAIL:{
			if(resultCode == RESULT_OK){
				if(data != null){
					SharedInfo sharedInfo = SharedInfo.getInstance();
					String email = data.getStringExtra("email");
					String passwdVal = sharedInfo.getPasswdVal(this);
					String accountName = userAccount.getText().toString();
					int accountType = Utils.getAccountType(accountName);
					if(mClickItem == ITEM_BIND__EMAIL){
						//rebind or not
						if(accountType == Constants.ACCOUNT_TYPE_EMAIL && "0".equals(passwdVal)){
							sharedInfo.deleteAccount(this, email, true);
						}else{
							getUserInfo();
						}
					}
				}
			}

		}
		break;
			
		case RESET_PWD_GET_SECURITY:{
			if(resultCode == RESULT_OK){
				//String uid = data.getStringExtra("uid");
				String token = data.getStringExtra("token");
				String securityCode = data.getStringExtra("securityCode");
				//String codeType = data.getStringExtra(Constants.CODE_TYPE);
				Intent intent = new Intent(this, PasswordSetActivity.class);
				intent.putExtra("title", getResources().getString(mMyResources.getString("lib_droi_account_create_psw_title")));
				intent.putExtra(Constants.REQUEST_TYPE, Constants.PASSWD_RESET);
				intent.putExtra("securityCode", securityCode);
				intent.putExtra("token", token);
				startActivityForResult(intent, RESET_PWD_FOR_BIND_PHONE);
			}
		}
			
			break;
			
		case RESET_PWD_FOR_BIND_PHONE:{
			if(resultCode == RESULT_OK){
				//go to welcom
				SharedInfo sharedInfo = SharedInfo.getInstance();
				sharedInfo.deleteAccount(this, mUserName, true);
			}
		}
			break;
		case SET_PWD_FOR_BIND_MAIL:{
			if(resultCode == RESULT_OK){
				//go to welcom
				SharedInfo sharedInfo = SharedInfo.getInstance();
				sharedInfo.deleteAccount(this, mUserName, true);
			}
		}
			break;
		case REQUEST_CODE_CAMERA_WITH_DATA:{
			/*
			if(resultCode == RESULT_OK){
				if (data != null && data.getData() != null) {
					final Uri toCrop = data.getData();
					Utils.showMessage(this, toCrop.toString());
				}
				//Log.i(TAG, "DATA " + );
			}*/
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "REQUEST_CODE_CAMERA_WITH_DATA  mTempPhotoUri = " + 
							mTempPhotoUri + ", mCroppedPhotoUri : " + mCroppedPhotoUri);
			}
			cropPhoto(mTempPhotoUri, mCroppedPhotoUri, CROP_PHOTO_FROM_CAMERA);
		}
		break;
		case REQUEST_CROP_PHOTO:{
			saveImage();
		}
			break;
		case REQUEST_CODE_PHOTO_PICKED_WITH_DATA:{
			if(resultCode == RESULT_OK){
				if (data != null && data.getData() != null) {
					final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
					Uri toCrop = data.getData();
					String pathString = null;
					if(isKitKat){
						pathString = PhotoUtils.getPath(this, toCrop);
					}else{
						pathString = PhotoUtils.selectImage(this,data);
					}
					if( pathString != null ){
						toCrop = Uri.fromFile(new File(pathString));
						if(mCroppedPhotoUri == null){
							mCroppedPhotoUri = PhotoUtils.generateTempCroppedImageUri();
						}
						cropPhoto(toCrop, mCroppedPhotoUri, CROP_PHOTO_FROM_GALLERY);
					}
				}
			}
		}
			break;
		case MY_ADDRESS:
			if(data != null){
				boolean address_avaliable = data.getBooleanExtra("address", false);
				if(address_avaliable){
					userAddress.setText(mMyResources.getString("lib_droi_account_address_saved"));
//					mMyAddressPreference.setSummary(mMyResources.getString("lib_droi_account_address_saved"));
					
				}else{
					userAddress.setText(mMyResources.getString("lib_droi_account_address_empty"));
//					mMyAddressPreference.setSummary(mMyResources.getString("lib_droi_account_address_empty"));
				}
			}
			break;
		}
		
	}
	
	private void showUpdatedMsg(){
		if(mPasswdSeted == false){
			//如果是绑定邮箱的话，且是第一次绑定的话，那么需要更新用户的token
			//sharedInfo.saveDataToAccount(getApplicationContext());
			mPasswdSeted = true;
			Intent intent = new Intent();
			intent.setClass(ActivitySettings.this, UserInfoUpdated.class);
			startActivityForResult(intent, ACCOUNT_INFO_UPDATED);
		}else{
			//如果仅仅是绑定的话，那么绑定之后更新用户信息
			getUserInfo();
		}
	}
	
	private void bindAccount(String accountName, int accountType, String password){
		SharedInfo sharedInfo = SharedInfo.getInstance();
		String passwdVal = sharedInfo.getPasswdVal(this);
		if("0".equals(passwdVal) == false){
			mPasswdSeted = false;
		}else{
			mPasswdSeted = true;
		}
		
		if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
			Intent intent = new Intent();
			intent.setClass(this, SecurityCodeActivity.class);
			intent.putExtra("phonenumber", accountName);
			intent.putExtra("codetype", Constants.CODE_TYPE_BIND_MOBILE);
			//此处为第三方登陆后，服务器返回的token
			intent.putExtra("bindToken", sharedInfo.getAccessToken(getApplicationContext()));
			intent.putExtra(Constants.ACCOUNT_PWD_VAL, passwdVal);
			startActivityForResult(intent, GET_SECURITY_CODE);
		}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
			//showProgress();
			//new BindMailTask(mLogGetOpenId, TEMP_TOKEN, accountName).execute();
			if("0".equals(passwdVal) == false){
				//没有设置密码的账户，需要用户首先输入密码
				Intent intent = new Intent();
				intent.setClass(ActivitySettings.this, SetPwdWhenBinded.class);
				intent.putExtra("accountName", accountName);
				intent.putExtra("accountType", accountType);
				startActivityForResult(intent, SET_PWD_WHEN_BINDED);
			}else{
				doBindEmail(accountName, null);
			}
		}
	}
	
	private void doBindMobile(String uid, String securityCode, String token, String password){
		showProgress();
		//绑定手机，如果之前设置过密码，那么直接绑定即可，如果没有，那么需要设置密码。
		new BindMobileTask(uid, securityCode, token, password).execute();
	}
	
	private void doBindEmail(String accountName, String password){
		/*
		SharedInfo sharedInfo = SharedInfo.getInstance();
		String passwdVal = sharedInfo.getPasswdVal(this);
		Intent intent = new Intent();
		intent.setClass(AccountSettings.this, SendEmailActivity.class);
		intent.putExtra("title", getResources().getText(mMyResources.getString("lib_droi_account_send_email_title));
		intent.putExtra("email", accountName);
		intent.putExtra("openid", sharedInfo.getOpenId(getApplicationContext()));
		intent.putExtra("what", "bindmail");
		intent.putExtra("token", sharedInfo.getAccessToken(getApplicationContext()));
		if("0".equals(passwdVal) == false && !TextUtils.isEmpty(password)){
			intent.putExtra(Constants.JSON_S_PWD, password);
		}
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "doBindEmail passwdVal = " + passwdVal);
		}
		intent.putExtra(Constants.ACCOUNT_PWD_VAL, passwdVal);
		startActivityForResult(intent, SEND_EMAIL);
		*/
		SharedInfo sharedInfo = SharedInfo.getInstance();
		String passwdVal = sharedInfo.getPasswdVal(this);
		Intent intent = new Intent(ActivitySettings.this, SendActiveEmailActivity.class);
		intent.putExtra("email", accountName);
		intent.putExtra("what", SendActiveEmailActivity.EMAIL_BIND);
		intent.putExtra("account", userAccount.getText().toString());
		if("0".equals(passwdVal) == false && !TextUtils.isEmpty(password)){
			intent.putExtra("pwd", password);
		}
		intent.putExtra(Constants.ACCOUNT_PWD_VAL, passwdVal);
		intent.putExtra("openid", sharedInfo.getOpenId(getApplicationContext()));
		intent.putExtra("token", sharedInfo.getAccessToken(getApplicationContext()));
		startActivityForResult(intent, SEND_ACTIVE_EMAIL);
	}
	
	private class LoginTask extends AsyncTask<Void, Void, String>{
		 private String mUserName;
		 private String mPasswordMD5;
		 private String uType;
		 private Runnable mRunnable;
		 
		 public LoginTask(String username, String password){
			 mUserName = username;
			 mPasswordMD5 = password;
			 uType = Utils.getAccountUtype(username);
		 }
		 
		 public LoginTask(String username, String password, Runnable runnable){
			 this(username, password);
			 mRunnable = runnable;
		 }
		 
		@Override
		protected String doInBackground(Void... params) {
			// TODO Autogenerated method stub
			final Map<String, String> loginParams = new HashMap<String, String>();
			loginParams.put(Constants.JSON_S_UID, mUserName);
			loginParams.put(Constants.JSON_S_PWD, mPasswordMD5);
			loginParams.put("utype", uType);
			loginParams.put("devinfo", " ");
			loginParams.put("sign", MD5Util.md5(mUserName + mPasswordMD5 + uType + " "+Constants.SIGNKEY));
			
			String loginResult = null;
			try{
				loginResult = HttpOperation.postRequest(Constants.ACCOUNT_LOGIN, loginParams);
				
				
	            JSONObject jsonObject = new JSONObject(loginResult);
	            String avatarStr = Constants.AVATAR_URL_QQ_WB_DEFAULT;
	            String avatarUrl = jsonObject.has(avatarStr) ? jsonObject.getString(avatarStr) : null;
	            //supprt wb-qq selfdef avatar
	            if(TextUtils.isEmpty(avatarUrl)){
	            	avatarUrl = jsonObject.has(Constants.AVATAR_USER_SPECIFY) ? 
	            			jsonObject.getString(Constants.AVATAR_USER_SPECIFY) : null;
	            }
	            if(TextUtils.isEmpty(avatarUrl)){
	            	AvatarUtils.deleteUserAvatar();
	            }else{
	            	AvatarUtils.downloadUserAvatar(avatarUrl);
	            }
	            
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return loginResult;
		}
		
       @Override
       protected void onPostExecute(String result){
       	super.onPostExecute(result);
       	hideProgress();
       	if(DebugUtils.DEBUG){
       		DebugUtils.i(TAG, "LoginTask : " + result);
       	}
       	if (!TextUtils.isEmpty(result)){
       		try {
					JSONObject jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == 0){
						TEMP_TOKEN = jsonObject.getString("token");
						mLogGetOpenId = jsonObject.getString("openid");
						int pwsswdSeted = jsonObject.has(Constants.ACCOUNT_PWD_VAL) ? 
								jsonObject.getInt(Constants.ACCOUNT_PWD_VAL) : -1;
						
						mPasswdSeted = (pwsswdSeted == 0);
						
						if(mRunnable != null){
							mRunnable.run();
						}else{
							updatePreferences(jsonObject);
							//showProgress();
							//new GetUserInfo(mLogGetOpenId, TEMP_TOKEN).execute();
						}
					}else{
                   	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                   		getResources().getString(mMyResources.getString("lib_droi_account_login_fail"));
                   	//Utils.showMessage(getApplicationContext(), desc);
                   	if(mRunnable == null){
	                    	mErrorMsg = desc;
	                    	showDialog(DIALOG_ENTRY_APP_ERROR);
	                    	return;
                   	}else{
                   		finish();
                   	}
                   	/*
                   	if(mRunnable == null){
                   		finish();
                   	}else{
                   		finish();
                   	}*/
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
       	}else{
       		Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_login_fail"));
       	}
       }
       
       @Override
       protected void onCancelled(){
       	setResult(RESULT_CANCELED);
       	finish();
       }
		 
	 }

	//对于手机或邮箱用户，获取用户信息，需登录之后
	private class GetUserInfo extends AsyncTask<Void, Void, String> {
		private final String mGUI_openId;
		private final String mGUI_token;
		
		public GetUserInfo(String openId, String token){
			mGUI_openId = openId;
			mGUI_token = token;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			final Map<String, String> userParams = new HashMap<String, String>();
			String signString = MD5Util.md5(mGUI_openId + mGUI_token + Constants.SIGNKEY);
			userParams.put(Constants.JSON_S_OPENID, mGUI_openId);
			userParams.put(Constants.JSON_S_TOKEN, mGUI_token);
			userParams.put("sign", signString);
			//try to get address
			userParams.put("delivery", "1");
			String result = null;
			try{
				result = HttpOperation.postRequest(Constants.ACCOUNT_GET_USER_INFO, userParams);
				JSONObject jsonObject = new JSONObject(result);

	            String avatarStr = Constants.AVATAR_USER_SPECIFY;//Constants.AVATAR_URL_QQ_WB_DEFAULT;
	            String avatarUrl = jsonObject.has(avatarStr) ? jsonObject.getString(avatarStr) : null;
	            //supprt wb-qq selfdef avatar
	            if(TextUtils.isEmpty(avatarUrl)){
	            	avatarUrl = jsonObject.has(Constants.AVATAR_URL_QQ_WB_DEFAULT) ? 
	            			jsonObject.getString(Constants.AVATAR_URL_QQ_WB_DEFAULT) : null;
	            }
	            if(TextUtils.isEmpty(avatarUrl)){
	            	AvatarUtils.deleteUserAvatar();
	            }else{
	            	AvatarUtils.downloadUserAvatar(avatarUrl);
	            }
	        }catch (Exception e){
	            e.printStackTrace();
	        }
			return result;
		}
		
		@Override
       protected void onPostExecute(String result){
       	super.onPostExecute(result);
       	hideProgress();
       	if(DebugUtils.DEBUG){
       		DebugUtils.i(TAG, "getUserInfo : " + result);
       	}
       	if (!TextUtils.isEmpty(result)){
       		try {
					JSONObject jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == 0){
						SharedInfo sharedInfo = SharedInfo.getInstance();
						User user = sharedInfo.getData();
						String oldpasswdVal = sharedInfo.getPasswdVal(getApplicationContext());
						int passwdVal = jsonObject.has("passwdval")?jsonObject.getInt("passwdval"): -1;
						if(oldpasswdVal != null && oldpasswdVal.equals(passwdVal+"") == false){
							user.setPasswdVal(passwdVal);
							//save the passwdval to the data
							sharedInfo.updatePasswdval(getApplicationContext(), passwdVal+"");
						}
						sharedInfo.updateAvatarUrl(getApplicationContext(), AvatarUtils.getAvatarPath());
						updatePreferences(jsonObject);
					}else if(rs == 2){
						//需要用户重新登录
						SharedInfo sharedInfo = SharedInfo.getInstance();
						sharedInfo.deleteAccount(ActivitySettings.this, mUserName, true);
					}else if(rs == -2001){
						showDialog(DIALOG_NO_ACCOUNT_EXITS);
					}else{
						Utils.showMessage(ActivitySettings.this, mMyResources.getString("lib_droi_account_get_user_info_fail"));
						finish();
					}
       		} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					finish();
				}
       	}else{
       		Utils.showMessage(ActivitySettings.this, mMyResources.getString("lib_droi_account_get_user_info_fail"));
       		finish();
       	}
		}
		
		@Override
       protected void onCancelled(){
			finish();
		}
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
						JSONObject updateBindPhone = new JSONObject();
						updateBindPhone.put(ShareInfoManager.SHARED_BINDPHONE, mUid);
						SharedInfo sharedInfo = SharedInfo.getInstance();
						sharedInfo.updateLocalUserInfo(getApplicationContext(), updateBindPhone);
						DroiSDKHelper droiAccount = DroiSDKHelper.getInstance(getApplicationContext());
						droiAccount.onAccountBinded("phone", mUid);
						String passwdVal = jsonObject.has("passwdval") ? jsonObject.getString("passwdval") : "-1";  
						String accountName = userAccount.getText().toString();
						int accountType = Utils.getAccountType(accountName);
						if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER && "0".equals(passwdVal)){
							sharedInfo.deleteAccount(ActivitySettings.this, mUid, true);
						}else{
							getUserInfo();
						}
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

	private class UploadUserInfo extends AsyncTask<Void, Void, String> {
		private final String mUUI_openId;
		private final String mUUI_token;
		private final String mUUI_selfEditName;
		private final String mUUI_gender;
		private final FormFile mUUI_avatar;
		
		public UploadUserInfo(String openId, String token, String selfEditName, String gender, FormFile avatar){
			mUUI_openId = openId;
			mUUI_token = token;
			mUUI_selfEditName = selfEditName;
			mUUI_gender = gender;
			mUUI_avatar = avatar;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			final Map<String, String> userParams = new HashMap<String, String>();
			userParams.put(Constants.JSON_S_OPENID, mUUI_openId);
			userParams.put(Constants.JSON_S_TOKEN, mUUI_token);
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "UpdateUserInfo openId : " + mUUI_openId);
			}
			String signString = MD5Util.md5(mUUI_openId + mUUI_token + Constants.SIGNKEY);
			userParams.put("sign", signString);
			JSONObject jsonData = new JSONObject();
			try {
				if(!TextUtils.isEmpty(mUUI_selfEditName)){
					jsonData.put("selfeditname", mUUI_selfEditName);
				}
				
				if(!TextUtils.isEmpty(mUUI_gender)){
					jsonData.put("gender", mUUI_gender);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "data : " + jsonData.toString() + ", jason Length : " + jsonData.length());
			}
			if(jsonData.length() > 0){
				userParams.put("data", jsonData.toString());
			}
			String result = null;
			try{
				//result = HttpOperation.postRequest(Constants.ACCOUNT_EDIT_USER_INFO, userParams);
				result = SocketHttpRequester.postExternalFile(Constants.ACCOUNT_EDIT_USER_INFO, userParams, mUUI_avatar);
	        }catch (Exception e){
	            e.printStackTrace();
	        }
			return result;
		}
		
		@Override
       protected void onPostExecute(String result){
       	super.onPostExecute(result);
       	hideProgress();
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "update result : " + result);
			}
       	if (!TextUtils.isEmpty(result)){
       		try {
					JSONObject jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == 0){
						/*
						String avatarUrl = jsonObject.has("avatarurl") ? jsonObject.getString("avatarurl") : null;
						if(!TextUtils.isEmpty(avatarUrl)){
							//update database
							SharedInfo sharedInfo = SharedInfo.getInstance();
							User user = sharedInfo.getData();
							user.setLogoUrl(avatarUrl);
							sharedInfo.updateAvatarUrl(getApplicationContext(), AvatarUtils.getAvatarPath());
						}*/
						
						DroiSDKHelper droiAccount = DroiSDKHelper.getInstance(ActivitySettings.this);
						//build update user info
						JSONObject updatedJsonData = new JSONObject();
						if(!TextUtils.isEmpty(mUUI_selfEditName)){
							updatedJsonData.put(ShareInfoManager.SHARED_NICKNAME, mUUI_selfEditName);
						}
						
						if(!TextUtils.isEmpty(mUUI_gender)){
							updatedJsonData.put(ShareInfoManager.SHARED_GENDER, mUUI_gender);
						}
						
						if(mUUI_avatar != null){
							updatedJsonData.put(ShareInfoManager.SHARED_AVATAR, AvatarUtils.getAvatarPath());
						}
						//build end
						
						SharedInfo sharedInfo = SharedInfo.getInstance();
						sharedInfo.updateLocalUserInfo(getApplicationContext(), updatedJsonData);
						if(droiAccount != null){
							droiAccount.onAccountUpdated(updatedJsonData.toString());
						}
						
						if(!TextUtils.isEmpty(mUUI_selfEditName)){
							userName.setText(mUUI_selfEditName);
						}
						Utils.showMessage(ActivitySettings.this, mMyResources.getString("lib_droi_account_update_userinfo_success"));
					}else{
						Utils.showMessage(ActivitySettings.this, mMyResources.getString("lib_droi_account_update_userinfo_fail"));
					}
       		} catch (JSONException e) {
					e.printStackTrace();
				}
       	}else{
       		Utils.showMessage(ActivitySettings.this, mMyResources.getString("lib_droi_account_update_userinfo_fail"));
       	}
		}
		
		@Override
       protected void onCancelled(){
			
		}
		
	}
	
	class OtherUserLoginTask extends AsyncTask<Void, Void, String> {
		 private String mData;
		 private String mUtype;
		 private String mUid;
		 private String mAccess_token;
		 
		 public OtherUserLoginTask(String uid, String access_token, String data, String type){
			 mUid = uid;
			 mAccess_token = access_token;
			 mData = data;
			 mUtype = type;
		 }
		 
		@Override
		protected String doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			final Map<String, String> loginParams = new HashMap<String, String>();
			String signString = MD5Util.md5(mUid + mAccess_token + mUtype + mData + " " + Constants.SIGNKEY);
	        loginParams.put("uid",mUid);
	        loginParams.put("passwd", mAccess_token);
	        loginParams.put("utype", mUtype);
	        loginParams.put("data", mData);
	        loginParams.put("sign", signString);
	        loginParams.put("devinfo", " ");
	        String loginResult = null;
	        try{
	        	loginResult = HttpOperation.postRequest(Constants.AUTH, loginParams);
	            if(DebugUtils.DEBUG){
	            	DebugUtils.i(TAG, "Other login data = " + mData);
	            	DebugUtils.i(TAG, "return = " + loginResult);
	            }
	            JSONObject jsonObject = new JSONObject(loginResult);
	            String avatarStr = Constants.AVATAR_URL_QQ_WB_DEFAULT;
	            String avatarUrl = jsonObject.has(avatarStr) ? jsonObject.getString(avatarStr) : null;
	            //supprt wb-qq selfdef avatar
	            if(TextUtils.isEmpty(avatarUrl)){
	            	avatarUrl = jsonObject.has(Constants.AVATAR_USER_SPECIFY) ? 
	            			jsonObject.getString(Constants.AVATAR_USER_SPECIFY) : null;
	            }
	            if(TextUtils.isEmpty(avatarUrl)){
	            	AvatarUtils.deleteUserAvatar();
	            }else{
	            	AvatarUtils.downloadUserAvatar(avatarUrl);
	            }
	        }catch (Exception e){
	            e.printStackTrace();
	        }
			return loginResult;
		}
		
       @Override
       protected void onPostExecute(String result){
       	super.onPostExecute(result);
       	hideProgress();
       	if (!TextUtils.isEmpty(result)){
       		try {
					JSONObject jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(DebugUtils.DEBUG){
						DebugUtils.i(TAG, "OtherUserLoginTask result = " + result);
					}
					if(rs == 0){
						TEMP_TOKEN = jsonObject.getString("token");
						mLogGetOpenId = jsonObject.getString("openid");
	
						//第三方用户登陆比较特殊，如果用户绑定了账户，且绑定的账户有密码，那么ACCOUNT_PWD_VAL字段值为0
						int pwsswdSeted = jsonObject.has(Constants.ACCOUNT_PWD_VAL) ? 
								jsonObject.getInt(Constants.ACCOUNT_PWD_VAL) : -1;
						
						mPasswdSeted = (pwsswdSeted == 0);
						//如果用户有绑定的手机或绑定的邮箱，提示用户使用绑定账号登陆
						mLoginCheckBindPhone = Utils.getStringFromJSON(jsonObject, "username");
						mLoginCheckBindEmail = Utils.getStringFromJSON(jsonObject, "mail");
						if(TextUtils.isEmpty(mLoginCheckBindPhone) || TextUtils.isEmpty(mLoginCheckBindEmail)){
							updatePreferences(jsonObject);
							
						}else{
							showDialog(DIALOG_MSG_BINDED_FOR_QQ_WB);
						}
						
						//showProgress();
						//new GetUserInfo(mLogGetOpenId, TEMP_TOKEN).execute();

					}else{
                   	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                   		getResources().getString(mMyResources.getString("lib_droi_account_login_fail"));
                   	//Utils.showMessage(getApplicationContext(), desc);
                   	mErrorMsg = desc;
                   	showDialog(DIALOG_ENTRY_APP_ERROR);
                   	return;
                   	/*
                   	if(mRunnable == null){
                   		finish();
                   	}*/
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
       	}else{
       		Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_login_fail"));
       	}
       }
       
       @Override
       protected void onCancelled(){
       	setResult(RESULT_CANCELED);
       	finish();
       }
	}
	
	private void updatePreferences(JSONObject jsonObject){
		//String openid = jsonObject.getString("openid");
		String gender = Utils.getStringFromJSON(jsonObject, "gender");
		String phone = Utils.getStringFromJSON(jsonObject, "username");
		String mail = Utils.getStringFromJSON(jsonObject, "mail");
		String nickName = Utils.getStringFromJSON(jsonObject, "nickname");
		SharedInfo sharedInfo = SharedInfo.getInstance();
		//String regType = sharedInfo.getRegType(this);
		int hasPwd = Utils.getIntFromJSON(jsonObject, Constants.ACCOUNT_PWD_VAL);
		//String passwdVal = sharedInfo.getPasswdVal(this);
		if(!TextUtils.isEmpty(gender)){
			userGender.setText(gender);
		}
		if(!TextUtils.isEmpty(nickName)){
//			mAccountNickName.setSummary(nickName);
//			mAccountNickName.setText(nickName);
			userName.setText(nickName);			
		}else{
//			mAccountNickName.setSummary(mMyResources.getString("lib_droi_account_nickname_not_set"));?
			userName.setText(mMyResources.getString("lib_droi_account_nickname_not_set"));			
		}
		// 更新电话号码
		if(!TextUtils.isEmpty(phone)){
//			mAccountBindPhone.setSummary(phone);
//			mAccountBindPhone.showRebindButton();
			//mAccountBindPhone.setEnabled(false);
			int tag = 1;
			userBindPhone.setTag(tag);
			userBindPhone.setText(phone);
		}else{
			userBindPhone.setText(mMyResources.getString("lib_droi_account_not_binded_phone"));
//			mAccountBindPhone.setSummary(mMyResources.getString("lib_droi_account_not_binded_phone"));
		}
		
		// 更新地址
		if(retrieveDeliveryInfo(jsonObject)){
			userAddress.setText(mMyResources.getString("lib_droi_account_address_saved"));
//			mMyAddressPreference.setSummary(mMyResources.getString("lib_droi_account_address_saved"));
		}else{
			userAddress.setText(mMyResources.getString("lib_droi_account_address_empty"));
//			mMyAddressPreference.setSummary(mMyResources.getString("lib_droi_account_address_empty"));
		}
		
		//待激活状态
		String mail_active_type = Utils.getStringFromJSON(jsonObject, "mail_active_type");
		int bingMailTag;
		if("bindmail".equals(mail_active_type)){
			//表示用户待激活
			//待激活邮箱的地址
			findViewById(mMyResources.getId("lib_droi_account_user_notiviemail")).setVisibility(View.VISIBLE);
			String active_mail = Utils.getStringFromJSON(jsonObject, "active_mail");
//			mAccountBindEmail.setSummary(active_mail);
//			mAccountBindEmail.showNotActive();
			userBindEmail.setText(active_mail);
			bingMailTag = 0;
			userBindEmail.setTag(bingMailTag);
		}else if(!TextUtils.isEmpty(mail)){
			userBindEmail.setText(mail);
			bingMailTag = 1;
			userBindEmail.setTag(bingMailTag);
//			mAccountBindEmail.setSummary(mail);
//			mAccountBindEmail.showRebindButton();
			//mAccountBindEmail.setEnabled(false);
		}else{
//			mAccountBindEmail.hideAll();
//			mAccountBindEmail.setSummary(mMyResources.getString("lib_droi_account_not_binded_email"));
			bingMailTag = 2;
			userBindEmail.setTag(bingMailTag);
			userBindEmail.setText(mMyResources.getString("lib_droi_account_not_binded_email"));
		}/*else if(hasPwd != -1){
			mAccountBindEmail.setSummary(mUserName);
			mAccountBindEmail.setEnabled(false);
		}*/
		
		// 更新用户名
		if(/*"openqq".equals(regType) || "openweibo".equals(regType)*/ hasPwd != 0){
			userHeaderLayout.setClickable(false);
			userGenderLayout.setClickable(false);
			userNameLayout.setClickable(false);
			signalGender.setTextColor(Color.GRAY);
			signalNickName.setTextColor(Color.GRAY);
		}else{
			userHeaderLayout.setClickable(true);
			userGenderLayout.setClickable(true);
			userNameLayout.setClickable(true);
			signalGender.setTextColor(Color.BLACK);
			signalNickName.setTextColor(Color.BLACK);
		}
		
		updateAvatar();
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "hasPwd : " + hasPwd + ", name = " + sharedInfo.getData().getName());
		}
		if(hasPwd == 0){
			/*
			String name = sharedInfo.getData().getName();
			if(!TextUtils.isEmpty(name)){
				mAccountName.setSummary(name);
			}else{
				mAccountName.setSummary(nickName);
			}*/
			String accountName = Utils.getStringFromJSON(jsonObject, "fs_name");
			if(TextUtils.isEmpty(accountName)){
				accountName = Utils.getStringFromJSON(jsonObject, "username");
			}

			if(TextUtils.isEmpty(accountName)){
				accountName = nickName;
			}
			userAccount.setText(accountName);
//			mAccountName.setSummary(accountName);
		}else{
			userAccount.setText(nickName);
//			mAccountName.setSummary(nickName);
		}
		//mAccountName.setEnabled(false);
		mUserName = userAccount.getText().toString();
	}
	
//	private void updateGenderPreference(int value, int defaultIndex){
//		ListPreference pref = (ListPreference)mAccountGender;
//		CharSequence[] values = pref.getEntryValues();
//		for(int i = 0; i < values.length; i++){
//			int val = Integer.parseInt(values[i].toString());
//			if(value == val){
//				pref.setValueIndex(i);
//				pref.setSummary(pref.getEntries()[i]);
//				return;
//			}
//		}
//		
//		pref.setValueIndex(defaultIndex);
//		pref.setSummary(pref.getEntries()[defaultIndex]);
//	}
	
	private boolean retrieveDeliveryInfo(JSONObject jsonObject){
		String delivery_info = jsonObject.has("delivery_info") ? Utils.getStringFromJSON(jsonObject, "delivery_info") : null;
		if(!TextUtils.isEmpty(delivery_info) && !"{}".equals(delivery_info)){
			JSONArray jsonArray;
			try {
				jsonArray = new JSONArray(delivery_info);
				if(jsonArray.length() >= 1){
					return true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private void updateAvatar(){
		String avatarPath = AvatarUtils.getAvatarPath();
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "updateAvatar path : " + avatarPath);
		}
		if(!TextUtils.isEmpty(avatarPath)){
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(avatarPath);
				Bitmap bitmap = BitmapFactory.decodeStream(fis);
				bitmap = Utils.getRoundedCornerBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),bitmap.getWidth()/2, bitmap.getHeight()/2);
				Drawable drawable = new BitmapDrawable(this.getResources(),bitmap);
				userImage.setImageDrawable(drawable);
				fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}catch (IOException e){
				
			}
		}
	}
	
	private void cropPhoto(Uri uri , Uri outputUri, int source){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "cropPhoto uri :" + uri + ", outputUri : " + outputUri);
		}
		if(source == CROP_PHOTO_FROM_CAMERA){
			if(uri != null){
				File file = new File(uri.getPath());
				if(file == null || file.exists() == false){
					return;
				}
			}
		}
		/*
		if(outputUri != null){
			File file = new File(outputUri.getPath());
			if(file == null || file.exists() == false){
				return;
			}
		}*/
		//以后可自定义图片裁剪功能，安卓系统有自带图片裁剪功能
		Intent intent = new Intent("com.android.camera.action.CROP");
		//which image to crop
		intent.setDataAndType(uri, "image/*");
		//where to save the cropped photo
		PhotoUtils.addPhotoPickerExtras(intent, outputUri);
		//crop data
		PhotoUtils.addCropExtras(intent, mPhotoPickSize);
        //
        
        startActivityForResult(intent, REQUEST_CROP_PHOTO);
	}
	
	private void saveImage(){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG,  "saveImage : " + mCroppedPhotoUri);
		}
		if(mCroppedPhotoUri == null){
			Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_update_avatar_failed"));
			return;
		}
		try {
			Bitmap photo = PhotoUtils.getBitmapFromUri(this, mCroppedPhotoUri);
			AvatarUtils.saveBitmap(photo);
			photo = Utils.getRoundedCornerBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(),photo.getWidth()/2, photo.getHeight()/2);
			setPhoto(photo);
			File file = new File(mCroppedPhotoUri.getPath());
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "save file : " + mCroppedPhotoUri.getPath());
				DebugUtils.i(TAG, "fileName : " + file.getName());
			}
			FormFile formFile = new FormFile(file.getName(), file, "avatar", null);
			//showProgress();
			new UploadUserInfo(mLogGetOpenId, TEMP_TOKEN, null, null, formFile).execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "update avatar failed , mCroppedPhotoUri : " + mCroppedPhotoUri);
			}
			Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_update_avatar_failed"));
		}
	}
	
	private void setPhoto(Bitmap photo){
		if (photo == null || photo.getHeight() < 0 || photo.getWidth() < 0) {
			Log.w(TAG, "Invalid bitmap passed to setPhoto()");
		}
		Drawable drawable = new BitmapDrawable(this.getResources(),photo);
		userImage.setImageDrawable(drawable);
	}
	
}