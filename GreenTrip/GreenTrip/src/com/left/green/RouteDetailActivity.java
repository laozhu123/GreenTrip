package com.left.green;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.left.adapter.AllLineAdapter;
import com.left.tool.Values;
import com.left.tool.pullrefresh.CusListView;

public class RouteDetailActivity extends Activity{
	
	CusListView listView;
	TextView linename,time;
	View store,share;
	AllLineAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routedetail);
		initView();
		initdata();
	}

	private void initdata() {
		// TODO Auto-generated method stub
		linename.setText(Values.busline);
		time.setText(Values.path);
		listView.isHaveMoreDate(false);
		listView.setGroupIndicator(null);
		adapter = new AllLineAdapter(this);
		listView.setAdapter(adapter);
	}

	private void initView() {
		// TODO Auto-generated method stub
		listView = (CusListView) findViewById(R.id.listview);
		linename = (TextView) findViewById(R.id.linename);
		time = (TextView) findViewById(R.id.time);
		store = findViewById(R.id.store);
		share = findViewById(R.id.share);
	}
}
