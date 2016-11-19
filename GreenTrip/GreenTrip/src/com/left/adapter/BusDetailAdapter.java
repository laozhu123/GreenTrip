package com.left.adapter;

import java.util.ArrayList;
import com.left.green.R;
import com.left.instruction.BusDetailInstruction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BusDetailAdapter extends BaseAdapter {
	private ArrayList<BusDetailInstruction> list;
	private LayoutInflater inflater;  

	public BusDetailAdapter(Context context,ArrayList<BusDetailInstruction> list) {
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
		BusDetailInstruction entity = list.get(position);
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.busdetail_item, null);
			holder.name = (TextView) convertView.findViewById(R.id.busdetailinstruction);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(entity.getName());
		return convertView;
	}

	class ViewHolder {
		TextView name;
	}
}
