package com.droi.account.login;


import android.accounts.AccountAuthenticatorActivity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;

import com.droi.account.MyResource;
import com.droi.account.Utils;

public class BaseActivity extends AccountAuthenticatorActivity {
	ProgressDialog mDialog = null;
	
	private static final int DIALOG_NETWORK_ERROR = 1000;
	protected MyResource mMyResources = new MyResource(this);
	
    @Override
    protected void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	if(Utils.SHOW_ACTION_BAR){
	    	 ActionBar actionBar = getActionBar();
	    	 if(actionBar != null){
	    		 actionBar.setDisplayOptions(
		   	                ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
	    	 }
    	}
    	mDialog = new ProgressDialog(BaseActivity.this);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
    }
    
    @Override
	 protected Dialog onCreateDialog(int id, Bundle args) {
		 Dialog dialog = null;
		 if(DIALOG_NETWORK_ERROR == id){
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 builder.setTitle(mMyResources.getString("lib_droi_account_dialog_title_hint"));
			 builder.setCancelable(false);
			 builder.setMessage(mMyResources.getString("lib_droi_account_network_wrong_text"));
			 builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						//finish();
					}
					 
				 });
			 dialog = builder.create();
		 }
		 return dialog;
    }
    
    protected void showNetWorkError(){
    	showDialog(DIALOG_NETWORK_ERROR);
    }
    
    protected void showProgressbar(String content){
    	 mDialog.setMessage(content);
    	 if (!mDialog.isShowing()){
    		 mDialog.show();
    	 }
    }
    
    
    protected void dismissProgressbar(){
    	if (mDialog.isShowing()){
    		mDialog.dismiss();
    	}
    }
    
    @Override
    public void setContentView(int layoutResID){
    	super.setContentView(layoutResID);
    }
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return true;
	}
	
	// This snippet hides the system bars.
	/*
	protected void hideNavigationBar() {
		View decorView = getWindow().getDecorView();
		if(decorView != null){
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			decorView.setSystemUiVisibility(uiOptions);
		}
	 }*/
	 
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
}
