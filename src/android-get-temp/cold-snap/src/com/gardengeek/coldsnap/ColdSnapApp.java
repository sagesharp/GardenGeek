package com.gardengeek.coldsnap;

import com.gardengeek.coldsnap.ColdSnapService;

import android.app.*;

public class ColdSnapApp extends Application {
	public ColdSnapService alert = new ColdSnapService();
	public boolean debug = false;

    @Override
    public void onCreate()
    {
            super.onCreate();
    }
}
