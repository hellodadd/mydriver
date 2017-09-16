package com.droi.account.setup;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.droi.account.DebugUtils;

import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.login.BaseActivity;
import com.droi.account.login.SharedInfo;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;
import com.droi.account.widget.wheel.WheelView;
import com.droi.account.widget.wheel.WheelViewManager;


public class EditAddressActivity extends BaseActivity{

	private static final String TAG = "EditAddressActivity";
	private static final int EDIT_NAME_LENGTH_MAX = 25;
	private static final int EDIT_ADDRESS_DETAILED_LENGTH_MAX = 100;
	public static final int ADD_ADDRESS = 1;
	public static final int UPDATE_ADDRESS = 2;
	//Dialog id
	private static final int DIALOG_ON_PROGRESS = 100;
	private static final int DIALOG_ID_PICK_CITY = DIALOG_ON_PROGRESS + 1;
	private static final int DIALOG_ID_DISCARD =  DIALOG_ID_PICK_CITY + 1;
	private String mName;
	private String mPhone;
	private String mProvince;
	private String mAddressDetailed;
	
	private EditText mEditName;
	private EditText mEditPhone;
	private EditText mEditProvince;
	private EditText mEditDetailed;
	
	private Button mButtonOk;
	
	private ProgressDialog mProgressDialog = null;
	private DeliveryInfoTask mTask;
	
