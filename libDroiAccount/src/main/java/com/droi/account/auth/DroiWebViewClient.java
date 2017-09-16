package com.droi.account.auth;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.droi.account.DebugUtils;

public class DroiWebViewClient extends WebViewClient {
	private static final String TAG = "DroiWebViewClient";

	private AuthRequestCallback mCallback;
	
	public DroiWebViewClient(AuthRequestCallback callback){
		mCallback = callback;
	}
	
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		view.loadUrl(url);
		if(mCallback != null){
			mCallback.shouldOverrideUrlLoadingCallBack(view, url);
		}
		return true;
	}

	 @Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		// TODO Auto-generated method stub
		super.onPageStarted(view, url, favicon);
		if(mCallback != null){
			mCallback.onPageStartedCallBack(view, url, favicon);
		}
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onPageStarted url = " + url);
		}
	}
	
	@Override
	public void onPageFinished(WebView view, String url) {
		// TODO Auto-generated method stub
		super.onPageFinished(view, url);
		if(mCallback != null){
			mCallback.onPageFinishedCallBack(view, url);
		}
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onPageFinished url = " + url);
	        // CookieManager cookieManager = CookieManager.getInstance();
	        // String CookieStr = cookieManager.getCookie(url);
	        //DebugUtils.e(TAG, "Cookies = " + CookieStr);
		}

	}
	 
	 @Override
	public void onReceivedHttpAuthRequest(WebView view,
			HttpAuthHandler handler, String host, String realm) {
		// TODO Auto-generated method stub
		super.onReceivedHttpAuthRequest(view, handler, host, realm);
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onReceivedHttpAuthRequest realm = " + realm);
		}
	}
	
	@Override
	public void onReceivedLoginRequest(WebView view, String realm,
			String account, String args) {
		// TODO Auto-generated method stub
		super.onReceivedLoginRequest(view, realm, account, args);
		if (DebugUtils.DEBUG) {
			DebugUtils.i(TAG, "onReceivedLoginRequest realm = " + realm);
		}
	}
	 
	@Override
	public void onReceivedError(WebView view, int errorCode,
			String description, String failingUrl) {
		// TODO Auto-generated method stub
		super.onReceivedError(view, errorCode, description, failingUrl);
		if(mCallback != null){
			mCallback.onReceivedErrorCallBack(view, errorCode, description, failingUrl);
		}
		if (DebugUtils.DEBUG) {
			DebugUtils.i(TAG, "onReceivedError errorCode = " + errorCode
					+ ", description = " + description);
		}
	}

	@Override
	public void onReceivedSslError(WebView view, SslErrorHandler handler,
			SslError error) {
		// TODO Auto-generated method stub
		super.onReceivedSslError(view, handler, error);
		if(mCallback != null){
			mCallback.onReceivedSslErrorCallBack(view, handler, error);
		}
		if (DebugUtils.DEBUG) {
			DebugUtils.i(TAG, "onReceivedSslError handler = " + handler
					+ ", error = " + error);
		}
	}

}
