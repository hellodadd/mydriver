package com.droi.edriver.util;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;

public class DisplayUtil {

    /**
     * 根据传入的Context得到屏幕的宽和高，并存储在Point中
     * @param context
     */
    public static Point getScreenMetrics(Context context){    
        DisplayMetrics dm =context.getResources().getDisplayMetrics();    
        int w_screen = dm.widthPixels;    
        int h_screen = dm.heightPixels;    
        return new Point(w_screen, h_screen);    
    } 
}
