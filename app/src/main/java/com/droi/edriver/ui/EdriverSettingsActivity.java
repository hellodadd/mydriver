package com.droi.edriver.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.droi.edriver.R;
import com.droi.edriver.tools.FileSizeUtil;
import com.droi.edriver.tools.Settings;
import com.droi.edriver.tools.Tools;
import com.droi.edriver.view.SegmentedGroup;

public class EdriverSettingsActivity extends Activity {

	private TextView tvTitle;
	private CheckBox cebWaterMark;
	private CheckBox cebAutoScreen;
	private SegmentedGroup sgMaxStorage;
	private SegmentedGroup sgClarityLevel;
	private SegmentedGroup sgVideoTime;

	private RadioButton[] rdStorages = new RadioButton[4];
	private RadioButton[] rdClaritys = new RadioButton[3];
	private RadioButton[] rdTimes = new RadioButton[2];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edriver_settings);
		tvTitle = (TextView) findViewById(R.id.actionbar_title);
		tvTitle.setText(R.string.actionbar_title_settings2);
		initView();
		setData();
	}

	private void initView() {
		/*cebWaterMark = (CheckBox) findViewById(R.id.switch_add_watermark);
		cebWaterMark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Settings.setWaterMark(isChecked);
			}
		});*/
		cebAutoScreen = (CheckBox) findViewById(R.id.switch_auto_screen);
		cebAutoScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Settings.setAutoScreen(isChecked);
			}
		});
		sgMaxStorage = (SegmentedGroup) findViewById(R.id.segmented_max_storage);
		sgMaxStorage.setOnCheckedChangeListener(onCheckedChangeListener);
		sgClarityLevel = (SegmentedGroup) findViewById(R.id.segmented_clarity);
		sgClarityLevel.setOnCheckedChangeListener(onCheckedChangeListener);
		sgVideoTime = (SegmentedGroup) findViewById(R.id.segmented_video_time);
		sgVideoTime.setOnCheckedChangeListener(onCheckedChangeListener);

		rdStorages[0] = (RadioButton) findViewById(R.id.radio_max_storage0);
		rdStorages[1] = (RadioButton) findViewById(R.id.radio_max_storage1);
		rdStorages[2] = (RadioButton) findViewById(R.id.radio_max_storage2);
		rdStorages[3] = (RadioButton) findViewById(R.id.radio_max_storage3);

		rdClaritys[0] = (RadioButton) findViewById(R.id.radio_clarity0);
		rdClaritys[1] = (RadioButton) findViewById(R.id.radio_clarity1);
		rdClaritys[2] = (RadioButton) findViewById(R.id.radio_clarity2);

		rdTimes[0] = (RadioButton) findViewById(R.id.radio_video_time0);
		rdTimes[1] = (RadioButton) findViewById(R.id.radio_video_time1);
	}

	private void setData() {
		rdClaritys[Settings.getClarityLevel()].setChecked(true);
		rdTimes[Settings.getVideoTime()].setChecked(true);
		//cebWaterMark.setChecked(Settings.getWaterMark());
		cebAutoScreen.setChecked(Settings.getAutoScreen());

		float used = (float) FileSizeUtil.getFileOrFilesSize(Tools.getPath(), 3);
		float available = Tools.readSDCardSize() / 1024 + used / 1024;
		int position = 0;
		if (available <= 2) {
			position = 1;
		} else if (available <= 4) {
			position = 2;
		} else if (available <= 8) {
			position = 3;
		} else {
			position = 4;
		}
		if (Settings.getMaxStorage() >= position) {
			Settings.setMaxStorage(position - 1);
		}
		rdStorages[Settings.getMaxStorage()].setChecked(true);
		for (; position < 4; position++) {
			rdStorages[position].setEnabled(false);
			rdStorages[position].setTextColor(Color.GRAY);
		}
		for (int i = 0; i < rdStorages.length; i++) {
			if (1024 * (int) Math.pow(2, i) - used <= 250) {
				rdStorages[i].setEnabled(false);
				rdStorages[i].setTextColor(Color.GRAY);
			}
		}
		//judgeStorage();
	}

	private void showHintDialog(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(EdriverSettingsActivity.this);
		dialog.setTitle(R.string.string_reminder);
		dialog.setMessage(msg);
		dialog.setCancelable(false);
		dialog.setNegativeButton(R.string.string_ok, null);
		dialog.show();
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

	private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
				case R.id.radio_max_storage0:
					Settings.setMaxStorage(0);
					judgeStorage();
					break;
				case R.id.radio_max_storage1:
					Settings.setMaxStorage(1);
					judgeStorage();
					break;
				case R.id.radio_max_storage2:
					Settings.setMaxStorage(2);
					judgeStorage();
					break;
				case R.id.radio_max_storage3:
					Settings.setMaxStorage(3);
					judgeStorage();
					break;
				case R.id.radio_clarity0:
					Settings.setClarityLevel(0);
					break;
				case R.id.radio_clarity1:
					Settings.setClarityLevel(1);
					break;
				case R.id.radio_clarity2:
					Settings.setClarityLevel(2);
					break;
				case R.id.radio_video_time0:
					Settings.setVideoTime(0);
					break;
				case R.id.radio_video_time1:
					Settings.setVideoTime(1);
					break;
				default:
					break;
			}
		}
	};

	private void judgeStorage() {
		float used = (float) FileSizeUtil.getFileOrFilesSize(Tools.getPath(), 3);
		if (Settings.getMaxStorageSize() - used <= 250) {
			showHintDialog(getString(R.string.string_storage_short));
		}
	}

}
