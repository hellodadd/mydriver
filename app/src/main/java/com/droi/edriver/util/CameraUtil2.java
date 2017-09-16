package com.droi.edriver.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;

import com.droi.edriver.ui.DebugActivity;
import com.droi.edriver.ui.MainActivity;

public class CameraUtil2 {

	/**
	 * 根据屏幕的宽高初始华Camera的预览尺寸
	 *
	 * @param camera
	 */
	public static void initParams(Camera camera, int width, int height) {

		//得到Camera的参数，并由参数得到Camera的预览尺寸集
		Camera.Parameters params = camera.getParameters();
		if (MainActivity.previewWidth != 0 && MainActivity.previewHeight != 0) {
			params.setPreviewFormat(ImageFormat.NV21);
			params.setPreviewSize(MainActivity.previewWidth, MainActivity.previewHeight);
			//params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			camera.setParameters(params);
			return;
		}
		List<Camera.Size> sizes = getSortParmameters(camera);

		//通过遍历所有尺寸，找到最接近允许尺寸（一般是屏幕宽高）的尺寸，作为Camera真正的预览尺寸
		if (sizes != null) {
			for (Camera.Size size : sizes) {
				//Log.i("zhuqichao", "CameraUtil2: " + size.width + " " + size.height);
				if ((float) size.width / size.height == (float) width / height && (size.width > 1000 || size.height > 1000)) {
					MainActivity.previewWidth = size.width;
					MainActivity.previewHeight = size.height;
					break;
				}
			}
			if (MainActivity.previewWidth == 0 || MainActivity.previewHeight == 0) {
				CameraUtil.initParams(camera, width, height);
				MainActivity.previewWidth = camera.getParameters().getPreviewSize().width;
				MainActivity.previewHeight = camera.getParameters().getPreviewSize().height;
				DebugActivity.showLog("初始化参数: ");
				return;
			}
			DebugActivity.showLog("初始化参数: ");
			//根据上一步得到的最合适的尺寸设置Camera的预览尺寸
			params.setPreviewFormat(ImageFormat.NV21);
			params.setPreviewSize(MainActivity.previewWidth, MainActivity.previewHeight);
			//params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			camera.setParameters(params);
		}
	}

	public static List<Camera.Size> getSortParmameters(Camera mCamera) {
		List<Camera.Size> support = mCamera.getParameters().getSupportedVideoSizes();
		if (support == null) {
			return null;
		}
		//排序
		Collections.sort(support, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size lhs, Camera.Size rhs) {
				if (lhs.width > rhs.width) {
					return 1;
				} else if (lhs.width == rhs.width) {
					if (lhs.height > rhs.height) {
						return 1;
					} else {
						return -1;
					}
				} else {
					return -1;
				}
			}
		});
		return support;
	}

}
