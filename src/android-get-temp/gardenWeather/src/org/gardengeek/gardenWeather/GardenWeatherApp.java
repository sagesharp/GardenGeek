package org.gardengeek.gardenWeather;

import org.gardengeek.gardenWeather.GardenWeatherService;

import android.app.*;

public class GardenWeatherApp extends Application {
	public GardenWeatherService alert = new GardenWeatherService();
	public boolean debug = false;

    @Override
    public void onCreate()
    {
            super.onCreate();
    }
}
