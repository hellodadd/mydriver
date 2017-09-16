package com.droi.account.view;

import com.droi.account.MyResource;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;


public class NoBackgroundLinearLayout extends LinearLayout {

	protected MyResource mMyResources = null;
	
	public NoBackgroundLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mMyResources = new MyResource(context);
	}

	
	@Override
	public void setBackgroundResource(int resid) {
		// TODO Auto-generated method stub
		super.setBackgroundResource(mMyResources.getDrawable("lib_droi_account_transparent"));
	}
}
