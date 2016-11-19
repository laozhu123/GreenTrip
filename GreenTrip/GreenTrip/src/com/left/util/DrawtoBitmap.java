package com.left.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;

public class DrawtoBitmap {
	Context ctx;
	public DrawtoBitmap(Context ctx) {
		// TODO Auto-generated constructor stub
		this.ctx = ctx;
	}
	
	public BitmapDescriptor getBitmapDes(Drawable drawable) {
		ImageView textView = new ImageView(ctx);
		BitmapDrawable bd = (BitmapDrawable) drawable;
		textView.setLayoutParams(new LayoutParams(35, 45));
		textView.setBackgroundDrawable(new BitmapDrawable(bd.getBitmap()));
		return BitmapDescriptorFactory.fromView(textView);
	}
}
