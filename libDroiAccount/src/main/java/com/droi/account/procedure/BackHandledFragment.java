package com.droi.account.procedure;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droi.account.DebugUtils;
import com.droi.account.MyResource;


public abstract class BackHandledFragment extends Fragment {
	private static final String TAG = "TESTFRAGMENT";
	protected IAcitivtyFragment mBackHandledInterface;
	
	protected static final int DIALOG_ON_PROGRESS = 100;
	protected static final int DIALOG_APP_ERROR = DIALOG_ON_PROGRESS + 1;
	protected abstract void findViewByIds(View view);
	protected abstract void setupViews();
	protected String mOldTitle;
	
	private ProgressDialog mProgressDialog = null;
	protected MyResource mMyResources = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!(getActivity() instanceof IAcitivtyFragment)) {
			throw new ClassCastException(
					"Hosting Activity must implement BackHandledInterface");
		} else {
			this.mBackHandledInterface = (IAcitivtyFragment) getActivity();
		}
		mMyResources = new MyResource(getActivity());
	}

	@Override
	public void onStart() {
		super.onStart();
		mBackHandledInterface.setSelectedFragment(this);
		final Activity activity = getActivity();
		mOldTitle = activity.getTitle().toString();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		final Activity activity = getActivity();
		if(!TextUtils.isEmpty(mOldTitle)){
			activity.setTitle(mOldTitle);
		}
	}
	
	protected void showDialog(int id, final AsyncTask task){
		 Dialog dialog = null;
		 if(DIALOG_ON_PROGRESS == id){
			 final ProgressDialog progressDialog = new ProgressDialog(getActivity());
			 progressDialog.setMessage(getString(mMyResources.getString("lib_droi_account_msg_on_process")));
			 progressDialog.setIndeterminate(true);
			 progressDialog.setCancelable(true);
			 progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
	
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					if(task != null){
						task.cancel(true);
					}
				}
		    	 
		     });
			 dialog = mProgressDialog = progressDialog;
		 }else if(DIALOG_APP_ERROR == id){
			 AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
	     dialog.show();
	}
	
	protected void hideProgress(){
	    if (mProgressDialog != null){
	        mProgressDialog.dismiss();
	    }
	}
	
	protected void setResult(int resultCode){
		Intent intent = new Intent();
		setResult(resultCode, intent);
	}
	
	protected void setResult(int resultCode, Intent data){
		final IAcitivtyFragment activity = (IAcitivtyFragment)getActivity();
		activity.onFragmentResult(getTargetRequestCode(), resultCode, data);
	}
	
	protected boolean onBackPressed(){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "onBackPressed");
		}
		setResult(Activity.RESULT_CANCELED);
		getFragmentManager().popBackStack();
		return false;
	}
	
	protected void finish(){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "finish");
		}
		getFragmentManager().popBackStack();
	}
}
