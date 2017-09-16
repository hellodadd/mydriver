package com.droi.account.weibosdk;

import android.webkit.WebViewClient;

abstract class WeiboWebViewClient extends WebViewClient
{
  protected BrowserRequestCallBack mCallBack;

  public void setBrowserRequestCallBack(BrowserRequestCallBack callback)
  {
    this.mCallBack = callback;
  }
}