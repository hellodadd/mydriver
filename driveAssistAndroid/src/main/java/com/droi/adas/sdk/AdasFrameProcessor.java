package com.droi.adas.sdk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

public class AdasFrameProcessor {

	private static final String TAG = "droi.adas.sdk.ProcessFrame";
	
	private Context mContext;		//传入Context用以加载资源文件
	private File 	mCascadeFile;	//车辆检测的资源文件（样本库）
 	
	/**
	 * 构造器
	 * @param context Activity中创建此类时传入的Context
	 */
	public AdasFrameProcessor(Context context) {
		this.mContext = context;
		loadCasadeFile();
	}

	/**
	 * 检测车道线
	 * @param width			Camera的预览尺寸宽
	 * @param height		Camera的预览尺寸高
	 * @param FrameData		Camera的预览数据
	 * @param laneData		检测到的车道线，总共8个值，分别是右上角、右下角、左下角及左上角的x和y坐标
	 * @param flagData		此标志用于判断是否偏离车道，1表示偏离，0表示未偏离
	 * @return				返回值为整型，1表示检测到车道线，否则为0
	 */
	public int adasDetectLane(int width, 
			int 	height, 
			byte[] 	FrameData,
			float[] laneData, 
			int[] 	flagData) {
		return detectLane(width, 
				height, 
				FrameData,
				laneData, 
				flagData);
	}
	
	/**
	 * 车辆检测
	 * @param width			Camera的预览尺寸宽
	 * @param height		Camera的预览尺寸高
	 * @param focalLength	焦距
	 * @param sensorWidth	摄像头芯片的宽
	 * @param FrameData		Camera的预览数据
	 * @param carsData		检测到的车辆
	 * @param flagData		此值表示检测到的车辆数
	 * @return 返回值为整型，暂不表示任何意思
	 */
	public int adasDetectCar(int width, 
			int 	height, 
			float	focalLength,
			float	sensorWidth,
			byte[] 	FrameData, 
			float[] carsData, 
			int[] 	flagData) {
		return detectCar(width, 
				height, 
				focalLength,
				sensorWidth,
				FrameData, 
				carsData, 
				flagData, 
				this.mCascadeFile.getAbsolutePath());
	}
	
	private void loadCasadeFile() {
        try {
        	InputStream is = this.mContext.getAssets().open("cascade.xml");
            File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "cascade.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	private native int detectLane(int width, 
			int 	height, 
			byte[] 	FrameData,
			float[] laneData, 
			int[] 	flagData);
	private native int detectCar(int width, 
			int 	height, 
			float	focalLength,
			float	sensorWidth,
			byte[] 	FrameData, 
			float[] carsData, 
			int[] 	flagData, 
			String 	fileName);
	public native void setCarLeft(float leftShift);
	public native void setCarRight(float rightShift);
	public native void setCarTop(float topShift);
	public native void setLaneTop(float topShift);
	public native void setCarScale(float carScale);
	public native void setLaneScale(float laneScale);
	public native void setCarMinSize(int minSize);
	public native void setCarThreshold(int threshold);
}
