package com.left.util;

import java.util.List;

import com.amap.api.services.route.WalkStep;
//用在公交和混乘模式下，以在点击listview获取步行详细信息时做对比匹配使用
public class WalkStepList {
	private String walkId;
	private List<WalkStep> walkstep;
	public String getWalkId() {
		return walkId;
	}
	public void setWalkId(String walkId) {
		this.walkId = walkId;
	}
	public List<WalkStep> getWalkstep() {
		return walkstep;
	}
	public void setWalkstep(List<WalkStep> walkstep) {
		this.walkstep = walkstep;
	}

}
