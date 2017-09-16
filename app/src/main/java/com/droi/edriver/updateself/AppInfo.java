package com.droi.edriver.updateself;

import android.graphics.Bitmap;

public class AppInfo {
	private String appName;
	private String url;
	private String appPackageName;
	private String mainActivity;
	private byte[] bitmap;
	private String size;
	private String localFile;
	private String version;
	private int flag;// 1,installed. 2,downlaod not installed. 3,need download
						// and installed
	private boolean downloading;
	// 保留网络数据
	private String logo;
	private Bitmap mbitmap;

	public AppInfo() {

	}

	public AppInfo(String appName, String url, String appPackageName, String mainActivity, byte[] bitmap, String localFile, int flag, String version,
			String size) {
		super();
		this.appName = appName;
		this.url = url;
		this.appPackageName = appPackageName;
		this.mainActivity = mainActivity;
		this.bitmap = bitmap;
		this.size = size;
		this.localFile = localFile;
		this.version = version;
		this.flag = flag;
	}

	public boolean getDownloading() {
		return downloading;
	}

	public void setDownloading(boolean downloading) {
		this.downloading = downloading;
	}

	public String getLocalFile() {
		return localFile;
	}

	public void setLocalFile(String localFile) {
		this.localFile = localFile;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public Bitmap getMbitmap() {
		return mbitmap;
	}

	public void setMbitmap(Bitmap mbitmap) {
		this.mbitmap = mbitmap;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAppPackageName() {
		return appPackageName;
	}

	public void setAppPackageName(String appPackageName) {
		this.appPackageName = appPackageName;
	}

	public String getMainActivity() {
		return mainActivity;
	}

	public void setMainActivity(String mainActivity) {
		this.mainActivity = mainActivity;
	}

	public byte[] getBitmap() {
		return bitmap;
	}

	public void setBitmap(byte[] bitmap) {
		this.bitmap = bitmap;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {

		this.version = version;

	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
}
