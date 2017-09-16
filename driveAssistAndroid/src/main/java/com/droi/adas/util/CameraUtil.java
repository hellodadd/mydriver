package com.droi.adas.util;

import java.util.List;

import android.graphics.ImageFormat;
import android.hardware.Camera;

public class CameraUtil {
	
    private static final int MAX_UNSPECIFIED = -1;
    
    /**
     * 根据屏幕的宽高初始华Camera的预览尺寸
     * @param camera
     * @param view
     */
	public static void initParams(Camera camera, int width, int height) {
		
		int maxWidth = MAX_UNSPECIFIED;
		int maxHeight = MAX_UNSPECIFIED;

		int calcWidth = 0;
        int calcHeight = 0;

        int maxAllowedWidth = (maxWidth != MAX_UNSPECIFIED && maxWidth < width)? maxWidth : width;
        int maxAllowedHeight = (maxHeight != MAX_UNSPECIFIED && maxHeight < height)? maxHeight : height;

        //得到Camera的参数，并由参数得到Camera的预览尺寸集
        Camera.Parameters params = camera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();

        //通过遍历所有尺寸，找到最接近允许尺寸（一般是屏幕宽高）的尺寸，作为Camera真正的预览尺寸
        if (sizes != null) {
            for (Camera.Size size : sizes) {
            	System.out.println("CameraUtil: " + size.width + " " + size.height);
                int tmpWidth = size.width;
                int tmpHeight = size.height;

                if (tmpWidth <= maxAllowedWidth && tmpHeight <= maxAllowedHeight) {
                    if (tmpWidth >= calcWidth && tmpHeight >= calcHeight) {
                        calcWidth = (int) tmpWidth;
                        calcHeight = (int) tmpHeight;
                    }
                }
            }
        	
            //根据上一步得到的最合适的尺寸设置Camera的预览尺寸 
            params.setPreviewFormat(ImageFormat.NV21);
            params.setPreviewSize(calcWidth, calcHeight);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(params);
        }
	}

}
