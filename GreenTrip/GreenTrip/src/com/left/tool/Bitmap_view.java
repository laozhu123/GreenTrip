package com.left.tool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Bitmap_view {
	public Bitmap createViewBitmap() {
		int r, g, b;
		r = (int) (Math.random() * 255);
		g = (int) (Math.random() * 255);
		b = (int) (Math.random() * 255);
		Bitmap bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint;
		paint = new Paint(); // 设置�?��笔刷大小�?的黄色的画笔
		paint.setColor(Color.argb(200, r, g, b));
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(3);

		RectF rect = new RectF(0, 0, 120, 120);

		canvas.drawRoundRect(rect, 60, // x轴的半径
				60, // y轴的半径
				paint);

//		Path path = new Path(); // 定义�?��路径
//		path.moveTo(30, 100);
//		path.lineTo(60, 120);
//		path.lineTo(90, 100);
//		path.lineTo(30, 100);

//		canvas.drawPath(path, paint);
		return bitmap;
	}
	
	public Bitmap createViewBitmap(int id) {
		Bitmap bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint;
		paint = new Paint(); // 设置�?��笔刷大小�?的黄色的画笔
		paint.setColor(id);
		paint.setAlpha(200);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(3);

		RectF rect = new RectF(0, 0, 120, 120);

		canvas.drawRoundRect(rect, 60, // x轴的半径
				60, // y轴的半径
				paint);

//		Path path = new Path(); // 定义�?��路径
//		path.moveTo(30, 100);
//		path.lineTo(60, 120);
//		path.lineTo(90, 100);
//		path.lineTo(30, 100);
//
//		canvas.drawPath(path, paint);
		return bitmap;
	}

}
