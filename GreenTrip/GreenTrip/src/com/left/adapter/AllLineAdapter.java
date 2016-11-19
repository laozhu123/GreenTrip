package com.left.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.left.green.R;
import com.left.tool.Values;

public class AllLineAdapter extends BaseExpandableListAdapter {
	Activity context;
	ViewHolder holder_group;
	ViewHolder holder_child;

	public AllLineAdapter(Activity context) {
		this.context = context;
	}

	/*-----------------Child */
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return Values.secondItems.get(groupPosition).getSteps()
				.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		if (convertView == null) {
			holder_child = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.seconditem, null);
			holder_child.title = (TextView) convertView
					.findViewById(R.id.title);

			convertView.setTag(holder_child);
		} else {
			holder_child = (ViewHolder) convertView.getTag();
		}

		holder_child.title.setText(Values.secondItems.get(groupPosition)
				.getSteps().get(childPosition));

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		if (Values.secondItems.get(groupPosition).getSteps() == null) {
			return 0;
		}
		return Values.secondItems.get(groupPosition).getSteps().size();
	}

	/* ----------------------------Group */
	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return getGroup(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return Values.firstItems.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			holder_group = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.firstitem, null);
			holder_group.title1 = (TextView) convertView
					.findViewById(R.id.title1);
			holder_group.title2 = (TextView) convertView
					.findViewById(R.id.title2);
			holder_group.passnum = (TextView) convertView
					.findViewById(R.id.passnum);
			holder_group.left1 = convertView.findViewById(R.id.left1);
			holder_group.left2 = convertView.findViewById(R.id.left2);
			holder_group.left3 = convertView.findViewById(R.id.left3);
			holder_group.left4 = convertView.findViewById(R.id.left4);
			holder_group.middle1 = convertView.findViewById(R.id.middle1);
			holder_group.middle2 = convertView.findViewById(R.id.middle2);
			holder_group.hold = convertView.findViewById(R.id.hold);
			holder_group.xiaoxianxian = convertView
					.findViewById(R.id.xiaoxianxian);
			holder_group.xiala = (ImageView) convertView
					.findViewById(R.id.xiala);
			holder_group.left1_pic = (ImageView) convertView
					.findViewById(R.id.left1_pic);
			holder_group.left2_pic = (ImageView) convertView
					.findViewById(R.id.left2_pic);
			holder_group.left3_pic = (ImageView) convertView
					.findViewById(R.id.left3_pic);

			convertView.setTag(holder_group);
		} else {
			holder_group = (ViewHolder) convertView.getTag();
		}
		holder_group.left1.setVisibility(View.GONE);
		holder_group.left2.setVisibility(View.GONE);
		holder_group.left3.setVisibility(View.GONE);
		holder_group.left4.setVisibility(View.GONE);
		holder_group.middle1.setVisibility(View.GONE);
		holder_group.middle2.setVisibility(View.GONE);
		holder_group.xiala.setVisibility(View.GONE);

		if (groupPosition == 0) {
			holder_group.left3.setVisibility(View.VISIBLE);
			holder_group.middle1.setVisibility(View.VISIBLE);
			holder_group.title1.setText(Values.firstItems.get(groupPosition)
					.getTitle());
			return convertView;
		}
		if (groupPosition + 1 == Values.firstItems.size()) {
			holder_group.left2.setVisibility(View.VISIBLE);
			holder_group.middle1.setVisibility(View.VISIBLE);
			holder_group.title1.setText(Values.firstItems.get(groupPosition)
					.getTitle());
			return convertView;
		}
		switch (Values.firstItems.get(groupPosition).getType()) {
		case 1:
			holder_group.left1.setVisibility(View.VISIBLE);
			holder_group.xiala.setVisibility(View.VISIBLE);
			holder_group.middle1.setVisibility(View.VISIBLE);
			holder_group.title1.setText(Values.firstItems.get(groupPosition)
					.getTitle());
			holder_group.left1_pic.setImageDrawable(context.getResources()
					.getDrawable(R.drawable.walkpress));
			break;
		case 2:
			holder_group.left1.setVisibility(View.VISIBLE);
			holder_group.xiala.setVisibility(View.VISIBLE);
			holder_group.middle1.setVisibility(View.VISIBLE);
			holder_group.title1.setText(Values.firstItems.get(groupPosition)
					.getTitle());
			holder_group.left1_pic.setImageDrawable(context.getResources()
					.getDrawable(R.drawable.bicyclepress));
			break;
		case 3:
			holder_group.left1.setVisibility(View.VISIBLE);
			holder_group.middle2.setVisibility(View.VISIBLE);
			holder_group.title2.setText(Values.firstItems.get(groupPosition)
					.getTitle());
			holder_group.passnum.setText(Values.firstItems.get(groupPosition)
					.getTitle2());
			holder_group.left1_pic.setImageDrawable(context.getResources()
					.getDrawable(R.drawable.buspress));
			holder_group.xiala.setVisibility(View.VISIBLE);
			break;
		case 4:
			holder_group.left4.setVisibility(View.VISIBLE);
			holder_group.middle1.setVisibility(View.VISIBLE);
			holder_group.title1.setText(Values.firstItems.get(groupPosition)
					.getTitle());
			break;
		default:
			break;
		}

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	static class ViewHolder {
		public TextView title, title1, title2, passnum;
		public View left1, left2, left3, left4, middle1, middle2, xiaoxianxian,
				hold;
		public ImageView xiala, left1_pic, left2_pic, left3_pic;

	}
}
