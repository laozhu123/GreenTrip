package com.left.util;

import com.amap.api.services.core.LatLonPoint;
//������¼���г����޵�λ��
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
