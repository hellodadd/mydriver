package com.droi.account.auth;

public interface DroiAuthListener {

	public void onComplete(String result);
	public void onCancel();
	public void onError(String message);
}
