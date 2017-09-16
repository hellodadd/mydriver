package com.droi.edriver.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

/**
 * Created by pc0001 on 2016/3/7.
 */
public class DetialGallery extends Gallery {

	private final int OFFSET_X = 100; 
	public DetialGallery(Context context) {
		super(context);
	}

	public DetialGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DetialGallery(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/*public DetialGallery(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}*/
	
	private boolean isScroolingLeft(MotionEvent e1, MotionEvent e2){
		return e2.getX()-e1.getX()> OFFSET_X;
	}
	private boolean isScroolingRight(MotionEvent e1, MotionEvent e2){
		return e1.getX()-e2.getX()> OFFSET_X;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
						   float velocityY) {
		// TODO Auto-generated method stub
		//return false;
		int keyEvent = -1;
		if(isScroolingLeft(e1,e2)){
			keyEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		}else if(isScroolingRight(e1,e2)){
			keyEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		onKeyDown(keyEvent,null);
		return true;
	}

}
