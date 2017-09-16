package com.droi.edriver.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.droi.edriver.R;
import com.droi.edriver.tools.Tools;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.List;

/**
 * Created by pc0001 on 2016/3/7.
 */
public class GalleryAdapter extends BaseAdapter {
	private List<String> list;
	private Context mContext;

	public GalleryAdapter(List<String> paths, Context context) {
		this.list = paths;
		mContext = context;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CacheView cacheView = null;
		String path = list.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.gallery_item, null);
			cacheView = new CacheView();
			cacheView.imgv_img = (ImageView) convertView.findViewById(R.id.gallery);
			convertView.setTag(cacheView);
		} else {
			cacheView = (CacheView) convertView.getTag();
		}
		cacheView.imgv_img.setImageResource(R.drawable.default_thumb_pic);
		Uri uri = Uri.fromFile(new File(path));
		ImageLoader.getInstance().displayImage(uri+"", cacheView.imgv_img, Tools.getImageOptions());
		/*if (bitmap != null) {
			cacheView.imgv_img.setImageBitmap(bitmap);
		}*/
		return convertView;
	}

	private class CacheView {
		private ImageView imgv_img;
	}
}
