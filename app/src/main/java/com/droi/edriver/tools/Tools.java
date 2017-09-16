package com.droi.edriver.tools;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.droi.edriver.R;
import com.droi.edriver.app.WatchApplication;
import com.droi.edriver.bean.UserInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by ZhuQichao on 2016/2/19.
 */
public class Tools {

	public static final boolean OPEN_LOG = false;

	public static final int FILE_PHOTO = 1;
	public static final int FILE_VIDEO = 2;
	private static final String PATH = Environment.getExternalStorageDirectory().toString() + "/" + "EDriver" + "/";
	private static final String LOG_PATH = Environment.getExternalStorageDirectory().toString() + "/" + "EDriver.log";
	public static final String LOCK_SP = "lock_sp";
	public static final String SAVE_SP = "save_sp";
	private static final String SP_NAME_USERINFO = "sp_name_userinfo";
	private static final String KEY_NAME_NICKNAME = "key_name_nickname_";
	private static final String KEY_NAME_SEX = "key_name_sex_";
	private static final String KEY_NAME_BIRTH = "key_name_birth_";
	private static final String KEY_NAME_CARINFO = "key_name_carinfo_";

	public static String getPath() {
		File file = new File(PATH);
		if (!file.exists()) {
			//Log.i("zhuqichao", "file.mkdirs()=" + file.mkdirs());
			file.mkdirs();
		}
		return PATH;
	}

