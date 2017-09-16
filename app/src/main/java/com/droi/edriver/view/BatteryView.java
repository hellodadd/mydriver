package com.droi.edriver.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.droi.edriver.R;
import com.droi.edriver.app.WatchApplication;

/**
 * Created by ZhuQichao on 2016/3/10.
 */
public class BatteryView extends View {

	private int battery_width;
	private int battery_height;

	private int mPower;
	private Paint paint;
	private Rect rect;
	private Bitmap bmp;
	private Resources res;

	public BatteryView(Context context) {
		super(context);
		init();
	}

	public BatteryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		res = WatchApplication.getInstance().getResources();
		paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		bmp = BitmapFactory.decodeResource(res, R.drawable.bar_battery_0);
		battery_width = bmp.getWidth();
		battery_height = bmp.getHeight();
		int space = res.getDimensionPixelSize(R.dimen.battery_space);
		rect = new Rect(space, space, 0, battery_height - space);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(battery_width, battery_height);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		//先画外框
		canvas.drawBitmap(bmp, 0, 0, paint);

		//画电量
		rect.right = (int) ((battery_width - res.getDimensionPixelSize(R.dimen.battery_space2)) * mPower / 100.0f);
		if (mPower != 0) {
			canvas.drawRect(rect, paint);
		}
	}

	public void setPower(int power) {
		mPower = power;
		if (mPower < 0) {
			mPower = 0;
		}
		invalidate();
	}
}
