package com.droi.edriver.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droi.account.shared.DroiAccount;
import com.droi.adas.sdk.AdasFrameProcessor;
import com.droi.edriver.R;
import com.droi.edriver.bean.Car;
import com.droi.edriver.bean.UserInfo;
import com.droi.edriver.net.NetMsgCode;
import com.droi.edriver.net.Request;
import com.droi.edriver.tools.FileSizeUtil;
import com.droi.edriver.tools.Settings;
import com.droi.edriver.tools.Tools;
import com.droi.edriver.updateself.Constants;
import com.droi.edriver.updateself.SelfUpdateMain;
import com.droi.edriver.util.CameraUtil2;
import com.droi.edriver.util.DisplayUtil;
import com.droi.edriver.util.ShakeDetector;
import com.droi.edriver.view.BatteryView;
import com.droi.edriver.view.CameraSurface;
import com.droi.edriver.view.DrawCarView;
import com.droi.edriver.view.DrawLaneView;
import com.droi.edriver.view.MenuRightAnimations;
import com.droi.edriver.view.ProgressWheel;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity implements Camera.PreviewCallback, Camera.AutoFocusCallback, View.OnTouchListener {

	private Camera mCamera;                                //Camera
	private CameraSurface mCameraSurface;               //用于预览Camera
	private DrawLaneView mDrawLaneView;                            //用于画车道线信息的新建View
	private DrawCarView mDrawCarView;                            //用于画车辆信息的新建View

	private int mViewWidth = 640;                        //设置View的宽
	private int mViewHeight = 480;                    //设置View的高
	public static int previewWidth = 0;                        //Camera的preview宽
	public static int previewHeight = 0;                    //Camera的preview高
	private float focalLength;
	private float sensorWidth;

	private boolean isRecord;
	private MediaRecorder mediaRecorder;
	private boolean isLockVideo = false;
	private float progress = 0f;
	private PowerManager.WakeLock wakeLock;
	private KeyguardManager.KeyguardLock mKeyguardLock;
	private TextView batteryInfo;
	private BatteryView mBatteryView;
	private ImageView batteryEatting;
	private TextView tvSpeed;
	private ImageView btnSave, btnAbout, btnSetting, btnFlash, btnMenuIcon;
	private boolean isFlashOn = false;
	private ImageView titleBar;
	private LinearLayout lockVideoLayout, screenLayout;
	private ImageView lockVideoButton;
	private ProgressWheel startRecoder;
	private boolean canPlayLaneSound = true;
	private boolean canPlayCarSound = true;
	private SoundPool mSoundPool;
	private int[] sounds = new int[3];
	private AdasFrameProcessor mAdasFrameProcessor;        //ADAS处理类

	private ArrayList<Car> lastCarList = new ArrayList<>();
	private ArrayList<Car> newCarList = new ArrayList<>();
	private byte[] FrameLaneData = null;                //帧的数据
	private byte[] FrameCarData = null;                    //帧的数据
	private float[] laneData = null;                    //检测到的车道线数据（以比例返回）
	private float[] carsData = null;                    //检测到的车辆数据（以比例返回）
	private int[] deviateFlag = null;                    //flagData[0]为1表示车道偏离，0表示未偏离
	private int[] carNum = null;                        //检测到的车辆数
	private int hasDetectedLane = 0;                    //1表示检测到了车道线

	public static float adasLaneTop = 0.7f;            //车道检测，顶部切割比例
	public static float adasLaneScale = 2.0f;            //车道检测，图片缩小比例
	public static float adasCarLeft = 0.3f;            //车辆检测，左边切割比例
	public static float adasCarRight = 0.3f;            //车辆检测，右边切割比例
	public static float adasCarTop = 0.3f;            //车辆检测，顶部切割比例
	public static float adasCarScale = 2.0f;        //车辆检测，图片缩小比例
	public static int adasMinSize = 25;            //车辆检测，检测最小目标
	public static int adasThreshold = 7;        //车辆检测，检测强度
	public static float speed = 0;         //实时车速
	public static boolean speedOpen = false;   //是否打开模拟车速
	public static float alarmTime = 0.3F;   //车辆撞击报警时间:s
	public static int carOffSet = 13;   //检测出的车辆间距

	private boolean isProcessingLaneDetect = false;    //判断是否正在检测车道线的标识
	private boolean isProcessingCarDetect = false;        //判断是否正在检测车辆的标识

	private static final int TRANS_X1 = Tools.dip2px(22);
	private static final int TRANS_Y1 = Tools.dip2px(88);
	private static final int TRANS_X2 = Tools.dip2px(70);
	private static final int TRANS_Y2 = Tools.dip2px(36);
	private boolean isShowMenu = false;
	private DroiAccount mDroiAccount;
	private TextView tvUserName;
	private ImageView imgEditUser;
	private UserInfo mUserInfo;
	private Request mRequest;

	private static final int MSG_DRAW_CAR_VIEW = 0x122;
	private static final int MSG_DRAW_LANE_VIEW = 0x123;
	private static final int MSG_FINISH_RECORDER = 0x1001;
	private static final int MSG_CAN_PLAYLANESOUND = 0x1002;
	private static final int MSG_CAN_PLAYCARSOUND = 0x1003;
	private static final int MSG_TAKE_PICTURE = 0x1004;
	private static final int MSG_CLOSE_SCREEN = 0x1005;
	private static final int MSG_DELETE_FAILED = 0X1006;
	private static final int MSG_DELETE_SUCCESS = 0X1007;
	private static final int REQUEST_CODE_USER_INFO = 0X1008;
	private static final int MSG_START_RECORDER = 0x1009;
	private static final int MSG_START_CAMERA = 0x1010;
	public static final int MSG_PLAY_LANE_SOUND = 0x1011;
	private LocationManager mLocationManager;
	private static final int LOCATION_TIME = 500;
	private static final int LOCATION_METERS = 0;
	private Camera.Size size = null; //視頻分辨率
	private String fileName;//文件名
	private RelativeLayout closeScreenLayout;
	private SurfaceHolder holder;
	private Camera.Size previewSize;
	private YuvImage yunImage;
	private static int BRIGHTNESS = 150;//屏幕亮度
	private static int MIN_BRIGHTNESS = 1;//最低屏幕亮度
	private static final int LOCK_SCREEN_DELAY = 10000;//自动灭屏延时时间
	private static final double FREE_SPACE_LIMIT = 200F;//剩余空间限制，少于50M时自动清除
	private boolean isTakePic = false; //录像时拍照
	private boolean isTakingPic = false; //正在拍照
	private boolean mCurrentOrient = false;//屏幕是否旋转180度
	private boolean videoTimeMinLimit = false;
	private ShakeDetector mShakeDetector;

	private Handler mHandler = new Handler() {                        //控制DrawView的绘制，及fps的显示
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case NetMsgCode.MSG_SUCCESS:
					if (msg.arg1 == NetMsgCode.GET_USER_INFO) {
						if (msg.obj == null) {
							mRequest.updateUserInfo(Tools.getUserinfo(mDroiAccount.getOpenId()));
						} else {
							mUserInfo = (UserInfo) msg.obj;
							updateUserinfo();
						}
					}
					break;
				case MSG_DRAW_CAR_VIEW:
					mDrawCarView.invalidate();
					break;
				case MSG_DRAW_LANE_VIEW:  //绘制DrawView
					mDrawLaneView.invalidate();
					break;
				case MSG_FINISH_RECORDER:
					mediaRecorder.stop();
					videoTimeMinLimit = false;
					AutoDeleteFile();
					break;
				case MSG_CAN_PLAYLANESOUND:
					canPlayLaneSound = true;
					break;
				case MSG_CAN_PLAYCARSOUND:
					canPlayCarSound = true;
					break;
				case MSG_TAKE_PICTURE:
					Tools.makeToast(R.string.pic_saved);
					break;
				case MSG_CLOSE_SCREEN:
					Tools.setBright(MainActivity.this, 1);
					closeScreenLayout.setVisibility(View.VISIBLE);
					break;
				case MSG_DELETE_FAILED:
					Tools.makeToast(R.string.auto_delete_failed);
					break;
				case MSG_DELETE_SUCCESS:
					startRecorder();
					startCalculateProgress();
					break;
				case MSG_START_RECORDER:
					mCamera.setPreviewCallback(MainActivity.this);
					videoTimeMinLimit = true;
					break;
				case MSG_START_CAMERA:
					startCamera();
					break;
				case MSG_PLAY_LANE_SOUND:
					playLaneSound();
					break;
				default:
					break;
			}
		}
	};

	//创建jpeg图片回调数据对象
	Camera.PictureCallback jpeg = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			try {// 获得图片
				Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
				String fileName = format.format(new Date());
				File file = new File(Tools.getPath() + fileName + ".jpg");
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
				bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩到流中
				bos.flush();//输出
				bos.close();//关闭
				bm.recycle();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mCamera.startPreview();
			mCamera.setPreviewCallback(MainActivity.this);
			isTakingPic = false;
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
		Log.i("zhuqichao", "MainActivity.onCreate");
		setContentView(R.layout.activity_main);
		mDroiAccount = DroiAccount.getInstance(this);
		mRequest = new Request(mHandler);
		//保持屏幕
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		findView();
		initData();
		initReceiver();
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mRequest.getUserInfo(mDroiAccount.getOpenId());
		SelfUpdateMain.execApkSelfUpdateRequest(this, Constants.APPID, Constants.CHNID, false);
		mShakeDetector = new ShakeDetector(this);
		mShakeDetector.registerOnShakeListener(onShakeListener);
	}

	private void findView() {
		mCameraSurface = (CameraSurface) findViewById(R.id.camera_surface);
		mCameraSurface.setOnTouchListener(this);
		holder = mCameraSurface.getHolder();
		mDrawLaneView = (DrawLaneView) findViewById(R.id.draw_lane_view);
		mDrawLaneView.setHandler(mHandler);
		mDrawCarView = (DrawCarView) findViewById(R.id.draw_car_view);
		titleBar = (ImageView) findViewById(R.id.view_bar_bg1);
		titleBar.setOnTouchListener(this);
		lockVideoLayout = (LinearLayout) findViewById(R.id.lock_layout);
		screenLayout = (LinearLayout) findViewById(R.id.screen_layout);
		lockVideoButton = (ImageView) findViewById(R.id.lock_video);
		batteryInfo = (TextView) findViewById(R.id.current_electricity);
		mBatteryView = (BatteryView) findViewById(R.id.img_battery);
		batteryEatting = (ImageView) findViewById(R.id.battery_eatting);
		tvSpeed = (TextView) findViewById(R.id.tv_speed);
		btnMenuIcon = (ImageView) findViewById(R.id.btn_menu);
		btnMenuIcon.setOnTouchListener(this);
		btnAbout = (ImageView) findViewById(R.id.btn_about);
		btnAbout.setOnTouchListener(this);
		btnSave = (ImageView) findViewById(R.id.btn_save);
		btnSave.setOnTouchListener(this);
		btnSetting = (ImageView) findViewById(R.id.btn_setting);
		btnSetting.setOnTouchListener(this);
		btnFlash = (ImageView) findViewById(R.id.btn_light);
		btnFlash.setOnTouchListener(this);
		ImageView takePhotoBtn = (ImageView) findViewById(R.id.take_photo);
		takePhotoBtn.setOnTouchListener(this);
		startRecoder = (ProgressWheel) findViewById(R.id.start_recoder);
		startRecoder.setOnTouchListener(this);
		closeScreenLayout = (RelativeLayout) findViewById(R.id.close_screen);
		closeScreenLayout.setOnTouchListener(this);
		tvUserName = (TextView) findViewById(R.id.tv_user_name);
		imgEditUser = (ImageView) findViewById(R.id.img_edit_username);
		updateUserinfo();
	}

	private void updateUserinfo() {
		if (mUserInfo == null) {
			mUserInfo = Tools.getUserinfo(mDroiAccount.getOpenId());
		}
		tvUserName.setText(mUserInfo.getNickName());
	}

	private void initData() {
		//初始化
		laneData = new float[8];
		carsData = new float[100];
		deviateFlag = new int[1];
		carNum = new int[1];
		focalLength = 3.47f;//焦距（不同的摄像头，焦距也有所不同）
		sensorWidth = 4.73f;//摄像头芯片的宽
		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		//载入音频流，返回在池中的id
		sounds[0] = mSoundPool.load(this, R.raw.alarm_lane, 1);
		sounds[1] = mSoundPool.load(this, R.raw.alarm_car, 1);
		sounds[2] = mSoundPool.load(this, R.raw.shutter_sound, 1);
	}

	private void judgeStorage() {
		float used = (float) FileSizeUtil.getFileOrFilesSize(Tools.getPath(), 3);
		if (Settings.getMaxStorageSize() - used <= 250) {
			showHintDialog(getString(R.string.string_storage_short));
		}
	}

	private void showHintDialog(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
		dialog.setTitle(R.string.string_reminder);
		dialog.setMessage(msg);
		dialog.setCancelable(false);
		dialog.setNegativeButton(R.string.string_ok, null);
		dialog.show();
	}

	private void showFinishDialog(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
		dialog.setTitle(R.string.string_reminder);
		dialog.setMessage(msg);
		dialog.setCancelable(false);
		dialog.setNegativeButton(R.string.string_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		dialog.show();
	}

	private void initReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		//filter.addAction(DroiAccount.INTENT_ACCOUNT_CHANGE_ACCOUNT);
		filter.addAction(DroiAccount.INTENT_ACCOUNT_DELETED);
		//filter.addAction(DroiAccount.INTENT_ACCOUNT_LOGIN);
		//filter.addAction(DroiAccount.INTENT_ACCOUNT_UPDATED);
		//filter.addAction(DroiAccount.INTENT_ACCOUNT_LOGINSUCCESS);
		registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Tools.setBright(MainActivity.this, BRIGHTNESS);
		closeScreenLayout.setVisibility(View.INVISIBLE);
		lockAcquire();

		mHandler.sendEmptyMessageDelayed(MSG_START_CAMERA, 500);

		//请求GPS位置
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_TIME, LOCATION_METERS, locListener);
		tvSpeed.setText(String.valueOf(Math.round(speed * 3.6)));
		mShakeDetector.start();
	}

	private void startCamera() {
		//配置Camera
		try {
			cameraInit();
		} catch (Exception e) {
			e.printStackTrace();
			showFinishDialog(getString(R.string.error_cannot_open_camera));
			return;
		}
		//startOrientationChangeListener();
		isFlashOn = false;
		setLightState(false);
		openLight(false);
		mCameraSurface.setCamera(mCamera);
		mCamera.setPreviewCallback(this);
		mCamera.startPreview();
		mCamera.autoFocus(this);

		initDrawView();
	}

	private void initDrawView() {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCameraSurface.getLayoutParams();
		params.width = mViewWidth;
		params.height = mViewHeight;
		mCameraSurface.setLayoutParams(params);
		if (Settings.getAssistanceMode()) {
			mDrawCarView.setLayoutParams(params);
			mDrawCarView.setVisibility(View.VISIBLE);
			if (Settings.getLaneRecognition()) {
				mDrawLaneView.setLayoutParams(params);
				mDrawLaneView.setVisibility(View.VISIBLE);
			} else {
				mDrawLaneView.setVisibility(View.GONE);
			}
			if (mAdasFrameProcessor == null) {
				mAdasFrameProcessor = new AdasFrameProcessor(this);
			}
			mAdasFrameProcessor.setCarLeft(adasCarLeft);
			mAdasFrameProcessor.setCarRight(adasCarRight);
			mAdasFrameProcessor.setCarTop(adasCarTop);
			mAdasFrameProcessor.setLaneTop(adasLaneTop);
			mAdasFrameProcessor.setCarScale(adasCarScale);
			mAdasFrameProcessor.setLaneScale(adasLaneScale);
			mAdasFrameProcessor.setCarMinSize(adasMinSize);
			mAdasFrameProcessor.setCarThreshold(adasThreshold);
		} else {
			mDrawLaneView.setVisibility(View.GONE);
			mDrawCarView.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		judgeStorage();
		final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!gpsEnabled) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
			dialog.setTitle(R.string.string_reminder);
			dialog.setMessage(R.string.gps_not_enable);
			dialog.setCancelable(false);
			dialog.setNegativeButton(R.string.string_cancel, null);
			dialog.setPositiveButton(R.string.string_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					enableLocationSettings();
				}
			}).show();
		}
	}

	private void enableLocationSettings() {
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
	}

	private void cameraInit() {
		if (mCamera == null) {
			mCamera = Camera.open();
		}
		//设置Camera最合适的预览尺寸
		Point point = DisplayUtil.getScreenMetrics(this);
		//CameraUtil.initParams(mCamera, point.x, point.y);
		CameraUtil2.initParams(mCamera, point.x, point.y);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//根据预览尺寸设置Surface的宽和高
		Camera.Parameters params = mCamera.getParameters();
//		focalLength = params.getFocalLength();//获取焦距
//		Log.i("zhuqichao", "焦距 = " + focalLength);
		float widthScale = (float) point.x / (float) params.getPreviewSize().width;
		float heightScale = (float) point.y / (float) params.getPreviewSize().height;
		if (widthScale <= heightScale) {
			mViewWidth = point.x;
			mViewHeight = (int) (params.getPreviewSize().height * widthScale);
		} else {
			mViewHeight = point.y;
			mViewWidth = (int) (params.getPreviewSize().width * heightScale);
		}
	}

	public void onClick(View v) {
		if (mCamera == null) {
			return;
		}
		Intent intent = new Intent();
		switch (v.getId()) {
			case R.id.btn_menu:
				isShowMenu = !isShowMenu;
				updateMenuState();
				break;
			case R.id.btn_about:
				intent.setClass(MainActivity.this, AboutActivity.class);
				startActivity(intent);
				break;
			case R.id.btn_save:
				intent.setClass(MainActivity.this, SaveActivity.class);
				startActivity(intent);
				break;
			case R.id.btn_setting:
				intent.setClass(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
				break;
			case R.id.btn_light:
				isFlashOn = !isFlashOn;
				setLightState(isFlashOn);
				openLight(isFlashOn);
				break;
			case R.id.take_photo:
				if (!Tools.setSavePath()) {
					return;
				}
				if (isTakingPic) {
					return;
				}
				if (isRecord) {
					isTakePic = true;
					return;
				}
				takePhoto();
				break;
			case R.id.start_recoder:
				if (!Tools.setSavePath()) {
					return;
				}
				if (!isRecord) {
					//startRecorder();
					//startCalculateProgress();
					AutoDeleteFile();
				} else {
					if (videoTimeMinLimit) {
						stopRecorder();
						videoTimeMinLimit = false;
					} else {
						showHintDialog(getResources().getString(R.string.min_video_time));
						//Tools.makeToast(R.string.min_video_time);
						return;
					}
				}
				break;
			case R.id.camera_surface:
				mCamera.autoFocus(this);
				if (isShowMenu) {//菜单出现时，让菜单消失
					isShowMenu = false;
					updateMenuState();
				}
				break;
			case R.id.close_screen:
				Tools.setBright(MainActivity.this, BRIGHTNESS);
				closeScreenLayout.setVisibility(View.INVISIBLE);
				break;
			case R.id.lock_screen:
				Tools.setBright(MainActivity.this, MIN_BRIGHTNESS);
				closeScreenLayout.setVisibility(View.VISIBLE);
				break;
			case R.id.lock_video:
				String path = Tools.getPath() + fileName + ".3gp";
				if (isLockVideo) {
					lockVideoButton.setImageResource(R.drawable.ic_unlock);
					Tools.setFileLockState(MainActivity.this, path, false);
				} else {
					lockVideoButton.setImageResource(R.drawable.ic_lock);
					Tools.setFileLockState(MainActivity.this, path, true);
				}
				isLockVideo = !isLockVideo;
				break;
			case R.id.img_edit_username:
			case R.id.tv_user_name:
				intent.setClass(MainActivity.this, UserInfoActivity.class);
				//intent = mDroidAccount.getSettingsIntent(getString(R.string.app_name));
				startActivityForResult(intent, REQUEST_CODE_USER_INFO);
				break;
			default:
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (REQUEST_CODE_USER_INFO == requestCode) {
			if (resultCode == RESULT_OK) {
				mUserInfo = Tools.getUserinfo(mDroiAccount.getOpenId());
				updateUserinfo();
			}
		}
	}

	private void startCalculateProgress() {
		progress = 0;
		startRecoder.setProgress(0);
		Thread thread = new Thread(progressThread);
		thread.start();
	}

	private void setLightState(boolean open) {
		if (open) {
			btnFlash.setImageResource(R.drawable.ic_more_light_off_selector);
			//open = false;
		} else {
			btnFlash.setImageResource(R.drawable.ic_more_light_on_selector);
			//open = true;
		}
	}

	private void openLight(boolean isOpen) {
		if (mCamera != null) {
			Camera.Parameters params = mCamera.getParameters();
			//params.setPictureFormat(PixelFormat.JPEG);
			if (isOpen) {
				params.setFlashMode(params.FLASH_MODE_TORCH);//打开闪光灯
			} else {
				params.setFlashMode(params.FLASH_MODE_OFF);//关闭闪光灯
			}
			mCamera.setParameters(params);
		}
	}

	private float getVideoTime() {
		float time = 0f;
		switch (Settings.getVideoTime()) {
			case 0:
				time = 3f;
				break;
			case 1:
				time = 5f;
				break;
		}
		return time;
	}

	//计算进度线程
	final Runnable progressThread = new Runnable() {
		public void run() {
			while (progress < 360.0f) {
				if (isRecord) {
					startRecoder.incrementProgress(startRecoder.getProgressEveryStep(getVideoTime()));
					progress = progress + startRecoder.getProgressEveryStep(getVideoTime());
					try {
						Thread.sleep(100);//每100ms
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					return;
				}
			}
			mHandler.sendEmptyMessage(MSG_FINISH_RECORDER);
		}

	};

	//关闭相机预览，释放资源
	private void closeCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	//停止录像，开启预览
	private void stopRecorder() {
		mHandler.removeMessages(MSG_CLOSE_SCREEN);
		closeMediaRecoder();
		resetView();
		mCamera.startPreview();
	}

	//录像结束时，将titlebar恢复，进度条清0.
	private void resetView() {
		setTitleBarWidth(120);
		lockVideoLayout.setVisibility(View.INVISIBLE);
		screenLayout.setVisibility(View.INVISIBLE);
		startRecoder.setProgress(0);
		startRecoder.setBackgroundResource(R.drawable.btn_video_selector);
		progress = 0;
	}

	private void setTitleBarWidth(int dpWidth) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) titleBar.getLayoutParams();
		params.width = Tools.dip2px(dpWidth);
		titleBar.setLayoutParams(params);
	}

	//关闭录像，释放资源并将camera锁住
	private void closeMediaRecoder() {
		isRecord = false;
		setMenuEnable();
		try {
			mediaRecorder.stop();
			mediaRecorder.release();
			mediaRecorder = null;
			mCamera.lock();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//扇形菜单出现/消失
	private void updateMenuState() {
		if (isShowMenu) {
			MenuRightAnimations.playTranslateAnim(300, btnFlash, -TRANS_X1, -TRANS_Y1, true, false);
			MenuRightAnimations.playTranslateAnim(300, btnSetting, -TRANS_X2, -TRANS_Y2, true, false);
			MenuRightAnimations.playTranslateAnim(300, btnSave, -TRANS_X2, TRANS_Y2, true, false);
			MenuRightAnimations.playTranslateAnim(300, btnAbout, -TRANS_X1, TRANS_Y1, true, false);
			MenuRightAnimations.playTranslateAnim(300, btnMenuIcon, 0, 0, false, false);
		} else {
			MenuRightAnimations.playTranslateAnim(300, btnFlash, 0, 0, false, true);
			MenuRightAnimations.playTranslateAnim(300, btnSetting, 0, 0, false, true);
			MenuRightAnimations.playTranslateAnim(300, btnSave, 0, 0, false, true);
			MenuRightAnimations.playTranslateAnim(300, btnAbout, 0, 0, false, true);
			MenuRightAnimations.playTranslateAnim(300, btnMenuIcon, 0, 0, true, false);
		}
	}

	//拍照，保存的图片分辨率为720p
	private void takePhoto() {
		isTakingPic = true;
		Camera.Parameters params = mCamera.getParameters();
		List<Camera.Size> mSupportSize = params.getSupportedPictureSizes();
		int photoSizeHeight = 720;
		for (Camera.Size item : mSupportSize) {
			if (photoSizeHeight == item.height) {
				params.setPictureSize(item.width, item.height);
				break;
			}
		}
		params.setPictureFormat(ImageFormat.JPEG);
		mCamera.setParameters(params);
		mCamera.takePicture(shutterCallback, null, jpeg);
	}

	private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
		public void onShutter() {
			mSoundPool.play(sounds[2], 1, 1, 0, 0, 1);
		}
	};

	private Camera.Size getSupportVideoSize() {
		List<Camera.Size> support = CameraUtil2.getSortParmameters(mCamera);
		if (support == null) {
			return null;
		}
		Camera.Size size = null;
		int groupSize = support.size() / 3;
		List<Camera.Size> lowQuality = support.subList(0, groupSize);
		List<Camera.Size> midQuality = support.subList(groupSize + 1, 2 * groupSize);
		List<Camera.Size> heighQuality = support.subList(2 * groupSize + 1, support.size());
		switch (Settings.getClarityLevel()) {
			case 0:
				size = lowQuality.get(lowQuality.size() - 1);
				break;
			case 1:
				size = midQuality.get(midQuality.size() - 1);
				break;
			case 2://最大分辨率选择与屏幕宽高一致的分辨率
				Point point = DisplayUtil.getScreenMetrics(this);
				for (int i = 0; i < heighQuality.size(); i++) {
					if (heighQuality.get(i).width == point.x && heighQuality.get(i).height == point.y) {
						size = heighQuality.get(i);
						break;
					}
				}
				if (size == null) {
					size = heighQuality.get(heighQuality.size() - 1);
				}
				break;
		}
		Log.i("wangchao", "video size==" + size.width + "," + size.height);
		return size;
	}


	/**
	 * 监听屏幕旋转的角度
	 */
	private final void startOrientationChangeListener() {
		OrientationEventListener mOrientationListener = new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int rotation) {
				if (((rotation > 225) && (rotation < 315))) {//屏幕正常横屏
					mCurrentOrient = false;
					if (mCamera != null) {
						setPicRotation(0);
					}

				} else if (((rotation > 45) && (rotation < 135))) {//屏幕旋转180度，横屏
					mCurrentOrient = true;
					if (mCamera != null) {
						setPicRotation(180);
					}
				}
			}
		};
		mOrientationListener.enable();
	}

	/**
	 * 设置图片exif信息
	 *
	 * @param degrees
	 */
	private void setPicRotation(int degrees) {
		//mCamera.setDisplayOrientation(degrees);
		Camera.Parameters par = mCamera.getParameters();
		par.setRotation(degrees);
		par.set("rotation", degrees);
		mCamera.setParameters(par);
	}

	//有对焦功能的设备，录像时对焦
	private void setVideoParams() {
		if (mCamera != null) {
			mCamera.stopPreview();
			Camera.Parameters params = mCamera.getParameters();
			size = getSupportVideoSize();
			params.setPreviewFormat(ImageFormat.NV21);
			setFocus(params);
			//params.set("orientation", "portrait");
			mCamera.setParameters(params);
			mCamera.unlock();
		}
	}

	//有自动对焦功能时，设置自动对焦
	private void setFocus(Camera.Parameters params) {
		List<String> list = params.getSupportedFocusModes();
		if (list.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		}
	}

	//开始录像
	private void startRecorder() {
		isRecord = true;
		setMenuEnable();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		fileName = format.format(new Date());
		if (mediaRecorder == null) {
			mediaRecorder = new MediaRecorder();
		} else {
			mediaRecorder.reset();
		}
		setVideoParams();
		mediaRecorder.setCamera(mCamera);
		//setVideoOrientation();
		if (size != null) {//获取到手机支持的视频分辨率
			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); //从照相机采集视频
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mediaRecorder.setVideoSize(size.width, size.height);
			mediaRecorder.setVideoFrameRate(15);
			mediaRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);
			setVideoMaxDuration();
			File videoFile = new File(Tools.getPath(), fileName + ".3gp"); //保存路径及名称
			mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
		} else {//获取不到手机支持的视频分辨率
			VideoSizeUnSupport();
			return;
		}
		try {
			mediaRecorder.prepare();//预期准备
			mediaRecorder.start();//开始刻录
		} catch (Exception e) {
			Log.i("wangchao", "直接设置videosize方式失败");
			VideoSizeUnSupport();
			return;
		}
		setTitleBarWidth(290);
		lockVideoLayout.setVisibility(View.VISIBLE);
		screenLayout.setVisibility(View.VISIBLE);
		lockVideoButton.setImageResource(R.drawable.ic_unlock);
		startRecoder.setBackgroundResource(R.drawable.btn_video_selector2);
		if (Settings.getAutoScreen()) {
			mHandler.sendEmptyMessageDelayed(MSG_CLOSE_SCREEN, LOCK_SCREEN_DELAY);
		}
		mHandler.sendEmptyMessageDelayed(MSG_START_RECORDER, 3000);//延时设置previewCallBack，防止回调消失。
	}

	/**
	 * 根据设置中的清晰度获取CamcorderProfile中的视频质量
	 *
	 * @return
	 */
	private int getVideoQulity() {
		int qulity = -1;
		switch (Settings.getClarityLevel()) {
			case 0:
				qulity = CamcorderProfile.QUALITY_480P;
				break;
			case 1:
				qulity = CamcorderProfile.QUALITY_720P;
				break;
			case 2:
				qulity = CamcorderProfile.QUALITY_1080P;
				break;
		}
		return qulity;
	}

	/**
	 * 当设置VideoSize崩溃时，调用此方法。（目前作用是适配三星S6）
	 */
	private void VideoSizeUnSupport() {
		mediaRecorder.reset();
		mediaRecorder.setCamera(mCamera);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); //从照相机采集视频
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mediaRecorder.setProfile(CamcorderProfile.get(getVideoQulity()));
		setVideoMaxDuration();
		File videoFile = new File(Tools.getPath(), fileName + ".3gp"); //保存路径及名称
		mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
		try {
			mediaRecorder.prepare();//预期准备
			mediaRecorder.start();
		} catch (Exception e) {
			Log.i("wangchao", "设置CamcorderProfile 方式失败");
		}
		setTitleBarWidth(290);
		lockVideoLayout.setVisibility(View.VISIBLE);
		screenLayout.setVisibility(View.VISIBLE);
		lockVideoButton.setImageResource(R.drawable.ic_unlock);
		if (Settings.getAutoScreen()) {
			mHandler.sendEmptyMessageDelayed(MSG_CLOSE_SCREEN, LOCK_SCREEN_DELAY);
		}
		mHandler.sendEmptyMessageDelayed(MSG_START_RECORDER, 3000);
	}

	/**
	 * 设置视频的最大时长
	 */
	private void setVideoMaxDuration() {
		if (Settings.getVideoTime() == 0) {
			mediaRecorder.setMaxDuration(3 * 60 * 1000);
		} else if (Settings.getVideoTime() == 1) {
			mediaRecorder.setMaxDuration(5 * 60 * 1000);
		}
	}

	/**
	 * 设置视频的成像方向（EXIF）
	 */
	private void setVideoOrientation() {
		if (mCurrentOrient) {
			mediaRecorder.setOrientationHint(180);
		} else {
			mediaRecorder.setOrientationHint(0);
		}
	}

	/**
	 * 空间不足时删除文件
	 */
	private void AutoDeleteFile() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (Settings.getMaxStorageSize() - FileSizeUtil.getFileOrFilesSize(Tools.getPath(), FileSizeUtil.SIZETYPE_MB) <= FREE_SPACE_LIMIT) {
					ArrayList<String> allFilePath = Tools.getAllFilePath(Tools.getPath());
					for (int i = 0; i < allFilePath.size(); i++) {
						if (Tools.getFileLockState(MainActivity.this, allFilePath.get(i))) {
							allFilePath.remove(allFilePath.get(i));
						}
					}
					if (allFilePath.size() == 0) {
						mHandler.sendEmptyMessage(MSG_DELETE_FAILED);
						return;
					}
					String deleteFilePath = allFilePath.get(allFilePath.size() - 1);
					Tools.deleteFile(deleteFilePath);
				}
				mHandler.sendEmptyMessage(MSG_DELETE_SUCCESS);
			}

		}).start();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setMenuEnable() {
		if (isRecord) {
			btnAbout.setEnabled(false);
			btnSave.setEnabled(false);
			btnSetting.setEnabled(false);
			btnAbout.setImageResource(R.drawable.ic_more_about_pressed);
			btnSave.setImageResource(R.drawable.ic_more_save_pressed);
			btnSetting.setImageResource(R.drawable.ic_more_setting_pressed);
			tvUserName.setEnabled(false);
			imgEditUser.setEnabled(false);
		} else {
			btnAbout.setEnabled(true);
			btnSave.setEnabled(true);
			btnSetting.setEnabled(true);
			btnAbout.setImageResource(R.drawable.ic_more_about);
			btnSave.setImageResource(R.drawable.ic_more_save);
			btnSetting.setImageResource(R.drawable.ic_more_setting);
			tvUserName.setEnabled(true);
			imgEditUser.setEnabled(true);
		}
	}

	//接受到电量广播后，显示剩余电量
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("zhuqichao", "action=" + intent.getAction());
			switch (intent.getAction()) {
				case Intent.ACTION_BATTERY_CHANGED:
					updateBatteryInfo(intent);
					break;
				case DroiAccount.INTENT_ACCOUNT_DELETED:
					startActivity(new Intent(MainActivity.this, SplashActivity.class).putExtra("from_logout", true));
					finish();
					break;
				default:
					break;
			}
		}
	};

	//获取手机电量
	private void updateBatteryInfo(Intent intent) {
		int rawlevel = intent.getIntExtra("level", 0);
		int scale = intent.getIntExtra("scale", 100);
		int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
		if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
			batteryEatting.setVisibility(View.VISIBLE);
		} else {
			batteryEatting.setVisibility(View.GONE);
		}
		if (rawlevel >= 0 && scale > 0) {
			int info = (rawlevel * 100) / scale;
			batteryInfo.setText(info + "%");
			mBatteryView.setPower(info);
		}
	}

	//释放GPS资源
	private void releaseGPS() {
		mLocationManager.removeUpdates(locListener);
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeMessages(MSG_CLOSE_SCREEN);
		mHandler.removeMessages(MSG_START_RECORDER);
		if (mediaRecorder != null) {
			closeMediaRecoder();
			resetView();
		}
		if (mCamera != null) {
			try {
				closeCamera();
			} catch (Exception e) {
			}
		}
		lockRelease();
		releaseGPS();
		mShakeDetector.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i("zhuqichao", "MainActivity.onDestroy");
		unregisterReceiver(mReceiver);
		lockRelease();
		System.gc();
	}

	private void lockAcquire() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "tag");
		if (!wakeLock.isHeld())
			wakeLock.acquire();
		KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		mKeyguardLock = mKeyguardManager.newKeyguardLock("");
		mKeyguardLock.disableKeyguard();
	}

	private void lockRelease() {
		if (wakeLock.isHeld()) {
			if (mKeyguardLock != null) {
				mKeyguardLock.reenableKeyguard();
				mKeyguardLock = null;
			}
			wakeLock.release();
		}
	}

	private void playLaneSound() {
		//车道偏离报警处理
		if (Settings.getLaneWarning()) {
			if (speed * 3.6 >= Settings.getMinimumSpeed() && deviateFlag[0] == 1) {
				if (canPlayLaneSound) {
					canPlayLaneSound = false;
					mHandler.sendEmptyMessageDelayed(MSG_CAN_PLAYLANESOUND, Settings.getWarnInterval() * 1000);
					mSoundPool.play(sounds[0], 1, 1, 0, 3, 1);
				}
			} else {
				mHandler.removeMessages(MSG_CAN_PLAYLANESOUND);
				canPlayLaneSound = true;
			}
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// At preview mode, the frame data will push to here.
		FrameLaneData = data;//.clone();
		FrameCarData = data;//.clone();
		if (isTakePic) {
			isTakePic = false;
			isTakingPic = true;
			shutterCallback.onShutter();
			previewSize = mCamera.getParameters().getPreviewSize();
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						yunImage = new YuvImage(FrameLaneData, ImageFormat.NV21, previewSize.width, previewSize.height, null);
						if (yunImage != null) {
							ByteArrayOutputStream strem = new ByteArrayOutputStream();
							yunImage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, strem);
							Bitmap bitmap = BitmapFactory.decodeByteArray(strem.toByteArray(), 0, strem.size());
							SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
							String fileName = format.format(new Date());
							File file = new File(Tools.getPath() + fileName + ".jpg");
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
							bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);//将图片压缩到流中
							bos.flush();//输出
							bos.close();//关闭
							strem.close();
							bitmap.recycle();
							mHandler.sendEmptyMessage(MSG_TAKE_PICTURE);
							isTakingPic = false;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}).start();

		}

		if (!Settings.getAssistanceMode()) {
			return;
		}

		if (!isProcessingLaneDetect && Settings.getLaneRecognition()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					isProcessingLaneDetect = true;
//					long startTime = System.currentTimeMillis();
					hasDetectedLane = mAdasFrameProcessor.adasDetectLane(previewWidth, previewHeight, FrameLaneData, laneData, deviateFlag);
//					laneTime.add(System.currentTimeMillis() - startTime);
//					if (laneTime.size() >= 20) {
//						Tools.outputLog("zhuqichao", "车道检测执行" + laneTime.size() + "次，平均耗时：" + getAverage(laneTime) + "毫秒");
//						laneTime.clear();
//					}

					if (hasDetectedLane == 0) {
						isProcessingLaneDetect = false;
						return;
					}
					mDrawLaneView.setLanePoints(laneData);
					mDrawLaneView.setIsRed(deviateFlag[0] == 1);
					mHandler.sendEmptyMessage(MSG_DRAW_LANE_VIEW);
					isProcessingLaneDetect = false;
				}
			}).start();
		}

		if (!isProcessingCarDetect && speed > 0) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					isProcessingCarDetect = true;
