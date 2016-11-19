package com.left.view;

import com.left.green.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("InflateParams") public class Busstation  extends LinearLayout{
	
	private View Busstationlayout;
	private String busname;//公交站名
    private String busdetail;//站点信息
    
	public Busstation(Context context) {
		super(context);
		initView(context); 
	}

	public Busstation(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context); 
	}
	
	@SuppressLint("NewApi") public Busstation(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}

	//初始化界面
	private void initView(Context context){
		LayoutInflater inflater=LayoutInflater.from(context);
		Busstationlayout=inflater.inflate(R.layout.busstation,null);
		this.addView(Busstationlayout);	 
	}
		
	public String getBusname() {
		return busname;
	}

	public void setBusname(String busname) {
		this.busname = busname;
		TextView textView=(TextView) findViewById(R.id.busname);
		textView.setText(busname);
	}

	public String getBusdetail() {
		return busdetail;
	}

	public void setBusdetail(String busdetail) {
		this.busdetail = busdetail;
		TextView textView=(TextView) findViewById(R.id.busdetail);
		textView.setText(busdetail);
	}
}
