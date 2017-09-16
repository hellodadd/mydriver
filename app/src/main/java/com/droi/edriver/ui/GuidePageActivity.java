package com.droi.edriver.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.droi.edriver.R;
import com.droi.edriver.view.CircleFlowIndicator;
import com.droi.edriver.view.ViewFlow;

public class GuidePageActivity extends Activity {
	/**
	 * Called when the activity is first created.
	 */
	private ViewFlow viewFlow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide_page);
		viewFlow = (ViewFlow) findViewById(R.id.viewflow);
		viewFlow.setAdapter(new ViewFlowAdapter());
		viewFlow.setmSideBuffer(guidePicID.length); //  实际图片张数，
		//CircleFlowIndicator indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
		//Resources res = getResources();
		//indic.setFillColor(res.getColor(R.color.progress_color));//设置滑动圆点的颜色
		//indic.setStrokeColor(res.getColor(R.color.settings_item_divider_color));//设置滑动圆点的颜色
		//viewFlow.setFlowIndicator(indic);
		viewFlow.setSelection(0);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	public int[] guidePicID = {R.drawable.guide1, R.drawable.guide2, R.drawable.guide3};
	//public int[] guidetTitleID = { R.string.guide_title1, R.string.guide_title2,R.string.guide_title2};
	//public int[] guideContentID = { R.string.guide_content1,R.string.guide_content2,R.string.guide_content2};

	class ViewFlowAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Button btn_start;
		private TextView guideTitle;
		private TextView guideContent;

		public ViewFlowAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return guidePicID.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.guide_page_item, null);
			}
			((ImageView) convertView.findViewById(R.id.imgView)).setImageResource(guidePicID[position % guidePicID.length]);
			guideTitle = (TextView) convertView.findViewById(R.id.guide_title);
			//guideTitle.setText(guidetTitleID[position % guidetTitleID.length]);
			guideContent = (TextView) convertView.findViewById(R.id.guide_content);
			//guideContent.setText(guideContentID[position % guidetTitleID.length]);
			if (position == guidePicID.length - 1) {
				guideTitle.setVisibility(View.INVISIBLE);
				guideContent.setVisibility(View.INVISIBLE);
				btn_start = (Button) convertView.findViewById(R.id.start);
				btn_start.setVisibility(View.VISIBLE);
				btn_start.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
//						Intent intent  = new Intent(GuidePageActivity.this,MainActivity.class);
//						startActivity(intent);
						GuidePageActivity.this.finish();
					}
				});
			}
			return convertView;
		}
	}
}
