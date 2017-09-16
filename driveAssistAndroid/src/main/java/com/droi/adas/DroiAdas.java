package com.droi.adas;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.droi.adas.sdk.AdasFrameProcessor;
import com.droi.adas.util.CameraUtil;
import com.droi.adas.util.DisplayUtil;

public class DroiAdas extends Activity implements Camera.PreviewCallback {

	private ViewGroup rootView;                    //父容器
	private Camera mCamera;                    //Camera
	private CameraSurface mCameraSurface;                //用于预览Camera
	private TextView mTextView;                    //显示信息（如：fps）
	private DrawLaneView mDrawLaneView;                    //用于画车道线等信息的新建View
	private DrawCarView mDrawCarView;                    //用于画车道线等信息的新建View

	private int mViewWidth = 640;            //设置View的宽
	private int mViewHeight = 480;            //设置View的高
	private int previewWidth = 640;            //Camera的preview宽
	private int previewHeight = 480;        //Camera的preview高
	private float focalLength;
	private float sensorWidth;
	private float shiftScale = 17f / 24f;                //感兴趣区域（即将图像纵向切分，从shiftScale到1比例的部分为感兴趣区域）

	private AdasFrameProcessor mAdasFrameProcessor;        //ADAS处理类
	private byte[] FrameLaneData = null;        //帧的数据
	private byte[] FrameCarData = null;        //帧的数据
	private float[] laneData = null;            //检测到的车道线数据（以比例返回）
	private float[] carsData = null;            //检测到的车辆数据（以比例返回）
	private int[] deviateFlag = null;            //flagData[0]为1表示车道偏离，0表示未偏离
	private int[] carNum = null;                //检测到的车辆数
	private int hasDetectedLane = 0;        //1表示检测到了车道线

	private boolean isProcessingLaneDetect = false; //判断是否正在检测车道线的标识
	private boolean isProcessingCarDetect = false;    //判断是否正在检测车辆的标识

	Handler mHandler = new Handler() {                        //控制DrawView的绘制，及fps的显示
		@Override
		public void handleMessage(Message msg) {
			//绘制DrawView
			if (msg.what == 0x122) {
				if (deviateFlag[0] == 0) {
					mDrawLaneView.setLaneRed(0);
					mDrawLaneView.setLaneGreen(255);
				} else {
					mDrawLaneView.setLaneRed(255);
					mDrawLaneView.setLaneGreen(0);
				}
				mDrawLaneView.invalidate();
			}
			if (msg.what == 0x123) {
				mDrawCarView.invalidate();
			}
			super.handleMessage(msg);
		}
	};

	//加载jni中经过ndk-build后生成的动态库文件
	static {
		System.loadLibrary("opencv_java3");
		System.loadLibrary("process");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//保持屏幕
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		rootView = (ViewGroup) findViewById(R.id.root);
		mTextView = (TextView) findViewById(R.id.text);

		//初始化
		laneData = new float[8];
		carsData = new float[100];
		deviateFlag = new int[1];
		carNum = new int[1];
		focalLength = 3.47f;
		sensorWidth = 4.73f;
		mAdasFrameProcessor = new AdasFrameProcessor(this);
		mAdasFrameProcessor.setCarLeft((float) 0.3);
		mAdasFrameProcessor.setCarRight((float) 0.3);
		mAdasFrameProcessor.setCarTop((float) 0.3);
		mAdasFrameProcessor.setLaneTop((float) 0.7);
		mAdasFrameProcessor.setCarScale((float) 2.5);
		mAdasFrameProcessor.setLaneScale((float) 2.5);
		mAdasFrameProcessor.setCarMinSize(30);
		mAdasFrameProcessor.setCarThreshold(7);
	}

	@Override
	public void onResume() {
		super.onResume();

		//配置Camera
		cameraInit();

		//创建预览Camera的Surface及绘制车道线的DrawView
		mCameraSurface = new CameraSurface(this);
		mCameraSurface.setCamera(mCamera);
		mDrawCarView = new DrawCarView(this);
		mDrawLaneView = new DrawLaneView(this);
		rootView.addView(mCameraSurface, new LayoutParams(mViewWidth, mViewHeight));
		rootView.addView(mDrawCarView, new LayoutParams(mViewWidth, mViewHeight));
		rootView.addView(mDrawLaneView, new LayoutParams(mViewWidth, mViewHeight));

		mTextView.setWidth(getResources().getDisplayMetrics().widthPixels / 2);
		mTextView.bringToFront();
		mCamera.startPreview();

		//定时更新fps的显示
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(0x124);
			}
		}, 300, 300);
	}

	private void cameraInit() {
		mCamera = Camera.open();
		//设置Camera最合适的预览尺寸
		Point point = DisplayUtil.getScreenMetrics(this);
		CameraUtil.initParams(mCamera, point.x, point.y);
		//根据预览尺寸设置Surface的宽和高
		Camera.Parameters params = mCamera.getParameters();
		previewWidth = params.getPreviewSize().width;
		previewHeight = params.getPreviewSize().height;
		float widthScale = (float) point.x / (float) params.getPreviewSize().width;
		float heightScale = (float) point.y / (float) params.getPreviewSize().height;
		if (widthScale <= heightScale) {
			mViewWidth = point.x;
			mViewHeight = (int) (params.getPreviewSize().height * widthScale);
		} else {
			mViewHeight = point.y;
			mViewWidth = (int) (params.getPreviewSize().width * heightScale);
		}
		mCamera.setPreviewCallback(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// At preview mode, the frame data will push to here.
		FrameLaneData = data;//.clone();
		FrameCarData = data;//.clone();

		if (!isProcessingLaneDetect) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					isProcessingLaneDetect = true;
					long startTime = System.currentTimeMillis();
					hasDetectedLane = mAdasFrameProcessor.adasDetectLane(previewWidth, previewHeight, FrameLaneData, laneData, deviateFlag);
					laneTime.add(System.currentTimeMillis() - startTime);
					if (laneTime.size() >= 20) {
						Log.i("zhuqichao", "车道检测执行" + laneTime.size() + "次，平均耗时：" + getAverage(laneTime) + "毫秒");
						laneTime.clear();
					}
					mDrawCarView.setCarsData(carsData);
					mDrawCarView.setCarNum(carNum[0]);
					mHandler.sendEmptyMessage(0x123);

					isProcessingLaneDetect = false;

				}
			}).start();
		}

		if (!isProcessingCarDetect) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					isProcessingCarDetect = true;
					long startTime = System.currentTimeMillis();
					mAdasFrameProcessor.adasDetectCar(previewWidth, previewHeight, focalLength, sensorWidth, FrameCarData, carsData, carNum);
					carTime.add(System.currentTimeMillis() - startTime);
					if (carTime.size() >= 20) {
						Log.i("zhuqichao", "车辆检测执行" + carTime.size() + "次，平均耗时：" + getAverage(carTime) + "毫秒");
						carTime.clear();
					}
					mDrawLaneView.setLanePoints(laneData);
					mDrawLaneView.setHasDetectedLane(hasDetectedLane == 1);
					mHandler.sendEmptyMessage(0x122);

					isProcessingCarDetect = false;
				}
			}).start();
		}
	}

	ArrayList<Long> laneTime = new ArrayList<Long>();
	ArrayList<Long> carTime = new ArrayList<Long>();

	//计算list平均值
	private long getAverage(ArrayList<Long> list) {
		long sum = 0;
		for (float item : list) {
			sum += item;
		}
		return sum / list.size();
	}
}
