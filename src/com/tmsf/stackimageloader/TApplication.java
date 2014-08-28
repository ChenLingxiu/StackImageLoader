package com.tmsf.stackimageloader;

import android.app.Application;

public class TApplication extends Application {
	private static Application instance;

	public static Application getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		instance = this;
	}

}
