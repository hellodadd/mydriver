package com.droi.adas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class DrawCarView extends View {
	
	private Paint 	lanePaint;	//绘制车道线的画笔
	private Path 	lanePath;	//车道线路径
    private float[] carsData;	//检测到的车辆数据
    private int 	carNum;		//检测到的车辆数

	private int 	laneAlpha;	//画笔的透明度
    
    private Rect 	rect;		//绘制车辆的矩形
    
	public DrawCarView(Context context) {
		super(context);
		init();
	}

	public DrawCarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

    private void init() {
    	// 初始化
		lanePaint = new Paint(Paint.ANTI_ALIAS_FLAG); 
		lanePath = new Path();
		rect = new Rect();
		
		laneAlpha = 150;
	}

	@Override  
    protected void onDraw(Canvas canvas) {  
        super.onDraw(canvas);

        // 绘制检测到的车辆
    	lanePaint.setARGB(laneAlpha, 0, 0, 255);
    	lanePaint.setStyle(Paint.Style.STROKE); //set hollow
    	lanePaint.setStrokeWidth(2);
    	lanePaint.setTextSize(45);
        for (int i = 0; i < carNum; i++) {
        	lanePath.reset();
            rect.left = (int)(carsData[5*i] * getWidth());
    		rect.top = (int)(carsData[5*i+1] * getHeight());
    		rect.right = (int)((carsData[5*i] + carsData[5*i+2]) * getWidth());
    		rect.bottom = (int)((carsData[5*i+1] + carsData[5*i+3]) * getHeight());
        	canvas.drawRect(rect,lanePaint);
        	canvas.drawText(String.format( "%.1f", carsData[5*i+4] ), (int)(carsData[5*i] * getWidth()),
        				(int)(carsData[5*i+1] * getHeight()) - 40, lanePaint);
        }
    }
    
    public float[] getCarsData() {
		return carsData;
	}

	public void setCarsData(float[] carData) {
		this.carsData = carData;
	}

	public int getCarNum() {
		return carNum;
	}

	public void setCarNum(int carNum) {
		this.carNum = carNum;
	}
}
