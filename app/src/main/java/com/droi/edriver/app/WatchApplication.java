package com.droi.edriver.app;

import android.app.Application;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.droi.edriver.net.GetDataFromVolly;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by ZhuQichao on 2016/2/19.
 */
public class WatchApplication extends Application {

	private static WatchApplication mInstance;
	private RequestQueue mRequestQueue;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
	}

	public static WatchApplication getInstance() {
		return mInstance;
	}

	/**
	 * @return The Volley Request queue, the queue will be created if it is null
	 */
	public RequestQueue getRequestQueue() {
		// lazy initialize the request queue, the queue instance will be
		// created when it is accessed for the first time
		if (mRequestQueue == null) {
			synchronized (WatchApplication.class) {
				if (mRequestQueue == null) {
					mRequestQueue = Volley.newRequestQueue(getApplicationContext());
					mRequestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Request>() {
						@Override
						public void onRequestFinished(Request<Request> request) {
							Log.i(GetDataFromVolly.TAG, "request finish : " + request.getTag());
						}
					});
				}
			}
		}
		return mRequestQueue;
	}
}
