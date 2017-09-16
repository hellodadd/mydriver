package com.droi.account.download;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.droi.account.MyResource;


public class DownloadNotification {

	private static final String TAG = "DownloadNotification";

	public static final int DOWNLOAD_FAILED = 1001;
	public static final int DOWNLOAD_SUCCESS = DOWNLOAD_FAILED + 1;
	public static final int DOWNLOAD_UPDATE = DOWNLOAD_SUCCESS + 1;

	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder mNotification;
	private PendingIntent mPendingIntent;
	private RemoteViews mRemoteView = null;
	private Context mContext;
	private int mNotifyId;
	protected MyResource mMyResources = null;

	public DownloadNotification(Context context, int notifyId, PendingIntent contentIntent) {
		mContext = context;
		mNotifyId = notifyId;
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mPendingIntent = contentIntent;
		mMyResources = new MyResource(context);
	}

	public void updatePendingIntent(PendingIntent contentIntent) {
		mPendingIntent = contentIntent;
	}

	public void show() {
		//show(mContext.getString(mMyResources.getString("lib_droi_account_freeme_acount), R.drawable.lib_droi_account_ic_launcher, mMyResources.getLayout("lib_droi_account_notification);
		showDefaultNotification(mContext.getString(mMyResources.getString("lib_droi_account_freeme_acount")));
	}

	private void show(String title, int iconId, int layout) {
		mNotification = new NotificationCompat.Builder(mContext)
				.setSmallIcon(mMyResources.getId("lib_droi_account_ic_launcher"))
				.setTicker(title)
				.setContentIntent(mPendingIntent)
				.setAutoCancel(true)
				.setOnlyAlertOnce(true)
				.setShowWhen(true);

		if (mRemoteView == null) {
			mRemoteView = new RemoteViews(mContext.getPackageName(), layout);
			mRemoteView.setImageViewResource(mMyResources.getId("lib_droi_account_image"), iconId);
			mRemoteView.setTextViewText(mMyResources.getId("lib_droi_account_text"), title);
			mRemoteView.setProgressBar(mMyResources.getId("lib_droi_account_progress_horizontal"), 100, 0, false);
			mNotification.setContent(mRemoteView);
		}

		mNotificationManager.notify(mNotifyId, mNotification.build());
	}

	public void showDefaultNotification(String title) {
		mNotification = new NotificationCompat.Builder(mContext)
				.setSmallIcon(mMyResources.getId("lib_droi_account_ic_launcher"))
				.setTicker(title)
				.setShowWhen(true);
		mNotificationManager.notify(mNotifyId, mNotification.build());
	}


	public void updateDefault(int percentage, int state) {
		if (mNotification.build().contentView != null) {
			mNotification.setContentTitle(mContext.getString(mMyResources.getString("lib_droi_account_freeme_acount")));
			if (state == DOWNLOAD_FAILED) {
				mNotification.setContentText(mContext.getString(mMyResources.getString("lib_droi_account_download_failed")))
						.setContentIntent(null);
			} else if (state == DOWNLOAD_SUCCESS) {
				mNotification.setContentText(mContext.getString(mMyResources.getString("lib_droi_account_download_complete")))
						.setContentIntent(mPendingIntent);
			} else if (state == DOWNLOAD_UPDATE) {
				mNotification.setContentText(mContext.getString(mMyResources.getString("lib_droi_account_downloading")) + "  " + percentage + "%")
						.setContentIntent(mPendingIntent);

			}
		}
		mNotificationManager.notify(mNotifyId, mNotification.build());
	}

	public void update(int percentage, int state) {
		if (mNotification.build().contentView != null) {
			if (state == DOWNLOAD_FAILED) {
				mNotification.build().contentView.setTextViewText(mMyResources.getId("lib_droi_account_text"), mContext.getString(
						mMyResources.getString("lib_droi_account_download_failed")));
				//mNotification.setLatestEventInfo(mContext, mContext.getString(mMyResources.getString("lib_droi_account_freeme_acount), 
				//		mContext.getString(mMyResources.getString("lib_droi_account_download_failed), null);
			} else if (state == DOWNLOAD_SUCCESS) {
				//mNotification.contentView.setTextViewText(mMyResources.getId("lib_droi_account_text, mContext.getString(mMyResources.getString("lib_droi_account_download_complete));
				mNotification.setContentTitle(mContext.getString(mMyResources.getString("lib_droi_account_freeme_acount")))
						.setContentText(mContext.getString(mMyResources.getString("lib_droi_account_download_complete")))
						.setContentIntent(mPendingIntent);
			} else if (state == DOWNLOAD_UPDATE) {
				mNotification.build().contentView.setProgressBar(mMyResources.getId("lib_droi_account_progress_horizontal"), 100, percentage, false);
			}
		}
		mNotificationManager.notify(mNotifyId, mNotification.build());
	}

	public void cancel() {
		mNotificationManager.cancel(mNotifyId);
	}
}
