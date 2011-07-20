package org.gardengeek.gardenWeather;

import java.util.ArrayList;
import java.util.List;

import org.gardengeek.gardenWeather.GardenWeatherApp;
import org.gardengeek.gardenWeather.R;
import org.gardengeek.gardenWeather.GardenWeatherService.DateTemp;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.GridView;
import android.widget.Toast;

public class CurrentTemp extends Activity {
	private int NUMBERDAYS = 3;

    public void onCreate(Bundle savedInstanceState) {
    	final Button refreshButton;
    	final Button configButton;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
    	Button button = (Button) findViewById(R.id.okbutton);
    	button.setText("OK");
    	
        setContentView(R.layout.temp);
        refreshButton = (Button) findViewById(R.id.refreshbutton);
        configButton = (Button) findViewById(R.id.configbutton);
        
        if (!isAppSetUp()) {
        	Intent i = new Intent(getBaseContext(), GardenWeatherSettings.class);
        	startActivity(i);
        }
        
        configButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent(getBaseContext(), GardenWeatherSettings.class);
            	startActivity(i);
            }
        });
        refreshButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	updateTemperatures();
            }
        });
    }
    
    public void onStart() {
    	super.onStart();
    	updateTemperatures();
    }
    
    public void onResume() {
    	GardenWeatherApp appState;
    	
    	super.onResume();
        appState = ((GardenWeatherApp)getApplication());
        setZipInView(appState);
        setLatLongInView(appState);
        updateTemperatures();
    }
    
    private boolean isAppSetUp()
    {
    	GardenWeatherApp appState;
    	String zip;
    	
    	appState = ((GardenWeatherApp)getApplication());
    	zip = appState.alert.getZipcode();
    	if (zip == null)
    		return false;
    	return true;
    }
    
    private void updateTemperatures()
    {
    	GardenWeatherApp appState;
    	GridView gridview;
    	List<GardenWeatherService.DateTemp> dateTemps;
    	ImageAdapter imagesview;
    	Integer coldTemp;

        appState = ((GardenWeatherApp)getApplication());
        gridview = (GridView) findViewById(R.id.gridview);
        imagesview = new ImageAdapter(this);
        gridview.setAdapter(imagesview);

        dateTemps = getDateTemp();
        while (dateTemps.size() < NUMBERDAYS)
        	dateTemps.add(new GardenWeatherService.DateTemp("Never", "-400"));

        coldTemp = appState.alert.getColdTemperature();
        for (int i = 0; i < NUMBERDAYS; i++)
        	updateDayView(dateTemps, coldTemp, i, imagesview);
    }
    
    private void updateDayView(List<GardenWeatherService.DateTemp> dateTemps,
    		Integer coldTemp, int day, ImageAdapter imagesview)
    {
    	int dayIDs[] = {
    			R.id.today,
    			R.id.tomorrow,
    			R.id.next_day,
    	};
    	int tempIDs[] = {
    			R.id.todays_temp,
    			R.id.tomorrows_temp,
    			R.id.next_days_temp,
    	};
    	TextView tempText;

    	tempText = (TextView) findViewById(dayIDs[day]);
    	tempText.setText(dateTemps.get(day).date);

    	tempText = (TextView) findViewById(tempIDs[day]);
    	tempText.setText(dateTemps.get(day).temp + getString(R.string.tempunits));
    	if (Integer.parseInt(dateTemps.get(day).temp) <= coldTemp) {
    		tempText.setTextColor(Color.RED);
    		tempText.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
    		imagesview.setColdPlant(day);
    	} else {
    		tempText.setTextColor(Color.WHITE);
    		tempText.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
    		imagesview.setHappyPlant(day);
    	}
    }
    
    private void setZipInView(GardenWeatherApp appState)
    {
    	TextView tempText;

    	if (appState.debug == false)
    		return;
    	String foo = appState.alert.getZipcode();
    	tempText = (TextView) findViewById(R.id.zipcode);
    	tempText.setText(getString(R.string.forzip).concat(" ").concat(foo).concat(getString(R.string.forzipending)));
    }
    private void setLatLongInView(GardenWeatherApp appState)
    {
    	TextView tempText;
    	String latlong;

    	if (appState.debug == false)
    		return;
    	latlong = appState.alert.getLatLong();
    	tempText = (TextView) findViewById(R.id.latlong);
    	/* Internet might be down. */
    	if (latlong == null) {
    		tempText.setText(getString(R.string.forlatlong).concat(" ").concat("0,0").concat(getString(R.string.forlatlongending)));
    		return;
    	}
    	tempText.setText(getString(R.string.forlatlong).concat(" ").concat(latlong).concat(getString(R.string.forlatlongending)));
    }
    private List<GardenWeatherService.DateTemp> getDateTemp()
    {
    	GardenWeatherApp appState = ((GardenWeatherApp)getApplication());
    	Context context = getApplicationContext();

    	try {
    		return appState.alert.getMinimumTemperatures();
    	} catch (Exception e) {
    		/* Return an empty list, which will be filled in later with bogus values. */
    		Toast.makeText(context, "Cannot fetch temperatures, data/internet is down", Toast.LENGTH_LONG);
    		return new ArrayList<DateTemp>();
    	}
    }
}