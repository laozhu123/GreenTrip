package com.left.green;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

@SuppressWarnings("deprecation")     //����ȡ�����棬����Ӧ��������ȡ��tabactivity��ʱ�ľ���
public class MainActivity extends TabActivity { 
	private TabHost tabHost; 
	private RadioButton goText;
	private RadioButton findText;
	private RadioButton myText;

	@Override 
	public void onCreate(Bundle savedInstanceState) { 
	super.onCreate(savedInstanceState); 
	this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
	setContentView(R.layout.activity_main); 
	RadioGroup radioGroup=(RadioGroup)findViewById(R.id.main_tab_group); 
	goText=(RadioButton) findViewById(R.id.main_tab_goout);
	findText=(RadioButton) findViewById(R.id.main_tab_find);
	myText=(RadioButton) findViewById(R.id.main_tab_my);
	tabHost=this.getTabHost(); 
	TabHost.TabSpec spec; 
	Intent intent; 
	intent=new Intent().setClass(this, GoOutActivity.class); 
	spec=tabHost.newTabSpec("����").setIndicator("����").setContent(intent); 
	tabHost.addTab(spec); 
	intent=new Intent().setClass(this,FindActivity.class); 
	spec=tabHost.newTabSpec("����").setIndicator("����").setContent(intent); 
	tabHost.addTab(spec); 
	intent=new Intent().setClass(this, MyActivity.class); 
	spec=tabHost.newTabSpec("�ҵ�").setIndicator("�ҵ�").setContent(intent); 
	tabHost.addTab(spec); 
	tabHost.setCurrentTab(0);
	goText.setTextColor(Color.parseColor("#31B42D"));
	radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() { 

		@Override 
		public void onCheckedChanged(RadioGroup group, int checkedId) { 
			switch (checkedId) { 
			case R.id.main_tab_goout: 
			tabHost.setCurrentTabByTag("����"); 
			reset();
			goText.setTextColor(Color.parseColor("#77B943"));
			break; 
			case R.id.main_tab_find:
			tabHost.setCurrentTabByTag("����"); 
			reset();
			findText.setTextColor(Color.parseColor("#77B943"));
			break; 
			case R.id.main_tab_my: 
			tabHost.setCurrentTabByTag("�ҵ�"); 
			reset();
			myText.setTextColor(Color.parseColor("#77B943"));
			break; 
			default: 
			break; 
			} 
		} 
	}); 
  } 
	
	private void reset()
	{
		findText.setTextColor(Color.parseColor("#9C9C9C"));
		myText.setTextColor(Color.parseColor("#9C9C9C"));
		goText.setTextColor(Color.parseColor("#9C9C9C"));
	}
} 