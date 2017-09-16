package com.droi.edriver.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.droi.edriver.R;
import com.droi.edriver.tools.Tools;

public class EditActivity extends Activity {

	public static final String KEY_ACTIONBAR_TITLE = "key_actionbar_title";
	public static final String KEY_TEXT_LENGTH_LIMIT = "key_text_length_limit";
	public static final String KEY_TEXT_MAX_LENGTH = "key_text_max_length";
	public static final String KEY_TEXT_DEFAULT = "key_text_default";
	public static final String KEY_EDIT_VALUE = "key_edit_value";
	private EditText etValue;
	private TextView tvTitle;
	private int maxLenght;
	private String overHint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);

		etValue = (EditText) findViewById(R.id.et_edit_value);
		tvTitle = (TextView) findViewById(R.id.actionbar_title);

		maxLenght = getIntent().getIntExtra(KEY_TEXT_MAX_LENGTH, 5);
		overHint = getIntent().getStringExtra(KEY_TEXT_LENGTH_LIMIT);

		tvTitle.setText(getIntent().getStringExtra(KEY_ACTIONBAR_TITLE));
		etValue.setText(getIntent().getStringExtra(KEY_TEXT_DEFAULT));
		etValue.setHint(overHint);
		etValue.setSelection(etValue.getText().length());
		etValue.setFilters(new InputFilter[]{
				new InputFilter() {
					public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
						for (int i = start; i < end; i++) {
							char ch = source.charAt(i);
							if (!Character.isLetterOrDigit(ch) && ch != '-' && ch != '_') {
								return "";
							}
						}
						return null;
					}
				}});
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.actionbar_back:
				onBackPressed();
				break;
			default:
				break;
		}
	}

	@Override
	public void onBackPressed() {
		String value = etValue.getText().toString();
		if (value.length() > maxLenght) {
			Tools.makeToast(overHint);
		} else {
			setResult(RESULT_OK, new Intent().putExtra(KEY_EDIT_VALUE, value));
			super.onBackPressed();
		}
	}
}
