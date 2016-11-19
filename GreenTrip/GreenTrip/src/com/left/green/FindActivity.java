package com.left.green;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class FindActivity extends Activity{
	
	private RelativeLayout greencircle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_find);
		greencircle=(RelativeLayout) findViewById(R.id.greencircle);
		greencircle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent=new Intent(FindActivity.this, GreencircleActivity.class);
				startActivity(intent);
			}
		});
	}
}
