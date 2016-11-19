package com.left.util;
//用在AR里面，以确定在屏幕上显示的地方
public class Point {

	private double x;
	private double y;
	private boolean show;
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public boolean isShow() {
		return show;
	}
	public void setShow(boolean show) {
		this.show = show;
	}
}
