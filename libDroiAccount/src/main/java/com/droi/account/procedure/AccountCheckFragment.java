package com.droi.account.procedure;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.droi.account.DebugUtils;

import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.login.SharedInfo;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;
import com.droi.account.shared.DroiSDKHelper;
import com.droi.account.view.AccountAutoCompleteTextView;

public class AccountCheckFragment extends BackHandledFragment implements TextWatcher{
	private static final String TAG = "AccountCheckFragment";
	private static final int CHECK_PRIVACY_POLICY = 1;
	
	private Button mOkBtn;
	private AccountAutoCompleteTextView mAcccountView;
	private TextView mPrivacyPolicy;
	private String mHint = "";
	private int mType = 0;
	
	private CheckBox mCheckBox;
	private CheckAccountExist mCheckTask;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(mMyResources.getLayout("lib_droi_account_check_fragment_layout"), null);
		findViewByIds(view);
		setupViews();
		return view;
	}
	
	protected void findViewByIds(View view){
		mOkBtn = (Button) view.findViewById(mMyResources.getId("lib_droi_account_ok"));
		mPrivacyPolicy = (TextView)view.findViewById(mMyResources.getId("lib_droi_account_privacy_policy"));
		mCheckBox = (CheckBox)view.findViewById(mMyResources.getId("lib_droi_account_user_contract"));
		mAcccountView = (AccountAutoCompleteTextView)view.findViewById(mMyResources.getId("lib_droi_account_account_name"));
	}
	
	@Override
	public void onStart() {
		super.onStart();
		final Activity activity = getActivity();
		//default
		mHint = getResources().getString(mMyResources.getString("lib_droi_account_hint_input_phonenumer"));
		mType = Constants.ACCOUNT_TYPE_PHONENUMBER;
		int titleRes =  mMyResources.getString("lib_droi_account_user_bind_phonenumber");
		
		Bundle data = getArguments();
		if(data != null){
			int bindAccountType = data.getInt(DroiSDKHelper.BIND_ACCOUNT_TYPE);
			if(bindAccountType == DroiSDKHelper.BIND_ACCOUNT_PHONE){
				mType = Constants.ACCOUNT_TYPE_PHONENUMBER;
				mHint = getResources().getString(mMyResources.getString("lib_droi_account_hint_input_phonenumer"));
				titleRes =  mMyResources.getString("lib_droi_account_user_bind_phonenumber");
			}else if(bindAccountType == DroiSDKHelper.BIND_ACCOUNT_EMAIL){
				mType = Constants.ACCOUNT_TYPE_EMAIL;
				mHint = getResources().getString(mMyResources.getString("lib_droi_account_hint_input_email"));
				titleRes =  mMyResources.getString("lib_droi_account_user_bind_email");
			}
		}
		
		activity.setTitle(titleRes);

	}
	
	protected void setupViews(){
		mOkBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//start different activity depends on the input text
				String accountName = mAcccountView.getText().toString().trim();
				int accountType = Utils.getAccountType(accountName);
				 if(Utils.getAvailableNetWorkType(getActivity()) == -1){
					  showDialog(DIALOG_APP_ERROR, null);
					  return;
				 }
				 
				if(!mCheckBox.isChecked()){
					Utils.showMessage(getActivity(), mMyResources.getString("lib_droi_account_need_to_agree_plicy_contrat"));
					return;
				}
				if(accountType == Constants.ACCOUNT_TYPE_PHONENUMBER){
					showDialog(DIALOG_ON_PROGRESS, mCheckTask);
					new CheckAccountExist(accountName, "mobile").execute();
				}else if(accountType == Constants.ACCOUNT_TYPE_EMAIL){
					showDialog(DIALOG_ON_PROGRESS, mCheckTask);
					new CheckAccountExist(accountName, "mail").execute();
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
					ComponentName coponentName = new ComponentName(getActivity().getApplicationContext().getPackageName(),"com.droi.account.login.PrivacyPolicy");
					//intent.setClass(BindAccountActivity.this, PrivacyPolicy.class);
					intent.setComponent(coponentName);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivityForResult(intent, CHECK_PRIVACY_POLICY);
				}
			});
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
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
						SharedInfo sharedInfo = SharedInfo.getInstance();
						String passwdVal = sharedInfo.getPasswdVal(getActivity());
						int accountType = Utils.getAccountType(mAccountName);
						Intent intent = new Intent();
						intent.putExtra("accountname", mAccountName);
						intent.putExtra("accountType", accountType);
						if(DebugUtils.DEBUG){
							DebugUtils.i(TAG, "CheckAccountExist set Result");
						}
						intent.putExtra("accountname", mAccountName);
						intent.putExtra("accountType", accountType);
						setResult(Activity.RESULT_OK, intent);
					}else{
                    	String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
                    		getResources().getString(mMyResources.getString("lib_droi_account_account_already_exits"));
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
        		Utils.showMessage(getActivity(), mMyResources.getString("lib_droi_account_account_already_exits"));
        	}
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
