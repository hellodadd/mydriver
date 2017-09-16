package com.droi.account.login;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.droi.account.BitmapUtils;
import com.droi.account.DebugUtils;
import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;

public class AvatarUtils {

	private static final String TAG = "AvatarUtils";
	
	private static final String DIR_NAME = Utils.DOWNLOAD_DIR + "/"+ Constants.ACCOUNT_TYPE;;
	private static final String AVATAR_NAME = "logo.png";
	
	private static String avatarFilePath ;
	
    public static void deleteUserAvatar(){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "deleteUserAvatar");
		}
      	String logoFile = getSDPath() + "/"+DIR_NAME+"/" + AVATAR_NAME;
        if (new File(logoFile).exists()) {
            new File(logoFile).delete();
        }
           
        avatarFilePath = null;
    }
    
    public static String getAvatarPath(){
    	return avatarFilePath;
    }
    
    //user logo
    public static void downloadUserAvatar(String avatarUrl){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "downloadUserAvatar avatarUrl : " + avatarUrl);
		}
    	if(TextUtils.isEmpty(avatarUrl)){
    		return;
    	}
    	//download from server
    	if(DebugUtils.DEBUG){
    		DebugUtils.i(TAG, "downloadUserAvatar : " + avatarUrl);
    	}
    	Bitmap bitmap = getBitmap(avatarUrl);
    	if(bitmap != null){
    		saveBitmap(bitmap);
    	}else{
    		avatarFilePath = null;
    	}
    	/*
    	if(bitmap != null){
    		String urlDir = getSDPath() + "/"+DIR_NAME+"/";
    		if(DebugUtils.DEBUG){
    			DebugUtils.i(TAG, urlDir);
    		}
    		File file = new File(urlDir);
    		
    		if(!file.exists()){
    			file.mkdir();
    		}
    		
    		File avatar = new File(urlDir, AVATAR_NAME);
    		avatarFilePath = avatar.toString();
    		try {
    			//This method returns true if it creates a file, false if the file already existed
    			avatar.setWritable(true);
				if(!avatar.createNewFile()){
					//the file already existed
					if(DebugUtils.DEBUG){
						DebugUtils.i(TAG, "File already exists!");
					}
				}
				
				FileOutputStream out = new FileOutputStream(avatar);
				out.write(BitmapUtils.createBitByteArray(bitmap));
				out.flush();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//failed to update logo
				avatarFilePath = null;
			}
    	}else{
    		//invalid logo on server
    		avatarFilePath = null;
    	}*/
    	
    	
    }    
    
    public static void saveBitmap(Bitmap bitmap){
    	if(bitmap != null){
    		String urlDir = getSDPath() + "/"+DIR_NAME+"/";
    		if(DebugUtils.DEBUG){
    			DebugUtils.i(TAG, urlDir);
    		}
    		File file = new File(urlDir);
    		
    		if(!file.exists()){
    			file.mkdirs();
    		}
    		
    		File avatar = new File(urlDir, AVATAR_NAME);
    		avatarFilePath = avatar.toString();
    		try {
    			//This method returns true if it creates a file, false if the file already existed
    			avatar.setWritable(true);
				if(!avatar.createNewFile()){
					//the file already existed
					if(DebugUtils.DEBUG){
						DebugUtils.i(TAG, "File already exists!");
					}
				}
				
				FileOutputStream out = new FileOutputStream(avatar);
				out.write(BitmapUtils.createBitByteArray(bitmap));
				out.flush();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//failed to update logo
				avatarFilePath = null;
			}
    	}else{
    		//invalid logo on server
    		avatarFilePath = null;
    	}
    	
    	
    }    
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist){
            sdDir = Environment.getExternalStorageDirectory();
        }else {
            return null;
        }
        return sdDir.toString();
    }
    
    public static Bitmap getBitmap(String urlStr){
    	Bitmap bitmap = null;
    	try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setDoInput(true);
			conn.setConnectTimeout(4*1000);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return bitmap;
    }
    
    private String getAbsoluteImagePath(Activity context, Uri uri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.managedQuery(uri, proj, null, null, null);
        int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
}
}
