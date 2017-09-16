package com.droi.edriver.bean;

/**
 * Created by ZhuQichao on 2016/5/9.
 */
public class Car {
	public int x;//中心点x坐标
	public int y;//中心点y坐标
	public int left;
	public int top;
	public int right;
	public int bottom;
	public int width;
	public int height;
	public float distance;
	public int flag = 3;//出现次数

	public Car(int left, int top, int right, int bottom, float distance) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.distance = distance;
		x = (right + left) / 2;
		y = (bottom + top) / 2;
		height = right - left;
		width = bottom - top;
	}

	public void flagUp() {
		flag += 3;
		if (flag > 9) {
			flag = 9;
		}
	}

	public void flagDown() {
		flag--;
	}
}
