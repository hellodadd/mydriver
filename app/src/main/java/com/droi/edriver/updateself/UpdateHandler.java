package com.droi.edriver.updateself;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.droi.edriver.R;
import com.droi.edriver.tools.Tools;

import java.io.File;
import java.util.HashMap;

public class UpdateHandler extends Handler {
	public static final int MSG_UPDATE_START = 1000;
	public static final int MSG_UPDATE_VIEW = MSG_UPDATE_START + 1;

	private Context mCtx = null;

	private boolean isHand = false;

	public UpdateHandler(Context ctx) {
		super();
		mCtx = ctx;
	}

	public UpdateHandler(Context ctx, boolean isHand) {
		super();
		mCtx = ctx;
		this.isHand = isHand;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MSG_UPDATE_START:
				break;
			case MSG_UPDATE_VIEW:
				// get hashmap form message
				HashMap<String, Object> map = (HashMap<String, Object>) msg.obj;
				if (map == null) {
					if (isHand) {
						Tools.makeToast(R.string.islasted_version);
					}
					return;
				}

				// check policy
				int policy = (Integer) map.get("policy");
				String content = (String) map.get("content");
				String url = (String) map.get("fileUrl");
				String version = (String) map.get("ver");
				final String data = url + "&" + version;
				Log.i("zhuqichao", "data = " + data);
				/**
				 * policy : 1:强制更新 2:提示更新 3:不更新 4:后台更新 we only use 1 here
				 */
				if (policy == 3 || policy == 0) {
					if (isHand) {
						Tools.makeToast(mCtx.getString(R.string.islasted_version));
					}
					return;
				} else {
					/**
					 * 如果有更新.跳转到下载模块去处理.
					 */
					Log.i("zhuqichao", "need update policy =" + policy + " ||content = " + content + " ||");

					/**
					 * CHECK
					 */
					final String sdcardPath = Util_update.FileManage.getSDPath();
					Log.i("zhuqichao", "sdcardPath  = " + sdcardPath);

					if (sdcardPath == null) {
						Tools.makeToast(R.string.please_insert_sd_card);
						return;
					}
					Util_update.FileManage.FileHolder fileHolder = Util_update.FileManage.readSDCardSpace();
					if (fileHolder != null && fileHolder.availSpace < 1024 * 10) {
						Tools.makeToast(R.string.space_not_enough);
						return;
					}

					/**
					 * 检查是否有新版本已经下载过了.上次没安装而已.
					 */
					String path = Util_update.FileManage.getSDPath() + Constants.DownloadApkPath + mCtx.getString(R.string.app_name) + ".apk";
					File f = new File(path);
					if (f.exists()) {
						// check version code
						// try {
						// PackageInfo pinfo =
						// mCtx.getPackageManager().getPackageInfo(mCtx.getPackageName(),
						// PackageManager.GET_CONFIGURATIONS);
						// int v = pinfo.versionCode;
						// if (v < Integer.parseInt(version)) {
						// showAlertDialog(policy, content, path, data, true);
						// }
						// } catch (NameNotFoundException e) {
						// e.printStackTrace();
						// }
						f.delete();
					}
					try {
						showAlertDialog(policy, content, sdcardPath, data, false);
					} catch (Exception e) {
					}
				}
				break;
			default:
				break;
		}
	}

	private void showAlertDialog(int policy, String content, final String sdcardPath, final String data, final boolean exsit) {
		/**
		 * show alert dialog
		 */
		int yes_id = 0;
		int no_id = 0;
		if (exsit) {
			yes_id = R.string.alert_btn_install;
			no_id = R.string.alert_btn_no_install;
		} else {
			yes_id = R.string.alert_btn_yes;
			no_id = R.string.alert_btn_no;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
		builder.setTitle(R.string.alert_title);
		if (TextUtils.isEmpty(content)) {
			builder.setMessage(R.string.alert_msg);
		} else {
			builder.setMessage(content);
		}

		final Intent downloadServiceIntent = new Intent(mCtx, DownloadService.class);
		downloadServiceIntent.putExtra("sdPath", sdcardPath);
		downloadServiceIntent.putExtra("url", data);
		downloadServiceIntent.putExtra("appName", mCtx.getString(R.string.app_name));

		if (policy == 1) {
			// 强制更新
			builder.setMessage(content + "\n" + mCtx.getString(R.string.must_update));
			builder.setPositiveButton(yes_id, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (exsit) {
						Util_update.AppInfoManager.AppInstall(sdcardPath, mCtx);
					} else {
						Log.i("zhuqichao", "sdPath:" + sdcardPath + " url:" + data + " appName:" + mCtx.getString(R.string.app_name));
						ComponentName name = mCtx.startService(downloadServiceIntent);
						Log.i("zhuqichao", "start download thread end name=" + name.getClassName());
						SelfUpdateMain.isDownloading = true;
					}
				}
			});
			builder.setNegativeButton(no_id, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//showExitDialog();
					((Activity) mCtx).finish();
				}
			});
			builder.setCancelable(false);
			builder.show();
		} else if (policy == 2) {
			builder.setPositiveButton(yes_id, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (exsit) {
						Util_update.AppInfoManager.AppInstall(sdcardPath, mCtx);
					} else {
						ComponentName name = mCtx.startService(downloadServiceIntent);
						SelfUpdateMain.isDownloading = true;
					}
				}
			});
			builder.setNegativeButton(no_id, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//showExitDialog();
				}
			});
			builder.setCancelable(false);
			builder.show();
		} else if (policy == 4) {
			ComponentName name = mCtx.startService(downloadServiceIntent);
			Log.i("zhuqichao", "start download thread end name=" + name.getClassName());
			SelfUpdateMain.isDownloading = true;
		}
	}

	private void showExitDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
		builder.setTitle(R.string.alert_title);
		builder.setMessage(R.string.must_update);
		builder.setPositiveButton(R.string.string_ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((Activity) mCtx).finish();
			}
		});
		builder.setCancelable(false);
		builder.show();
	}
}
