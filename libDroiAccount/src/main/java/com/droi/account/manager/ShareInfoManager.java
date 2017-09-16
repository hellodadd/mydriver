package com.droi.account.manager;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.TextUtils;

import com.droi.account.DebugUtils;
import com.droi.account.DroiAccountProvider;
import com.droi.account.login.AvatarUtils;
import com.droi.account.login.SharedInfo;
import com.droi.account.login.User;

public class ShareInfoManager{

	private static final String TAG = "ShareInfoManager";
	public static final String SHARED_UID = "uid";
	public static final String SHARED_OPENID = "openid";
	public static final String SHARED_TOKEN = "token";
	public static final String SHARED_DATA = "data";
	private static final String SHARED_REGTYPE = "regtype";
	public static final String SHARED_PWDVAL = "passwdval";
	public static final String SHARED_EXPIRE = "expire";
	public static final String SHARED_BINDPHONE = "bindphone";
	public static final String SHARED_BINDMAIL = "bindemail";
	public static final String SHARED_USERNAME = "username";
	public static final String SHARED_AVATAR = "avatar";
	//added user fino
	public static final String SHARED_GENDER = "gender";
	public static final String SHARED_NICKNAME = "nickname";

	
	private static final String NOTIFY_TOKEN_UPDATED = "freeme.account.notify.token_updated";
	private static final String REQUEST_UPDATE_TOKEN = "freeme.account.request.update_token";
	//
    public static final long RELOGIN_TIMEOUT = 10*1000;//;30*60*1000;//
    
    private static final String KEY_PUBLIC_ACCOUNT  = "droi-account-userinfo";
    /**
     * The maximum number of incorrect attempts before the user is prevented
     * from trying again
     */
    public static final int FAILED_ATTEMPTS_BEFORE_TIMEOUT = 5;
    
	private AccountManager mAccountManager;
	private CountDownTimer mCountdownTimer = null;//1426399182
	private static ShareInfoManager mInstance;
	private Context mContext;
	private SharedInfo mSharedInfo;
	
	private ShareInfoManager(Context context){
		mSharedInfo = SharedInfo.getInstance();
		mAccountManager = AccountManager.get(context);
		mContext = context;
		registerReceiver();
	}
	
