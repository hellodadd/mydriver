package com.droi.account.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ScrollView;


public class CustomScrollView extends ScrollView {

	private View inner;

	private float y;

	private Rect normal = new Rect();

	private boolean isCount = false;

	private boolean isMoveing = false;

	private ImageView imageView;

	private int initTop, initbottom;
	private int top, bottom;

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}

	public CustomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	@Override
	protected void onFinishInflate() {
		if (getChildCount() > 0) {
			inner = getChildAt(0);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (inner != null) {
			//commOnTouchEvent(ev);
		}
		return super.onTouchEvent(ev);
	}

	public void commOnTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			top = initTop = imageView.getTop();
			bottom = initbottom = imageView.getBottom();
			break;

		case MotionEvent.ACTION_UP:

			isMoveing = false;
			if (isNeedAnimation()) {

				animation();

			}
			break;
		case MotionEvent.ACTION_MOVE:

			final float preY = y;

			float nowY = ev.getY();
			int deltaY = (int) (nowY - preY);
			if (!isCount) {
				deltaY = 0;
			}

			if (deltaY < 0 && top <= initTop)
				return;

			isNeedMove();

			if (isMoveing) {
				if (normal.isEmpty()) {
					normal.set(inner.getLeft(), inner.getTop(),
							inner.getRight(), inner.getBottom());
				}

				inner.layout(inner.getLeft(), inner.getTop() + deltaY / 3,
						inner.getRight(), inner.getBottom() + deltaY / 3);

				top += (deltaY / 6);
				bottom += (deltaY / 6);
				imageView.layout(imageView.getLeft(), top,
						imageView.getRight(), bottom);
			}

			isCount = true;
			y = nowY;
			break;

		default:
			break;

		}
	}

	public void animation() {

		TranslateAnimation taa = new TranslateAnimation(0, 0, top + 200,
				initTop + 200);
		taa.setDuration(200);
		imageView.startAnimation(taa);
		imageView.layout(imageView.getLeft(), initTop, imageView.getRight(),
				initbottom);

		TranslateAnimation ta = new TranslateAnimation(0, 0, inner.getTop(),
				normal.top);
		ta.setDuration(200);
		inner.startAnimation(ta);
		inner.layout(normal.left, normal.top, normal.right, normal.bottom);
		normal.setEmpty();

		isCount = false;
		y = 0;

	}

	public boolean isNeedAnimation() {
		return !normal.isEmpty();
	}

	public void isNeedMove() {
		int offset = inner.getMeasuredHeight() - getHeight();
		int scrollY = getScrollY();
		if (scrollY == 0 || scrollY == offset) {
			isMoveing = true;
		}
	}

}
