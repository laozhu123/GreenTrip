package com.left.util;

import java.util.Date;
import java.util.List;

import com.amap.api.services.busline.BusStationItem;
//用在公交模式下，以在点击listview获取公交详细信息时做对比匹配使用
public class BusStationList {
	private String BusLineId;
	private List<BusStationItem> busStationItems;
	private String BusLineName;
	private Date FirstBusTime;
	private Date LastBusTime;
	private int PassStationNum;
	
	public String getBusLineId() {
		return BusLineId;
	}
	public void setBusLineId(String busLineId) {
		BusLineId = busLineId;
	}
	public List<BusStationItem> getBusStationItems() {
		return busStationItems;
	}
	public void setBusStationItems(List<BusStationItem> busStationItems) {
		this.busStationItems = busStationItems;
	}
	public String getBusLineName() {
		return BusLineName;
	}
	public void setBusLineName(String busLineName) {
		BusLineName = busLineName;
	}
	public int getPassStationNum() {
		return PassStationNum;
	}
	public void setPassStationNum(int passStationNum) {
		PassStationNum = passStationNum;
	}
	public Date getFirstBusTime() {
		return FirstBusTime;
	}
	public void setFirstBusTime(Date firstBusTime) {
		FirstBusTime = firstBusTime;
	}
	public Date getLastBusTime() {
		return LastBusTime;
	}
	public void setLastBusTime(Date lastBusTime) {
		LastBusTime = lastBusTime;
	}
}
