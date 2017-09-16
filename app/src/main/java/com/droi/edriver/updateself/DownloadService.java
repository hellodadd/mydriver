package com.droi.edriver.updateself;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.droi.edriver.R;
import com.droi.edriver.tools.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DownloadService extends Service {
	public static final String Tag = "zhuqichao";

	// notification
	private static NotificationManager NCmanager;
	private NotificationCompat.Builder builder;

	// DownloadService instance
	public static DownloadService downloadServiceInstance;

	// 存放各个下载器
	public static Map<String, Downloader> downloaders = new HashMap<String, Downloader>();

	private String sdPathString;
	private String mApkPath;

	@Override
	public void onCreate() {
		Log.e(Tag, "download service onCreate");
		NCmanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		downloadServiceInstance = this;
		builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.plug_download_m_icon);
		builder.setShowWhen(true);

		// initiate download folder
		sdPathString = Util_update.FileManage.getSDPath();
		if (null != sdPathString) {
			Util_update.FileManage.newFolder(sdPathString + Constants.DownloadPath);
		}
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(Tag, "**************start download service onStartCommand");

		if (intent == null) {
			return super.onStartCommand(intent, flags, startId);
		}

		String dataString = intent.getStringExtra("url");
		String[] dataStrings = dataString.split("&");
		String urlstr = dataStrings[0];

		String appName = intent.getStringExtra("appName");
		byte[] bitmap = intent.getByteArrayExtra("bitmap");
		String version = intent.getStringExtra("version");

		Log.e("version", "version + service " + version);
		String size = intent.getStringExtra("size");
		String mTempPath = sdPathString + Constants.DownloadPath + appName + ".tmp";
		mApkPath = sdPathString + Constants.DownloadApkPath + appName + ".apk";

		String folder = sdPathString + Constants.DownloadPath;
		File f = new File(folder);
		if (!f.exists()) {
			f.mkdir();
		}

		/**
		 * delete the temporary file if it existed
		 */
		File tmpFile = new File(mTempPath);
		if (tmpFile.exists()) {
			tmpFile.delete();
		}

		int notifaction_flag = (int) System.currentTimeMillis();
		Downloader downloader = new Downloader(this, urlstr, mTempPath, mHandler, notifaction_flag, appName, bitmap, size, version);
		Log.i(Tag, "urlstr:" + urlstr + " mTempPath:" + mTempPath + " notifaction_flag:" + notifaction_flag + " appName:" + appName
				+ " size:" + size + " version:" + version);
		downloaders.put(urlstr, downloader);

		Log.e(Tag, "-----init");
		new DownloaderThread(downloader, this).start();

		Log.e(Tag, "**************end download service onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		SelfUpdateMain.isDownloading = false;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 2:
					String split_string = (String) msg.obj;
					String temp_str[] = split_string.split(",");
					String url1 = temp_str[0];
					long Surplus_size = Long.parseLong(temp_str[1]);
					if (null == downloaders.get(url1)) {
						return;
					}

					int notifaction_flag = msg.arg1;
					int rate = msg.arg2;

					builder.setContentTitle(getString(R.string.update_downloading));
					builder.setContentText(getString(R.string.Surplus) + humanReadableByteCount(Surplus_size, true));
					builder.setTicker(getString(R.string.has_new_download));
					builder.setOngoing(true);
					NCmanager.notify(notifaction_flag, builder.build());
					if (rate >= 100) {
						NCmanager.cancel(notifaction_flag);
						SelfUpdateMain.isDownloading = false;
					}
					break;
				case 3:
					String url = (String) msg.obj;
					String localPath = downloaders.get(url).getLocalfile();
					String mappName = downloaders.get(url).getAppName();
					downloaders.remove(url);
					if (null != downloaders && downloaders.size() == 0) {
						stopSelf();
					}

					/**
					 * copy temporary file to download folder and deleted
					 */
					copyFile(localPath, mApkPath);
					/**
					 * delete old path
					 */
					File old = new File(localPath);
					if (old.exists()) {
						old.delete();
					}
					Intent i = new Intent();
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.setAction(Intent.ACTION_VIEW);
					i.setDataAndType(Uri.fromFile(new File(mApkPath)), "application/vnd.android.package-archive");
					PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), i, PendingIntent.FLAG_ONE_SHOT);
					builder.setContentTitle(mappName);
					builder.setContentText(getString(R.string.download_notification));
					builder.setTicker(getString(R.string.download_complete));
					builder.setAutoCancel(true);
					builder.setOngoing(false);
					builder.setContentIntent(contentIntent);
					NCmanager.notify(1, builder.build());
					Util_update.AppInfoManager.AppInstall(mApkPath, DownloadService.this);
					break;
				case 4:
					// 提示用户出错
					Tools.makeToast(getResources().getString(R.string.canot_getsize));
					break;
				default:
					break;
			}
		}
	};

	/**
	 * 复制单个文件
	 *
	 * @param oldPath String 原文件路径 如：c:/fqf.txt
	 * @param newPath String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldPath); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format(Locale.CHINA, "%.2f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
