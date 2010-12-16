package com.gardengeek.frostalert;

import com.gardengeek.frostalert.FrostAlertService;
import android.app.*;

public class FrostAlertApp extends Application {
	public FrostAlertService alert = new FrostAlertService();

    @Override
    public void onCreate()
    {
            super.onCreate();
    }
}
