package com.droi.adas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class DrawLaneView extends View {

	private boolean hasDetectedLane = false;

	private Paint lanePaint;    //绘制车道线的画笔
	private Path lanePath;    //车道线路径
	private float[] lanePoints;    //对应的车道线的顶点（8个值，分别是四个顶的x和y坐标）

	private Line lineRight;
	private Line lineLeft;

	private int laneAlpha;    //画笔的透明度
	private int laneRed;    //ARGB中的红色值
	private int laneGreen;    //ARGB中的红色值
	private int laneBlue;    //ARGB中的红色值

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
		lanePath = new Path();
		lineLeft = new Line();
		lineRight = new Line();
		laneAlpha = 150;
		laneRed = 0;
		laneGreen = 255;
		laneBlue = 0;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 绘制车道线
		lanePaint.setARGB(laneAlpha, laneRed, laneGreen, laneBlue);
		lanePaint.setStyle(Paint.Style.FILL); //set solid
		lanePaint.setStrokeWidth(20);
		if (hasDetectedLane) {
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
			//canvas.drawLine(x1, y1, x2, y2, lanePaint);
			//canvas.drawLine(x3, y3, x4, y4, lanePaint);

			lineRight.a.x = x4;
			lineRight.a.y = y4;
			lineRight.b.x = x3;
			lineRight.b.y = y3;
			lineLeft.a.x = x1;
			lineLeft.a.y = y1;
			lineLeft.b.x = x2;
			lineLeft.b.y = y2;
			Point res = Line.interSection(lineLeft, lineRight);
			x1 = res.x;
			y1 = res.y;
			x4 = x1;
			y4 = y1;

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
			System.out.println(lanePath);
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

	public void setHasDetectedLane(boolean hasDetectedLane) {
		this.hasDetectedLane = hasDetectedLane;
	}

	public int getLaneAlpha() {
		return laneAlpha;
	}

	public void setLaneAlpha(int laneAlpha) {
		this.laneAlpha = laneAlpha;
	}

	public int getLaneRed() {
		return laneRed;
	}

	public void setLaneRed(int laneRed) {
		this.laneRed = laneRed;
	}

	public int getLaneGreen() {
		return laneGreen;
	}

	public void setLaneGreen(int laneGreen) {
		this.laneGreen = laneGreen;
	}

	public int getLaneBlue() {
		return laneBlue;
	}

	public void setLaneBlue(int laneBlue) {
		this.laneBlue = laneBlue;
	}

	public float[] getLanePoints() {
		return lanePoints;
	}

	public void setLanePoints(float[] lanePoints) {
		this.lanePoints = lanePoints;
	}

}
