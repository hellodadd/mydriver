package com.droi.edriver.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.droi.edriver.R;
import com.droi.edriver.adapter.GalleryAdapter;
import com.droi.edriver.tools.Tools;
import com.droi.edriver.view.DetialGallery;

import java.util.List;

public class PhotosDetialActivity extends Activity implements View.OnClickListener {
	ImageView imageDel;
	TextView lock;
	String path;
	boolean isLock;
	DetialGallery  mGallery;
	private List<String> photoList;
	private int position;
	private GalleryAdapter adapter;
	private int currentPosition = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photos_detail);
		path = getIntent().getStringExtra("path");
		photoList = getIntent().getStringArrayListExtra("list");
		position = getIntent().getIntExtra("position", -1);
		mGallery = (DetialGallery)findViewById(R.id.photo);
		//mGallery.setCallbackDuringFling(false);

		TextView tvTitle = (TextView) findViewById(R.id.actionbar_title);
		tvTitle.setText(R.string.actionbar_title_photo);

		mGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				currentPosition = position;
				path = photoList.get(position);
				isLock = Tools.getFileLockState(PhotosDetialActivity.this, path);
				if (isLock) {
					lock.setText(R.string.unlocked);
				} else {
					lock.setText(R.string.locked);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Tools.makeToast(R.string.pic_all_delete);
				setResult(RESULT_OK);
				finish();
			}
		});
		adapter = new GalleryAdapter(photoList,PhotosDetialActivity.this);
		mGallery.setAdapter(adapter);
		mGallery.setSelection(position);
		imageDel  = (ImageView)findViewById(R.id.delete_one);
		imageDel.setOnClickListener(this);
		lock = (TextView)findViewById(R.id.lock_one);
		lock.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.actionbar_back:
				setResult(RESULT_OK);
				finish();
				break;
			case R.id.delete_one:
				if(photoList.size()==0){
					Tools.makeToast(R.string.file_no_exist);
					return ;
				}
				showDeleteDialog();
				break;
			case R.id.lock_one:
				if(photoList.size()==0){
					Tools.makeToast(R.string.file_no_exist);
					return ;
				}
				if(isLock){
					Tools.setFileLockState(PhotosDetialActivity.this, path, false);
					lock.setText(R.string.locked);
					isLock  =false;
				}else{
					Tools.setFileLockState(PhotosDetialActivity.this, path, true);
					lock.setText(R.string.unlocked);
					isLock  =true;
				}
				break;
		}
	}

	private void showDeleteDialog(){
		AlertDialog.Builder dialog = new AlertDialog.Builder(PhotosDetialActivity.this);
		dialog.setTitle(R.string.dialog_title);
		dialog.setMessage(R.string.dialog_delete_current_msg);
		dialog.setPositiveButton(R.string.string_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (Tools.deleteFile(photoList.get(currentPosition))) {
					photoList.remove(photoList.get(currentPosition));
					adapter.notifyDataSetChanged();
					Tools.makeToast(R.string.trip_del_suc);
				} else {
					Tools.makeToast(R.string.trip_del_failed);
				}
			}
		});
		dialog.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}
		);
		dialog.show();
	}

	@Override
	protected void onDestroy() {
		System.gc();
		super.onDestroy();
	}
}
