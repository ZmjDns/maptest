package com.maptest.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZMJ on 2018/1/2.
 */
public class GetPermissionsutil {

	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE};

	public static void verifyStoragePermissions(Activity activity) {
		// Check if we have write permission
		int permission = ActivityCompat.checkSelfPermission(activity,
				Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE);
		}
	}
	//获取定位权限
	private static final int REQUEST_ACCESS_COARSE_LOCATION = 2;
	private static String[] PERMISSIONS_LOCATION = {
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_NETWORK_STATE,
			Manifest.permission.ACCESS_WIFI_STATE,
			Manifest.permission.CHANGE_WIFI_STATE,
			Manifest.permission.INTERNET,
			Manifest.permission.READ_PHONE_STATE
	};

	List<String> mPermissionList = new ArrayList<>();

	public void checkPermission(Activity activity){
		mPermissionList.clear();
		for (String permission:PERMISSIONS_LOCATION){
			if(ContextCompat.checkSelfPermission(activity,permission) != PackageManager.PERMISSION_GRANTED){
				mPermissionList.add(permission);
			}
		}
	}

	public static void getLoactionPermission(Activity activity){
		int permission = ActivityCompat.checkSelfPermission(activity,
				Manifest.permission.ACCESS_COARSE_LOCATION);

	}


}