	public static int dip2px(float dipValue) {
		final float scale = WatchApplication.getInstance().getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static float px2dip(float pxValue) {
		final float scale = WatchApplication.getInstance().getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static void makeToast(String msg) {
		Toast.makeText(WatchApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();
	}

	public static void makeToast(int resid) {
		Toast.makeText(WatchApplication.getInstance(), resid, Toast.LENGTH_SHORT).show();
	}

	//判断是否有SD卡
	public static boolean hasSdcard() {
		String state = Environment.getExternalStorageState();
		if (state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	//设置文件存储路径
	public static boolean setSavePath() {
		if (hasSdcard()) {
			File eis = new File(PATH);
			try {
				if (!eis.exists()) {
					eis.mkdir();
				}
			} catch (Exception e) {

			}
		} else {
			makeToast(R.string.no_sdcard);
			return false;
		}
		return true;
	}

	/**
	 * 获取视频的缩略图
	 * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	 * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	 *
	 * @param videoPath 视频的路径
	 * @param width     指定输出视频缩略图的宽度
	 * @param height    指定输出视频缩略图的高度度
	 * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	 *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	 * @return 指定大小的视频缩略图
	 */
	public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图  
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	 * 获取文件地址列表
	 *
	 * @param file
	 * @return
	 */
	public static ArrayList<String> getAllFilePathByEnd(File file, String endwith) {
		ArrayList<String> list = new ArrayList<String>();

		File[] files = file.listFiles();
		for (File f : files) {
			if (f.getAbsoluteFile().toString().endsWith(endwith)) {
				list.add(f.getAbsolutePath());
			}
		}
		Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				if (lhs.compareTo(rhs) > 0) {
					return -1;
				}
				return 1;
			}
		});
		return list;
	}

	public static ArrayList<String> getAllFilePath(String path) {
		File file = new File(path);
		ArrayList<String> list = new ArrayList<String>();
		File[] files = file.listFiles();
		for (File f : files) {
			if (f.getAbsoluteFile().toString().endsWith(".jpg") || f.getAbsoluteFile().toString().endsWith(".3gp")) {
				list.add(f.getAbsolutePath());
			}
		}
		Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				if (lhs.compareTo(rhs) > 0) {
					return -1;
				}
				return 1;
			}
		});
		return list;
	}

	/**
	 * @param fromFile 被复制的文件
	 * @param toFile   复制的目录文件
	 * @param rewrite  是否重新创建文件
	 *                 <p/>
	 *                 <p>文件的复制操作方法
	 */
	public static void copyfile(File fromFile, File toFile, Boolean rewrite) {

		if (!fromFile.exists()) {
			return;
		}

		if (!fromFile.isFile()) {
			return;
		}
		if (!fromFile.canRead()) {
			return;
		}
		if (!toFile.getParentFile().exists()) {
			toFile.getParentFile().mkdirs();
		}
		if (toFile.exists() && rewrite) {
			toFile.delete();
		}

		try {
			FileInputStream fosfrom = new FileInputStream(fromFile);
			FileOutputStream fosto = new FileOutputStream(toFile);

			byte[] bt = new byte[1024];
			int c;
			while ((c = fosfrom.read(bt)) > 0) {
				fosto.write(bt, 0, c);
			}
			//关闭输入、输出流  
			fosfrom.close();
			fosto.close();


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block  
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block  
			e.printStackTrace();
		}

	}

	/**
	 * 获取文件夹剩余空间
	 *
	 * @param path 文件/文件夹路径
	 */
	public static String getSurpusSize(String path) {
		int totalSetting = 0;
		switch (Settings.getMaxStorage()) {
			case 0:
				totalSetting = 1;
				break;
			case 1:
				totalSetting = 2;
				break;
			case 2:
				totalSetting = 4;
				break;
			case 3:
				totalSetting = 8;
				break;
		}
		return FileSizeUtil.getAutoFileOrFilesSize(path, totalSetting);
	}

	/**
	 * 删除文件	 *
	 *
	 * @param path 文件路径
	 * @return 删除成功返回true
	 */
	public static boolean deleteFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			file.delete();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 锁定文件
	 *
	 * @param ctx
	 * @param path 文件路径，在此作为sp的key值
	 */
	public static void setFileLockState(Context ctx, String path, boolean islock) {
		SharedPreferences sp = ctx.getSharedPreferences(LOCK_SP, 0);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(path, islock);
		editor.commit();
	}

	/**
	 * 获取文件状态
	 *
	 * @param ctx
	 * @param path
	 * @return
	 */
	public static boolean getFileLockState(Context ctx, String path) {
		SharedPreferences sp = ctx.getSharedPreferences(LOCK_SP, 0);
		return sp.getBoolean(path, false);
	}

	/**
	 * 获取文件保存状态
	 *
	 * @param ctx
	 * @param path
	 * @return true 已保存，false 未保存
	 */
	public static boolean getFileSaveState(Context ctx, String path) {
		SharedPreferences sp = ctx.getSharedPreferences(SAVE_SP, 0);
		return sp.getBoolean(path, false);
	}

	/**
	 * 设置文件保存状态
	 *
	 * @param ctx
	 * @param path
	 * @param isSave
	 */
	public static void setFileSaveState(Context ctx, String path, boolean isSave) {
		SharedPreferences sp = ctx.getSharedPreferences(SAVE_SP, 0);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(path, isSave);
		editor.commit();
	}

	/**
	 * 获取手机SD卡剩余空间大小
	 *
	 * @return 单位 MB
	 */
	public static float readSDCardSize() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			long blockSize = sf.getBlockSize();
			long availCount = sf.getAvailableBlocks();
			return availCount * blockSize / 1024 / 1024;
		} else {
			return 0;
		}
	}

	/**
	 * 获取屏幕亮度(系统)
	 *
	 * @param context
	 * @return
	 */
	public static int getScreenBrightness(Context context) {
		int nowBrightnessValue = 0;
		ContentResolver resolver = context.getContentResolver();
		try {
			nowBrightnessValue = android.provider.Settings.System.getInt(
					resolver, android.provider.Settings.System.SCREEN_BRIGHTNESS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nowBrightnessValue;
	}

	/**
	 * 设置屏幕亮度（只设置activity）
	 *
	 * @param activity
	 * @param brrghtness
	 */
	public static void setBright(Activity activity, int brrghtness) {
		WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
		lp.screenBrightness = Float.valueOf(brrghtness) * (1f / 255f);
		activity.getWindow().setAttributes(lp);
	}

	public static UserInfo getUserinfo(String openid) {
		UserInfo value = new UserInfo();
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_USERINFO, 0);
		value.setNickName(sp.getString(KEY_NAME_NICKNAME + openid, ""));
		value.setSex(sp.getInt(KEY_NAME_SEX + openid, 0));
		value.setBirth(sp.getString(KEY_NAME_BIRTH + openid, "1992-01-01"));
		value.setCar(sp.getString(KEY_NAME_CARINFO + openid, ""));
		value.setOpenid(openid);
		return value;
	}

	public static boolean setUserInfo(UserInfo value) {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_USERINFO, 0);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(KEY_NAME_NICKNAME + value.getOpenid(), value.getNickName());
		editor.putInt(KEY_NAME_SEX + value.getOpenid(), value.getSex());
		editor.putString(KEY_NAME_BIRTH + value.getOpenid(), value.getBirth());
		editor.putString(KEY_NAME_CARINFO + value.getOpenid(), value.getCar());
		return editor.commit();
	}

	public static void outputLog(String tag, String log) {
		if (OPEN_LOG) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Log.i(tag, log);
			outLogToFile(LOG_PATH, dateFormat.format(Calendar.getInstance().getTime()) + ":" + tag + " :  " + log);
		}
	}

	private static void outLogToFile(String filePath, String loginfo) {
		try {
			FileWriter fw = new FileWriter(filePath, true);
			fw.write(loginfo + "\r\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String formatJson(String jsonStr) {
		if (null == jsonStr || "".equals(jsonStr)) return "";
		StringBuilder sb = new StringBuilder();
		char last = '\0';
		char current = '\0';
		int indent = 0;
		for (int i = 0; i < jsonStr.length(); i++) {
			last = current;
			current = jsonStr.charAt(i);
			switch (current) {
				case '{':
				case '[':
					sb.append(current);
					sb.append('\n');
					indent++;
					addIndentBlank(sb, indent);
					break;
				case '}':
				case ']':
					sb.append('\n');
					indent--;
					addIndentBlank(sb, indent);
					sb.append(current);
					break;
				case ',':
					sb.append(current);
					if (last != '\\') {
						sb.append('\n');
						addIndentBlank(sb, indent);
					}
					break;
				default:
					sb.append(current);
			}
		}

		return sb.toString();
	}

	private static void addIndentBlank(StringBuilder sb, int indent) {
		for (int i = 0; i < indent; i++) {
			sb.append('\t');
		}
	}

	public static DisplayImageOptions getImageOptions() {
		return new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.default_thumb_pic)
				.showImageOnLoading(R.drawable.default_thumb_pic)//默认图片
				.showImageOnFail(R.drawable.default_thumb_pic)//默认图片
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	// 版本名
	public static String getVersionName() {
		PackageInfo pi = getPackageInfo();
		if (pi != null) {
			return pi.versionName;
		} else {
			return null;
		}
	}

	// 版本号
	public static int getVersionCode() {
		PackageInfo pi = getPackageInfo();
		if (pi != null) {
			return pi.versionCode;
		} else {
			return 0;
		}
	}

	private static PackageInfo getPackageInfo() {
		PackageInfo pi = null;
		try {
			PackageManager pm = WatchApplication.getInstance().getPackageManager();
			pi = pm.getPackageInfo(WatchApplication.getInstance().getPackageName(), PackageManager.GET_CONFIGURATIONS);
			return pi;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pi;
	}
}
