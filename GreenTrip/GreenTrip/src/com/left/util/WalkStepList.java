package com.left.util;

import java.util.List;

import com.amap.api.services.route.WalkStep;
//���ڹ����ͻ��ģʽ�£����ڵ��listview��ȡ������ϸ��Ϣʱ���Ա�ƥ��ʹ��
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
