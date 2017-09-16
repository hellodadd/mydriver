package com.droi.edriver.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.droi.edriver.R;
import com.droi.edriver.tools.Tools;
import com.droi.edriver.view.MyImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by pc0001 on 2016/2/29.
 */
public class PhotoAdapter extends BaseAdapter{
	private ArrayList<String> lists;
	private int viewWidth=120,viewHeight=120;
	private Context mContext;
	private GridView mGridview;
	private Integer checkedPositionIndex;

	private ArrayList<String> checkedList = new ArrayList<String>();
	private ArrayList<Integer> listPosition = new ArrayList<Integer>();//纪录选中的item的下标
	private boolean isVideo;
	public  PhotoAdapter(Context context,ArrayList<String>list,GridView gridView){
		this.mContext = context;
		this.lists = list;
		this.mGridview = gridView;
	}
	public  PhotoAdapter(Context context,ArrayList<String>list,GridView gridView,Boolean isVideo){
		this.mContext = context;
		this.lists = list;
		this.mGridview = gridView;
		this.isVideo = isVideo;
	}
	public void setShowChecked(Boolean isshow ,int position){
		if(isshow){
			checkedPositionIndex = position;
			if(listPosition.contains(checkedPositionIndex)){
				checkedList.remove(lists.get(checkedPositionIndex));
				listPosition.remove(checkedPositionIndex);
				notifyDataSetChanged();
				return;
			}
			checkedList.add(lists.get(checkedPositionIndex));
			listPosition.add(checkedPositionIndex);
		}
		notifyDataSetChanged();
	}
	
	public void setAllFileList(ArrayList<String> list ){
		this.lists = list;
		notifyDataSetChanged();
	}
	
	//删除选中的文件
	public boolean deleteCheckedFile() {
		for (int i = 0; i < checkedList.size(); i++) {
			File file = new File(checkedList.get(i));
			if (file.exists()) { // 判断文件是否存在
				file.delete(); // delete()方法
				lists.remove(checkedList.get(i));
			} 
		}
		setCheckedListEmpty();
		return true;
	}
	//将选中文件锁定
	public boolean LockFile(){
		if(checkedList.size()==0){
			Tools.makeToast(R.string.trip_save_failed);
			return false;
		}
		for(int i =0;i<checkedList.size();i++){
			Tools.setFileLockState(mContext,checkedList.get(i),true);
		}
		setCheckedListEmpty();
		return true;
	}
	//清空数据
	public void setCheckedListEmpty(){
		listPosition.clear();
		checkedList.clear();
		notifyDataSetChanged();
	}
	
	public ArrayList<String> getCheckedList(){
		return checkedList;
	}
	
	@Override
	public int getCount() {
		return lists.size();
	}
	@Override
	public Object getItem(int position) {
		return lists.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	ViewHolder viewHolder=null;
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final String path=lists.get(position);
		if(convertView==null){
			viewHolder=new ViewHolder();
			convertView= LayoutInflater.from(mContext).inflate(R.layout.grid_child_item, null);
			viewHolder.image=(MyImageView) convertView.findViewById(R.id.child_image);
			viewHolder.check=(ImageView) convertView.findViewById(R.id.child_checkbox);
			viewHolder.videoPlay = (ImageView)convertView.findViewById(R.id.video_play);
			viewHolder.lockFile = (ImageView)convertView.findViewById(R.id.lock_file);
			convertView.setTag(viewHolder);
			
		}else{
			viewHolder=(ViewHolder) convertView.getTag();
			viewHolder.image.setImageResource(R.drawable.default_thumb_pic);
		}
		viewHolder.image.setTag(path);
		if(isVideo){
			viewHolder.videoPlay.setVisibility(View.VISIBLE);
		}else{
			viewHolder.videoPlay.setVisibility(View.INVISIBLE);
		}
		if(Tools.getFileLockState(mContext, lists.get(position))){
			viewHolder.lockFile.setVisibility(View.VISIBLE);
		}else{
			viewHolder.lockFile.setVisibility(View.INVISIBLE);
		}
		Uri uri = Uri.fromFile(new File(path));
		ImageLoader.getInstance().displayImage(uri+"", viewHolder.image, Tools.getImageOptions());
		//判断此item是否应该显示选中图标
		if(listPosition.contains(position)){
			viewHolder.check.setVisibility(View.VISIBLE);
			//checkedList.add(path);
		}else{
			viewHolder.check.setVisibility(View.INVISIBLE);
			//checkedList.remove(path);
		}
		return convertView;

	}
	
	public  static class ViewHolder {
		public MyImageView image;
		public ImageView check;
		public ImageView videoPlay;
		public ImageView lockFile;
	}
}
