package com.droi.account.manager;

public interface AccountChangeListener {

	//login notify
	
	//password mofify notify
	
	//third party, qq or weibo handle
	
	public void onAccountChanged();
	
	public void onAccountLogin();
	
	public void onAccountDelete();
	
	public void onAccountChangePwd();
	
	//receieve msg from apps
	
}
