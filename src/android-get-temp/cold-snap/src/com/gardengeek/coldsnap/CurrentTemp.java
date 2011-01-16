package com.gardengeek.coldsnap;

import java.util.List;

import com.gardengeek.coldsnap.ColdSnapApp;
import com.gardengeek.coldsnap.R;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.GridView;

public class CurrentTemp extends Activity {
	private int NUMBERDAYS = 3;

    public void onCreate(Bundle savedInstanceState) {
    	GridView gridview;
    	List<ColdSnapService.DateTemp> dateTemps;
    	ColdSnapApp appState;
    	ImageAdapter imagesview;
    	Integer coldTemp;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
    	Button button = (Button) findViewById(R.id.okbutton);
    	button.setText("OK");
    	
        setContentView(R.layout.temp);
        
        appState = ((ColdSnapApp)getApplication());
        setZipInView(appState);
        setLatLongInView(appState);
        
        gridview = (GridView) findViewById(R.id.gridview);
        imagesview = new ImageAdapter(this);
        gridview.setAdapter(imagesview);

        dateTemps = getDateTemp(appState.alert.getLatLong());
        while (dateTemps.size() < NUMBERDAYS)
        	dateTemps.add(new ColdSnapService.DateTemp("Never", "-400"));

        coldTemp = appState.alert.getColdTemperature();
        for (int i = 0; i < NUMBERDAYS; i++)
        	updateDayView(dateTemps, coldTemp, i, imagesview);
    }
    
    private void updateDayView(List<ColdSnapService.DateTemp> dateTemps,
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
    		imagesview.setHappyPlant(day);
    	}
    }
    
    private void setZipInView(ColdSnapApp appState)
    {
    	TextView tempText;

    	if (appState.debug == false)
    		return;
    	String foo = appState.alert.getZipcode();
    	tempText = (TextView) findViewById(R.id.zipcode);
    	tempText.setText(getString(R.string.forzip).concat(" ").concat(foo).concat(getString(R.string.forzipending)));
    }
    private void setLatLongInView(ColdSnapApp appState)
    {
    	TextView tempText;
    	String latlong;

    	if (appState.debug == false)
    		return;
    	latlong = appState.alert.getLatLong();
    	tempText = (TextView) findViewById(R.id.latlong);
    	tempText.setText(getString(R.string.forlatlong).concat(" ").concat(latlong).concat(getString(R.string.forlatlongending)));
    }
    private List<ColdSnapService.DateTemp> getDateTemp(String latlong)
    {
    	ColdSnapApp appState = ((ColdSnapApp)getApplication());
    	return appState.alert.getMinimumTemperatures(latlong);
    }
}