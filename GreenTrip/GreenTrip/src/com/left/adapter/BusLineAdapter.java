package com.left.adapter;

import java.util.ArrayList;
import com.left.green.R;
import com.left.instruction.BusLineInstruction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BusLineAdapter extends BaseAdapter {
	private ArrayList<BusLineInstruction> list;
	private LayoutInflater inflater;  

	public BusLineAdapter(Context context,ArrayList<BusLineInstruction> list) {
		this.list = list; 
		inflater =  LayoutInflater.from(context);  
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BusLineInstruction entity = list.get(position);
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.busline_item, null);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.detail = (TextView) convertView.findViewById(R.id.detail);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(entity.getName());
		holder.detail.setText(entity.getDetail());
		return convertView;
	}

	class ViewHolder {
		TextView name;
		TextView detail;
	}
}
