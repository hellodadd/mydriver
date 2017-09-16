package com.droi.edriver.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.droi.edriver.R;
import com.droi.edriver.tools.Tools;

/**
 * Created by pc0001 on 2016/2/24.
 */
public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		TextView tvTitle = (TextView) findViewById(R.id.actionbar_title);
		tvTitle.setText(R.string.actionbar_title_about);
		TextView tvVersion = (TextView) findViewById(R.id.tv_version);
		tvVersion.setText("V" + Tools.getVersionName());
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.actionbar_back:
				finish();
				break;
			case R.id.tv_phone:
			case R.id.tv_fax:
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:02134768300"));
				startActivity(intent);
				break;
			default:
				break;
		}
	}
}
