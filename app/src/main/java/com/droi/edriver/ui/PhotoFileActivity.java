package com.droi.edriver.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.droi.edriver.R;
import com.droi.edriver.adapter.PhotoAdapter;
import com.droi.edriver.tools.Tools;

import java.io.File;
import java.util.ArrayList;

public class PhotoFileActivity extends Activity implements View.OnClickListener{
	private PhotoAdapter adapter;
	private ArrayList<String> photoList = new ArrayList<String>();
	private TextView operation;
	private ImageView imageSave,imageDelete;
	private boolean isChoice;
	private final String PHOTO_TYPE = ".jpg";
	private final String VIDEO_TYPE =".3gp";
	private int type;
	private GridView gridview;
	private File file;
	private final static int REQUEST_DETAIL = 0x1001;
	//private TextView lockText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thumb_activity);
		gridview = (GridView) findViewById(R.id.main_grid);
		operation  =(TextView)findViewById(R.id.option);
		operation.setOnClickListener(this);
		imageDelete = (ImageView)findViewById(R.id.delete);
		imageDelete.setOnClickListener(this);
		//lockText = (TextView)findViewById(R.id.lock);
		//lockText.setOnClickListener(this);
		Intent intent =getIntent();
		type = intent.getIntExtra("type", -1);
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (isChoice) {
					adapter.setShowChecked(isChoice, position);
				} else {
					if (type == Tools.FILE_VIDEO) {
						playVideo(position);
					} else {
						Intent intent = new Intent(PhotoFileActivity.this, PhotosDetialActivity.class);
						intent.putExtra("path", photoList.get(position));
						intent.putExtra("position", position);
						intent.putStringArrayListExtra("list", photoList);
						startActivityForResult(intent,REQUEST_DETAIL);
					}
				}
			}
		});

		file = new File(Tools.getPath());
		if(type==Tools.FILE_PHOTO){
			photoList = Tools.getAllFilePathByEnd(file, PHOTO_TYPE);
			adapter = new PhotoAdapter(PhotoFileActivity.this,photoList,gridview);
		}else if(type==Tools.FILE_VIDEO){
			photoList = Tools.getAllFilePathByEnd(file, VIDEO_TYPE);
			adapter = new PhotoAdapter(PhotoFileActivity.this,photoList,gridview,true);
		}
		gridview.setFocusable(true);
		gridview.setAdapter(adapter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==REQUEST_DETAIL&&resultCode==RESULT_OK){
			file = new File(Tools.getPath());
			photoList = Tools.getAllFilePathByEnd(file, PHOTO_TYPE);
			adapter.setAllFileList(photoList);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
	}

	//调用系统播放器播放视频
	private void playVideo(int position){
		Intent it = new Intent(Intent.ACTION_VIEW);
		it.setDataAndType(Uri.parse("file://" + photoList.get(position)), "video/3gp");
		startActivity(it);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.option:
				isChoice = !isChoice;
				updateActionbar();
				break;
			/*case R.id.lock:
				if(adapter.LockFile()){
					isChoice = false;
					setViewVisible(R.string.choice, View.INVISIBLE);
					updateActionbar();
				}else{
					isChoice = true;
					setViewVisible(R.string.cancle, View.VISIBLE);
				}
				
				break;*/
			case R.id.delete:
				if(adapter.getCheckedList().size()==0){
					Tools.makeToast(R.string.trip_del_failed);
					return;
				}
				showDeleteDialog();
				break;
			default:
				break;
		}
	}

	private void showDeleteDialog(){
		AlertDialog.Builder dialog = new AlertDialog.Builder(PhotoFileActivity.this);
		dialog.setTitle(R.string.dialog_title);
		dialog.setMessage(R.string.dialog_delete_msg);
		dialog.setPositiveButton(R.string.string_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (adapter.deleteCheckedFile()) {
					Tools.makeToast(R.string.trip_del_suc);
					SaveActivity.surplusSize.setText(Tools.getSurpusSize(Tools.getPath()));
					isChoice = false;
					updateActionbar();
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
	private void updateActionbar() {
		if(isChoice){
			setViewVisible(R.string.cancle, View.VISIBLE);
			SaveActivity.sgTabs.setVisibility(View.GONE);
			SaveActivity.tvTitle.setVisibility(View.VISIBLE);
			SaveActivity.tvBack.setVisibility(View.GONE);
			SaveActivity.surplusSize.setVisibility(View.GONE);
		}else{
			setViewVisible(R.string.choice, View.INVISIBLE);
			SaveActivity.sgTabs.setVisibility(View.VISIBLE);
			SaveActivity.tvTitle.setVisibility(View.GONE);
			SaveActivity.tvBack.setVisibility(View.VISIBLE);
			SaveActivity.surplusSize.setVisibility(View.VISIBLE);
			adapter.setCheckedListEmpty();
		}
	}

	private void setViewVisible(int cancle, int visible) {
		operation.setText(cancle);
		imageDelete.setVisibility(visible);
		//lockText.setVisibility(visible);
	}
}
