package com.droi.edriver.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.droi.edriver.ui.MainActivity;

public class DrawLaneView extends View {

	private Paint lanePaint;    //绘制车道线的画笔
	private Path lanePath;    //车道线路径
	private float[] lanePoints;    //对应的车道线的顶点（8个值，分别是四个顶的x和y坐标）

	private Line lineRight;
	private Line lineLeft;

	private int redColor1 = Color.parseColor("#00ff2a00");
	private int redColor2 = Color.parseColor("#ccff2a00");
	private int greenColor1 = Color.parseColor("#000cff00");
	private int greenColor2 = Color.parseColor("#cc0cff00");
	private boolean isRed; //是否超出车道
	private Handler mHandler;

	public DrawLaneView(Context context) {
		super(context);
		init();
	}

	public DrawLaneView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		// 初始化
		lanePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		lineLeft = new Line();
		lineRight = new Line();
		lanePath = new Path();
		lanePaint.setStyle(Paint.Style.FILL); //set solid
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (lanePoints == null) {
			return;
		}

		// 绘制车道线
		lanePath.reset();
		// right top
		float x1 = lanePoints[0] * getWidth();
		float y1 = lanePoints[1] * getHeight();
		// right bottom
		float x2 = lanePoints[2] * getWidth();
		float y2 = lanePoints[3] * getHeight();
		// left bottom
		float x3 = lanePoints[4] * getWidth();
		float y3 = lanePoints[5] * getHeight();
		// left top
		float x4 = lanePoints[6] * getWidth();
		float y4 = lanePoints[7] * getHeight();

		//Log.i("zhuqichao", "x1=" + x1 + ", x2=" + x2 + ", x3=" + x3 + ", x4=" + x4 + "; y1=" + y1 + ", y2=" + y2 + ", y3=" + y3 + ", y4=" + y4);

		if (y2 > y1 && y3 > y4 && x1 > x4 && x2 > x3 && y1 == y4 && y2 == y3 && x2 > x1 && x3 < x4) {
			mHandler.sendEmptyMessage(MainActivity.MSG_PLAY_LANE_SOUND);
			lineRight.a.x = x4;
			lineRight.a.y = y4;
			lineRight.b.x = x3;
			lineRight.b.y = y3;
			lineLeft.a.x = x1;
			lineLeft.a.y = y1;
			lineLeft.b.x = x2;
			lineLeft.b.y = y2;
			Point res = Line.interSection(lineLeft, lineRight);
			if (res.x > getWidth() / 3 && res.x < getWidth() / 3 * 2 && res.y > 0) {
				x1 = res.x;
				y1 = res.y;
				x4 = x1;
				y4 = y1;
			}
			LinearGradient shader;
			if (isRed) {
				shader = new LinearGradient(x4, y4, x4, y3, redColor1, redColor2, Shader.TileMode.MIRROR);
			} else {
				shader = new LinearGradient(x4, y4, x4, y3, greenColor1, greenColor2, Shader.TileMode.MIRROR);
			}
			lanePaint.setShader(shader);

			lanePath.moveTo(x1, y1);
			lanePath.lineTo(x2, y2);
			if (y2 < getHeight()) {
				lanePath.lineTo(getWidth(), getHeight());
			}
			if (y3 < getHeight()) {
				lanePath.lineTo(0, getHeight());
			}
			lanePath.lineTo(x3, y3);
			lanePath.lineTo(x4, y4);
			canvas.drawPath(lanePath, lanePaint);
		}
	}

	public static class Point {
		public float x;
		public float y;

		public Point() {
		}

		public Point(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	public static class Line {
		public Point a;
		public Point b;

		public Line() {
			a = new Point();
			b = new Point();
		}

		public Line(Point a, Point b) {
			this.a = a;
			this.b = b;
		}

		public Line(float x1, float y1, float x2, float y2) {
			this.a = new Point(x1, y1);
			this.b = new Point(x2, y2);
		}

		//求两直线的交点，斜率相同的话res=u.a
		public static Point interSection(Line u, Line v) {
			Point res = u.a;
			float t = ((u.a.x - v.a.x) * (v.b.y - v.a.y) - (u.a.y - v.a.y) * (v.b.x - v.a.x))
					/ ((u.a.x - u.b.x) * (v.b.y - v.a.y) - (u.a.y - u.b.y) * (v.b.x - v.a.x));
			res.x += (u.b.x - u.a.x) * t;
			res.y += (u.b.y - u.a.y) * t;
			return res;
		}
	}

	public void setIsRed(boolean isRed) {
		this.isRed = isRed;
	}

	public float[] getLanePoints() {
		return lanePoints;
	}

	public void setLanePoints(float[] lanePoints) {
		this.lanePoints = lanePoints;
	}

	public void setHandler(Handler handler) {
		mHandler = handler;
	}
}
