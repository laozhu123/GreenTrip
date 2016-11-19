package com.left.instruction;

import android.graphics.Color;

public class BusInstruction {
	private String name;
	private int imgId;
	private int color= Color.parseColor("#000000");
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getImgId() {
		return imgId;
	}
	public void setImgId(int imgId) {
		this.imgId = imgId;
	}
	public int getColor() {
		return color;
	}
	public void setColor(int color) {
		this.color = color;
	}
}