	private void registerReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(REQUEST_UPDATE_TOKEN);
	}
	
	public static ShareInfoManager getInstance(Context context){
		if(mInstance == null){
			mInstance = new ShareInfoManager(context.getApplicationContext());
		}
		return mInstance;
	}
	
	public void cancel(){
		if(mCountdownTimer != null){
			mCountdownTimer.cancel();
		}
	}
	
	public void publicToApps(User user, Account account){
		saveDataToAccount(account, user);
	}
	
	public void publicToApps(String data, Account account){
		if(mAccountManager == null){
			mAccountManager = AccountManager.get(mContext);
		}
		mAccountManager.setUserData(account, KEY_PUBLIC_ACCOUNT, data);
		updateDatabase(data);
	}
	
	private void handleRelogin(){
		if(mCountdownTimer != null){
			mCountdownTimer.cancel();
		}
		final long deadline = SystemClock.elapsedRealtime() + RELOGIN_TIMEOUT;
		final long realtime = SystemClock.elapsedRealtime();
		
		mCountdownTimer = new CountDownTimer(deadline - realtime, 1000){

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				//handleLogin();
			}

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				if(DebugUtils.DEBUG){
					final int secondsRemaining = (int) (millisUntilFinished / 1000);
					DebugUtils.i(TAG, "Tick: " + secondsRemaining);
				}
			}
			
		}.start();
		
	}
	/*
	private void handleLogin(){
		SharedInfo sharedInfo = SharedInfo.getInstance();
		String regType = sharedInfo.getRegType(mContext);
		if(Constants.UTYPE_QQ.equals(regType)){
			 String uid = sharedInfo.getUid(mContext);
			 String access_token = sharedInfo.getAccessToken(mContext);
			 String data = sharedInfo.getData(mContext);
			 new OtherUserLoginTask(uid, access_token, data, Constants.UTYPE_QQ).execute();
			 return;
		}else if(Constants.UTYPE_WEIBO.equals(regType)){
			 String uid = sharedInfo.getUid(mContext);
			 String access_token = sharedInfo.getAccessToken(mContext);
			 String data = sharedInfo.getData(mContext);
			 new OtherUserLoginTask(uid, access_token, data, Constants.UTYPE_WEIBO).execute();
			 return;
		}else{
			String pwdMD5 = sharedInfo.getPassword(mContext); 
			String uid = sharedInfo.getUid(mContext);
			if(!TextUtils.isEmpty(pwdMD5)&& !TextUtils.isEmpty(uid)){
				new LoginTask(uid, pwdMD5).execute();
			}
		}
	}*/
	
	private void saveDataToAccount(Account account, User user){
		if(mAccountManager == null){
			mAccountManager = AccountManager.get(mContext);
		}
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "saveDataToAccount regType : " + user.getRegtype());
		}
		String data = buildUserData(user);
		mAccountManager.setUserData(account, KEY_PUBLIC_ACCOUNT, data);
		
		updateDatabase(data);
	}
	
	private synchronized void updateDatabase(String data){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "updateDatabase data = " + data);
		}
		boolean isDbExit = DroiAccountProvider.isDBExit();
		if(!isDbExit){
			DroiAccountProvider.createDatabase(mContext);
	        ContentValues values = new ContentValues();  
	        values.put(DroiAccountProvider.TB_ITEM_SAHRED_DATA, data); 
	        mContext.getContentResolver().insert(DroiAccountProvider.CONTENT_URI, values); 
	        return;
		} 
		
		String columns[] = new String[] {DroiAccountProvider._ID, DroiAccountProvider.TB_ITEM_SAHRED_DATA};
		Uri uri = DroiAccountProvider.CONTENT_URI;  
		Cursor cur = mContext.getContentResolver().query(uri, columns, null, null, null);
		
		if(cur == null){
	        ContentValues values = new ContentValues();  
	        values.put(DroiAccountProvider.TB_ITEM_SAHRED_DATA, data); 
	        mContext.getContentResolver().insert(DroiAccountProvider.CONTENT_URI, values); 
		}else{
			if(cur.getCount() == 1){
		        ContentValues values = new ContentValues();  
		        values.put(DroiAccountProvider.TB_ITEM_SAHRED_DATA, data); 
		        mContext.getContentResolver().update(DroiAccountProvider.CONTENT_URI, values, null, null); 
			}else{
		        ContentValues values = new ContentValues();  
		        values.put(DroiAccountProvider.TB_ITEM_SAHRED_DATA, data); 
		        mContext.getContentResolver().delete(DroiAccountProvider.CONTENT_URI, null, null); 
		        mContext.getContentResolver().insert(DroiAccountProvider.CONTENT_URI, values); 
			}
		}
		
		if(cur != null){
			cur.close();
		}
		//testAddStatistics();
	}
	
	private void testAddStatistics(){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "testAddStatistics");
		}
		String packageName = "com.droi.markt";
		String interfaceName = "1";
		int count = 3;
		boolean isDbExit = DroiAccountProvider.isDBExit();
		if(!isDbExit){
			DroiAccountProvider.createDatabase(mContext);
	        ContentValues values = new ContentValues();  
	        values.put(DroiAccountProvider.TB_STATISTICS_ITEM_PACKAGENAME, packageName); 
	        values.put(DroiAccountProvider.TB_STATISTICS_ITEM_INTERFACE, interfaceName); 
	        values.put(DroiAccountProvider.TB_STATISTICS_ITEM_COUNTS, count); 
	        mContext.getContentResolver().insert(DroiAccountProvider.CONTENT_STATISTICS_URI, values); 
	        return;
		} 
		
		String columns[] = new String[] {DroiAccountProvider._ID, DroiAccountProvider.TB_STATISTICS_ITEM_PACKAGENAME,
				DroiAccountProvider.TB_STATISTICS_ITEM_INTERFACE,DroiAccountProvider.TB_STATISTICS_ITEM_COUNTS
				};
		
		Uri uri = DroiAccountProvider.CONTENT_STATISTICS_URI;  
		Cursor cur = mContext.getContentResolver().query(uri, columns, null, null, null);
		
		if(cur == null){
	        ContentValues values = new ContentValues();  
	        values.put(DroiAccountProvider.TB_STATISTICS_ITEM_PACKAGENAME, packageName); 
	        values.put(DroiAccountProvider.TB_STATISTICS_ITEM_INTERFACE, interfaceName); 
	        values.put(DroiAccountProvider.TB_STATISTICS_ITEM_COUNTS, count);
	        mContext.getContentResolver().insert(DroiAccountProvider.CONTENT_STATISTICS_URI, values); 
		}else{
			if(cur.getCount() == 1){
		        ContentValues values = new ContentValues();  
		        values.put(DroiAccountProvider.TB_STATISTICS_ITEM_PACKAGENAME, packageName); 
		        values.put(DroiAccountProvider.TB_STATISTICS_ITEM_INTERFACE, interfaceName); 
		        values.put(DroiAccountProvider.TB_STATISTICS_ITEM_COUNTS, count);
		        mContext.getContentResolver().update(DroiAccountProvider.CONTENT_STATISTICS_URI, values, null, null); 
			}else{
		        ContentValues values = new ContentValues();  
		        values.put(DroiAccountProvider.TB_STATISTICS_ITEM_PACKAGENAME, packageName); 
		        values.put(DroiAccountProvider.TB_STATISTICS_ITEM_INTERFACE, interfaceName); 
		        values.put(DroiAccountProvider.TB_STATISTICS_ITEM_COUNTS, count);
		        mContext.getContentResolver().delete(DroiAccountProvider.CONTENT_STATISTICS_URI, null, null); 
		        mContext.getContentResolver().insert(DroiAccountProvider.CONTENT_STATISTICS_URI, values); 
			}
		}
		
		if(cur != null){
			cur.close();
		}
	}
	
	public String getPublicDataFromInternalAccount(){
		String data = mSharedInfo.getInternalDataFromAccount(mContext);
		 String jsonStringer = null;
		 
		 try {
			JSONObject rawData = new JSONObject(data);
			jsonStringer = new JSONStringer().object()
					 .key(SHARED_USERNAME).value(rawData.has(SHARED_USERNAME) ? rawData.getString(SHARED_USERNAME) : null)
					 .key(SHARED_NICKNAME).value(rawData.has(SHARED_NICKNAME) ? rawData.getString(SHARED_NICKNAME) : null)
					 .key(SHARED_OPENID).value(rawData.has(SHARED_OPENID) ? rawData.getString(SHARED_OPENID) : null)
					 //for qq or weibo begin
					 .key(SHARED_UID).value(rawData.has(SHARED_UID) ? rawData.getString(SHARED_UID) : null) 
					 .key(SHARED_TOKEN).value(rawData.has(SHARED_TOKEN) ? rawData.getString(SHARED_TOKEN) : null)
					 .key(SHARED_DATA).value(rawData.has(SHARED_DATA) ? rawData.getString(SHARED_DATA) : null)
					 .key(SHARED_REGTYPE).value(rawData.has(SHARED_REGTYPE) ? rawData.getString(SHARED_REGTYPE) : null)
					 .key(SHARED_PWDVAL).value(rawData.has(SHARED_PWDVAL) ? rawData.getString(SHARED_PWDVAL) : null)
					 .key(SHARED_EXPIRE).value(rawData.has(SHARED_EXPIRE) ? rawData.getString(SHARED_EXPIRE) : null)
					 .key(SHARED_BINDPHONE).value(rawData.has(SHARED_BINDPHONE) ? rawData.getString(SHARED_BINDPHONE) : null)
					 .key(SHARED_BINDMAIL).value(rawData.has(SHARED_BINDMAIL) ? rawData.getString(SHARED_BINDMAIL) : null)
					 .key(SHARED_AVATAR).value(rawData.has(SHARED_AVATAR) ? rawData.getString(SHARED_AVATAR) : null)
					 .endObject().toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonStringer;
	}
	
	private String buildUserData(User user){
		 String jsonStringer = null;
		 try {
			jsonStringer = new JSONStringer().object()
					 .key(SHARED_USERNAME).value(TextUtils.isEmpty(user.getName()) ? "" : user.getName())
					 .key(SHARED_OPENID).value(user.getOpenId())
					 //for qq or weibo begin
					 .key(SHARED_UID).value(user.getUID()) 
					 .key(SHARED_TOKEN).value(user.getToken())
					 .key(SHARED_DATA).value(user.getData())
					 .key(SHARED_REGTYPE).value(user.getRegtype())
					 .key(SHARED_PWDVAL).value(user.getPasswdVal())
					 .key(SHARED_EXPIRE).value(user.getExpires())
					 .key(SHARED_BINDPHONE).value(user.getBindPhone())
					 .key(SHARED_BINDMAIL).value(user.getBindEmail())
					 .key(SHARED_AVATAR).value(AvatarUtils.getAvatarPath())
					 .endObject().toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonStringer;
	}
	
	/*
	private class LoginTask extends AsyncTask<Void, Void, String>{
		 private String mUserName;
		 private String mPasswordMD5;
		 private String uType;
		 
		 public LoginTask(String username, String password){
			 mUserName = username;
			 mPasswordMD5 = password;
			 uType = Utils.getAccountUtype(username);
		 }
		 
		@Override
		protected String doInBackground(Void... params) {
			// TODO Autogenerated method stub
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "mUserName : " + mUserName + ", uType : " + uType);
			}
			final Map<String, String> loginParams = new HashMap<String, String>();
			loginParams.put(Constants.JSON_S_UID, mUserName);
			loginParams.put(Constants.JSON_S_PWD, mPasswordMD5);
			loginParams.put("utype", uType);
			loginParams.put("devinfo", " ");
			loginParams.put("sign", MD5Util.md5(mUserName+ mPasswordMD5 + uType + " "+Constants.SIGNKEY));
			
			String loginResult = null;
			try{
				loginResult = HttpOperation.postRequest(Constants.ACCOUNT_LOGIN, loginParams);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return loginResult;
		}
		
       @Override
       protected void onPostExecute(String result){
       	super.onPostExecute(result);
       	if (!TextUtils.isEmpty(result)){
       		try {
					JSONObject jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == 0){
						if(DebugUtils.DEBUG){
							DebugUtils.i(TAG, "relogin sucess");
						}
						User user = new User();
						user.setName(jsonObject.has("nickname")?jsonObject.getString("nickname"): mUserName);
						user.setPassword(mPasswordMD5);
						user.setToken(jsonObject.getString("token"));
						user.setUID(jsonObject.has("uid")?jsonObject.getString("uid"): mUserName);
						user.setOpenId(jsonObject.getString("openid"));
						user.setGender(jsonObject.getString("gender"));
						user.setRegtype("");
						publicToApps(user);
					}else{
						if(DebugUtils.DEBUG){
							String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : "desc null";
							DebugUtils.i(TAG, "relogin faile : " + desc);
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
       	}else{
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "relogin fail : result null");
			}
       	}
       }
       
       @Override
       protected void onCancelled(){
 
       }
	}*/
	
	/*class OtherUserLoginTask extends AsyncTask<Void, Void, String> {
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
	            	DebugUtils.i(TAG, "data = " + mData);
	            	DebugUtils.i(TAG, "return = " + loginResult);
	            }
	        }catch (Exception e){
	            e.printStackTrace();
	        }
			return loginResult;
		}
		
       @Override
       protected void onPostExecute(String result){
       	super.onPostExecute(result);
       	if (!TextUtils.isEmpty(result)){
       		try {
					JSONObject jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == 0){
						//String token = jsonObject.has("token") ? jsonObject.getString("token"): null;
						//String openId = jsonObject.has("openid") ? jsonObject.getString("openid") : null;
						if(DebugUtils.DEBUG){
							DebugUtils.i(TAG, "relogin qq or wb success" );
						}
						User user = SharedInfo.getInstance().getData();
						publicToApps(user);
					}else{
						if(DebugUtils.DEBUG){
							String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : "desc null";
							DebugUtils.i(TAG, "relogin qq or wb fail : " + desc);
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
       	}else{
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "relogin qq or wb fail" );
			}
       	}
       }
       
       @Override
       protected void onCancelled(){
       }
	}*/

}
