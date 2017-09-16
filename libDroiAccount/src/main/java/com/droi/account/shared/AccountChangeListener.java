package com.droi.account.shared;

public interface AccountChangeListener {

	//public msg to apps
	public void onAccountLogin();
	
	public void onAccountDeleted();
	
	public void onAccountUpdated(String data);
	
	public void onAccountCancelled();
	
	public void onAccountBinded(String type, String data);
	//receieve msg from apps
	
}
