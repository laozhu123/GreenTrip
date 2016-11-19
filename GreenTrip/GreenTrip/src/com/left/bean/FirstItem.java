package com.left.bean;

public class FirstItem {
	int type;      //步行1 骑车2 坐车3 站点4   用于显示图片
	String title;      //显示的item文字
	String title2;    //方向站点数  乘车时使用
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle2() {
		return title2;
	}
	public void setTitle2(String title2) {
		this.title2 = title2;
	}
}
