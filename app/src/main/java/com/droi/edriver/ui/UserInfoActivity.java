package com.droi.edriver.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.droi.account.shared.DroiAccount;
import com.droi.edriver.R;
import com.droi.edriver.bean.UserInfo;
import com.droi.edriver.net.NetMsgCode;
import com.droi.edriver.net.Request;
import com.droi.edriver.tools.Tools;
import com.droi.edriver.view.NumberPickerView;

import java.util.Calendar;

public class UserInfoActivity extends Activity {

	private static final int REQUEST_CODE_NICKNAME = 0x101;
	private static final int REQUEST_CODE_CAR = 0x102;
	private static final int MSG_FINISH = 0x103;

	private int year = Calendar.getInstance().get(Calendar.YEAR);

	private UserInfo mUserInfo;
	private UserInfo mUserInfoOld;

	private TextView tvNickName;
	private ImageView imgSexMale, imgSexFemale;
	private TextView tvAge;
	private TextView tvCar;
	private ProgressBar mProgressBar;
	private boolean isSaving;

	private Request mRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_info);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mRequest = new Request(handler);
		if (savedInstanceState == null) {
			mUserInfo = Tools.getUserinfo(DroiAccount.getInstance(this).getOpenId());
			mUserInfoOld = Tools.getUserinfo(DroiAccount.getInstance(this).getOpenId());
		} else {
			mUserInfo = (UserInfo) savedInstanceState.getSerializable("temp_data");
		}
		initView();
	}

	private void initView() {
		tvNickName = (TextView) findViewById(R.id.tv_nick_name);
		tvAge = (TextView) findViewById(R.id.tv_age);
		tvCar = (TextView) findViewById(R.id.tv_car);
		imgSexMale = (ImageView) findViewById(R.id.img_sex_male);
		imgSexFemale = (ImageView) findViewById(R.id.img_sex_female);

		tvNickName.setText(mUserInfo.getNickName());
		tvAge.setText(String.valueOf(year - Integer.parseInt(mUserInfo.getBirth().split("-")[0])));
		tvCar.setText(mUserInfo.getCar());
		updateSex();
	}

	private void updateSex() {
		if (mUserInfo.getSex() == 0) {
			imgSexMale.setImageResource(R.drawable.sex_male_on);
			imgSexFemale.setImageResource(R.drawable.sex_female_off);
		} else {
			imgSexMale.setImageResource(R.drawable.sex_male_off);
			imgSexFemale.setImageResource(R.drawable.sex_female_on);
		}
	}

	public void onClick(View v) {
		if (v.getId() != R.id.actionbar_back && v.getId() != R.id.btn_logout && isSaving) {
			Tools.makeToast(R.string.userinfo_hint_saving);
			return;
		}
		Intent intent = new Intent(UserInfoActivity.this, EditActivity.class);
		switch (v.getId()) {
			case R.id.actionbar_back:
				onBackPressed();
				break;
			case R.id.actionbar_save:
				updateUserinfo();
				break;
			case R.id.btn_nick_name:
				intent.putExtra(EditActivity.KEY_ACTIONBAR_TITLE, getString(R.string.userinfo_title_nickname));
				intent.putExtra(EditActivity.KEY_TEXT_LENGTH_LIMIT, getString(R.string.userinfo_hint_edit_limit, 5));
				intent.putExtra(EditActivity.KEY_TEXT_DEFAULT, mUserInfo.getNickName());
				intent.putExtra(EditActivity.KEY_TEXT_MAX_LENGTH, 5);
				startActivityForResult(intent, REQUEST_CODE_NICKNAME);
				break;
			case R.id.btn_age:
				new SelectAgeDialog().showDialog();
				break;
			case R.id.btn_car:
				intent.putExtra(EditActivity.KEY_ACTIONBAR_TITLE, getString(R.string.userinfo_title_car));
				intent.putExtra(EditActivity.KEY_TEXT_LENGTH_LIMIT, getString(R.string.userinfo_hint_edit_limit, 12));
				intent.putExtra(EditActivity.KEY_TEXT_DEFAULT, mUserInfo.getCar());
				intent.putExtra(EditActivity.KEY_TEXT_MAX_LENGTH, 12);
				startActivityForResult(intent, REQUEST_CODE_CAR);
				break;
			case R.id.img_sex_male:
				mUserInfo.setSex(0);
				updateSex();
				break;
			case R.id.img_sex_female:
				mUserInfo.setSex(1);
				updateSex();
				break;
			case R.id.btn_logout:
				showHintDialog();
				break;
			default:
				break;
		}
	}

	private void updateUserinfo() {
		if (TextUtils.isEmpty(mUserInfo.getNickName())) {
			Tools.makeToast(R.string.userinfo_hint_no_nickname);
		} else if (TextUtils.isEmpty(mUserInfo.getCar())) {
			Tools.makeToast(R.string.userinfo_hint_no_car);
		} else if (mUserInfo.equals(mUserInfoOld)) {
			Tools.makeToast(R.string.userinfo_hint_no_change);
			finish();
		} else {
			isSaving = true;
			mProgressBar.setVisibility(View.VISIBLE);
			mRequest.updateUserInfo(mUserInfo);
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case NetMsgCode.MSG_SUCCESS:
					if (msg.arg1 == NetMsgCode.UPDATE_USER_INFO) {
						isSaving = false;
						mProgressBar.setVisibility(View.GONE);
						Tools.setUserInfo(mUserInfo);
						Tools.makeToast(R.string.userinfo_save_success);
						setResult(RESULT_OK);
						finish();
					}
					break;
				case NetMsgCode.MSG_FAIL:
					if (msg.arg1 == NetMsgCode.UPDATE_USER_INFO) {
						isSaving = false;
						Tools.makeToast(R.string.userinfo_save_fail);
						mProgressBar.setVisibility(View.GONE);
					}
					break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case REQUEST_CODE_NICKNAME:
					mUserInfo.setNickName(data.getStringExtra(EditActivity.KEY_EDIT_VALUE));
					tvNickName.setText(mUserInfo.getNickName());
					break;
				case REQUEST_CODE_CAR:
					mUserInfo.setCar(data.getStringExtra(EditActivity.KEY_EDIT_VALUE));
					tvCar.setText(mUserInfo.getCar());
					break;
				default:
					break;
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("temp_data", mUserInfo);
	}

	@Override
	public void onBackPressed() {
		if (isSaving) {
			Tools.makeToast(R.string.userinfo_hint_saving);
			return;
		}
		super.onBackPressed();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_FINISH:
					finish();
					break;
				default:
					break;
			}
		}
	};

	private void showHintDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(UserInfoActivity.this);
		dialog.setTitle(R.string.string_reminder);
		dialog.setMessage(R.string.userinfo_delete_account_hint);
		dialog.setCancelable(false);
		dialog.setPositiveButton(R.string.string_cancel, null);
		dialog.setNegativeButton(R.string.string_go, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DroiAccount.getInstance(UserInfoActivity.this).tokenInvalidate();
				mHandler.sendEmptyMessageDelayed(MSG_FINISH, 1000);
			}
		});
		dialog.show();
	}

	private class SelectAgeDialog {

		private Dialog dialog;
		private NumberPickerView mPickerView;

		public SelectAgeDialog() {
			dialog = new Dialog(UserInfoActivity.this, R.style.SettingDialog);
			dialog.setContentView(R.layout.dialog_edit_userage);
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
			mPickerView = (NumberPickerView) dialog.findViewById(R.id.picker_birth);
			mPickerView.setNumberRange(1930, year);
			mPickerView.setInitNumberPicked(Integer.parseInt(mUserInfo.getBirth().split("-")[0]));
			mPickerView.setLabel(getString(R.string.userinfo_label_birth));
			mPickerView.setOnNumberPickedListener(new NumberPickerView.OnNumberPickedListener() {
				@Override
				public void onNumberPicked(int index, int number) {
					mUserInfo.setBirth(number + "-01-01");
					tvAge.setText(String.valueOf(year - Integer.parseInt(mUserInfo.getBirth().split("-")[0])));
				}
			});
			mPickerView.show();
		}

		public void showDialog() {
			dialog.show();
		}
	}
}
