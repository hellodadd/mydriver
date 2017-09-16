package com.droi.edriver.util;

import android.graphics.ImageFormat;
import android.hardware.Camera;

import java.util.List;

public class CameraUtil {

	/**
	 * 根据屏幕的宽高初始华Camera的预览尺寸
	 *
	 * @param camera
	 * @param
	 */
	public static void initParams(Camera camera, int width, int height) {

		//得到Camera的参数，并由参数得到Camera的预览尺寸集
		Camera.Parameters params = camera.getParameters();
		List<Camera.Size> sizes = params.getSupportedPreviewSizes();
		Camera.Size size = getCloselyPreSize(width,height,sizes);
		//根据上一步得到的最合适的尺寸设置Camera的预览尺寸
		params.setPreviewFormat(ImageFormat.NV21);
		params.setPreviewSize(size.width, size.height);
		//params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
		camera.setParameters(params);
		}
	

	/** 通过对比得到与宽高比最接近的尺寸（如果有相同尺寸，优先选择）
	*
	* @param surfaceWidth
	*            需要被进行对比的原宽
	* @param surfaceHeight
	*            需要被进行对比的原高
	* @param preSizeList
	*            需要对比的预览尺寸列表
	* @return 得到与原宽高比例最接近的尺寸
	*/
	private static Camera.Size getCloselyPreSize(int surfaceWidth, int surfaceHeight,
											List<Camera.Size> preSizeList) {

		int ReqTmpWidth;
		int ReqTmpHeight;
		// 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
		if (surfaceWidth<surfaceHeight) {
			ReqTmpWidth = surfaceHeight;
			ReqTmpHeight = surfaceWidth;
		} else {
			ReqTmpWidth = surfaceWidth;
			ReqTmpHeight = surfaceHeight;
		}
		//先查找preview中是否存在与surfaceview相同宽高的尺寸
		for(Camera.Size size : preSizeList){
			if((size.width == ReqTmpWidth) && (size.height == ReqTmpHeight)){
				return size;
			}
		}

		// 得到与传入的宽高比最接近的size
		float reqRatio = ((float) ReqTmpWidth) / ReqTmpHeight;
		float curRatio, deltaRatio;
		float deltaRatioMin = Float.MAX_VALUE;
		Camera.Size retSize = null;
		for (Camera.Size size : preSizeList) {
			curRatio = ((float) size.width) / size.height;
			deltaRatio = Math.abs(reqRatio - curRatio);
			if (deltaRatio < deltaRatioMin) {
				deltaRatioMin = deltaRatio;
				retSize = size;
			}
		}

		return retSize;
	}
}
