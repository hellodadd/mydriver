package com.droi.adas;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback {
private static final String TAG = "CameraSurface";
	
	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera;
	
	public CameraSurface(Context context) {
		super(context);
		init();
	}

	/** if you want to create a View in the layout, you have to implement the constructor with two parameters **/
	public CameraSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CameraSurface(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
		//mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public void setCamera(Camera camera) {
		mCamera = camera;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
	      try
	      {
	         if( mCamera != null )
	         {
	        	 mCamera.setPreviewDisplay( mSurfaceHolder );
	         }
	      }
	      catch( Exception exception )
	      {
		      Log.d( TAG, "SurfaceCreated failed" );
	      }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
	}
}
