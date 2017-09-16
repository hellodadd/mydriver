package com.droi.account.login;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.droi.account.DebugUtils;
import com.droi.account.MyResource;
import com.droi.account.Utils;
import com.droi.account.authenticator.AuthenticatorActivity;
import com.droi.account.authenticator.Constants;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.statis.AccountOperation;
import com.droi.account.statis.StaticsCallback;
import com.droi.account.weibosdk.SinaConstants;
import com.droi.account.weibosdk.SsoHandler;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;

public class WbAuth3 {

	 private static final String TAG = "WBLoginUtils";
		//for WB
	    /**
	     * Scope 是 OAuth2.0 授权机制中 authorize 接口的一个参数。通过 Scope，平台将开放更多的微博 核心功能给开发者，同时也加强用户隐私保护，提升了用户体验，用户在新 OAuth2.0 授权页中有权利 选择赋予应用的功能。
	     * 
	     * 我们通过新浪微博开放平台-->管理中心-->我的应用-->接口管理处，能看到我们目前已有哪些接口的 使用权限，高级权限需要进行申请。
	     * 
	     * 目前 Scope 支持传入多个 Scope 权限，用逗号分隔。
	     * 
	     * 有关哪些 OpenAPI 需要权限申请，请查看：http://open.weibo.com/wiki/%E5%BE%AE%E5%8D%9AAPI 关于 Scope 概念及注意事项，请查看：http://open.weibo.com/wiki/Scope
	     */
	    String SCOPE = "email,direct_messages_read,direct_messages_write," + "friendships_groups_read,friendships_groups_write,statuses_to_me_read," + "follow_app_official_microblog," + "invitation_write";
	    
		private AuthenticatorActivity mContext;
		
		 /** 登陆认证对应的listener */
		private AuthListener mLoginListener = new AuthListener();
		// 创建授权认证信息
		private AuthInfo mAuthInfo = null;
		private SsoHandler mSsoHandler = null;
		private User mUser;
		protected MyResource mMyResources = null;
		
		public WbAuth3(AuthenticatorActivity context, User user){
			mContext = context;
			mMyResources = new MyResource(context);
			mAuthInfo = new AuthInfo(mContext, SinaConstants.APP_KEY, SinaConstants.REDIRECT_URL, SCOPE);
			mUser = user;
		}
		
		public void login(){
			mSsoHandler = new SsoHandler(mContext, mAuthInfo);
			 if(DebugUtils.DEBUG){
				 DebugUtils.i(TAG, "WBLogin start anthorize");
			 }
			 mSsoHandler.authorizeWeb(mLoginListener);
		}
		
		private class AuthListener implements WeiboAuthListener{

			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				mContext.hideProgress();
				Utils.showMessage(mContext, mMyResources.getString("lib_droi_account_toast_auth_canceled_again_bind"));
			}

			@Override
			public void onComplete(Bundle values) {
				// TODO Auto-generated method stub
				 if(DebugUtils.DEBUG){
					 DebugUtils.i(TAG, "WBLogin onComplete");
				 }
				 mContext.showProgress();
				 Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
				 if (accessToken != null && accessToken.isSessionValid()){
					 AccessTokenKeeperUtils.writeAccessToken(mContext.getApplicationContext(), accessToken);
					 final String uid = accessToken.getUid();
		             final String token = accessToken.getToken();
		             mUser.setUID(uid);
		             mUser.setToken(token);
		             mUser.setExpires(String.valueOf(accessToken.getExpiresTime()));
					 if(DebugUtils.DEBUG){
						 DebugUtils.i(TAG, "WB onComplete new Thread()");
					 }
		             new Thread(){

		                    @Override
		                    public void run(){
		                    	// 通过uid获取用户基础信息，只需要用户的昵称和头像
		                    	StringBuilder sBuilder = new StringBuilder();
		                        //sBuilder.append("https://api.weibo.com/2/users/show.json?").append("source=").append(SinaConstants.APP_KEY)
		                        //.append("&uid=").append(uid).append("&access_token=").append(token);
		                    	
		                        if(DebugUtils.DEBUG){
		                        	DebugUtils.i(TAG, "WB params token= " + token + ", uid = " + uid);
		                        }
		                        sBuilder.append("https://api.weibo.com/2/users/show.json?").append("access_token=").append(token)
		                        .append("&uid=").append(uid);
		                        
		                        
		                        String jsonString = null;
		                        try	{
		                            jsonString = HttpOperation.getRequest(sBuilder.toString());
			                        if(DebugUtils.DEBUG){
			                        	DebugUtils.i(TAG, "after getRequest = " + jsonString + ", builder = " + sBuilder.toString());
			                        }
		                            if (null != jsonString){
		                                try {
		                                    JSONObject response1 = new JSONObject(jsonString);
		                                    mUser.setName(response1.getString("screen_name"));
		                                    mUser.setLogoUrl(response1.getString("profile_image_url"));
		                                }catch (JSONException e){
		                                    e.printStackTrace();
		                                }
		                            }

		                        }catch (Exception e){
		                            e.printStackTrace();
		                        }
		                        
		                        if(DebugUtils.DEBUG){
		                        	DebugUtils.i(TAG, "WB jsonString = " + jsonString);
		                        }
		                        new OtherUserLoginTask(mContext.getApplicationContext(), jsonString, Constants.UTYPE_WEIBO, mUser, new OtherUserLoginTask.Callback(){

									@Override
									public void run() {
										// TODO Auto-generated method stub
										mContext.hideProgress();
										int result = mUser.getResult();
										if(DebugUtils.DEBUG){
											 DebugUtils.i(TAG, "WB loglin fun in FFFFFFFF result = " + result);
										}
										if(result == 0){
											StaticsCallback staticsCallback = StaticsCallback.getInstance(mContext);
											staticsCallback.onLoginResult(mUser.getName(), mUser, AccountOperation.ACCOUNT_LOGIN_TYPE_WEIBO);
											mContext.onThirdPartyLoginFinish();
										}
									}

									@Override
									public void onCancelled() {
										// TODO Auto-generated method stub
										mContext.hideProgress();
									}
		                        	
		                        }).execute();

		                    }

		                }.start();
				 }
			}

			@Override
			public void onWeiboException(WeiboException e) {
				// TODO Auto-generated method stub
				mContext.hideProgress();
				Utils.showMessage(mContext, e.getMessage());
			}
			
		}
}
