package com.droi.account;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;

public class BitmapUtils {

    public static byte[] createBitByteArray(Bitmap bitmap){
        if (null == bitmap){
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        return os.toByteArray();
    }
    
    
}
