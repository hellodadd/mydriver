package com.droi.edriver.updateself;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * 封装下载的逻辑
 */
public class Downloader{
    private String urlstr;// 下载的地址

    private String localfile;// 保存路径

    private int fileSize;// 所要下载的文件的大小

    private Handler mHandler;

    private int notification_flag;

    private String appName;

    private byte[] bitmap;

    private String size;

    private String version;

    private Context context;

    Cursor mcursor = null;

    public Downloader(){
    	
    }

    public Downloader(Context context, String urlstr, String localfile, Handler mHandler, int notification_flag, String appName, byte[] bitmap, String size,
            String version){
        super();
        this.urlstr = urlstr;
        this.localfile = localfile;
        this.mHandler = mHandler;
        this.notification_flag = notification_flag;
        this.appName = appName;
        this.bitmap = bitmap;
        this.size = size;
        this.context = context;
        this.version = version;
    }

    // 1,获取文件大小，如果获取不到，提示用户
    public boolean init(){
        try{
            URL url = new URL(urlstr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            int num = 0;// 获取三次
            while (num < 3){                
                fileSize = connection.getContentLength();// 获取文件大小
                Log.e("zhuqichao", "fileSize---" + fileSize);
                num++;
                if (fileSize > 0){
                    break;
                }
            }
            if (fileSize <= 0){
                return false;
            }
            File file = new File(localfile);
            if (!file.exists()){
                file.createNewFile();
            }
            // 本地访问文件
            RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
            accessFile.setLength(fileSize);
            accessFile.close();
            connection.disconnect();
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    // 2,下载软件，后台下载，在通知栏中更新下载进度
    public void downlaod(){
        int startPos = 0;
        int endPos = fileSize;
        long surplus_size = 0;
        Log.e("zhuqichao", "startPos:" + startPos + "--endPos:" + endPos);
        HttpURLConnection connection = null;
        RandomAccessFile randomAccessFile = null;
        InputStream is = null;
        try{
            URL url = new URL(urlstr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(4000);
            connection.setRequestMethod("GET");
            // 设置范围，格式为Range：bytes x-y;
            connection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);

            randomAccessFile = new RandomAccessFile(localfile, "rwd");
            randomAccessFile.seek(startPos);
            // 将要下载的文件写到保存在保存路径下的文件中
            is = connection.getInputStream();
            // int updateTotalSize = connection.getContentLength() ;
            byte[] buffer = new byte[4096];
            int length = -1;
            int downlaod_sun = 0;
            
            while ((length = is.read(buffer)) != -1){
                randomAccessFile.write(buffer, 0, length);
                downlaod_sun += length;
                surplus_size = (long)(endPos - downlaod_sun + 1);
                // Log.e("sunlei", "downlaod_sun:" + downlaod_sun + "--");
                // 更新数据库中的下载信息
                if ((downlaod_sun * 100 / endPos >= 100) || ((downlaod_sun * 100 / endPos) % 5 == 0)){
                    // 通知更新进度
                    Message message = Message.obtain();
                    message.what = 2;
                    message.obj = urlstr + ","+ surplus_size;
                    message.arg1 = notification_flag;
                    message.arg2 = downlaod_sun * 100 / endPos >= 100 ? 100 : downlaod_sun * 100 / endPos;
                    mHandler.sendMessage(message);
                }
            }
            // 用消息将下载信息传给handle(已经下载完成了)，保存在db
            AppInfo gameInfo = new AppInfo();
            gameInfo.setAppName(appName);
            gameInfo.setLocalFile(localfile);
            gameInfo.setUrl(urlstr);
            gameInfo.setSize(size);
            gameInfo.setBitmap(bitmap);
            gameInfo.setVersion(version);
            gameInfo.setFlag(2);
            gameInfo.setAppPackageName(getAppPackage(context, localfile));

			if (downlaod_sun >= endPos) {
	            Message message = new Message();
	            message.what = 3;
	            message.obj = urlstr;
	            mHandler.sendMessage(message);
			}
        }catch(SocketTimeoutException e){
           
        }catch(InterruptedIOException e){
            
        }catch(Exception e){
            try{
                if (is != null){
                    is.close();
                }
                if (null != randomAccessFile){
                    randomAccessFile.close();
                }
            }catch(IOException e1){
                e1.printStackTrace();
            }
            connection.disconnect();
            e.printStackTrace();
        } finally{
            try{
                if (null != is){
                    is.close();
                }
                if (null != randomAccessFile){
                    randomAccessFile.close();
                }
                if (null != connection){
                    connection.disconnect();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public String getUrlstr(){
        return urlstr;
    }

    public void setUrlstr(String urlstr){
        this.urlstr = urlstr;
    }

    public String getLocalfile(){
        return localfile;
    }

    public void setLocalfile(String localfile){
        this.localfile = localfile;
    }

    public int getFileSize(){
        return fileSize;
    }

    public void setFileSize(int fileSize){
        this.fileSize = fileSize;
    }

    public Handler getmHandler(){
        return mHandler;
    }

    public void setmHandler(Handler mHandler){
        this.mHandler = mHandler;
    }

    public int getNotification_flag(){
        return notification_flag;
    }

    public void setNotification_flag(int notification_flag){
        this.notification_flag = notification_flag;
    }

    public String getAppName(){
        return appName;
    }

    public void setAppName(String appName){
        this.appName = appName;
    }

    public String getAppPackage(Context context, String archiveFilePath){
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
        String packageName = null;
        if (info != null){
            ApplicationInfo appInfo = info.applicationInfo;
            packageName = appInfo.packageName; // 得到安装包名称
        }
        return packageName;
    }
}