//					long startTime = System.currentTimeMillis();
					mAdasFrameProcessor.adasDetectCar(previewWidth, previewHeight, focalLength, sensorWidth, FrameCarData, carsData, carNum);
//					carTime.add(System.currentTimeMillis() - startTime);
//					if (carTime.size() >= 20) {
//						Tools.outputLog("zhuqichao", "车辆检测执行" + carTime.size() + "次，平均耗时：" + getAverage(carTime) + "毫秒");
//						carTime.clear();
//					}

					getNewCars();
					mDrawCarView.setCarList(newCarList);
					mDrawCarView.setSpeed(speed);
					mHandler.sendEmptyMessage(MSG_DRAW_CAR_VIEW);

					//车辆近距离报警处理
					if (Settings.getCollisionWarn() && carNum[0] > 0) {
						float min = carsData[4];
						for (int i = 1; i < carNum[0]; i++) {
							if (carsData[5 * i + 4] < min) {
								min = carsData[5 * i + 4];
							}
						}
						float time = min / speed;
						if (speed * 3.6 >= Settings.getMinimumSpeed() && time >= 0 && time <= alarmTime) {
							if (canPlayCarSound) {
								canPlayCarSound = false;
								mHandler.sendEmptyMessageDelayed(MSG_CAN_PLAYCARSOUND, 1000);
								mSoundPool.play(sounds[1], 1, 1, 0, 0, 1);
							}
						}
					}
					isProcessingCarDetect = false;
				}
			}).start();
		}
	}

	private void getNewCars() {
		lastCarList = newCarList;
		newCarList = new ArrayList<>();
		for (int i = 0; i < carNum[0]; i++) {
			int left = (int) (carsData[5 * i] * mViewWidth);
			int top = (int) (carsData[5 * i + 1] * mViewHeight);
			int right = (int) ((carsData[5 * i] + carsData[5 * i + 2]) * mViewWidth);
			int bottom = (int) ((carsData[5 * i + 1] + carsData[5 * i + 3]) * mViewHeight);
			int height = bottom - top;
			top -= height;
			bottom -= height;
			Car car = new Car(left, top, right, bottom, carsData[5 * i + 4]);
			Car temp = isCarExist(lastCarList, car);
			if (temp != null) {
				temp.distance = car.distance;
				newCarList.add(temp);
			} else {
				newCarList.add(car);
			}
		}
	}

	private Car isCarExist(ArrayList<Car> list, Car car) {
		for (Car item : list) {
			if (getDistance(item.x, item.y, car.x, car.y) < Math.pow(Tools.dip2px(carOffSet), 2)) {
				return item;
			}
		}
		return null;
	}

	//求两点间距离的平方
	private int getDistance(int x1, int y1, int x2, int y2) {
		return (int) (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

//	ArrayList<Long> laneTime = new ArrayList<>();
//	ArrayList<Long> carTime = new ArrayList<>();

	//计算list平均值
	private long getAverage(ArrayList<Long> list) {
		long sum = 0;
		for (float item : list) {
			sum += item;
		}
		return sum / list.size();
	}

	private final LocationListener locListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onLocationChanged(Location location) {
			if (!speedOpen) {
				speed = location.getSpeed();
				tvSpeed.setText(String.valueOf(Math.round(speed * 3.6)));
			}
			speedChanged();
			Tools.outputLog("zhuqichao", "位置更新："
					+ "\n纬度 = " + location.getLatitude()
					+ "\n经度 = " + location.getLongitude()
					+ "\n速度 = " + location.getSpeed());
		}
	};

	private void speedChanged() {
		if (speed * 3.6 > 60) {
			alarmTime = 0.3F;
		} else {
			alarmTime = 0.4F;
		}
		if (speed < 1) {
			speed = 0;
			mDrawCarView.setVisibility(View.INVISIBLE);
		} else {
			mDrawCarView.setVisibility(View.VISIBLE);
		}
	}

	private ShakeDetector.OnShakeListener onShakeListener = new ShakeDetector.OnShakeListener() {
		@Override
		public void onNoShake() {
			if (speed < 3) {
				speed = 0;
				speedChanged();
			}
		}
	};

	@Override
	public void onAutoFocus(boolean success, Camera camera) {

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (isRecord && Settings.getAutoScreen()) {
			Log.i("wangchao", "MSG_CLOSE_SCREEN");
			mHandler.removeMessages(MSG_CLOSE_SCREEN);
			mHandler.sendEmptyMessageDelayed(MSG_CLOSE_SCREEN, LOCK_SCREEN_DELAY);
		}
		return false;
	}

}