	private int mWhat;
	private String mOpenId;
	private String mInfoId;
	private WheelViewManager mWheelManager;
	private boolean mTextChanged = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		super.onCreate(savedInstanceState);
		setContentView(mMyResources.getLayout("lib_droi_account_layout_edit_address"));	
		findViewsById();
		init();
		setupView();
	}
	
	private void findViewsById(){
		mEditName = (EditText)findViewById(mMyResources.getId("lib_droi_account_name"));
		mEditPhone = (EditText)findViewById(mMyResources.getId("lib_droi_account_phone_number"));
		mEditProvince = (EditText)findViewById(mMyResources.getId("lib_droi_account_province"));
		mEditDetailed = (EditText)findViewById(mMyResources.getId("lib_droi_account_edittext_detailed_address"));
		mButtonOk = (Button)findViewById(mMyResources.getId("lib_droi_account_ok"));
	}
	
	private void setupView(){
		mEditName.setFilters(getNameFilters());
		mEditName.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				updateButtonState();
				if(s.length() >= EDIT_NAME_LENGTH_MAX){
					//mEditName.setError(getResources().getText(mMyResources.getString("lib_droi_account_max_length_reached).toString());
					Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_max_length_reached"));
					return;
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
			}
			
		});

		mEditPhone.setFilters(getPhoneInputFilters());
		mEditPhone.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				updateButtonState();
				if(s != null ){
					if(s.length() > 11){
						
					}else if(s.length() >= 1){
						if(!Utils.isValidMobilePrefix(s.toString())){
							//mEditPhone.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_phonenumer).toString());
							//Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_msg_error_phonenumer"));
						}
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
			
		});

		mEditProvince.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				updateButtonState();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		mEditProvince.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
				imm.hideSoftInputFromWindow(mEditProvince.getWindowToken(),0);
				showDialog(DIALOG_ID_PICK_CITY);
				return false;
			}
			
		});
		
		mEditDetailed.setFilters(getAddressFilters());
		mEditDetailed.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				updateButtonState();
				if(s.length() >= EDIT_ADDRESS_DETAILED_LENGTH_MAX){
					Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_max_length_reached"));
					return;
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}
			
		});

		mButtonOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String name = mEditName.getText().toString();
				String phone = mEditPhone.getText().toString();
				String province = mEditProvince.getText().toString();//(String)mEditProvince.getTag();//
				String address = mEditDetailed.getText().toString();
				if(TextUtils.isEmpty(name)){
					Utils.showMessage(EditAddressActivity.this, mMyResources.getString("lib_droi_account_edit_address_toast_empty_name"));
					return;
				}
				
				if(name.getBytes().length >= 25){
					mEditName.requestFocus();
					mEditName.setError(getResources().getText(mMyResources.getString("lib_droi_account_edit_name_length_hint")).toString());
					return;
				}
				
				if(TextUtils.isEmpty(phone)){
					Utils.showMessage(EditAddressActivity.this, mMyResources.getString("lib_droi_account_edit_address_toast_phone_emtpy"));
					return;
				}
				
				if(!Utils.isValidPhone(phone)){
					mEditPhone.requestFocus();
					mEditPhone.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_phonenumer")).toString());
					return;
				}
				
				if(TextUtils.isEmpty(province)){
					Utils.showMessage(EditAddressActivity.this, mMyResources.getString("lib_droi_account_edit_address_toast_area_emtpy"));
					return;
				}
				if(TextUtils.isEmpty(address)){
					Utils.showMessage(EditAddressActivity.this, mMyResources.getString("lib_droi_account_edit_address_toast_address_detailed_empty"));
					return;
				}
				


				SharedInfo sharedInfo = SharedInfo.getInstance();
				String token = sharedInfo.getAccessToken(getApplicationContext());
				String openId = sharedInfo.getOpenId(getApplicationContext());
				JSONObject data = new JSONObject();
				JSONObject fullAddress = buildAddressObject(province, address);
				//cname infoid  cmobile caddress
				try {
					data.put("cname", name);
					data.put("cmobile", phone);
					data.put("caddress", fullAddress.toString());
					if(mWhat == ADD_ADDRESS){
						showProgress();
						mTask = new DeliveryInfoTask(openId, token, "add", null, data);
						mTask.execute();
					}else if(mWhat == UPDATE_ADDRESS){
						showProgress();
						mTask = new DeliveryInfoTask(openId, token, "update", mInfoId, data);
						mTask.execute();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	private void init(){
		mTextChanged = false;
		Intent intent = getIntent();
		if(intent != null){
			mOpenId = intent.getStringExtra("openid");
			mInfoId = intent.getStringExtra("infoid");
			String initName = intent.getStringExtra("cname");
			if(!TextUtils.isEmpty(initName)){
				mEditName.setText(initName);
				Editable etable = mEditName.getText();
				Selection.setSelection(etable, etable.length());
			}
			String initPhone = intent.getStringExtra("cmobile");
			if(!TextUtils.isEmpty(initPhone)){
				mEditPhone.setText(initPhone);
			}
			
			String initProvince = intent.getStringExtra("province");
			if(!TextUtils.isEmpty(initProvince)){
				mEditProvince.setText(initProvince);
			}
			
			String initAddressDetailed = intent.getStringExtra("caddress");
			if(!TextUtils.isEmpty(initAddressDetailed)){
				String province = getProvinceFromFullAddress(initAddressDetailed);
				String address = getDetailedFromFullAddress(initAddressDetailed);
				mEditProvince.setText(province);
				mEditDetailed.setText(address);
			}else{
				
			}
			
			mWhat = intent.getIntExtra("what", -1);
			if(mWhat == ADD_ADDRESS){
				mButtonOk.setEnabled(false);
			}else{
				mButtonOk.setEnabled(true);
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
					if(mTask != null){
						mTask.cancel(true);
					}
				}
		     });
		     mProgressDialog = progressDialog;
		     dialog = progressDialog;
		 }else if(id == DIALOG_ID_PICK_CITY){
			 View view = getLayoutInflater().inflate(mMyResources.getLayout("lib_droi_account_layout_pick_citys"), null);
			 WheelView	mProvince = (WheelView) view.findViewById(mMyResources.getId("lib_droi_account_id_province"));
			 WheelView	mCity = (WheelView) view.findViewById(mMyResources.getId("lib_droi_account_id_city"));
			 WheelView	mArea = (WheelView) view.findViewById(mMyResources.getId("lib_droi_account_id_area"));
			 String provinceVagur = mEditProvince.getText().toString();
			 String[] provinceArea = new String[3];
			 if(!TextUtils.isEmpty(provinceVagur)){
				String[] array = provinceVagur.split("-");
				for(int i = 0; i < array.length; i++){
					provinceArea[i] = array[i];
				}
			 }
			 
			 mWheelManager = new WheelViewManager(this, mProvince, mCity, mArea, provinceArea[0], provinceArea[1], provinceArea[2]);
			 dialog = new AlertDialog.Builder(this)
			 	 .setView(view)
			 	 .setTitle(mMyResources.getString("lib_droi_account_address"))
			 	 .setNegativeButton(android.R.string.cancel, null)
			 	 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
	
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
							String province = mWheelManager.getSelectedInfo();
							if(!TextUtils.isEmpty(province)){
								//String directpro = province.replaceAll("-", "");
								mEditProvince.setText(province);
								//mEditProvince.setTag(province);
							}
						}
					}
			 	 )
			     .create();
		 }else if(id == DIALOG_ID_DISCARD){
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 builder.setTitle(mMyResources.getString("lib_droi_account_dialog_title_hint"));
			 builder.setMessage(mMyResources.getString("lib_droi_account_edit_address_discard_message"));
			 builder.setNegativeButton(mMyResources.getString("lib_droi_account_edit_address_discard_address"), new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						setResult(RESULT_CANCELED);
						finish();
					}
					 
				 });
			 builder.setPositiveButton(mMyResources.getString("lib_droi_account_edit_address_discard_continue"), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
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
	
	private JSONObject buildAddressObject(String province, String detailedAddr){
		JSONObject fullAddress = new JSONObject();
		try{
			fullAddress.put("province", province);
			fullAddress.put("address", detailedAddr);
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fullAddress;
	}
	
	private static String getProvinceFromFullAddress(String address){
		try {
			JSONObject fullAddress = new JSONObject(address);
			return fullAddress.has("province") ? fullAddress.getString("province") : null;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static String getDetailedFromFullAddress(String address){
		try {
			JSONObject fullAddress = new JSONObject(address);
			return fullAddress.has("address") ? fullAddress.getString("address") : null;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String getStringFromAddresJson(String address){
		String province = getProvinceFromFullAddress(address);
		String provinceTrim = province.replaceAll("-", "");
		String addr = getDetailedFromFullAddress(address);
		return provinceTrim + addr;
	}
	
	private class DeliveryInfoTask extends AsyncTask<Void, Void, String>{

		private final String mOpendId;
		private final String mToken;
		private final String mActionType;
		private final String mInfoId;
		private final JSONObject mData;
		
		public DeliveryInfoTask(String openId, String token, String actionType, String infoId, JSONObject data){
			mOpendId = openId;
			mToken = token;
			mActionType = actionType;
			mInfoId = infoId;
			mData = data;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			final Map<String, String> userParams = new HashMap<String, String>();
			String signString = MD5Util.md5(mOpendId + mToken + mActionType + Constants.SIGNKEY);
			userParams.put(Constants.JSON_S_OPENID, mOpendId);
			userParams.put(Constants.JSON_S_TOKEN, mToken);
			userParams.put("actiontype", mActionType);
			
			if("add".equals(mActionType)){
				
			}else if("update".equals(mActionType)){
				userParams.put("infoid", mInfoId);
			}
			
			userParams.put("data", mData.toString());
			userParams.put("sign", signString);
			
			String result = null;
			try{
				result = HttpOperation.postRequest(Constants.DELIVERY_INFO, userParams);
			}catch (Exception e){
	            e.printStackTrace();
	        }
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			//cname infoid  cmobile caddress
			super.onPostExecute(result);
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "result : " + result);
			}
			hideProgress();
			if (!TextUtils.isEmpty(result)){
        		try {
					JSONObject jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == 0){
						Intent intent = new Intent();
						
						if("add".equals(mActionType)){
							String delivery_info = jsonObject.has("delivery_info") ? jsonObject.getString("delivery_info") : null;
							intent.putExtra("delivery_info", delivery_info);
						}else if("update".equals(mActionType)){
							String addr = mData.has("caddress") ? mData.getString("caddress") : null;
							String phone = mData.has("cmobile") ? mData.getString("cmobile") : null;
							String name = mData.has("cname") ? mData.getString("cname") : null;
							intent.putExtra("infoid", mInfoId);
							intent.putExtra("caddress", addr);
							intent.putExtra("cmobile", phone);
							intent.putExtra("cname", name);
						}

						setResult(RESULT_OK, intent);
						finish();
					}else{
						String desc = null;
						if("add".equals(mActionType)){
							desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
								getResources().getString(mMyResources.getString("lib_droi_account_add_address_failed"));
						}else if("update".equals(mActionType)){
							desc = jsonObject.has("desc") ? jsonObject.getString("desc") : 
								getResources().getString(mMyResources.getString("lib_droi_account_update_address_failed"));
						}
                    	Utils.showMessage(getApplicationContext(), desc);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				String desc = null;
				if("add".equals(mActionType)){
					desc = getResources().getString(mMyResources.getString("lib_droi_account_add_address_failed"));
				}else if("update".equals(mActionType)){
					desc = getResources().getString(mMyResources.getString("lib_droi_account_update_address_failed"));
				}
            	Utils.showMessage(getApplicationContext(), desc);
			}
		}
		
		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			hideProgress();
		}
		
	}	

	public void onBack(View view){
		onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		String name = mEditName.getText().toString();
		String phone = mEditPhone.getText().toString();
		String province = mEditProvince.getText().toString();
		String address = mEditDetailed.getText().toString();
		if(!TextUtils.isEmpty(name) || !TextUtils.isEmpty(phone) || !TextUtils.isEmpty(province) || !TextUtils.isEmpty(address)){
			if(mTextChanged){
				showDialog(DIALOG_ID_DISCARD);
			}else{
				super.onBackPressed();
			}
		}else{
			super.onBackPressed();
		}
		
	}

	private void updateButtonState(){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "updateButtonState");
		}
		mTextChanged = true;
		String name = mEditName.getText().toString();
		String phone = mEditPhone.getText().toString();
		String province = mEditProvince.getText().toString();
		String address = mEditDetailed.getText().toString();
		if(TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(province) || TextUtils.isEmpty(address)){
			mButtonOk.setEnabled(false);
		}else{
			mButtonOk.setEnabled(true);
		}
	}
	
	class PhoneFilter implements InputFilter{
		private int mMax;
		
		public PhoneFilter(int max){
			mMax = max;
		}
		
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {
			// TODO Auto-generated method stub
            int keep = mMax - (dest.length() - (dend - dstart));
            String phone = mEditPhone.getText().toString();
            if(keep == 0){
				if(!Utils.isValidMobilePrefix(phone)){
					//mEditPhone.setError(getResources().getText(mMyResources.getString("lib_droi_account_msg_error_phonenumer).toString());
					Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_msg_error_phonenumer"));
				}else{
					Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_edit_address_phone_limited_11"));
					//mEditPhone.setError(getResources().getText(mMyResources.getString("lib_droi_account_edit_address_phone_limited_11).toString());
				}
            }
            if (keep <= 0) {
                return "";
            } else if (keep >= end - start) {
                return null; // keep original
            } else {
                keep += start;
                if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                    --keep;
                    if (keep == start) {
                        return "";
                    }
                }
    			if(DebugUtils.DEBUG){
    				DebugUtils.i(TAG, "start = " + start + ", keep = " + keep );
    				
    			}
                CharSequence sub = source.subSequence(start, keep);

                return sub;
            }
		}
		
	}
	
	class NameFilter implements InputFilter{
		private int mMax;
		
		public NameFilter(int max){
			mMax = max;
		}
		
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {
			// TODO Auto-generated method stub
            int keep = mMax - (dest.length() - (dend - dstart));
            String name = mEditName.getText().toString();
            if(keep == 0){
            	if(!TextUtils.isEmpty(name)){
            		//mEditName.setError(getResources().getText(mMyResources.getString("lib_droi_account_max_length_reached).toString());
            		Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_max_length_reached"));
            	}
            }
            if (keep <= 0) {
                return "";
            } else if (keep >= end - start) {
                return null; // keep original
            } else {
                keep += start;
                if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                    --keep;
                    if (keep == start) {
                        return "";
                    }
                }
                CharSequence sub = source.subSequence(start, keep);

                return sub;
            }
        }
	}
	
	class AddressFilter implements InputFilter{
		private int mMax;
		
		public AddressFilter(int max){
			mMax = max;
		}
		
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {
			// TODO Auto-generated method stub
            int keep = mMax - (dest.length() - (dend - dstart));
            String name = mEditDetailed.getText().toString();
            if(keep == 0){
            	if(!TextUtils.isEmpty(name)){
            		//mEditName.setError(getResources().getText(mMyResources.getString("lib_droi_account_max_length_reached).toString());
            		Utils.showMessage(getApplicationContext(), mMyResources.getString("lib_droi_account_max_length_reached"));
            	}
            }
            if (keep <= 0) {
                return "";
            } else if (keep >= end - start) {
                return null; // keep original
            } else {
                keep += start;
                if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                    --keep;
                    if (keep == start) {
                        return "";
                    }
                }
                CharSequence sub = source.subSequence(start, keep);

                return sub;
            }
		}
		
	}
	
	private InputFilter[] getPhoneInputFilters(){
		return (new InputFilter[]{new PhoneFilter(11)});
	}
	
	private InputFilter[] getNameFilters(){
		return (new InputFilter[]{new NameFilter(EDIT_NAME_LENGTH_MAX)});
		
	}
	
	private InputFilter[] getAddressFilters(){
		return (new InputFilter[]{new AddressFilter(EDIT_ADDRESS_DETAILED_LENGTH_MAX)});
	}
		
}
