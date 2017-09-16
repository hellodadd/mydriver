package com.droi.account.login;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AccessTokenKeeperUtils {

	private static final String PREFERENCES_NAME_QQ = "com_qq_sdk_android";
	
    private static final String KEY_QQ_OPENID = "openid";
    private static final String KEY_QQ_ACCESS_TOKEN = "access_token";
    private static final String KEY_QQ_EXPIRES_IN = "expires_in";
	
    //
    private static final String PREFERENCES_NAME_WB = "com_weibo_sdk_android";
    private static final String KEY_WB_UID = "uid";
    private static final String KEY_WB_ACCESS_TOKEN = "access_token";
    private static final String KEY_WB_EXPIRES_IN = "expires_in";
    
    public static void writeAccessTokenQQ(Context context, JSONObject values) {
        if (null == context || null == values) {
            return;
        }
		try {
			String expires = (values.has("expires_in") ? values.getString("expires_in") : null);
			Long expires_in = System.currentTimeMillis() + Long.parseLong(expires) * 1000;
	    	String token = (values.has("access_token") ? values.getString("access_token") : null);
	    	String openId = (values.has("openid") ? values.getString("openid") : null);
	        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME_QQ, Context.MODE_PRIVATE);
	        Editor editor = pref.edit();
	        editor.putString(KEY_QQ_OPENID, openId);
	        editor.putString(KEY_QQ_ACCESS_TOKEN, token);
	        editor.putLong(KEY_QQ_EXPIRES_IN, expires_in);
	        editor.commit();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static Map<String, String> readAccessTokenQQ(Context context){
    	if(context == null){
    		return null;
    	}
    	SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME_QQ, Context.MODE_PRIVATE);
    	final Map<String, String> params = new HashMap<String, String>();
    	params.put(KEY_QQ_OPENID, pref.getString(KEY_QQ_OPENID, ""));
    	params.put(KEY_QQ_ACCESS_TOKEN, pref.getString(KEY_QQ_ACCESS_TOKEN, ""));
    	params.put(KEY_QQ_EXPIRES_IN, pref.getLong(KEY_QQ_EXPIRES_IN, 0)+"");
    	return params;
    }
    
    public static void writeAccessToken(Context context, Oauth2AccessToken token){
        if (null == context || null == token){
            return;
        }

        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME_WB, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.putString(KEY_WB_UID, token.getUid());
        editor.putString(KEY_WB_ACCESS_TOKEN, token.getToken());
        editor.putLong(KEY_WB_EXPIRES_IN, token.getExpiresTime());
        editor.commit();
    }
    
    public static Oauth2AccessToken readAccessToken(Context context){
        if (null == context){
            return null;
        }

        Oauth2AccessToken token = new Oauth2AccessToken();
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME_WB, Context.MODE_APPEND);
        token.setUid(pref.getString(KEY_WB_UID, ""));
        token.setToken(pref.getString(KEY_WB_ACCESS_TOKEN, ""));
        token.setExpiresTime(pref.getLong(KEY_WB_EXPIRES_IN, 0));
        return token;
    }
}
