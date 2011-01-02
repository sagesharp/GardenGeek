package com.gardengeek.frostalert;

import com.gardengeek.frostalert.R;
import com.gardengeek.frostalert.FrostAlertApp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.GridView;

public class CurrentTemp extends Activity {
    private String latlong;
    
    public void onCreate(Bundle savedInstanceState) {
    	TextView tempText;
    	GridView gridview;
    	String todaysTemp;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp);

        tempText = (TextView) findViewById(R.id.zipcode);
        tempText.setText(getZip());

        tempText = (TextView) findViewById(R.id.latlong);
        tempText.setText(getLatLong());
        
        todaysTemp = getTemp(latlong, 0);
        tempText = (TextView) findViewById(R.id.todays_temp);
        tempText.setText(todaysTemp + getString(R.string.tempunits));
        
        todaysTemp = getTemp(latlong, 1);
        tempText = (TextView) findViewById(R.id.tomorrows_temp);
        tempText.setText(todaysTemp + getString(R.string.tempunits));
        
        todaysTemp = getTemp(latlong, 2);
        tempText = (TextView) findViewById(R.id.next_days_temp);
        tempText.setText(todaysTemp + getString(R.string.tempunits));

        gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

    }
    
    private CharSequence getZip()
    {
    	FrostAlertApp appState = ((FrostAlertApp)getApplication());
    	String foo = appState.alert.getZipcode();
    	return getString(R.string.forzip).concat(" ").concat(foo).concat(getString(R.string.forzipending));
    }
    private CharSequence getLatLong()
    {
    	FrostAlertApp appState = ((FrostAlertApp)getApplication());
    	latlong = appState.alert.getLatLong();
    	return getString(R.string.forlatlong).concat(" ").concat(latlong).concat(getString(R.string.forlatlongending));
    }
    private String getTemp(String latlong, int daysInFuture)
    {
    	FrostAlertApp appState = ((FrostAlertApp)getApplication());
    	return appState.alert.getTempForNextDay(latlong, daysInFuture);
    }
}