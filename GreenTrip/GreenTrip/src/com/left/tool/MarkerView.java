package com.left.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;

public class MarkerView {
	Context ctx;

	public MarkerView(Context context) {
		// TODO Auto-generated constructor stub
		ctx = context;
	}

	/**
	 * 获取每个聚合点的绘制样式
	 * */
	public BitmapDescriptor getBitmapDes(int num,Bitmap bitmap) {
		TextView textView = new TextView(ctx);
		String tile = String.valueOf(num);
		textView.setText(tile);

		textView.setGravity(Gravity.CENTER);

		textView.setTextColor(Color.WHITE);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		textView.setBackgroundDrawable(new BitmapDrawable(bitmap));
		return BitmapDescriptorFactory.fromView(textView);
	}
	
	public BitmapDescriptor getBitmapDes(String num,Bitmap bitmap) {
		TextView textView = new TextView(ctx);
		String tile = num;
		textView.setText(tile);

		textView.setGravity(Gravity.CENTER);

		textView.setTextColor(Color.WHITE);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		textView.setBackgroundDrawable(new BitmapDrawable(bitmap));
		return BitmapDescriptorFactory.fromView(textView);
	}
}
