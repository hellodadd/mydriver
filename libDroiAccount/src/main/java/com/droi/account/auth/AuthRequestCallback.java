package com.droi.account.auth;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

public interface AuthRequestCallback {
	  public void onPageStartedCallBack(WebView paramWebView, String paramString, Bitmap paramBitmap);

	  public boolean shouldOverrideUrlLoadingCallBack(WebView paramWebView, String paramString);

	  public void onPageFinishedCallBack(WebView paramWebView, String paramString);

	  public void onReceivedErrorCallBack(WebView paramWebView, int paramInt, String paramString1, String paramString2);

	  public void onReceivedSslErrorCallBack(WebView paramWebView, SslErrorHandler paramSslErrorHandler, SslError paramSslError);
}
