package com.droi.account.shared;

public interface IAccountListener {
	public void onSuccess(String userInfo);
	
	public void onCancel();
}
