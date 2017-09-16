package com.droi.edriver.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.droi.edriver.R;
import com.droi.library.pickerviews.adapter.NumericWheelAdapter;
import com.droi.library.pickerviews.adapter.WheelAdapter;
import com.droi.library.pickerviews.lib.WheelView;
import com.droi.library.pickerviews.listener.OnItemSelectedListener;

/**
 * Created by ZhuQichao on 2016/1/25.
 */
public class NumberPickerView extends LinearLayout {

	private View contentView;
	private WheelView numberView;
	private WheelAdapter adapter;
	private int index;
	private int min = 0, max = 100;

	private OnNumberPickedListener listener;

	public NumberPickerView(Context context) {
		super(context);
		initView(context);
	}

	public NumberPickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public NumberPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}

	private void initView(Context context) {
		contentView = LayoutInflater.from(context).inflate(R.layout.layout_number_pickerview, this, true);
		numberView = (WheelView) contentView.findViewById(R.id.number_view);
		numberView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(int index) {
				NumberPickerView.this.index = index;
				listener.onNumberPicked(index, (int) adapter.getItem(index));
			}
		});
	}

	public void setNumberRange(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public void setInitNumberPicked(int number) {
		if (number > max || number < min) {
			number = min;
		}
		index = number - min;
	}

	public void setNumberPicked(int number) {
		if (number > max || number < min) {
			number = min;
		}
		index = number - min;
		numberView.setCurrentItem(index);
		listener.onNumberPicked(index, (int) adapter.getItem(index));
	}

	public void setLabel(String label) {
		numberView.setLabel(label);
	}

	public void show() {
		adapter = new NumericWheelAdapter(min, max);
		numberView.setAdapter(adapter);
		numberView.setCurrentItem(index);
	}

	public void setOnNumberPickedListener(OnNumberPickedListener listener) {
		this.listener = listener;
	}

	public interface OnNumberPickedListener {
		void onNumberPicked(int index, int number);
	}
}
