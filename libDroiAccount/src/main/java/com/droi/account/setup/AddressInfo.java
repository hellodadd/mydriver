package com.droi.account.setup;

public class AddressInfo {
	public String mName;
	public String mPhone;
	public String mAddressDes;
	public final String mInfoId;
	public String mOpenId;
	
	public AddressInfo(String infoid, String openId, String name, String phone, String address){
		mOpenId = openId;
		mInfoId = infoid;
		mName = name;
		mAddressDes = address;
		mPhone = phone;
	}
	
	public void setAddress(String addr){
		mAddressDes = addr;
	}
	
	public void setName(String name){
		mName = name;
	}
	
	public void updateInfo(AddressInfo info){
		mName = info.mName;
		mPhone = info.mPhone;
		mAddressDes = info.mAddressDes;
	}
	
}
