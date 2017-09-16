package com.droi.edriver.updateself;

import android.content.Context;

public class SelfUpdateMain {
	public static boolean isDownloading = false;

	public static void execApkSelfUpdateRequest(Context context, String appid, String chnid, boolean isHand) {
		if (!isDownloading) {
			UpdateHandler h = new UpdateHandler(context, isHand);
			new RequestAsyncTask(context, h, UpdateHandler.MSG_UPDATE_VIEW, appid, chnid).startRun();
		}
	}

}
