package com.left.adapter;

import java.util.ArrayList;
import com.left.green.R;
import com.left.instruction.BusInstruction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BusAdapter extends BaseAdapter {
	private ArrayList<BusInstruction> list;
	private LayoutInflater inflater;  

	public BusAdapter(Context context,ArrayList<BusInstruction> list) {
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
		BusInstruction entity = list.get(position);
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.bus_item, null);
			holder.name = (TextView) convertView.findViewById(R.id.businstruction);
			holder.img = (ImageView) convertView.findViewById(R.id.businstructionImg);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(entity.getName());
		holder.name.setTextColor(entity.getColor());
		holder.img.setImageResource(entity.getImgId());
		return convertView;
	}

	class ViewHolder {
		TextView name;
		ImageView img;
	}
}
