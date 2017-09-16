package com.droi.edriver.ui;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.droi.edriver.R;
import com.droi.edriver.tools.Tools;
import com.droi.edriver.util.CameraUtil2;

import java.util.ArrayList;
import java.util.List;

public class DebugActivity extends Activity {

	private EditText etCarLeft;
	private EditText etCarRight;
	private EditText etCarTop;
	private EditText etLaneTop;
	private EditText etCarScale;
	private EditText etLaneScale;
	private EditText etCarMiniSize;
	private EditText etCarThreshold;
	private EditText etCarAlarmTime;
	private EditText etSpeed;

	private CheckBox cebSpeed;

	private TextView tvPrevieSize;
	private Spinner mSpinner;
	private ArrayList<String> list = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);
		initView();
		initSpinner();
		initCheckBox();
	}

	private void initView() {
		etCarLeft = (EditText) findViewById(R.id.adas_car_left);
		etCarRight = (EditText) findViewById(R.id.adas_car_right);
		etCarTop = (EditText) findViewById(R.id.adas_car_top);
		etLaneTop = (EditText) findViewById(R.id.adas_lane_top);
		etCarScale = (EditText) findViewById(R.id.adas_car_scale);
		etLaneScale = (EditText) findViewById(R.id.adas_lane_scale);
		etCarMiniSize = (EditText) findViewById(R.id.adas_mini_size);
		etCarThreshold = (EditText) findViewById(R.id.adas_car_threshold);
		etCarAlarmTime = (EditText) findViewById(R.id.adas_car_alarmtime);
		etSpeed = (EditText) findViewById(R.id.adas_speed);
		etCarLeft.setText(MainActivity.adasCarLeft + "");
		etCarRight.setText(MainActivity.adasCarRight + "");
		etCarTop.setText(MainActivity.adasCarTop + "");
		etLaneTop.setText(MainActivity.adasLaneTop + "");
		etCarScale.setText(MainActivity.adasCarScale + "");
		etLaneScale.setText(MainActivity.adasLaneScale + "");
		etCarMiniSize.setText(MainActivity.adasMinSize + "");
		etCarThreshold.setText(MainActivity.adasThreshold + "");
		etCarAlarmTime.setText(MainActivity.alarmTime + "");
		etSpeed.setText(String.valueOf(Math.round(MainActivity.speed * 3.6)));
		etSpeed.setVisibility(MainActivity.speedOpen ? View.VISIBLE : View.GONE);
	}

	private void initSpinner() {
		mSpinner = (Spinner) findViewById(R.id.previe_size);
		tvPrevieSize = (TextView) findViewById(R.id.tv_previe_size);
		String oldSize = MainActivity.previewWidth + "x" + MainActivity.previewHeight;
		tvPrevieSize.setText("当前预览分辨率：" + oldSize);
		Camera mCamera = Camera.open();
		List<Camera.Size> sizes = CameraUtil2.getSortParmameters(mCamera);
		mCamera.release();
		if (sizes == null) {
			return;
		}
		for (Camera.Size item : sizes) {
			list.add(item.width + "x" + item.height);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<>(DebugActivity.this, android.R.layout.simple_list_item_1, list);
		mSpinner.setAdapter(adapter);
		mSpinner.setSelection(list.indexOf(oldSize));
		mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				MainActivity.previewWidth = Integer.parseInt(list.get(position).split("x")[0]);
				MainActivity.previewHeight = Integer.parseInt(list.get(position).split("x")[1]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	private void initCheckBox() {
		cebSpeed = (CheckBox) findViewById(R.id.switch_speed);
		cebSpeed.setChecked(MainActivity.speedOpen);
		cebSpeed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					etSpeed.setVisibility(View.VISIBLE);
					MainActivity.speedOpen = true;
				} else {
					etSpeed.setVisibility(View.GONE);
					MainActivity.speed = 0;
					MainActivity.speedOpen = false;
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (!TextUtils.isEmpty(etCarLeft.getText())) {
			float value = Float.parseFloat(etCarLeft.getText().toString());
			MainActivity.adasCarLeft = Math.round(value * 10) / 10f;
		}
		if (!TextUtils.isEmpty(etCarRight.getText())) {
			float value = Float.parseFloat(etCarRight.getText().toString());
			MainActivity.adasCarRight = Math.round(value * 10) / 10f;
		}
		if (!TextUtils.isEmpty(etCarTop.getText())) {
			float value = Float.parseFloat(etCarTop.getText().toString());
			MainActivity.adasCarTop = Math.round(value * 10) / 10f;
		}
		if (!TextUtils.isEmpty(etLaneTop.getText())) {
			float value = Float.parseFloat(etLaneTop.getText().toString());
			MainActivity.adasLaneTop = Math.round(value * 10) / 10f;
		}
		if (!TextUtils.isEmpty(etCarScale.getText())) {
			float value = Float.parseFloat(etCarScale.getText().toString());
			MainActivity.adasCarScale = Math.round(value * 10) / 10f;
		}
		if (!TextUtils.isEmpty(etLaneScale.getText())) {
			float value = Float.parseFloat(etLaneScale.getText().toString());
			MainActivity.adasLaneScale = Math.round(value * 10) / 10f;
		}
		if (!TextUtils.isEmpty(etCarMiniSize.getText())) {
			MainActivity.adasMinSize = Integer.parseInt(etCarMiniSize.getText().toString());
		}
		if (!TextUtils.isEmpty(etCarThreshold.getText())) {
			MainActivity.adasThreshold = Integer.parseInt(etCarThreshold.getText().toString());
		}
		if (!TextUtils.isEmpty(etCarAlarmTime.getText())) {
			float value = Float.parseFloat(etCarAlarmTime.getText().toString());
			MainActivity.alarmTime = Math.round(value * 10) / 10f;
		}
		if (!TextUtils.isEmpty(etSpeed.getText()) && MainActivity.speedOpen) {
			MainActivity.speed = Integer.parseInt(etSpeed.getText().toString()) / 3.6f;
		}
		showLog("更新检测参数: ");
	}

	public static void showLog(String info) {
		Tools.outputLog("zhuqichao", info
				+ "\n模拟速度=" + MainActivity.speedOpen + ", " + String.valueOf(Math.round(MainActivity.speed * 3.6)) + "km/h"
				+ "\nCarLeft=" + MainActivity.adasCarLeft
				+ "\nCarRight=" + MainActivity.adasCarRight
				+ "\nCarTop=" + MainActivity.adasCarTop
				+ "\nLaneTop=" + MainActivity.adasLaneTop
				+ "\nCarScale=" + MainActivity.adasCarScale
				+ "\nLaneScale=" + MainActivity.adasLaneScale
				+ "\nCarMiniSize=" + MainActivity.adasMinSize
				+ "\nCarThreshold=" + MainActivity.adasThreshold
				+ "\nCarAlarmTime=" + MainActivity.alarmTime
				+ "\nPreviewSize=" + MainActivity.previewWidth + "x" + MainActivity.previewHeight);
	}
}
