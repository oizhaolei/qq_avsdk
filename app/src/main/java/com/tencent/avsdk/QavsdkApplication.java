package com.tencent.avsdk;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import com.tencent.avsdk.control.QavsdkControl;
import com.tencent.openqq.IMBaseApplication;

public class QavsdkApplication extends Application {
	private static final String TAG = "QavsdkApplication";
	private QavsdkControl mQavsdkControl = null;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "WL_DEBUG onConfigurationChanged");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mQavsdkControl = new QavsdkControl(this);
		Log.d(TAG, "WL_DEBUG onCreate");
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.d(TAG, "WL_DEBUG onLowMemory");
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.d(TAG, "WL_DEBUG onTerminate");
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		Log.d(TAG, "WL_DEBUG onTrimMemory");
	}

	public QavsdkControl getQavsdkControl() {
		return mQavsdkControl;
	}
}