package com.droi.edriver.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droi.edriver.R;
import com.droi.edriver.tools.Settings;
import com.droi.edriver.tools.Tools;

public class SettingsActivity extends Activity {

	private TextView tvTitle;
	private CheckBox cebAssistanceMode;
	private CheckBox cebLaneRecognition;
	private CheckBox cebLaneWarning;
	private CheckBox cebCollisionWarn;
	private TextView tvMinmumSpeed;
	private TextView tvWarnInterval;

	private View layoutAssistance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		tvTitle = (TextView) findViewById(R.id.actionbar_title);
		tvTitle.setText(R.string.actionbar_title_settings);
		initView();
		setData();
	}

	private void initView() {
		cebAssistanceMode = (CheckBox) findViewById(R.id.switch_assistance);
		cebAssistanceMode.setOnCheckedChangeListener(onCheckedChangeListener);
		cebLaneRecognition = (CheckBox) findViewById(R.id.switch_lane_recognition);
		cebLaneRecognition.setOnCheckedChangeListener(onCheckedChangeListener);
		cebLaneWarning = (CheckBox) findViewById(R.id.switch_lane_warning);
		cebLaneWarning.setOnCheckedChangeListener(onCheckedChangeListener);
		cebCollisionWarn = (CheckBox) findViewById(R.id.switch_collision_warning);
		cebCollisionWarn.setOnCheckedChangeListener(onCheckedChangeListener);
		tvMinmumSpeed = (TextView) findViewById(R.id.tv_minimum_speed);
		tvWarnInterval = (TextView) findViewById(R.id.tv_warn_interval);

		layoutAssistance = findViewById(R.id.layout_assistance);
	}

	private void setData() {
		cebAssistanceMode.setChecked(Settings.getAssistanceMode());
		cebLaneRecognition.setChecked(Settings.getLaneRecognition());
		cebLaneWarning.setChecked(Settings.getLaneWarning());
		cebCollisionWarn.setChecked(Settings.getCollisionWarn());
		tvMinmumSpeed.setText(Settings.getMinimumSpeed() + "km/h");
		tvWarnInterval.setText(Settings.getWarnInterval() + "s");
		layoutAssistance.setVisibility(Settings.getAssistanceMode() ? View.VISIBLE : View.GONE);
	}

	private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			switch (buttonView.getId()) {
				case R.id.switch_assistance:
					Settings.setAssistanceMode(isChecked);
					layoutAssistance.setVisibility(isChecked ? View.VISIBLE : View.GONE);
					break;
				case R.id.switch_lane_recognition:
					Settings.setLaneRecognition(isChecked);
					if (!isChecked) {
						cebLaneWarning.setChecked(false);
					}
					break;
				case R.id.switch_lane_warning:
					if (isChecked && !Settings.getLaneRecognition()) {
						Tools.makeToast("请先打开车道识别开关！");
						cebLaneWarning.setChecked(false);
						return;
					}
					Settings.setLaneWarning(isChecked);
					break;
				case R.id.switch_collision_warning:
					Settings.setCollisionWarn(isChecked);
					break;
				default:
					break;
			}
		}
	};

	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
			case R.id.actionbar_back:
				finish();
				break;
			case R.id.button_edriver_setting:
				intent.setClass(SettingsActivity.this, EdriverSettingsActivity.class);
				startActivity(intent);
				break;
			case R.id.button_debug:
				intent.setClass(SettingsActivity.this, DebugActivity.class);
				startActivity(intent);
				break;
			case R.id.btn_min_speed:
				new MinSpeedDialog().showDialog();
				break;
			case R.id.btn_warn_interval:
				new WarningIntervalDialog().showDialog();
				break;
			default:
				break;
		}
	}

	private class MinSpeedDialog {

		private Dialog dialog;
		private RelativeLayout btnSpeedValue0;
		private RelativeLayout btnSpeedValue1;
		private RelativeLayout btnSpeedValue2;
		private RelativeLayout btnSpeedValue3;

		public MinSpeedDialog() {
			dialog = new Dialog(SettingsActivity.this, R.style.SettingDialog);
			dialog.setContentView(R.layout.dialog_min_speed);
			dialog.setCanceledOnTouchOutside(true);
			Window window = dialog.getWindow();
			window.setGravity(Gravity.BOTTOM); // 此处可以设置dialog显示的位置
			window.setWindowAnimations(R.style.DialogShowAnim);
			Display display = getWindowManager().getDefaultDisplay();
			WindowManager.LayoutParams lp = window.getAttributes();
			lp.width = display.getWidth(); // 设置宽度
			window.setAttributes(lp);
			initView();
		}

		private void initView() {
			btnSpeedValue0 = (RelativeLayout) dialog.findViewById(R.id.speed_value0);
			btnSpeedValue0.setOnClickListener(mOnClickListener);
			btnSpeedValue1 = (RelativeLayout) dialog.findViewById(R.id.speed_value1);
			btnSpeedValue1.setOnClickListener(mOnClickListener);
			btnSpeedValue2 = (RelativeLayout) dialog.findViewById(R.id.speed_value2);
			btnSpeedValue2.setOnClickListener(mOnClickListener);
			btnSpeedValue3 = (RelativeLayout) dialog.findViewById(R.id.speed_value3);
			btnSpeedValue3.setOnClickListener(mOnClickListener);
			dialog.findViewById(R.id.btn_cancel).setOnClickListener(mOnClickListener);
			switch (Settings.getMinimumSpeed()) {
				case 10:
					dialog.findViewById(R.id.img_value0).setVisibility(View.VISIBLE);
					break;
				case 30:
					dialog.findViewById(R.id.img_value1).setVisibility(View.VISIBLE);
					break;
				case 60:
					dialog.findViewById(R.id.img_value2).setVisibility(View.VISIBLE);
					break;
				case 90:
					dialog.findViewById(R.id.img_value3).setVisibility(View.VISIBLE);
					break;
				default:
					break;
			}
		}

		private OnClickListener mOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.speed_value0:
						Settings.setMinimumSpeed(10);
						break;
					case R.id.speed_value1:
						Settings.setMinimumSpeed(30);
						break;
					case R.id.speed_value2:
						Settings.setMinimumSpeed(60);
						break;
					case R.id.speed_value3:
						Settings.setMinimumSpeed(90);
						break;
					default:
						break;
				}
				tvMinmumSpeed.setText(Settings.getMinimumSpeed() + "km/h");
				dialog.dismiss();
			}
		};

		public void showDialog() {
			dialog.show();
		}
	}

	private class WarningIntervalDialog {

		private Dialog dialog;
		private RelativeLayout btnIntervalValue0;
		private RelativeLayout btnIntervalValue1;
		private RelativeLayout btnIntervalValue2;

		public WarningIntervalDialog() {
			dialog = new Dialog(SettingsActivity.this, R.style.SettingDialog);
			dialog.setContentView(R.layout.dialog_warning_interval);
			dialog.setCanceledOnTouchOutside(true);
			Window window = dialog.getWindow();
			window.setGravity(Gravity.BOTTOM); // 此处可以设置dialog显示的位置
			window.setWindowAnimations(R.style.DialogShowAnim);
			Display display = getWindowManager().getDefaultDisplay();
			WindowManager.LayoutParams lp = window.getAttributes();
			lp.width = display.getWidth(); // 设置宽度
			window.setAttributes(lp);
			initView();
		}

		private void initView() {
			btnIntervalValue0 = (RelativeLayout) dialog.findViewById(R.id.interval_value0);
			btnIntervalValue0.setOnClickListener(mOnClickListener);
			btnIntervalValue1 = (RelativeLayout) dialog.findViewById(R.id.interval_value1);
			btnIntervalValue1.setOnClickListener(mOnClickListener);
			btnIntervalValue2 = (RelativeLayout) dialog.findViewById(R.id.interval_value2);
			btnIntervalValue2.setOnClickListener(mOnClickListener);
			dialog.findViewById(R.id.btn_cancel).setOnClickListener(mOnClickListener);
			switch (Settings.getWarnInterval()) {
				case 3:
					dialog.findViewById(R.id.img_value0).setVisibility(View.VISIBLE);
					break;
				case 5:
					dialog.findViewById(R.id.img_value1).setVisibility(View.VISIBLE);
					break;
				case 10:
					dialog.findViewById(R.id.img_value2).setVisibility(View.VISIBLE);
					break;
				default:
					break;
			}
		}

		private OnClickListener mOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.interval_value0:
						Settings.setWarnInterval(3);
						break;
					case R.id.interval_value1:
						Settings.setWarnInterval(5);
						break;
					case R.id.interval_value2:
						Settings.setWarnInterval(10);
						break;
					case R.id.btn_cancel:
						break;
					default:
						break;
				}
				tvWarnInterval.setText(Settings.getWarnInterval() + "s");
				dialog.dismiss();
			}
		};

		public void showDialog() {
			dialog.show();
		}
	}


}
