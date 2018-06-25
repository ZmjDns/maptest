package com.maptest.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.maptest.R;
import com.maptest.utils.AMapUtil;

import java.util.List;

/**
 * Created by ZMJ on 2018/1/26.
 */
public class BusResultListAdapter extends BaseAdapter {
	private Context context;
	private List<BusPath> mBusPathList;
	private BusRouteResult mBusRouteResult;


	public BusResultListAdapter(Context context, BusRouteResult busRouteResult) {
		this.context = context;
		this.mBusRouteResult = busRouteResult;
		this.mBusPathList = busRouteResult.getPaths();

	}

	@Override
	public int getCount() {
		return mBusPathList.size();
	}

	@Override
	public Object getItem(int position) {
		return mBusPathList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null){
			holder = new ViewHolder();
			convertView = View.inflate(context,R.layout.bus_result_list_item,null);
			holder.title = (TextView)convertView.findViewById(R.id.bus_path_title);
			holder.des  = (TextView)convertView.findViewById(R.id.bus_path_des);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder)convertView.getTag();
		}

		final BusPath busPathItem = mBusPathList.get(position);
		holder.title.setText(AMapUtil.getBusPathTitle(busPathItem));
		holder.des.setText(AMapUtil.getBusPathDes(busPathItem));

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onlistener.onItemClick((BusPath) getItem(position));
			}
		});
		return convertView;
	}

	private class ViewHolder{
		TextView title;
		TextView des;
	}
	private  OnItemClickListener onlistener;

	public interface OnItemClickListener {
		void onItemClick(BusPath busPath);
	}

	public void setOnItemClick(OnItemClickListener listener){
		this.onlistener = listener;
	}
}
