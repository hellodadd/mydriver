package com.droi.edriver.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

/**
 * Created by ZhuQichao on 2016/3/25.
 */
public class BitmapUtils {

	public static byte[] zoomBitmap(byte[] src, float scale) {
		return getBytesFromBitmap(zoomBitmap(getBitmapFromBytes(src), scale));
	}

	public static Bitmap zoomBitmap(Bitmap bitmap, float scale) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = w / scale;
		float scaleHeight = h / scale;
		matrix.postScale(scaleWidth, scaleHeight);
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
	}

	public static byte[] getBytesFromBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		// 实例化字节数组输出流
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);// 压缩位图
		return baos.toByteArray();// 创建分配字节数组
	}

	public static Bitmap getBitmapFromBytes(byte[] data) {
		if (data == null) {
			return null;
		}
		return BitmapFactory.decodeByteArray(data, 0, data.length);// 从字节数组解码位图
	}
}
