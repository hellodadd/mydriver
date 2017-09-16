package com.droi.account.login;

import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQAuth;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import com.droi.account.DebugUtils;
import com.droi.account.MyResource;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;

public class QQLoginUtils {
	private static final String TAG = "QQLoginUtils";
	//for QQ
	public static final String QQ_APP_ID = "1103957320";
	
	public static QQAuth mQQAuth = null;
	private WelcomeActivity mContext;
	private User mUser;
	public Tencent mTencent;
	private UserInfo mInfo;
	
	private MyResource mMyResources;
	
	public QQLoginUtils(WelcomeActivity context, User user){
		mContext = context;
		mUser = user;
		if(mQQAuth == null){
			mQQAuth = QQAuth.createInstance(QQ_APP_ID, context.getApplicationContext());
		}
		if(mTencent == null){
			mTencent = Tencent.createInstance(QQ_APP_ID, context.getApplicationContext());
		}
		mMyResources = new MyResource(context);
	}
	
	public void login(){
		if(mTencent != null){
			if(mQQAuth != null){
				IUiListener listener = new BaseUiListener(){
					 @Override
			         protected void doComplete(JSONObject values){
						 updateUserInfo();
					 }
					 
					 @Override
			         public void onError(UiError e){
						 mContext.hideProgress();
						 Utils.showMessage(mContext, mMyResources.getString("lib_droi_account_toast_auth_canceled_again_bind"));
					 }
					 
					 @Override
					 public void onCancel() {
						 // TODO Auto-generated method stub
						 mContext.hideProgress();
						 Utils.showMessage(mContext, mMyResources.getString("lib_droi_account_toast_auth_canceled_again_bind"));
					 }
					 
				};
				try{
					mContext.showProgress();
					//recommand call setAccessToken setOpenId before login
					mTencent.setAccessToken(mUser.getToken(), mUser.getExpires());
					mTencent.setOpenId(mUser.getUID());
					 if(DebugUtils.DEBUG){
						 DebugUtils.i(TAG, "qq start login");
					 }
					mTencent.login(mContext, "all", listener);
				}catch(Exception e){
					Utils.showMessage(mContext, "Exception : " + e.getMessage());
					mContext.hideProgress();
					e.printStackTrace();
				}
			}else{
				updateUserInfo();
				Utils.showMessage(mContext, mMyResources.getString("lib_droi_account_toast_auth_canceled_again_bind"));
			}
		}
	}
	
	private void updateUserInfo(){
		 if (mQQAuth != null){
			 IUiListener listener = new IUiListener(){

				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
					mContext.hideProgress();
				}

				@Override
				public void onComplete(Object response) {
					// TODO Auto-generated method stub
					final JSONObject json = (JSONObject)response;
					if(response != null){
                        try{
                            mUser.setName(json.has("nickname")?json.getString("nickname") : null);
                            mUser.setLogoUrl(json.has("figureurl_qq_2")?json.getString("figureurl_qq_2") : null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
					}
					if(DebugUtils.DEBUG){
						DebugUtils.i(TAG, "qq response = " + response.toString());
					}
					new OtherUserLoginTask(mContext.getApplicationContext(), response.toString(), Constants.UTYPE_QQ, mUser, new OtherUserLoginTask.Callback(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mContext.hideProgress();
							int result = mUser.getResult();
							if(DebugUtils.DEBUG){
								DebugUtils.i(TAG, "qq login result = " + result );
							}
							if(result == 0){
								mContext.otherLoginFinish();
								//Intent intent = new Intent();
								//intent.putExtra("type", AuthenticatorActivity.ACCOUNT_LOGIN_QQ);
								//mContext.setResult(mContext.RESULT_OK, intent);
								//mContext.finish();
							}else{
								//mContext.onAuthenticationCancel();
								//mContext.setResult(mContext.RESULT_CANCELED);
								//Utils.showMessage(mContext, R.string.login_fail);
								//mContext.finish();
							}
						}

						@Override
						public void onCancelled() {
							// TODO Auto-generated method stub
							mContext.hideProgress();
						}
						
					}).execute();
				}

				@Override
				public void onError(UiError e) {
					// TODO Auto-generated method stub
					mContext.hideProgress();
					Utils.showMessage(mContext, e.errorMessage);
				}
				 
			 };
			 
			 mInfo = new UserInfo(mContext, /*mQQAuth.getQQToken()*/mTencent.getQQToken());
			 mInfo.getUserInfo(listener);
		 }else{
			 mContext.hideProgress();
			 Utils.showMessage(mContext, mMyResources.getString("lib_droi_account_toast_auth_canceled_again_bind"));
		 }
	}
	
	private class BaseUiListener implements IUiListener{

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			mContext.hideProgress();
			Utils.showMessage(mContext, mMyResources.getString("lib_droi_account_toast_auth_canceled_again_bind"));
		}

		protected void doComplete(JSONObject values) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onComplete(Object response) {
			// TODO Auto-generated method stub
            JSONObject values = (JSONObject) response;
            try{
            	mUser.setExpires(values.has("expires_in") ? values.getString("expires_in") : null);
            	mUser.setToken(values.has("access_token") ? values.getString("access_token") : null);
            	mUser.setUID(values.has("openid") ? values.getString("openid") : null);
            	mUser.setOpenKey(values.has("pfkey") ? values.getString("pfkey") : null);
            	AccessTokenKeeperUtils.writeAccessTokenQQ(mContext.getApplicationContext(), values);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.doComplete(values);
			
		}

		@Override
		public void onError(UiError e) {
			// TODO Auto-generated method stub
			Utils.showMessage(mContext, "onError: " + e.errorDetail);
		}
		
	}
}
