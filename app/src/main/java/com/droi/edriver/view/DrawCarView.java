package com.droi.edriver.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.droi.edriver.R;
import com.droi.edriver.app.WatchApplication;
import com.droi.edriver.bean.Car;
import com.droi.edriver.ui.MainActivity;

import java.util.ArrayList;

public class DrawCarView extends View {

	private Context mContext;

	private Paint carPaint;    //绘制车辆的画笔
	private float speed;      //车速
	private ArrayList<Car> carList = new ArrayList<>();

	private Rect rect;        //绘制车辆的矩形

	private Bitmap bmpGreen;   //绿色气泡
	private Bitmap bmpOrang;   //橙色气泡
	private Bitmap bmpRed;   //红色气泡

	public DrawCarView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public DrawCarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	private void init() {
		// 初始化
		carPaint = new Paint();
		rect = new Rect();
		carPaint.setColor(Color.WHITE);
		carPaint.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/CAR_TIME.TTF"));

		Resources res = WatchApplication.getInstance().getResources();
		bmpGreen = BitmapFactory.decodeResource(res, R.drawable.ic_indicate_green);
		bmpOrang = BitmapFactory.decodeResource(res, R.drawable.ic_indicate_orange);
		bmpRed = BitmapFactory.decodeResource(res, R.drawable.ic_indicate_red);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (speed < 1) {
			setVisibility(View.INVISIBLE);
		} else {
			setVisibility(View.VISIBLE);
		}

		// 绘制检测到的车辆
		for (Car car : carList) {
			rect.left = car.left;
			rect.top = car.top;
			rect.right = car.right;
			rect.bottom = car.bottom;
			carPaint.setTextSize(car.height / 3);
			if (speed <= 0) {
				canvas.drawBitmap(bmpGreen, null, rect, carPaint);
				canvas.drawText(String.format("%.1f%s", car.distance, "m"), rect.left + car.height / 6, rect.bottom - car.height / 2.5f, carPaint);
			} else {
				float time = car.distance / speed;
				if (time <= MainActivity.alarmTime) {
					canvas.drawBitmap(bmpRed, null, rect, carPaint);
				} else if (time <= MainActivity.alarmTime + 0.5) {
					canvas.drawBitmap(bmpOrang, null, rect, carPaint);
				} else if (time < 10) {
					canvas.drawBitmap(bmpGreen, null, rect, carPaint);
				}
				if (time < 10) {
					canvas.drawText(String.format("%.1f%s", time, "s"), rect.left + car.height / 6, rect.bottom - car.height / 2.5f, carPaint);
				}
			}
		}
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public void setCarList(ArrayList<Car> list) {
		carList = list;
	}
}
