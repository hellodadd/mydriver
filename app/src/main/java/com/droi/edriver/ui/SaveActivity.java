package com.droi.edriver.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.droi.edriver.R;
import com.droi.edriver.tools.Tools;
import com.droi.edriver.view.SegmentedGroup;

public class SaveActivity extends TabActivity {
	private TabHost mHost;

	public static SegmentedGroup sgTabs;
	public static TextView tvBack;
	public static TextView tvTitle;
	public static TextView surplusSize;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save);
		initView();
		initTabHost();
	}

	private void initView() {
		surplusSize = (TextView) findViewById(R.id.surplus_size);
		sgTabs = (SegmentedGroup) findViewById(R.id.segmented_tabs);
		tvBack = (TextView) findViewById(R.id.actionbar_back);
		tvTitle = (TextView) findViewById(R.id.actionbar_title);
		RadioButton rdTab0 = (RadioButton) findViewById(R.id.radio_tab0);
		rdTab0.setChecked(true);
		
		sgTabs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.radio_tab0:
						mHost.setCurrentTab(0);
						break;
					case R.id.radio_tab1:
						mHost.setCurrentTab(1);
						break;
					default:
						break;
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		surplusSize.setText(Tools.getSurpusSize(Tools.getPath()));
	}

	private void initTabHost() {
		mHost = getTabHost();
		Intent photoIntent = new Intent(SaveActivity.this, PhotoFileActivity.class);
		photoIntent.putExtra("type", Tools.FILE_PHOTO);
		Intent videoIntent = new Intent(SaveActivity.this, PhotoFileActivity.class);
		videoIntent.putExtra("type", Tools.FILE_VIDEO);
		mHost.addTab(buildTagSpec("video", R.string.video, videoIntent));
		mHost.addTab(buildTagSpec("photo", R.string.photo, photoIntent));
		mHost.setCurrentTab(0);
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.actionbar_back:
				finish();
				break;
			default:
				break;
		}
	}

	/**
	 * 自定义创建标签项的方法
	 *
	 * @param tagName  标签标识
	 * @param tagLable 标签文字
	 * @param content  标签对应的内容
	 * @return
	 */
	private TabHost.TabSpec buildTagSpec(String tagName, int tagLable, Intent content) {
		return mHost.newTabSpec(tagName).setIndicator(getResources().getString(tagLable)).setContent(content);
	}
}