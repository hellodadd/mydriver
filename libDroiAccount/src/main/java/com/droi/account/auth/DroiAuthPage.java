package com.droi.account.auth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droi.account.DebugUtils;
import com.droi.account.MyResource;
import com.droi.account.ResourceUtils;
import com.droi.account.widget.LoadingBar;

public class DroiAuthPage extends Activity implements AuthRequestCallback{

	private static final String TAG = "DroiAuthPage";
	private WebView mWebView;
	private String mUrl;
	private LoadingBar mLoadingBar;
	private boolean isLoading;
	private TextView mTitleText;
	private String mHtmlTitle;
	private TextView mLeftBtn;
	private String mSpecifyTitle;
	private LinearLayout mLoadErrorView;
	private Button mLoadErrorRetryBtn;
	private boolean mShowErrorPage;
	private MyResource mMyResources = new MyResource(this);
	//675917412
	//12345678
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		super.onCreate(savedInstanceState);
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onCreate");
		}
		initDataFromIntent(getIntent());
		setContentView();
		setupViews();
		openUrl(mUrl);
	}
	
	private void setupViews(){
		setupWebView();
	    mLeftBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//DroiAuthPage.this.finish();
				onBackPressed();
			}
		});
	}
	
	private boolean initDataFromIntent(Intent data){
		mSpecifyTitle = data.getStringExtra("title");
		mUrl = data.getStringExtra("url");
		return true;
	}
	
	private void openUrl(String url) {
		mWebView.loadUrl(url);
	}
	
	private void setupWebView(){
		mWebView.getSettings().setJavaScriptEnabled(true);
	    mWebView.getSettings().setSavePassword(false);
	    DroiWebViewClient droiWebViewClient = new DroiWebViewClient(this);
	    mWebView.setWebViewClient(droiWebViewClient);
	    mWebView.setWebChromeClient(new DroiChromeClient());
	    mWebView.requestFocus();
	    mWebView.setScrollBarStyle(0);
	    mWebView.addJavascriptInterface(this, "droiaccount");
	}
	
	private void setContentView(){
		RelativeLayout contentLy = new RelativeLayout(this);
		contentLy.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
		contentLy.setBackgroundColor(-1);

		LinearLayout titleBarLy = new LinearLayout(this);
		titleBarLy.setId(1);
		titleBarLy.setOrientation(LinearLayout.VERTICAL);
		titleBarLy.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
	    
		RelativeLayout titleBar = new RelativeLayout(this);
		titleBar.setLayoutParams(new ViewGroup.LayoutParams(-1, ResourceUtils.dp2px(this, 45)));
		titleBar.setBackgroundResource(mMyResources.getDrawable("lib_droi_account_droiauth_navigationbar_background"));
		
		mLeftBtn = new TextView(this);
		mLeftBtn.setClickable(true);
		mLeftBtn.setTextSize(2, 17.0F);
		mLeftBtn.setText(mMyResources.getString("lib_droi_account_droi_auth_close"));
		RelativeLayout.LayoutParams leftBtnLp = new RelativeLayout.LayoutParams(
				-2, -2);
		leftBtnLp.addRule(5);
		leftBtnLp.addRule(15);
		leftBtnLp.leftMargin = ResourceUtils.dp2px(this, 10);
		leftBtnLp.rightMargin = ResourceUtils.dp2px(this, 10);
		mLeftBtn.setLayoutParams(leftBtnLp);
	    
	    mTitleText = new TextView(this);
	    mTitleText.setTextSize(2, 18.0F);
	    mTitleText.setTextColor(-11382190);
	    mTitleText.setEllipsize(TextUtils.TruncateAt.END);
	    mTitleText.setSingleLine(true);
		mTitleText.setGravity(17);
		mTitleText.setMaxWidth(ResourceUtils.dp2px(this, 160));
		RelativeLayout.LayoutParams titleTextLy = new RelativeLayout.LayoutParams(
				-2, -2);
		titleTextLy.addRule(13);
		mTitleText.setLayoutParams(titleTextLy);
		//
	    titleBar.addView(mTitleText);
	    titleBar.addView(mLeftBtn);
	    
	    mLoadingBar = new LoadingBar(this);
	    mLoadingBar.setBackgroundColor(0);
	    mLoadingBar.drawProgress(0);
	    LinearLayout.LayoutParams loadingBarLy = new LinearLayout.LayoutParams(
	      -1, ResourceUtils.dp2px(this, 3));
	    mLoadingBar.setLayoutParams(loadingBarLy);
	    
	    titleBarLy.addView(titleBar);
	    titleBarLy.addView(mLoadingBar);
	    
	    mWebView = new WebView(getApplicationContext());
	    mWebView.setBackgroundColor(-1);
	    RelativeLayout.LayoutParams webViewLp = new RelativeLayout.LayoutParams(
	      -1, 
	      -1);
	    webViewLp.addRule(RelativeLayout.BELOW, 1);
	    
		mWebView.setLayoutParams(webViewLp);

		mLoadErrorView = new LinearLayout(this);
		mLoadErrorView.setVisibility(View.GONE);
		mLoadErrorView.setOrientation(LinearLayout.VERTICAL);
		mLoadErrorView.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams loadErrorLp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);

		loadErrorLp.addRule(RelativeLayout.BELOW, 1);
		mLoadErrorView.setLayoutParams(loadErrorLp);
		
		//add error img
		ImageView errorImg = new ImageView(this);
		errorImg.setImageResource(mMyResources.getDrawable("lib_droi_account_droiauth_empty_failed"));
		LinearLayout.LayoutParams errorLp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		errorLp.leftMargin = errorLp.topMargin = errorLp.rightMargin = errorLp.bottomMargin = ResourceUtils
				.dp2px(this, 8);
		errorImg.setLayoutParams(errorLp);
		
		mLoadErrorView.addView(errorImg);
		//add hint textview
		TextView errorContent = new TextView(this);
		errorContent.setGravity(Gravity.CENTER_HORIZONTAL);
		errorContent.setTextColor(0xFFBDBDBD);
		errorContent.setTextSize(2, 14.0F);
		errorContent.setText(mMyResources.getString("lib_droi_account_droi_auth_network_error"));
		LinearLayout.LayoutParams errorConentLp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		errorContent.setLayoutParams(errorConentLp);
		mLoadErrorView.addView(errorContent);
		
		//add retry button
		mLoadErrorRetryBtn = new Button(this);
		mLoadErrorRetryBtn.setGravity(Gravity.CENTER);
		mLoadErrorRetryBtn.setTextColor(0xFF787878);
		mLoadErrorRetryBtn.setTextSize(2, 16.0F);
		mLoadErrorRetryBtn.setText(mMyResources.getString("lib_droi_account_droi_auth_retry_btn"));
		mLoadErrorRetryBtn.setBackgroundResource(mMyResources.getDrawable("lib_droi_account_droiauth_button_selector"));
		LinearLayout.LayoutParams retryBtnLp = new LinearLayout.LayoutParams(
				ResourceUtils.dp2px(this, 142), ResourceUtils.dp2px(this, 46));
		retryBtnLp.topMargin = ResourceUtils.dp2px(this, 10);
		mLoadErrorRetryBtn.setLayoutParams(retryBtnLp);
		mLoadErrorRetryBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openUrl(mUrl);
				mShowErrorPage = false;
			}
		});
		mLoadErrorView.addView(mLoadErrorRetryBtn);
		
	    contentLy.addView(titleBarLy);
	    contentLy.addView(mWebView);
	    contentLy.addView(mLoadErrorView);
	    setContentView(contentLy);
	}
	
	 private class DroiChromeClient extends WebChromeClient {
	    public DroiChromeClient() {
	    }

	    public void onProgressChanged(WebView view, int newProgress) {
	      DroiAuthPage.this.mLoadingBar.drawProgress(newProgress);
	      if (newProgress == 100) {
	    	  DroiAuthPage.this.isLoading = false;
	    	  DroiAuthPage.this.refreshAllViews();
	      } else if (!DroiAuthPage.this.isLoading) {
	    	  DroiAuthPage.this.isLoading = true;
	    	  DroiAuthPage.this.refreshAllViews();
	      }
	    }
	    
	    public void onReceivedTitle(WebView view, String title) {
	    	if(DebugUtils.DEBUG){
	    		DebugUtils.i(TAG, "onReceivedTitle title = " + title);
	    	}
	    	mHtmlTitle = title;
	    	updateTitleName();
	    }
	    
        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                JsResult result)  {
            // TODO Auto-generated method stub
        	if(DebugUtils.DEBUG){
        		DebugUtils.i(TAG, "onJsAlert message = " + message + ", result = " + result);
        	}
            return super.onJsAlert(view, url, message, result);
        }

		@Override
		public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        	if(DebugUtils.DEBUG){
        		DebugUtils.i(TAG, "onConsoleMessage message = " + consoleMessage.message());
        	}
			return super.onConsoleMessage(consoleMessage);
		}
	    
	  }
	 
	protected void refreshAllViews() {
		if (isLoading){
		  setViewLoading();
		}else{
		  setViewNormal();
		}
	}
	
	private void setViewNormal() {
	    updateTitleName();
	    mLoadingBar.setVisibility(View.GONE);
	}
	
	private void setViewLoading(){
	    mTitleText.setText(mMyResources.getString("lib_droi_account_droi_auth_loding"));
	    mLoadingBar.setVisibility(View.VISIBLE);
	}
	 
	  private void updateTitleName() {
	    String showTitle = "";

	    if (!TextUtils.isEmpty(this.mHtmlTitle)){
	      showTitle = this.mHtmlTitle;
	    } else if (!TextUtils.isEmpty(this.mSpecifyTitle)) {
	      showTitle = this.mSpecifyTitle;
	    }

	    this.mTitleText.setText(showTitle);
	  }

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mWebView.canGoBack()){
				mWebView.goBack();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
    	if(DebugUtils.DEBUG){
    		DebugUtils.i(TAG, "onDestroy");
    	}
    	if(mWebView != null){
    		mWebView.removeAllViews();
    		mWebView.destroy();
    		mWebView = null;
    	}
	}
	
	@JavascriptInterface
	public void onAuthResult(String result) {
		if (DebugUtils.DEBUG) {
			DebugUtils.i(TAG, "onAuthResult result = " + result);
		}
		DroiAuthListener listener = DroiCallbackInstance.getInstance(this)
				.getDroiAuthListener(DroiLoginHandler.KEY_AUTHLISTENER);
		listener.onComplete(result);
		DroiCallbackInstance.getInstance(this).removeDroiAuthListener(
				DroiLoginHandler.KEY_AUTHLISTENER);
		if (DebugUtils.DEBUG) {
			DebugUtils.i(TAG, "remove listener");
		}
		finish();
	}

	private void showError(){
		mShowErrorPage = true;
		mLoadErrorView.setVisibility(View.VISIBLE);
		mWebView.setVisibility(View.GONE);
	}
	
	private void hideError(){
		mShowErrorPage = false;
		mLoadErrorView.setVisibility(View.GONE);
		mWebView.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onBackPressed() {
		DroiAuthListener listener = DroiCallbackInstance.getInstance(this)
				.getDroiAuthListener(DroiLoginHandler.KEY_AUTHLISTENER);
		if(listener != null){
			listener.onCancel();
		}
		super.onBackPressed();
	}
	
	@Override
	public void onPageStartedCallBack(WebView paramWebView, String paramString,
			Bitmap paramBitmap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean shouldOverrideUrlLoadingCallBack(WebView paramWebView,
			String paramString) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onPageFinishedCallBack(WebView paramWebView, String paramString) {
		// TODO Auto-generated method stub
		if(mShowErrorPage){
			showError();
		}else{
			hideError();
		}
	}

	@Override
	public void onReceivedErrorCallBack(WebView paramWebView, int paramInt,
			String paramString1, String paramString2) {
		// TODO Auto-generated method stub
		showError();
	}

	@Override
	public void onReceivedSslErrorCallBack(WebView paramWebView,
			SslErrorHandler paramSslErrorHandler, SslError paramSslError) {
		// TODO Auto-generated method stub
		
	}
}
