package com.droi.account.login;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.droi.account.MyResource;



public class PrivacyPolicy extends Activity {

	private WebView mWebView;
	protected MyResource mMyResources = new MyResource(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(mMyResources.getLayout("lib_droi_account_privacy_policy_layout"));
		mWebView = (WebView)findViewById(mMyResources.getId("lib_droi_account_webView"));
		setTitle(mMyResources.getString("lib_droi_account_user_items_privacy_text"));
		initWebView();
		mWebView.loadUrl("file:///android_asset/lib_droi_account_privacy.html");
	}
	
	private void initWebView(){
		mWebView.setInitialScale(100);
	    mWebView.setLongClickable(true);
	    mWebView.setClickable(true);
	    mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
	    WebSettings webSettings = mWebView.getSettings();
	    webSettings.setBuiltInZoomControls(true);
	    webSettings.setSupportZoom(true);
	    webSettings.setUseWideViewPort(true);
	    webSettings.setJavaScriptEnabled(true);
	    //webSettings.setLoadsImagesAutomatically(true);
	    //webSettings.setLoadWithOverviewMode(true);
	   // webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
	    //webSettings.setDatabaseEnabled(true);
	    //*/ Added by tyd wulianghuan 2014-03-05 for adapt to different screen density
		int screenDensity = getResources().getDisplayMetrics().densityDpi;
		WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM;
		switch (screenDensity) {
		
		case DisplayMetrics.DENSITY_MEDIUM:
			zoomDensity = WebSettings.ZoomDensity.MEDIUM;
			break;

		case DisplayMetrics.DENSITY_HIGH:
			mWebView.setInitialScale(75);
			zoomDensity = WebSettings.ZoomDensity.FAR;
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			zoomDensity = WebSettings.ZoomDensity.FAR;
			
		}
		webSettings.setDefaultZoom(zoomDensity);
	}
	
	private String readTextFromResource(int resourceID)	{
		InputStream raw = getResources().openRawResource(resourceID);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		int i;
		try	{
			i = raw.read();
			while (i != -1)	{
				stream.write(i);
				i = raw.read();
			}
			raw.close();
		}catch (IOException e){
			e.printStackTrace();
		}
		return stream.toString();
	}
	
	private String readFromRaw(int resourceID){
		StringBuffer buffer = new StringBuffer();
		try {
			InputStream inputStream = getResources().openRawResource(resourceID);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String info = "";
			while ((info = bufferedReader.readLine()) != null) {
				buffer.append(info);
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
			
		return buffer.toString();
	}

	public void onBack(View view){
		onBackPressed();
	}
	
	private static final String PRIVARY_TEXT = "重要须知:" +
			"卓悠网络科技有限公司（下称“卓悠”）在此特别提醒用户认真阅读、充分理解本《服务协议》（下称《协议》）--- 用户应认真阅读、充分理解本《协议》中各条款 ，包括免除或者限制卓悠责任的免责条款及对用户的权利限制条款。请您审慎阅读并选择接受或不接受本《协议》（未成年人应在 法定监护人陪同下阅读）。除非您接受本《协议》所有条款，否则您无权注册、登录或使用本协议所涉相关服务。您的注册、登录 、使用等行为将视为对本《协议》的接受，并同意接受本《协议》各项条款的约束."
			+"本《协议》是您（下称“用户”）与卓悠公司之间关于用户注册、登录、使用卓易相关服务所订立的协 议。本《协议》描述卓悠与用户之间关于Freeme OS帐号服务相关方面的权利义务。“用户”是指注册、登录、使用、浏览本服务的个人或 组织。"
			+ "您对本协议的接受即受全部条款的约束，包括接受卓悠公司对任一服务条款随时所做的任何修改。本《协议》可由卓悠随时更新， 更新后的协议条款一旦公布即代替原来的协议条款，恕不再另行通知，用户可在本网站查阅最新版协议条款。在卓悠修改《协议》 条款后，如果用户不接受修改后的条款，请立即停止使用卓易提供的服务，用户继续使用卓易提供的服务将被视为已接受了修改后的协议。"
			+"";
}
