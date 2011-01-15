package com.gardengeek.coldsnap;

import com.gardengeek.coldsnap.ColdSnapService;

import android.app.*;

public class ColdSnapApp extends Application {
	public ColdSnapService alert = new ColdSnapService();

    @Override
    public void onCreate()
    {
            super.onCreate();
    }
}
