package com.left.util;

//用来记录搜索时匹配的位置
public class PoiPlace {
	private String Adcode;
	private String District;
	private String Name;
	public String getAdcode() {
		return Adcode;
	}
	public void setAdcode(String adcode) {
		Adcode = adcode;
	}
	public String getDistrict() {
		return District;
	}
	public void setDistrict(String district) {
		District = district;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
}
