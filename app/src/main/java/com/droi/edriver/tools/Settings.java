package com.droi.edriver.tools;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.droi.edriver.app.WatchApplication;

/**
 * Created by ZhuQichao on 2016/2/29.
 */
public class Settings {

	private static final String SP_NAME_SETTINGS = "sp_name_settings";
	private static final String KEY_NAME_ASSISTANCE_MODE = "key_name_assistance_mode";
	private static final String KEY_NAME_MINIMUN_SPEED = "key_name_minimum_speed";
	private static final String KEY_NAME_WARN_INTERVAL = "key_name_warn_interval";
	private static final String KEY_NAME_LANE_RECOGNITION = "key_name_lane_recognition";
	private static final String KEY_NAME_LANE_WARNING = "key_name_lane_warning";
	private static final String KEY_NAME_COLLISION_WARN = "key_name_collision_warn";
	private static final String KEY_NAME_WATER_MARK = "key_name_water_mark";
	private static final String KEY_NAME_AUTO_SCREEN = "key_name_auto_screen";
	private static final String KEY_NAME_MAX_STORAGE = "key_name_max_storage";
	private static final String KEY_NAME_CLARITY_LEVEL = "key_name_clarity_level";
	private static final String KEY_NAME_VIDEO_TIME = "key_name_video_time";
	private static final String KEY_IS_FIRST_START_APP = "key_is_first_start_app";

	public static void setAssistanceMode(boolean value) {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		Editor editor = sp.edit();
		editor.putBoolean(KEY_NAME_ASSISTANCE_MODE, value);
		editor.apply();
	}

	/**
	 * 是否打开驾驶辅助模式
	 *
	 * @return
	 */
	public static boolean getAssistanceMode() {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		return sp.getBoolean(KEY_NAME_ASSISTANCE_MODE, true);
	}

	public static void setMinimumSpeed(int value) {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		Editor editor = sp.edit();
		editor.putInt(KEY_NAME_MINIMUN_SPEED, value);
		editor.apply();
	}

	/**
	 * 报警最低时速
	 *
	 * @return
	 */
	public static int getMinimumSpeed() {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		return sp.getInt(KEY_NAME_MINIMUN_SPEED, 30);
	}

	public static void setWarnInterval(int value) {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		Editor editor = sp.edit();
		editor.putInt(KEY_NAME_WARN_INTERVAL, value);
		editor.apply();
	}

	/**
	 * 两次车道偏离报警时间间隔
	 *
	 * @return
	 */
	public static int getWarnInterval() {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		return sp.getInt(KEY_NAME_WARN_INTERVAL, 3);
	}

	public static void setLaneRecognition(boolean value) {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		Editor editor = sp.edit();
		editor.putBoolean(KEY_NAME_LANE_RECOGNITION, value);
		editor.apply();
	}

	/**
	 * 是否打开车道识别
	 *
	 * @return
	 */
	public static boolean getLaneRecognition() {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		return sp.getBoolean(KEY_NAME_LANE_RECOGNITION, true);
	}

	public static void setLaneWarning(boolean value) {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		Editor editor = sp.edit();
		editor.putBoolean(KEY_NAME_LANE_WARNING, value);
		editor.apply();
	}

	/**
	 * 是否打开车道偏离报警
	 *
	 * @return
	 */
	public static boolean getLaneWarning() {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		return sp.getBoolean(KEY_NAME_LANE_WARNING, true);
	}

	public static void setCollisionWarn(boolean value) {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		Editor editor = sp.edit();
		editor.putBoolean(KEY_NAME_COLLISION_WARN, value);
		editor.apply();
	}

	/**
	 * 是否打开防撞预警
	 *
	 * @return
	 */
	public static boolean getCollisionWarn() {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		return sp.getBoolean(KEY_NAME_COLLISION_WARN, true);
	}

	public static void setWaterMark(boolean value) {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		Editor editor = sp.edit();
		editor.putBoolean(KEY_NAME_WATER_MARK, value);
		editor.apply();
	}

	/**
	 * 是否打开视频水印
	 *
	 * @return
	 */
	public static boolean getWaterMark() {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		return sp.getBoolean(KEY_NAME_WATER_MARK, true);
	}

	/**
	 * 是否打开自动灭屏
	 *
	 * @param value
	 */
	public static void setAutoScreen(boolean value) {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		Editor editor = sp.edit();
		editor.putBoolean(KEY_NAME_AUTO_SCREEN, value);
		editor.apply();
	}

	public static boolean getAutoScreen() {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		return sp.getBoolean(KEY_NAME_AUTO_SCREEN, true);
	}

	/**
	 * 最大存储空间
	 *
	 * @param value 0:1G， 1:2G， 2:4G， 3:8G
	 */
	public static void setMaxStorage(int value) {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		Editor editor = sp.edit();
		editor.putInt(KEY_NAME_MAX_STORAGE, value);
		editor.apply();
	}

	public static int getMaxStorage() {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		return sp.getInt(KEY_NAME_MAX_STORAGE, 1);
	}

	public static int getMaxStorageSize() {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		return 1024 * (int) Math.pow(2, sp.getInt(KEY_NAME_MAX_STORAGE, 1));
	}

	/**
	 * 视屏清晰度水平
	 *
	 * @param value 0：低， 1：中， 2：高
	 */
	public static void setClarityLevel(int value) {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		Editor editor = sp.edit();
		editor.putInt(KEY_NAME_CLARITY_LEVEL, value);
		editor.apply();
	}

	public static int getClarityLevel() {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		return sp.getInt(KEY_NAME_CLARITY_LEVEL, 1);
	}

	/**
	 * 单个视频时间
	 *
	 * @param value 0:3分钟， 1:5分钟
	 */
	public static void setVideoTime(int value) {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		Editor editor = sp.edit();
		editor.putInt(KEY_NAME_VIDEO_TIME, value);
		editor.apply();
	}

	public static int getVideoTime() {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		return sp.getInt(KEY_NAME_VIDEO_TIME, 0);
	}

	/**
	 * 保存应用是否第一次开启
	 *
	 * @param first
	 */
	public static void setFirstStartApp(boolean first) {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		Editor editor = sp.edit();
		editor.putBoolean(KEY_IS_FIRST_START_APP, first);
		editor.apply();
	}

	public static boolean isFirstStartApp() {
		SharedPreferences sp = WatchApplication.getInstance().getSharedPreferences(SP_NAME_SETTINGS, 0);
		return sp.getBoolean(KEY_IS_FIRST_START_APP, true);
	}
}
