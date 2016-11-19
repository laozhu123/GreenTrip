package com.left.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.left.bean.Poibean;
import com.left.green.R;
import com.left.instruction.BusInstruction;

public class PoiAdapter extends BaseAdapter {
	private LayoutInflater inflater;  
	private ArrayList<Poibean> list;

	public PoiAdapter(Context context,ArrayList<Poibean> list) {
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
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.poi_item, null);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.district = (TextView) convertView.findViewById(R.id.district);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(list.get(position).getName());
		holder.district.setText(list.get(position).getDistrict());
		return convertView;
	}

	class ViewHolder {
		TextView name,district;
	}
}
