package com.droi.account.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class AccountEditText extends EditText {
	
	 public AccountEditText(Context context, AttributeSet attrs) {
		 super(context, attrs);
		 setupEditText();
	 }
	 
	 public AccountEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		 super(context, attrs, defStyleAttr/*, defStyleRes*/);
		 setupEditText();
	 }
	 
	 private void setupEditText(){
		    setCustomSelectionActionModeCallback(new ActionMode.Callback() {
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public void onDestroyActionMode(ActionMode mode) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					// TODO Auto-generated method stub
					return false;
				}
			});
	 }
}
