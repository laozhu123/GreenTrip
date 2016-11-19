package com.left.util;

import com.amap.api.services.core.LatLonPoint;
//用来记录自行车租赁点位置
public class PoiBike {
	private String BikeRental;
	private LatLonPoint position;
	public String getBikeRental() {
		return BikeRental;
	}
	public void setBikeRental(String bikeRental) {
		BikeRental = bikeRental;
	}
	public LatLonPoint getPosition() {
		return position;
	}
	public void setPosition(LatLonPoint position) {
		this.position = position;
	}
}
