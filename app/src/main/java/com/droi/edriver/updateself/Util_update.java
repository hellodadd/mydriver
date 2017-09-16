package com.droi.edriver.updateself;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.droi.edriver.tools.Tools;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Util_update {
	public static final class TimeManager {
		final public static long UNIT_DAY = 24 * 60 * 60 * 1000;
		final public static long UNIT_HOUR = 60 * 60 * 1000;
		final public static long UNIT_MINUTE = 60 * 1000;
		final public static long UNIT_SECOND = 1 * 1000;
		final public static long UNIT_MILLISECOND = 1;

		public static long TimeConvert(long time, long unitFrom, long unitTo) {
			return time * unitFrom / unitTo;
		}

		public static long TimeConvertToMillisecond(long time, long unitFrom) {
			return time * unitFrom / UNIT_MILLISECOND;
		}

		// DATE
		public static long getTimeMillis(int hour, int minute) {
			return getTimeMillis(null, -1, -1, -1, hour, minute);
		}

		public static long getTimeMillis(Calendar c, int hour, int minute) {
			return getTimeMillis(c, -1, -1, -1, hour, minute);
		}

		public static long getTimeMillis(Calendar c, int year, int month, int day, int hour, int minute) {
			if (c == null) {
				c = Calendar.getInstance();
				c.setTimeInMillis(System.currentTimeMillis());
			}
			if (year >= 0) {
				c.set(Calendar.YEAR, year);
			}
			if (month >= 0) {
				c.set(Calendar.MONTH, month);
			}
			if (day >= 0) {
				c.set(Calendar.DAY_OF_MONTH, day);
			}
			if (hour >= 0) {
				c.set(Calendar.HOUR_OF_DAY, hour);
			}
			if (minute >= 0) {
				c.set(Calendar.MINUTE, minute);
			}

			c.set(Calendar.SECOND, 0);
			return c.getTimeInMillis();
		}

		// public static String getDateStr(long timemillis) {
		// String s = "";
		// long tmp = 0;
		// long tmpmillis = timemillis;
		// // date
		// tmp = tmpmillis / UNIT_DAY;
		// if (tmp != 0) {
		// s += (tmp + "Yetta");
		// }
		// tmpmillis = tmpmillis - tmp * UNIT_DAY;
		// tmp = tmpmillis / UNIT_HOUR;
		// if (tmpmillis > UNIT_SECOND && (tmp != 0 || !s.equals(""))) {
		// s += (tmp + "Yetta");
		// }
		// tmpmillis = tmpmillis - tmp * UNIT_HOUR;
		// tmp = tmpmillis / UNIT_MINUTE;
		// if (tmpmillis > UNIT_SECOND && (tmp != 0 || !s.equals(""))) {
		// s += (tmp + "Yetta");
		// }
		// tmpmillis = tmpmillis - tmp * UNIT_MINUTE;
		// tmp = tmpmillis / UNIT_SECOND;
		// if (tmpmillis > UNIT_SECOND && (tmp != 0 || !s.equals(""))) {
		// s += (tmp + "Yetta");
		// }
		// return s;
		// }

		public static String getDataFormat(long timeMillis) {
			if (timeMillis == 0)
				return "Unknown";
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date curDate = new Date(timeMillis);
			String str = formatter.format(curDate);
			return str;
		}
	}

	public static final class FileManage {
		Folder root;

		public Folder getRoot() {
			return root;
		}

		public void setRoot(Folder root) {
			this.root = root;
		}

		public void initRoot() {
			root = new Folder();
			root.name = "sdcard";
			root.path = getSDPath();
		}

		public static String getSDPath() {
			File sdDir = null;
			boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
			if (sdCardExist) {
				sdDir = Environment.getExternalStorageDirectory();
			} else {
				return null;
			}
			return sdDir.toString();
		}

		public static FileHolder readSDCardSpace() {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				File sdcardDir = Environment.getExternalStorageDirectory();
				StatFs sf = new StatFs(sdcardDir.getPath());
				long blockSize = sf.getBlockSize();
				long blockCount = sf.getBlockCount();
				long availCount = sf.getAvailableBlocks();
				return new FileHolder(blockSize * blockCount, blockSize * availCount);
			}
			return null;
		}

		void readSystem() {
			File root = Environment.getRootDirectory();
			StatFs sf = new StatFs(root.getPath());
			long blockSize = sf.getBlockSize();
			long blockCount = sf.getBlockCount();
			long availCount = sf.getAvailableBlocks();
		}

		public static int getFileSize(String path) {
			File f = new File(path);
			if (f.exists()) {
				return (int) f.length() / 1024;
			}
			return 0;
		}

		public void traversal(Folder root) {
			File dir = new File(root.path);
			File[] files = dir.listFiles();
			if (files == null) {
				return;
			} else {
				for (int i = 0; i < files.length; i++) {
					Folder root1 = new Folder();
					root1.path = files[i].getAbsolutePath();
					root1.name = files[i].getName();
					root1.prefolder = root;
					if (files[i].isDirectory()) {
						root1.isfolder = true;
						traversal(root1);
						root.list.add(root1);
					} else if (isMusicFile(root1.path)) {
						root1.isfolder = false;
						root1.size = this.getFileSize(root1.path);
						root.list.add(root1);
					}
				}
			}
		}

		public static Folder searchPath(Folder root, String path) {
			if (root.getPath().equals(path)) {
				return root;
			} else {
				ArrayList<Folder> folderlist = root.getList();
				for (int i = 0; i < folderlist.size(); i++) {
					Folder f = searchPath(folderlist.get(i), path);
					if (f != null) {
						break;
					}
				}
			}
			return null;
		}

		public static class FileHolder {
			public FileHolder(long totalSpace, long availSpace) {
				this.totalSpace = totalSpace;
				this.availSpace = availSpace;
			}

			public long totalSpace;
			public long availSpace;
		}

		public static void newPathFolder(String folderPath) {
			int start1 = 0;
			int start2 = folderPath.indexOf("/");

			while (start2 > -1) {
				String s = folderPath.substring(0, start2);
				newFolder(s);
				start1 = start2;
				start2 = folderPath.indexOf("/", start1 + 1);
			}
			if (start1 < folderPath.length() - 1) {
				newFolder(folderPath);
			}
		}

		static public void newFolder(String folderPath) {
			if (folderPath == null || folderPath.equals("")) {
				return;
			}
			try {
				String filePath = folderPath;
				File myFilePath = new File(filePath);
				if (!myFilePath.exists()) {
					boolean flag = myFilePath.mkdirs();
					if (flag) {
					} else {
					}
				} else {
					Log.v("mytag", "folderPath is exists " + folderPath);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		static public boolean delFolder(String path) {
			delAllFileInFolder(path);
			return delFile(path);
		}

		static public boolean delFile(String path) {
			File delPath = new File(path);
			if (delPath.isFile()) {
				return delPath.delete();
			} else {
				return false;
			}
		}

		static public void delAllFileInFolder(String path) {
			File file = new File(path);
			if (!file.exists()) {
				return;
			}
			if (!file.isDirectory()) {
				return;
			}
			String[] tempList = file.list();
			File temp = null;
			for (int i = 0; i < tempList.length; i++) {
				if (path.endsWith(File.separator)) {
					temp = new File(path + tempList[i]);
				} else {
					temp = new File(path + File.separator + tempList[i]);
				}
				if (temp.isFile()) {
					temp.delete();
				}
				if (temp.isDirectory()) {
					delAllFileInFolder(path + "/" + tempList[i]);
					delFile(path + "/" + tempList[i]);
				}
			}
		}

		final static public String[] MusicEndWith = { ".mp3", ".midi", ".mid", ".rm", ".wma" };

		public boolean isMusicFile(String path) {
			for (int i = 0; i < MusicEndWith.length; i++)
				if (path.endsWith(MusicEndWith[i]))
					return true;
			return false;
		}
	}

	public static final class Folder {
		String name;// folder name
		String path;// folder path
		int size;// K
		boolean isfolder = true;// true >folder false >file
		ArrayList<Folder> list = new ArrayList();
		Folder prefolder;

		public Folder() {
		}

		public Folder(String path) {
			this.path = path;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public boolean isIsfolder() {
			return isfolder;
		}

		public void setIsfolder(boolean isfolder) {
			this.isfolder = isfolder;
		}

		public ArrayList<Folder> getList() {
			return list;
		}

		public void setList(ArrayList<Folder> list) {
			this.list = list;
		}

		public Folder getPrefolder() {
			return prefolder;
		}

		public void setPrefolder(Folder prefolder) {
			this.prefolder = prefolder;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}
	}

	public static final class AppInfo {
		public int versionCode = 0;
		public String appname = "";
		public String packagename = "";
		public String versionName = "";
		public Drawable appicon = null;
	}

	public static final class AppInfoManager {

		final public static int type_system = 0;
		final public static int type_download = 1;
		final public static int type_all = 2;

		ContextWrapper contextwrapper;

		public AppInfoManager(ContextWrapper contextwrapper) {
			this.contextwrapper = contextwrapper;
		}

		ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
		List<PackageInfo> packages;

		public ArrayList<AppInfo> getAppInfo(int type) {
			if (packages == null)
				packages = contextwrapper.getPackageManager().getInstalledPackages(0);
			for (int i = 0; i < packages.size(); i++) {
				PackageInfo packageInfo = packages.get(i);
				AppInfo tmpInfo = new AppInfo();
				tmpInfo.appname = packageInfo.applicationInfo.loadLabel(contextwrapper.getPackageManager()).toString();
				tmpInfo.packagename = packageInfo.packageName;
				tmpInfo.versionName = packageInfo.versionName;
				tmpInfo.versionCode = packageInfo.versionCode;
				tmpInfo.appicon = packageInfo.applicationInfo.loadIcon(contextwrapper.getPackageManager());

				ApplicationInfo applicationInfo = packageInfo.applicationInfo;
				switch (type) {
				case type_system:
					if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
						appList.add(tmpInfo);
					break;
				case type_download:
					if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
						appList.add(tmpInfo);
					}
					break;
				case type_all:
					appList.add(tmpInfo);
					break;
				default:
					break;
				}
			}
			return appList;
		}

		public static void AppInstall(String filePath, Context act) {
			File f = new File(filePath);
			Intent i = new Intent();
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setAction(Intent.ACTION_VIEW);
			i.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
			act.startActivity(i);
		}

		public static void AppUnInstall(String packagePath, Activity act) {
			Uri packageURI = Uri.parse("package:" + packagePath);
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
			act.startActivity(uninstallIntent);
		}
	}

	public static final class ToastManager {

		public static void show(Context context, String text) {
			try {
				Tools.makeToast(text);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static InputStream getImageUrl(String oldUrl) {
		InputStream is = null;
		try {
			String url = oldUrl;
			String[] arr = url.split("/");
			String url1 = arr[arr.length - 1].replace(".jpg", "");
			String url2 = URLEncoder.encode(url1, "gbk");
			url = url.replace(url1, url2);
			Log.e("url", "image=" + url);
			is = new URL(url).openStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return is;
	}

	public static String getTD(Context context, int id) {
		String td;
		InputStream is = context.getResources().openRawResource(id);
		DataInputStream dis = new DataInputStream(is);
		byte[] buffer = null;
		try {
			buffer = new byte[is.available()];
			dis.readFully(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				dis.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		td = new String(buffer);
		return td;
	}
}
