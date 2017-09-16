package com.droi.account.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;


import com.droi.account.MyResource;
import com.droi.account.Utils;
import com.droi.account.login.AvatarUtils;

public class DownloadService extends Service {

	private static final String TAG = "DownloadService";
	
	private final String APK_NAME = "FreemeAccount.apk";

	private int mState = -1;
	protected MyResource mMyResources = new MyResource(this);
	
	private DownloadFileThread mDownloadThread;
	private DownloadNotification mNotification;
	private boolean mCompleted = false;
	
	private Handler mServiceHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
				case DownloadNotification.DOWNLOAD_SUCCESS:{
					mState = DownloadNotification.DOWNLOAD_SUCCESS;
					Uri uri = Uri.fromFile(new File(mDownloadThread.getApkFile()));
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(uri, "application/vnd.android.package-archive");
					PendingIntent mPendingIntent = PendingIntent.getActivity(DownloadService.this, 0, intent, 0);
					mNotification.updatePendingIntent(mPendingIntent);
					mNotification.update(100, DownloadNotification.DOWNLOAD_SUCCESS);
					install(mDownloadThread.getApkFile());
				}
				break;
				case DownloadNotification.DOWNLOAD_FAILED:{
					mState = DownloadNotification.DOWNLOAD_FAILED;
					mNotification.update(0, DownloadNotification.DOWNLOAD_FAILED);
				}
				break;
				case DownloadNotification.DOWNLOAD_UPDATE:{
					mState = DownloadNotification.DOWNLOAD_UPDATE;
					Bundle data = msg.getData();
					int percentage = data.getInt("percent");
					mNotification.updateDefault(percentage, DownloadNotification.DOWNLOAD_UPDATE);
				}
				break;
			}
		};
	};

	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(mState == DownloadNotification.DOWNLOAD_UPDATE){
			Utils.showMessage(this, mMyResources.getString("lib_droi_account_downloading"));
			return super.onStartCommand(intent, flags, startId);
		}else if(mState == DownloadNotification.DOWNLOAD_FAILED){
			
		}
		
		String dir = AvatarUtils.getSDPath()+ "/"+Utils.DOWNLOAD_DIR+"/";
		File filePath = new File(dir, APK_NAME);
		if(filePath.exists()){
			if(checkApk(filePath.toString())){
				install(filePath.toString());
				return super.onStartCommand(intent, flags, startId);
			}else{
				filePath.delete();
			}
			

		}
		
		mNotification = new DownloadNotification(getApplicationContext(), 1, null);
		mNotification.show();
		mDownloadThread = new DownloadFileThread(mServiceHandler, "http://account.zhuoyi.com/apkinfo/FreemeAccount.apk");
		new Thread(mDownloadThread).start();
		return super.onStartCommand(intent, flags, startId);
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mDownloadThread != null){
			mDownloadThread.interuptThread();
		}
		
	}
	
	private void install(String apkFile){
		if(TextUtils.isEmpty(apkFile)){
			return;
		}
		File file = new File(apkFile);
		if(file.exists()){
			Uri uri = Uri.fromFile(file);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(uri, "application/vnd.android.package-archive");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
	
	private boolean checkApk(String archiveFilePath){
		PackageManager pm = getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath,
				PackageManager.GET_ACTIVITIES);
		
		if (info != null) {
			return true;
		}
		
		return false;
	}
	
	private class DownloadFileThread implements Runnable{

		private final String mUrlStr;
		boolean interupted = false;  //force to stop download ?
		private Handler mHandler;
		private String mFilePath;
		
		
		public DownloadFileThread(Handler handler, String urlStr){
			mHandler = handler;
			mUrlStr = urlStr;
			mCompleted = false;
		}
		
	    public String getApkFile()  {
	        if(mCompleted){
	            return mFilePath;
	        }else{
	            return null;
	        }
	    }
		
	    public void interuptThread()  {
	        interupted = true;
	    }
	    
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				InputStream is = null;
				int fileLength = 1;
				try {
					URL url = new URL(mUrlStr);
					
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					conn.setConnectTimeout(5*1000);
					conn.setReadTimeout(20*1000);
					is = conn.getInputStream();
					fileLength = conn.getContentLength();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mHandler.removeMessages(DownloadNotification.DOWNLOAD_FAILED);
					mHandler.sendEmptyMessage(DownloadNotification.DOWNLOAD_FAILED);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mHandler.removeMessages(DownloadNotification.DOWNLOAD_FAILED);
					mHandler.sendEmptyMessage(DownloadNotification.DOWNLOAD_FAILED);
				}
				
				//Create folder
				String dir = AvatarUtils.getSDPath()+ "/"+Utils.DOWNLOAD_DIR+"/";
				
				File file = new File(dir);
	    		if(!file.exists()){
	    			file.mkdir();
	    		}
				
				File filePath = new File(dir, APK_NAME);
				mFilePath = filePath.toString();
				if(filePath.exists()){
					try {
						if(is != null){
							is.close();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				}
				FileOutputStream fos = null;
				
				try {
					fos = new FileOutputStream(filePath);
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mHandler.removeMessages(DownloadNotification.DOWNLOAD_FAILED);
					mHandler.sendEmptyMessage(DownloadNotification.DOWNLOAD_FAILED);
				}
				
				BufferedInputStream bis = new BufferedInputStream(is);
				byte[] buffer = new byte[1024];
				double rate = (double)100/fileLength;
				
				int total = 0;
				int currProcess = 0;
				int lastProcess = 0;
				int len;
				
				try {
					while(interupted == false && ((len = bis.read(buffer)) != -1)){
						fos.write(buffer, 0, len);
						
						total += len;
						currProcess = (int)(total * rate);
						if(currProcess > lastProcess){
							Message msg = Message.obtain();
							msg.what = DownloadNotification.DOWNLOAD_UPDATE;
							Bundle data = new Bundle();
							data.putInt("percent", currProcess);
							msg.setData(data);
							mHandler.removeMessages(DownloadNotification.DOWNLOAD_UPDATE);
							mHandler.sendMessage(msg);
						}
						
					}
					if(fileLength == total){
						mCompleted = true;
						mHandler.removeMessages(DownloadNotification.DOWNLOAD_SUCCESS);
						mHandler.sendEmptyMessage(DownloadNotification.DOWNLOAD_SUCCESS);
					}

					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mHandler.removeMessages(DownloadNotification.DOWNLOAD_FAILED);
					mHandler.sendEmptyMessage(DownloadNotification.DOWNLOAD_FAILED);
				}
				
				
				try {
					if(fos != null){
						fos.close();
					}
					if(bis != null){
						bis.close();
					}
					if(is != null){
						is.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					mHandler.removeMessages(DownloadNotification.DOWNLOAD_FAILED);
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
}
