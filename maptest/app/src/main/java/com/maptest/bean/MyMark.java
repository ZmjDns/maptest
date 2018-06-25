package com.maptest.bean;

import android.os.Bundle;

import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;

/**
 * Created by ZMJ on 2018/1/2.
 */
public class MyMark {
	private Marker marker;
	private String title;
	private String snippet;
	private LatLng position;
	private BitmapDescriptor bitmapDescriptor;
	private Bundle otherInfo;
	private Object object;

	public MyMark() {

	}

	public Marker getMarker() {
		return marker;
	}

	public String getTitle() {
		return title;
	}

	public String getSnippet() {
		return snippet;
	}

	public LatLng getPosition() {
		return position;
	}

	public BitmapDescriptor getBitmapDescriptor() {
		return bitmapDescriptor;
	}

	public Bundle getOtherInfo() {
		return otherInfo;
	}

	public Object getObject() {
		return object;
	}

	public MyMark(Marker marker, String title, String snippet, BitmapDescriptor bitmapDescriptor, LatLng position, Bundle otherInfo, Object object) {
		this.marker = marker;
		this.title = title;
		this.snippet = snippet;
		this.bitmapDescriptor = bitmapDescriptor;
		this.position = position;
		this.otherInfo = otherInfo;
		this.object = object;

	}
}
