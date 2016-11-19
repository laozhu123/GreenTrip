package com.left.adapter;

import java.util.ArrayList;
import com.left.green.R;
import com.left.instruction.WalkInstruction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class WalkAdapter extends BaseAdapter {
	private ArrayList<WalkInstruction> list;
	private LayoutInflater inflater;  

	public WalkAdapter(Context context,ArrayList<WalkInstruction> list) {
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
		WalkInstruction entity = list.get(position);
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.walk_item, null);
			holder.name = (TextView) convertView.findViewById(R.id.instruction);
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
