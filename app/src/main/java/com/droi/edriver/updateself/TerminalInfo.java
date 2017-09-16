package com.droi.edriver.updateself;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

public class TerminalInfo {
	private String hsman; // MANUFACTURER 制作商
	private String hstype; // MODEL
	private String osVer; // android version
	private short screenWidth;
	private short screenHeight;
	private long ramSize;
	private String imsi;
	private String imei;
	private short lac;
	private String ip;
	private byte networkType;
	private String channelId;// 项目名称
	private String appId;
	private int apkVersion;
	private String apkVerName;
	private String packageName;
	private String cpu;

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getHsman() {
		return hsman;
	}

	public void setHsman(String hsman) {
		this.hsman = hsman;
	}

	public String getHstype() {
		return hstype;
	}

	public void setHstype(String hstype) {
		this.hstype = hstype;
	}

	public String getOsVer() {
		return osVer;
	}

	public void setOsVer(String osVer) {
		this.osVer = osVer;
	}

	public short getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(short screenWidth) {
		this.screenWidth = screenWidth;
	}

	public short getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(short screenHeight) {
		this.screenHeight = screenHeight;
	}

	public long getRamSize() {
		return ramSize;
	}

	public void setRamSize(long ramSize) {
		this.ramSize = ramSize;
	}

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public short getLac() {
		return lac;
	}

	public void setLac(short lac) {
		this.lac = lac;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public byte getNetworkType() {
		return networkType;
	}

	public void setNetworkType(byte networkType) {
		this.networkType = networkType;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getApkVersion() {
		return apkVersion;
	}

	public void setApkVersion(int apkVersion) {
		this.apkVersion = apkVersion;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getApkVerName() {
		return apkVerName;
	}

	public void setApkVerName(String apkVerName) {
		this.apkVerName = apkVerName;
	}

	public String toString() {
		JSONObject jsonTerminalInfo = new JSONObject();
		JSONObject jsonObjBody = new JSONObject();

		try {
			jsonTerminalInfo.put("hman", hsman);
			jsonTerminalInfo.put("htype", hstype);
			jsonTerminalInfo.put("sWidth", screenWidth);
			jsonTerminalInfo.put("sHeight", screenHeight);
			jsonTerminalInfo.put("ramSize", ramSize);
			jsonTerminalInfo.put("lac", lac);
			jsonTerminalInfo.put("netType", networkType);
			jsonTerminalInfo.put("chId", channelId);
			jsonTerminalInfo.put("osVer", osVer);
			jsonTerminalInfo.put("appId", appId);
			jsonTerminalInfo.put("apkVer", apkVersion);
			jsonTerminalInfo.put("pName", packageName);
			jsonTerminalInfo.put("apkVerName", apkVerName);
			jsonTerminalInfo.put("imsi", imsi);
			jsonTerminalInfo.put("imei", imei);
			jsonTerminalInfo.put("cpu", cpu);
			jsonObjBody.put("tInfo", jsonTerminalInfo);
			Log.i("msg", "jsonObjBody:" + jsonObjBody.toString());
			return jsonObjBody.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static TerminalInfo generateTerminalInfo(Context context) {
		return generateTerminalInfo(context, "", "");
	}

	public static TerminalInfo generateTerminalInfo(Context context, String appId, String chId) {
		TerminalInfo mTerminalInfo = null;
		int versionCode = 0;
		String pName = "";
		String apkVersionName = "";

		TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		mTerminalInfo = new TerminalInfo();

		DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();

		mTerminalInfo.setHsman(Build.MANUFACTURER);
		mTerminalInfo.setHstype(Build.MODEL);
		mTerminalInfo.setOsVer(Build.VERSION.RELEASE);
		mTerminalInfo.setScreenHeight((short) outMetrics.heightPixels);
		mTerminalInfo.setScreenWidth((short) outMetrics.widthPixels);
		mTerminalInfo.setAppId(appId);
		mTerminalInfo.setChannelId(chId);
		pName = context.getPackageName();
		PackageInfo pInfo = getPackageInfo(context, pName);
		if (pInfo != null) {
			versionCode = pInfo.versionCode;
			apkVersionName = pInfo.versionName;
		}
		String imei = "123456789012345";
		mTerminalInfo.setApkVersion(versionCode);
		mTerminalInfo.setPackageName(context.getPackageName());
		mTerminalInfo.setApkVerName(apkVersionName);
		mTerminalInfo.setImei(tManager.getDeviceId() == null ? imei : tManager.getDeviceId());
		mTerminalInfo.setImsi(tManager.getSubscriberId());
		mTerminalInfo.setNetworkType((byte) getNetworkType(context));
		mTerminalInfo.setRamSize(getAndroidRamSize());
		mTerminalInfo.setCpu(Build.HARDWARE);
		return mTerminalInfo;
	}

	private static PackageInfo getPackageInfo(Context context, String pName) {
		PackageInfo pinfo = null;
		try {
			pinfo = context.getPackageManager().getPackageInfo(pName, PackageManager.GET_CONFIGURATIONS);
		} catch (NameNotFoundException e) {
			
		}
		return pinfo;
	}

	private static byte getNetworkType(Context context) {
		byte netStatus = 0;
		ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo info = connectMgr.getActiveNetworkInfo();

		if (info != null) {
			if (info.getType() == ConnectivityManager.TYPE_WIFI) {
				// wifi network
				netStatus = 3;
			} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
				int subType = info.getSubtype();
				if (subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA
						|| subType == TelephonyManager.NETWORK_TYPE_EVDO_0 || subType == TelephonyManager.NETWORK_TYPE_EVDO_A) {
					// 3G network
					netStatus = 2;
				} else {
					// 2G or 2.5G
					netStatus = 1;
				}
			}
		}
		return netStatus;
	}

	private static long getAndroidRamSize() {
		String[] meminfoLabels = { "MemTotal:" };
		long[] meminfoValues = new long[1];
		meminfoValues[0] = -1;
		Class<?> proc = null;
		try {
			proc = Class.forName("android.os.Process");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Method method = proc.getMethod("readProcLines", String.class, String[].class, long[].class);
			method.invoke(proc.newInstance(), "/proc/meminfo", meminfoLabels, meminfoValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String RAM = "null";
		if (meminfoValues[0] != -1) {
			RAM = Long.toString(meminfoValues[0] / 1024) + "M";
		}
		return meminfoValues[0];
	}
}
