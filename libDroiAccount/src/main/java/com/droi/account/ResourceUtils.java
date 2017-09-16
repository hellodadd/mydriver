package com.droi.account;

import android.content.Context;
import android.util.DisplayMetrics;

public class ResourceUtils {

	public static int dp2px(Context context, int dp) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		int px = (int) (dp * dm.density + 0.5D);
		return px;
	}
}
